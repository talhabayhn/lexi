package dev.talhabayhan.lexi.feature.home

import app.cash.turbine.test
import dev.talhabayhan.lexi.core.domain.daily.GetDailyWheel
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.session.DailyResult
import dev.talhabayhan.lexi.core.domain.srs.CardProgress
import dev.talhabayhan.lexi.core.domain.streak.GetStreakStatus
import dev.talhabayhan.lexi.core.testing.FakeAppClock
import dev.talhabayhan.lexi.core.testing.FakeCardRepository
import dev.talhabayhan.lexi.core.testing.FakeContentVersionRepository
import dev.talhabayhan.lexi.core.testing.FakeDailyRepository
import dev.talhabayhan.lexi.core.testing.FakeProgressRepository
import dev.talhabayhan.lexi.core.testing.FakeStreakRepository
import dev.talhabayhan.lexi.core.testing.testAlphabetPool
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val clock = FakeAppClock()
    private val today = clock.epochDay(clock.nowMs())
    private val progress = FakeProgressRepository()
    private val streaks = FakeStreakRepository()
    private val cards = FakeCardRepository()
    private val contentVersion = FakeContentVersionRepository()
    private val daily = FakeDailyRepository()
    private val getStreakStatus = GetStreakStatus(streaks, clock)
    private val getDailyWheel = GetDailyWheel(cards, contentVersion, daily, clock)
    private val viewModels = mutableListOf<HomeViewModel>()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        cards.cards = Level.entries.flatMap { testAlphabetPool(it) }
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(): HomeViewModel =
        HomeViewModel(progress, getStreakStatus, getDailyWheel, daily, clock).also { viewModels += it }

    private fun runHomeTest(block: suspend TestScope.() -> Unit) = runTest {
        try {
            block()
        } finally {
            viewModels.forEach { it.cancelJobs() }
            viewModels.clear()
        }
    }

    private fun card(id: String, box: Int? = null, due: Long? = null, correct: Int = 0) =
        CardProgress(CardId(id), box = box, dueEpochDay = due, seen = 1, correct = correct)

    @Test
    fun `nothing played yet shows real zeros and today's date`() = runHomeTest {
        viewModel().state.test {
            val state = awaitItem()
            assertEquals(0, state.streakDays)
            assertEquals(0, state.dueReviews)
            assertEquals(0, state.learnedWords)
            assertEquals(15, state.dailyDay)
            assertEquals(10, state.dailyMonth)
            assertEquals(DailyCardState.Available, state.dailyState)
        }
    }

    @Test
    fun `numbers come from the repositories and follow their changes`() = runHomeTest {
        progress.save(
            listOf(
                card("a", correct = 2),
                card("b", box = 0, due = today, correct = 1),
                card("c", box = 1, due = today + 1),
            ),
        )
        streaks.days = listOf(today - 1, today)

        viewModel().state.test {
            val state = expectMostRecentItem()
            assertEquals(2, state.streakDays)
            assertEquals(1, state.dueReviews)
            assertEquals(2, state.learnedWords)

            streaks.days = listOf(today - 2, today - 1, today)
            assertEquals(3, awaitItem().streakDays)
        }
    }

    @Test
    fun `numbers move on to the new day when the screen is resumed after midnight`() = runHomeTest {
        progress.save(listOf(card("b", box = 0, due = today + 1, correct = 1)))
        streaks.days = listOf(today)
        val viewModel = viewModel()
        viewModel.state.test {
            val before = expectMostRecentItem()
            assertEquals(15, before.dailyDay)
            assertEquals(0, before.dueReviews)
            assertEquals(1, before.streakDays)

            clock.advanceBy(FakeAppClock.MS_PER_DAY)
            viewModel.onIntent(HomeIntent.Resumed)

            val after = expectMostRecentItem()
            assertEquals(16, after.dailyDay)
            assertEquals(1, after.dueReviews)
            // Yesterday was played, today is still open: the streak is alive.
            assertEquals(1, after.streakDays)
        }
    }

    @Test
    fun `streak and daily card recompute automatically when midnight passes while app is open`() = runHomeTest {
        streaks.days = listOf(today)
        daily.record(DailyResult(today, Level.A1, score = 150, timeMs = 45000))
        val viewModel = viewModel()
        viewModel.state.test {
            val before = expectMostRecentItem()
            assertEquals(1, before.streakDays)
            assertEquals(15, before.dailyDay)
            assertEquals(DailyCardState.AlreadyPlayed(150, hoursUntilNext = 12), before.dailyState)

            // Advance past midnight while app is open
            val msToMidnight = clock.msUntilNextDay()
            clock.advanceBy(msToMidnight + 1000L)
            advanceTimeBy(msToMidnight + 1050L)

            val after = expectMostRecentItem()
            assertEquals(16, after.dailyDay)
            assertEquals(1, after.streakDays) // streak alive from yesterday
            assertEquals(DailyCardState.Available, after.dailyState) // new daily available!
        }
    }

    @Test
    fun `streak badge resets on resume after a missed day`() = runHomeTest {
        // Played today - 1 (alive), then advance 2 days without playing (missed)
        streaks.days = listOf(today)
        val viewModel = viewModel()
        viewModel.state.test {
            val before = expectMostRecentItem()
            assertEquals(1, before.streakDays)

            clock.advanceBy(FakeAppClock.MS_PER_DAY * 2)
            viewModel.onIntent(HomeIntent.Resumed)

            val after = expectMostRecentItem()
            assertEquals(0, after.streakDays) // missed day resets streak
        }
    }

    @Test
    fun `daily wheel state is available when ready and sends NavigateToDaily on OpenDaily`() = runHomeTest {
        val viewModel = viewModel()
        viewModel.state.test {
            assertEquals(DailyCardState.Available, awaitItem().dailyState)
        }
        viewModel.effects.test {
            viewModel.onIntent(HomeIntent.OpenDaily)
            assertEquals(HomeEffect.NavigateToDaily, awaitItem())
        }
    }

    @Test
    fun `daily wheel state is already played with score when recorded and OpenDaily is inert`() = runHomeTest {
        daily.record(DailyResult(today, Level.A1, score = 320, timeMs = 85000))
        val viewModel = viewModel()
        viewModel.state.test {
            assertEquals(DailyCardState.AlreadyPlayed(320, hoursUntilNext = 12), awaitItem().dailyState)
        }
        viewModel.effects.test {
            viewModel.onIntent(HomeIntent.OpenDaily)
            expectNoEvents()
        }

        // Advance 2 hours, resume screen -> hoursUntilNext should update to 10
        clock.advanceBy(2 * 3_600_000L)
        viewModel.onIntent(HomeIntent.Resumed)
        viewModel.state.test {
            assertEquals(DailyCardState.AlreadyPlayed(320, hoursUntilNext = 10), awaitItem().dailyState)
        }
    }

    @Test
    fun `daily wheel state is unavailable when no cards exist and OpenDaily is inert`() = runHomeTest {
        cards.cards = emptyList()
        val viewModel = viewModel()
        viewModel.state.test {
            assertEquals(DailyCardState.Unavailable, awaitItem().dailyState)
        }
        viewModel.effects.test {
            viewModel.onIntent(HomeIntent.OpenDaily)
            expectNoEvents()
        }
    }

    @Test
    fun `play opens the level select`() = runHomeTest {
        val viewModel = viewModel()
        viewModel.effects.test {
            viewModel.onIntent(HomeIntent.Play)

            assertEquals(HomeEffect.NavigateToLevelSelect, awaitItem())
        }
    }

    @Test
    fun `the notebook tile opens the notebook`() = runHomeTest {
        val viewModel = viewModel()
        viewModel.effects.test {
            viewModel.onIntent(HomeIntent.OpenNotebook)

            assertEquals(HomeEffect.NavigateToNotebook, awaitItem())
        }
    }

    @Test
    fun `the statistics tile opens the statistics`() = runHomeTest {
        val viewModel = viewModel()
        viewModel.effects.test {
            viewModel.onIntent(HomeIntent.OpenStats)

            assertEquals(HomeEffect.NavigateToStats, awaitItem())
        }
    }

    @Test
    fun `the gear opens the settings`() = runHomeTest {
        val viewModel = viewModel()
        viewModel.effects.test {
            viewModel.onIntent(HomeIntent.OpenSettings)

            assertEquals(HomeEffect.NavigateToSettings, awaitItem())
        }
    }

    @Test
    fun `the practice tile opens the practice select`() = runHomeTest {
        val viewModel = viewModel()
        viewModel.effects.test {
            viewModel.onIntent(HomeIntent.OpenPractice)

            assertEquals(HomeEffect.NavigateToPractice, awaitItem())
        }
    }

    @Test
    fun `the exam packs tile opens the exam packs page`() = runHomeTest {
        val viewModel = viewModel()
        viewModel.effects.test {
            viewModel.onIntent(HomeIntent.OpenExamPacks)

            assertEquals(HomeEffect.NavigateToExamPacks, awaitItem())
        }
    }
}
