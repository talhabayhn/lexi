package dev.talhabayhan.lexi.feature.home

import app.cash.turbine.test
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.srs.CardProgress
import dev.talhabayhan.lexi.core.testing.FakeAppClock
import dev.talhabayhan.lexi.core.testing.FakeProgressRepository
import dev.talhabayhan.lexi.core.testing.FakeStreakRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = HomeViewModel(progress, streaks, clock)

    private fun card(id: String, box: Int? = null, due: Long? = null, correct: Int = 0) =
        CardProgress(CardId(id), box = box, dueEpochDay = due, seen = 1, correct = correct)

    @Test
    fun `nothing played yet shows real zeros and today's date`() = runTest {
        viewModel().state.test {
            val state = awaitItem()
            assertEquals(0, state.streakDays)
            assertEquals(0, state.dueReviews)
            assertEquals(0, state.learnedWords)
            assertEquals(15, state.dailyDay)
            assertEquals(10, state.dailyMonth)
        }
    }

    @Test
    fun `numbers come from the repositories and follow their changes`() = runTest {
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
    fun `play opens the level select`() = runTest {
        val viewModel = viewModel()
        viewModel.effects.test {
            viewModel.onIntent(HomeIntent.Play)

            assertEquals(HomeEffect.NavigateToLevelSelect, awaitItem())
        }
    }

    @Test
    fun `tiles of later milestones are inert`() = runTest {
        val viewModel = viewModel()
        viewModel.effects.test {
            listOf(
                HomeIntent.OpenDaily,
                HomeIntent.OpenSettings,
                HomeIntent.OpenNotebook,
                HomeIntent.OpenStats,
                HomeIntent.OpenPractice,
                HomeIntent.OpenExamPacks,
            ).forEach(viewModel::onIntent)

            expectNoEvents()
        }
    }

    @Test
    fun `streak counts the run ending today or yesterday`() {
        assertEquals(0, currentStreak(emptyList(), today))
        assertEquals(1, currentStreak(listOf(today), today))
        // Today is still open: yesterday's run is alive.
        assertEquals(2, currentStreak(listOf(today - 2, today - 1), today))
        // A day was skipped: the run ended.
        assertEquals(0, currentStreak(listOf(today - 3, today - 2), today))
        // Only the latest run counts, whatever the order.
        assertEquals(2, currentStreak(listOf(today, today - 1, today - 5, today - 4, today - 3), today))
    }
}
