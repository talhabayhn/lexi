package dev.talhabayhan.lexi.core.domain.goal

import app.cash.turbine.test
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.session.SessionResult
import dev.talhabayhan.lexi.core.domain.settings.Settings
import dev.talhabayhan.lexi.core.domain.stats.SessionRecord
import dev.talhabayhan.lexi.core.testing.FakeAppClock
import dev.talhabayhan.lexi.core.testing.FakeSessionRepository
import dev.talhabayhan.lexi.core.testing.FakeSettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DailyGoalTest {
    private val clock = FakeAppClock()
    private val sessions = FakeSessionRepository()
    private val settings = FakeSettingsRepository(Settings(dailyGoalWheels = 3))
    private val getDailyGoalProgress = GetDailyGoalProgress(sessions, settings, clock)

    private fun session(
        mode: GameMode = GameMode.CLASSIC,
        day: Long = clock.epochDay(clock.nowMs()),
        completed: Boolean = true,
    ): SessionResult = SessionResult(
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
    fun `counts today only and ignores other days`() = runTest {
        val today = clock.epochDay(clock.nowMs())
        val yesterday = today - 1
        val tomorrow = today + 1

        sessions.record(session(day = yesterday, completed = true))
        sessions.record(session(day = today, completed = true))
        sessions.record(session(day = tomorrow, completed = true))

        val progress = getDailyGoalProgress.observe().first()
        assertEquals(1, progress.done)
        assertEquals(3, progress.goal)
        assertFalse(progress.reached)
    }

    @Test
    fun `midnight edge 23 59 to 00 00 resets today count`() = runTest {
        val today = clock.epochDay(clock.nowMs())
        sessions.record(session(day = today, completed = true))

        val beforeMidnight = getDailyGoalProgress.observe().first()
        assertEquals(1, beforeMidnight.done)

        // Advance past midnight to tomorrow
        val msToMidnight = clock.msUntilNextDay()
        clock.advanceBy(msToMidnight + 1000L)
        val tomorrow = clock.epochDay(clock.nowMs())
        assertEquals(today + 1, tomorrow)

        val afterMidnight = getDailyGoalProgress.observe().first()
        assertEquals(0, afterMidnight.done)
        assertFalse(afterMidnight.reached)

        // A new game completed on the new day counts
        sessions.record(session(day = tomorrow, completed = true))
        val afterNewGame = getDailyGoalProgress.observe().first()
        assertEquals(1, afterNewGame.done)
    }

    @Test
    fun `level test is not counted while timed out and completed games count`() = runTest {
        val today = clock.epochDay(clock.nowMs())

        // Level test: should NOT count
        sessions.record(session(mode = GameMode.LEVEL_TEST, day = today, completed = true))

        val progressInitial = getDailyGoalProgress.observe().first()
        assertEquals(0, progressInitial.done)
        assertFalse(progressInitial.reached)

        // Classic game where time ran out (completed = false in engine): counts as a completed wheel!
        sessions.record(session(mode = GameMode.CLASSIC, day = today, completed = false))
        val progressAfterTimeUp = getDailyGoalProgress.observe().first()
        assertEquals(1, progressAfterTimeUp.done)

        // Classic game with all answered (completed = true): also counts!
        sessions.record(session(mode = GameMode.CLASSIC, day = today, completed = true))
        val progressAfterAllAnswered = getDailyGoalProgress.observe().first()
        assertEquals(2, progressAfterAllAnswered.done)
    }

    @Test
    fun `daily wheel counts as one completed wheel`() = runTest {
        val today = clock.epochDay(clock.nowMs())

        sessions.record(session(mode = GameMode.DAILY, day = today, completed = true))

        val progress = getDailyGoalProgress.observe().first()
        assertEquals(1, progress.done)
    }

    @Test
    fun `reaches goal when completed wheels meet or exceed target`() = runTest {
        val today = clock.epochDay(clock.nowMs())

        sessions.record(session(mode = GameMode.CLASSIC, day = today, completed = true))
        sessions.record(session(mode = GameMode.PRACTICE, day = today, completed = true))
        val progress2 = getDailyGoalProgress.observe().first()
        assertEquals(2, progress2.done)
        assertFalse(progress2.reached)

        // 3rd completed wheel reaches goal of 3
        sessions.record(session(mode = GameMode.HARDCORE, day = today, completed = true))
        val progress3 = getDailyGoalProgress.observe().first()
        assertEquals(3, progress3.done)
        assertTrue(progress3.reached)

        // 4th completed wheel stays reached
        sessions.record(session(mode = GameMode.REVIEW, day = today, completed = true))
        val progress4 = getDailyGoalProgress.observe().first()
        assertEquals(4, progress4.done)
        assertTrue(progress4.reached)
    }

    @Test
    fun `settings change updates the goal value reactively`() = runTest {
        val today = clock.epochDay(clock.nowMs())
        sessions.record(session(mode = GameMode.CLASSIC, day = today, completed = true))

        getDailyGoalProgress.observe().test {
            val initial = awaitItem()
            assertEquals(1, initial.done)
            assertEquals(3, initial.goal)
            assertFalse(initial.reached)

            // Lower goal to 1: goal is reached now!
            settings.update { it.copy(dailyGoalWheels = 1) }
            val updated = awaitItem()
            assertEquals(1, updated.done)
            assertEquals(1, updated.goal)
            assertTrue(updated.reached)
        }
    }
}
