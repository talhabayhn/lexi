package dev.talhabayhan.lexi.feature.home

import app.cash.turbine.test
import dev.talhabayhan.lexi.core.domain.daily.GetDailyWheel
import dev.talhabayhan.lexi.core.domain.game.GameConfig
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.goal.DailyGoalProgress
import dev.talhabayhan.lexi.core.domain.goal.GetDailyGoalProgress
import dev.talhabayhan.lexi.core.domain.joker.GrantDailyJokers
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.session.DailyResult
import dev.talhabayhan.lexi.core.domain.session.SessionResult
import dev.talhabayhan.lexi.core.domain.settings.Settings
import dev.talhabayhan.lexi.core.domain.srs.CardProgress
import dev.talhabayhan.lexi.core.domain.streak.GetStreakStatus
import dev.talhabayhan.lexi.core.domain.time.AppClock
import dev.talhabayhan.lexi.core.testing.FakeAppClock
import dev.talhabayhan.lexi.core.testing.FakeCardRepository
import dev.talhabayhan.lexi.core.testing.FakeContentVersionRepository
import dev.talhabayhan.lexi.core.testing.FakeDailyRepository
import dev.talhabayhan.lexi.core.testing.FakeJokerRepository
import dev.talhabayhan.lexi.core.testing.FakeProgressRepository
import dev.talhabayhan.lexi.core.testing.FakeSessionRepository
import dev.talhabayhan.lexi.core.testing.FakeSettingsRepository
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val clock = FakeAppClock()
    private val today = clock.epochDay(clock.nowMs())
    private val progress = FakeProgressRepository()
    private val streaks = FakeStreakRepository()
    private val jokers = FakeJokerRepository()
    private val cards = FakeCardRepository()
    private val contentVersion = FakeContentVersionRepository()
    private val daily = FakeDailyRepository()
    private val sessions = FakeSessionRepository()
    private val settings = FakeSettingsRepository()
    private val getStreakStatus = GetStreakStatus(streaks, clock)
    private val getDailyWheel = GetDailyWheel(cards, contentVersion, daily, clock)
    private val getDailyGoalProgress = GetDailyGoalProgress(sessions, settings, clock)
    private val grantDailyJokers = GrantDailyJokers(jokers, streaks, clock, GameConfig())

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
        HomeViewModel(progress, getStreakStatus, getDailyWheel, daily, clock, getDailyGoalProgress, grantDailyJokers)

    private fun runHomeTest(block: suspend TestScope.() -> Unit) = runTest {
        block()
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
            viewModel.onIntent(HomeIntent.Refresh)

            val after = expectMostRecentItem()
            assertEquals(16, after.dailyDay)
            assertEquals(1, after.dueReviews)
            // Yesterday was played, today is still open: the streak is alive.
            assertEquals(1, after.streakDays)
        }
    }

    @Test
    fun `streak and daily card recompute on refresh when midnight passes while app is open`() = runHomeTest {
        streaks.days = listOf(today)
        daily.record(DailyResult(today, Level.A1, score = 150, timeMs = 45000))
        val viewModel = viewModel()
        viewModel.state.test {
            val before = expectMostRecentItem()
            assertEquals(1, before.streakDays)
            assertEquals(15, before.dailyDay)
            assertEquals(
                DailyCardState.AlreadyPlayed(150, hoursUntilNext = 12, minutesUntilNext = 0),
                before.dailyState,
            )

            // Advance past midnight and drive periodic refresh
            val msToMidnight = clock.msUntilNextDay()
            clock.advanceBy(msToMidnight + 1000L)
            viewModel.onIntent(HomeIntent.Refresh)

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
            viewModel.onIntent(HomeIntent.Refresh)

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
    fun `daily wheel state is already played with score and minute resolution countdown`() = runHomeTest {
        daily.record(DailyResult(today, Level.A1, score = 320, timeMs = 85000))
        val viewModel = viewModel()
        viewModel.state.test {
            assertEquals(
                DailyCardState.AlreadyPlayed(320, hoursUntilNext = 12, minutesUntilNext = 0),
                awaitItem().dailyState,
            )
        }
        viewModel.effects.test {
            viewModel.onIntent(HomeIntent.OpenDaily)
            expectNoEvents()
        }

        // Advance 2 hours 15 minutes, refresh -> 9 hours 45 minutes
        clock.advanceBy(2 * 3_600_000L + 15 * 60_000L)
        viewModel.onIntent(HomeIntent.Refresh)
        viewModel.state.test {
            assertEquals(
                DailyCardState.AlreadyPlayed(320, hoursUntilNext = 9, minutesUntilNext = 45),
                awaitItem().dailyState,
            )
        }
    }

    @Test
    fun `daily card countdown under one hour shows minute resolution`() = runHomeTest {
        daily.record(DailyResult(today, Level.A1, score = 200, timeMs = 30000))
        val viewModel = viewModel()
        val msUntilMidnight = clock.msUntilNextDay()
        clock.advanceBy(msUntilMidnight - 45 * 60_000L)
        viewModel.onIntent(HomeIntent.Refresh)

        viewModel.state.test {
            assertEquals(
                DailyCardState.AlreadyPlayed(200, hoursUntilNext = 0, minutesUntilNext = 45),
                expectMostRecentItem().dailyState,
            )
        }
    }

    @Test
    fun `daily card countdown handles day with DST transition`() = runHomeTest {
        val dstClock = object : AppClock {
            var now = 10_000_000L
            override fun nowMs(): Long = now
            override fun epochDay(atMs: Long): Long = 100L
            override fun msUntilNextDay(atMs: Long): Long = 82_800_000L - atMs // 23h DST spring day
        }
        val getDaily = GetDailyWheel(cards, contentVersion, daily, dstClock)
        daily.record(DailyResult(100L, Level.A1, score = 180, timeMs = 20000))
        val vm = HomeViewModel(
            progress,
            GetStreakStatus(streaks, dstClock),
            getDaily,
            daily,
            dstClock,
            GetDailyGoalProgress(sessions, settings, dstClock),
            GrantDailyJokers(jokers, streaks, dstClock, GameConfig()),
        )
        vm.state.test {
            val state = awaitItem()
            val dailyState = state.dailyState as DailyCardState.AlreadyPlayed
            assertEquals(20, dailyState.hoursUntilNext)
            assertEquals(13, dailyState.minutesUntilNext)
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

    private fun session(mode: GameMode = GameMode.CLASSIC, day: Long = today, completed: Boolean = true) =
        SessionResult(
            mode = mode,
            level = Level.A1,
            category = CategoryId("basics"),
            seed = 1L,
            startedAtMs = clock.nowMs() - 60_000L,
            endedAtMs = clock.nowMs(),
            epochDay = day,
            score = 200,
            correct = 20,
            wrong = 3,
            passed = 3,
            completed = completed,
        )

    @Test
    fun `daily goal progress reflects completed wheels and updates on session record`() = runHomeTest {
        settings.update { it.copy(dailyGoalWheels = 2) }
        val viewModel = viewModel()
        viewModel.state.test {
            val initial = expectMostRecentItem()
            assertEquals(DailyGoalProgress(done = 0, goal = 2, reached = false), initial.dailyGoalProgress)

            sessions.record(session(completed = true))
            val afterOne = expectMostRecentItem()
            assertEquals(DailyGoalProgress(done = 1, goal = 2, reached = false), afterOne.dailyGoalProgress)

            sessions.record(session(completed = true))
            val reached = expectMostRecentItem()
            assertEquals(DailyGoalProgress(done = 2, goal = 2, reached = true), reached.dailyGoalProgress)
        }
    }

    @Test
    fun `daily goal progress updates when settings goal is changed`() = runHomeTest {
        sessions.record(session(completed = true))
        val viewModel = viewModel()
        viewModel.state.test {
            val initial = expectMostRecentItem()
            assertEquals(DailyGoalProgress(done = 1, goal = 1, reached = true), initial.dailyGoalProgress)

            settings.update { it.copy(dailyGoalWheels = 3) }
            val updated = expectMostRecentItem()
            assertEquals(DailyGoalProgress(done = 1, goal = 3, reached = false), updated.dailyGoalProgress)
        }
    }

    @Test
    fun `daily goal progress resets today count after midnight refresh`() = runHomeTest {
        settings.update { it.copy(dailyGoalWheels = 2) }
        sessions.record(session(day = today, completed = true))
        val viewModel = viewModel()
        viewModel.state.test {
            val before = expectMostRecentItem()
            assertEquals(DailyGoalProgress(done = 1, goal = 2, reached = false), before.dailyGoalProgress)

            // Advance past midnight and trigger refresh
            val msToMidnight = clock.msUntilNextDay()
            clock.advanceBy(msToMidnight + 1000L)
            viewModel.onIntent(HomeIntent.Refresh)

            val after = expectMostRecentItem()
            assertEquals(DailyGoalProgress(done = 0, goal = 2, reached = false), after.dailyGoalProgress)
        }
    }

    @Test
    fun `daily gift notice shows once when grant is returned and dismisses on intent`() = runHomeTest {
        val vm = viewModel()
        vm.state.test {
            val state = expectMostRecentItem()
            assertEquals(DailyGiftNotice(DailyGiftNoticeType.Daily, 1), state.dailyGiftNotice)

            vm.onIntent(HomeIntent.DismissNotice)
            val dismissed = expectMostRecentItem()
            assertEquals(null, dismissed.dailyGiftNotice)
        }
    }

    @Test
    fun `no grant shows no daily gift notice`() = runHomeTest {
        // Pre-grant for today so GrantDailyJokers returns emptyList()
        grantDailyJokers()
        val vm = viewModel()
        vm.state.test {
            val state = expectMostRecentItem()
            assertEquals(null, state.dailyGiftNotice)
        }
    }

    @Test
    fun `daily gift notice is not re-shown on the same day after refresh`() = runHomeTest {
        val vm = viewModel()
        vm.state.test {
            val state = expectMostRecentItem()
            assertEquals(DailyGiftNotice(DailyGiftNoticeType.Daily, 1), state.dailyGiftNotice)

            // Dismiss the notice
            vm.onIntent(HomeIntent.DismissNotice)
            assertEquals(null, expectMostRecentItem().dailyGiftNotice)

            // Refresh on the same day: should not re-show
            vm.onIntent(HomeIntent.Refresh)
            expectNoEvents()
            assertEquals(null, vm.state.value.dailyGiftNotice)
        }
    }
}
