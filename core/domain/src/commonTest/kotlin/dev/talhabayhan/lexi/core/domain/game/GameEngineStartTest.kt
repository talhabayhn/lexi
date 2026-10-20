package dev.talhabayhan.lexi.core.domain.game

import dev.talhabayhan.lexi.core.domain.model.Level
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** GAME_RULES.md §5 (timer) and §12 (`Start(timestampMs, remainingMs)`). */
class GameEngineStartTest {
    private val engine = GameEngine()

    private fun started(mode: GameMode, start: GameEvent.Start): GameState {
        val initial = engine.newGame(
            seed = 5L,
            mode = mode,
            level = Level.A1,
            wheel = wheelOf("APPLE", "BREAD"),
            jokers = allJokers,
        )
        return engine.reduce(initial, start).state
    }

    @Test
    fun `start without remaining time uses the mode's full limit`() {
        assertEquals(120_000L, started(GameMode.LEVEL_TEST, GameEvent.Start(1_000L)).remainingMs)
        assertEquals(180_000L, started(GameMode.CLASSIC, GameEvent.Start(1_000L)).remainingMs)
    }

    @Test
    fun `start with remaining time sets the clock to it`() {
        val state = started(GameMode.LEVEL_TEST, GameEvent.Start(1_000L, remainingMs = 47_250L))

        assertEquals(GameStatus.PLAYING, state.status)
        assertEquals(47_250L, state.remainingMs)
    }

    @Test
    fun `start remaining time is clamped to the mode's limit`() {
        assertEquals(
            120_000L,
            started(GameMode.LEVEL_TEST, GameEvent.Start(1_000L, remainingMs = 999_999L)).remainingMs,
        )
    }

    @Test
    fun `start with no remaining time ends the game at once with time up`() {
        val initial = engine.newGame(
            5L,
            GameMode.LEVEL_TEST,
            Level.A1,
            wheelOf("APPLE"),
        )
        val transition = engine.reduce(initial, GameEvent.Start(1_000L, remainingMs = 0L))

        assertEquals(GameStatus.FINISHED, transition.state.status)
        assertEquals(GameOverReason.TIME_UP, transition.state.summary?.reason)
        assertEquals(0L, transition.state.remainingMs)
        assertTrue(transition.effects.single() is EngineEffect.GameOver)
        assertEquals(1, transition.state.summary?.unanswered)
    }

    @Test
    fun `negative remaining time is clamped to zero and ends the game`() {
        val state = started(GameMode.CLASSIC, GameEvent.Start(1_000L, remainingMs = -5L))

        assertEquals(GameOverReason.TIME_UP, state.summary?.reason)
        assertEquals(0L, state.remainingMs)
    }

    @Test
    fun `untimed modes ignore the remaining time`() {
        val state = started(GameMode.PRACTICE, GameEvent.Start(1_000L, remainingMs = 0L))

        assertEquals(GameStatus.PLAYING, state.status)
        assertNull(state.remainingMs)
    }

    @Test
    fun `same seed and events give the same state with and without remaining time`() {
        fun run(start: GameEvent.Start): GameState = play("APPLE", "BREAD", mode = GameMode.LEVEL_TEST, start = false)
            .send(start, GameEvent.Tick(3_000L), GameEvent.Pass, GameEvent.Tick(500L)).state

        assertEquals(run(GameEvent.Start(1_000L)), run(GameEvent.Start(1_000L)))
        assertEquals(run(GameEvent.Start(1_000L, remainingMs = 60_000L)), run(GameEvent.Start(1_000L, 60_000L)))
        val full = run(GameEvent.Start(1_000L))
        val explicit = run(GameEvent.Start(1_000L, remainingMs = 120_000L))
        assertEquals(full, explicit)
        assertEquals(116_500L, full.remainingMs)
        assertEquals(56_500L, run(GameEvent.Start(1_000L, remainingMs = 60_000L)).remainingMs)
    }

    @Test
    fun `start without remaining time serializes as before`() {
        val serializer = GameEvent.serializer()

        val encoded = Json.encodeToString(serializer, GameEvent.Start(1_000L))

        assertFalse("remainingMs" in encoded, encoded)
        assertEquals(
            GameEvent.Start(1_000L),
            Json.decodeFromString(serializer, """{"type":"start","timestampMs":1000}"""),
        )
        val withTime = GameEvent.Start(1_000L, remainingMs = 5L)
        assertEquals(withTime, Json.decodeFromString(serializer, Json.encodeToString(serializer, withTime)))
    }
}
