package dev.talhabayhan.lexi.core.domain.game

import dev.talhabayhan.lexi.core.domain.model.CardId
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

/** Event logs and states must survive JSON (replays, golden tests, online events). */
class GameEngineSerializationTest {
    @Test
    fun `game state survives a JSON round trip mid-game`() {
        val state = play("APPLE", "BREAD", "CHAIR").type("PP").send(GameEvent.Pass).answerWrong().state

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
    fun `every engine effect survives a JSON round trip`() {
        val summary = checkNotNull(play("APPLE").send(GameEvent.Forfeit).state.summary)
        val effects = listOf(
            EngineEffect.LetterPlaced(cellIndex = 2, letter = 'P'),
            EngineEffect.Correct(letter = 'A', card = CardId("a1-apple"), points = 12),
            EngineEffect.Wrong(letter = 'B', card = CardId("a1-bread")),
            EngineEffect.Passed(letter = 'C'),
            EngineEffect.Advanced(toLetter = 'D', lap = 2),
            EngineEffect.ComboReached(streak = 5),
            EngineEffect.TimeWarning(secondsLeft = 7),
            EngineEffect.JokerApplied(JokerType.LISTEN),
            EngineEffect.TurnChanged(player = 1),
            EngineEffect.GameOver(summary),
            EngineEffect.MatchOver(MatchResult(MatchEndReason.ALL_FINISHED, winner = null)),
        )
        val serializer = ListSerializer(EngineEffect.serializer())

        assertEquals(effects, Json.decodeFromString(serializer, Json.encodeToString(serializer, effects)))
        assertEquals(EFFECT_KINDS, effects.map { it.kind() }.toSet().size)
    }

    /** Exhaustive: adding an effect type breaks compilation here until the test above covers it. */
    private fun EngineEffect.kind(): Int = when (this) {
        is EngineEffect.LetterPlaced -> 0
        is EngineEffect.Correct -> 1
        is EngineEffect.Wrong -> 2
        is EngineEffect.Passed -> 3
        is EngineEffect.Advanced -> 4
        is EngineEffect.ComboReached -> 5
        is EngineEffect.TimeWarning -> 6
        is EngineEffect.JokerApplied -> 7
        is EngineEffect.TurnChanged -> 8
        is EngineEffect.GameOver -> 9
        is EngineEffect.MatchOver -> 10
    }

    @Test
    fun `both match events survive a JSON round trip`() {
        val events =
            listOf(MatchEvent.BeginTurn(timestampMs = 42L), MatchEvent.Play(GameEvent.UseJoker(JokerType.EXAMPLE)))
        val serializer = ListSerializer(MatchEvent.serializer())

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

    private companion object {
        const val EFFECT_KINDS = 11
    }
}
