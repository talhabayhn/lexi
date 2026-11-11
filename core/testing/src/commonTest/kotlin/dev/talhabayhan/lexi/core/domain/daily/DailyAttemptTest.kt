package dev.talhabayhan.lexi.core.domain.daily

import dev.talhabayhan.lexi.core.domain.game.GameConfig
import dev.talhabayhan.lexi.core.domain.game.GameEngine
import dev.talhabayhan.lexi.core.domain.game.GameEvent
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.game.GameState
import dev.talhabayhan.lexi.core.domain.game.KeyState
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.session.DailyResult
import dev.talhabayhan.lexi.core.testing.FakeAppClock
import dev.talhabayhan.lexi.core.testing.FakeCardRepository
import dev.talhabayhan.lexi.core.testing.FakeContentVersionRepository
import dev.talhabayhan.lexi.core.testing.FakeDailyRepository
import dev.talhabayhan.lexi.core.testing.testAlphabetPool
import dev.talhabayhan.lexi.core.testing.testWheel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

// Lives in core:testing because it needs the repository fakes (see FinishGameTest).
class DailyAttemptTest {
    private val engine = GameEngine(GameConfig())
    private val clock = FakeAppClock()
    private val daily = FakeDailyRepository()
    private val attempt = DailyAttempt(daily, clock)
    private val today = clock.epochDay(clock.nowMs())

    /** A started game of [mode] with APPLE answered. */
    private fun playing(mode: GameMode = GameMode.DAILY): GameState {
        var state = engine.newGame(1L, mode, Level.A1, testWheel("APPLE", "BREAD"))
        state = engine.reduce(state, GameEvent.Start(timestampMs = clock.nowMs())).state
        "PPLE".forEach { letter ->
            val keys = checkNotNull(state.currentInput).keys
            val index = keys.indices.first { keys[it].letter == letter && keys[it].state == KeyState.AVAILABLE }
            state = engine.reduce(state, GameEvent.KeyTapped(index)).state
        }
        return state
    }

    @Test
    fun `a started attempt makes the day played with no points`() = runTest {
        assertTrue(attempt.started(today))

        assertEquals(DailyResult(epochDay = today, score = 0, timeMs = 0L), daily.get(today))
        val wheel = GetDailyWheel(FakeCardRepository(testAlphabetPool()), FakeContentVersionRepository(1), daily, clock)
        assertIs<DailyPlayState.AlreadyPlayed>(wheel(today))
    }

    @Test
    fun `a day that already has an attempt is not started again`() = runTest {
        daily.record(DailyResult(epochDay = today, score = 120, timeMs = 60_000L))

        assertFalse(attempt.started(today))
        assertEquals(120, daily.get(today)?.score)
    }

    @Test
    fun `progress replaces the attempt with the score and time so far`() = runTest {
        attempt.started(today)
        val game = playing()
        clock.advanceBy(42_000L)

        attempt.progress(game, today)

        assertTrue(game.score > 0)
        assertEquals(DailyResult(epochDay = today, score = game.score, timeMs = 42_000L), daily.get(today))
    }

    @Test
    fun `progress ignores other modes`() = runTest {
        attempt.progress(playing(GameMode.CLASSIC), today)

        assertNull(daily.get(today))
    }
}
