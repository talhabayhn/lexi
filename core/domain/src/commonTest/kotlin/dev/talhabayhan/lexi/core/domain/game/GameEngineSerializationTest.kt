package dev.talhabayhan.lexi.core.domain.game

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

/** Event logs and states must survive JSON (replays, golden tests, online events). */
class GameEngineSerializationTest {
    @Test
    fun `game state survives a JSON round trip mid-game`() {
        val state = play("APPLE", "BREAD", "CHAIR").type("AP").send(GameEvent.Pass).answerWrong().state

        val json = Json.encodeToString(GameState.serializer(), state)

        assertEquals(state, Json.decodeFromString(GameState.serializer(), json))
    }

    @Test
    fun `event log survives a JSON round trip`() {
        val events = listOf(
            GameEvent.Start(1L),
            GameEvent.KeyTapped(3),
            GameEvent.Backspace,
            GameEvent.UseJoker(JokerType.REVEAL_LETTER),
            GameEvent.Tick(16L),
            GameEvent.Pass,
            GameEvent.Pause,
            GameEvent.Resume,
            GameEvent.Forfeit,
        )
        val serializer = ListSerializer(GameEvent.serializer())

        assertEquals(events, Json.decodeFromString(serializer, Json.encodeToString(serializer, events)))
    }

    @Test
    fun `replaying a decoded event log gives the same final state`() {
        val events = listOf(GameEvent.Start(1L), GameEvent.KeyTapped(0), GameEvent.Pass, GameEvent.Tick(1_000L))
        val serializer = ListSerializer(GameEvent.serializer())
        val decoded = Json.decodeFromString(serializer, Json.encodeToString(serializer, events))

        val original = play("APPLE", "BREAD", start = false).send(*events.toTypedArray()).state
        val replayed = play("APPLE", "BREAD", start = false).send(*decoded.toTypedArray()).state

        assertEquals(original, replayed)
    }
}
