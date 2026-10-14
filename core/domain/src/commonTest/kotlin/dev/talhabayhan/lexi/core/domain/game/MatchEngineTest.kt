package dev.talhabayhan.lexi.core.domain.game

import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.random.Seeds
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/** GAME_RULES.md §8 (hot-seat duel). */
class MatchEngineTest {
    private class Duel(var state: MatchState, private val engine: MatchEngine) {
        val effects = mutableListOf<EngineEffect>()
        var lastEffects: List<EngineEffect> = emptyList()
            private set

        fun send(vararg events: MatchEvent): Duel {
            val collected = mutableListOf<EngineEffect>()
            for (event in events) {
                val transition = engine.reduce(state, event)
                state = transition.state
                collected += transition.effects
            }
            lastEffects = collected
            effects += collected
            return this
        }

        fun play(vararg events: GameEvent): Duel = send(*events.map { MatchEvent.Play(it) }.toTypedArray())

        fun begin(): Duel = send(MatchEvent.BeginTurn(timestampMs = 5_000L))

        val active: GameState get() = state.players[state.activePlayer]

        fun answer(wrong: Boolean = false): Duel {
            val letters = checkNotNull(active.currentSlot?.card).answer.filter { it != ' ' }
            val word = if (wrong) letters.drop(1) + letters.first() else letters
            for (letter in word) {
                val input = checkNotNull(active.currentInput)
                play(
                    GameEvent.KeyTapped(
                        input.keys.indexOfFirst {
                            it.letter == letter && it.state == KeyState.AVAILABLE
                        },
                    ),
                )
            }
            return this
        }

        fun wrongAndWait(): Duel = answer(wrong = true).play(GameEvent.Tick(1_200L))

        fun pass(): Duel = play(GameEvent.Pass)
    }

    private fun duel(
        first: Wheel = wheelOf("APPLE", "BREAD", "CHAIR"),
        second: Wheel = wheelOf("DOG", "EGG", "FISH"),
        seed: Long = 9L,
    ): Duel {
        val engine = MatchEngine()
        return Duel(engine.newMatch(seed = seed, level = Level.A1, wheels = listOf(first, second)), engine)
    }

    @Test
    fun `each player gets an own seed derived from the match seed`() {
        val match = duel(seed = 9L).state

        assertEquals(Seeds.derive(9L, "player:1"), match.players[0].seed)
        assertEquals(Seeds.derive(9L, "player:2"), match.players[1].seed)
        assertEquals(MatchEngine.playerSeed(9L, 1), match.players[1].seed)
        assertNotEquals(match.players[0].seed, match.players[1].seed)
        assertTrue(match.players.all { it.mode == GameMode.DUEL })
    }

    @Test
    fun `first turn starts when the handover card is dismissed`() {
        val game = duel()
        assertEquals(MatchPhase.HANDOVER, game.state.phase)
        val before = game.state
        game.play(GameEvent.KeyTapped(0), GameEvent.Tick(1_000L))
        assertSame(before, game.state)

        game.begin()

        assertEquals(MatchPhase.PLAYING, game.state.phase)
        assertEquals(GameStatus.PLAYING, game.state.players[0].status)
        assertEquals(GameStatus.NOT_STARTED, game.state.players[1].status)
        assertEquals(listOf<EngineEffect>(EngineEffect.Advanced('A', lap = 1)), game.lastEffects)
    }

    @Test
    fun `each player has a 150 s clock`() {
        val game = duel().begin()

        assertEquals(150_000L, game.active.remainingMs)
    }

    @Test
    fun `correct answer keeps the turn`() {
        val game = duel().begin().answer()

        assertEquals(0, game.state.activePlayer)
        assertEquals(MatchPhase.PLAYING, game.state.phase)
        assertEquals('B', game.active.currentSlot?.letter)
    }

    @Test
    fun `pass ends the turn and shows the handover card`() {
        val game = duel().begin().pass()

        assertEquals(1, game.state.activePlayer)
        assertEquals(MatchPhase.HANDOVER, game.state.phase)
        assertEquals(EngineEffect.TurnChanged(player = 1), game.lastEffects.last())
    }

    @Test
    fun `wrong answer ends the turn after the reveal`() {
        val game = duel().begin().answer(wrong = true)
        assertEquals(0, game.state.activePlayer)
        assertEquals(MatchPhase.PLAYING, game.state.phase)

        game.play(GameEvent.Tick(1_200L))

        assertEquals(1, game.state.activePlayer)
        assertEquals(MatchPhase.HANDOVER, game.state.phase)
        assertEquals(EngineEffect.TurnChanged(player = 1), game.lastEffects.last())
    }

    @Test
    fun `second player starts on their first turn`() {
        val game = duel().begin().pass().send(MatchEvent.BeginTurn(timestampMs = 8_000L))

        assertEquals(GameStatus.PLAYING, game.active.status)
        assertEquals(8_000L, game.active.startedAtMs)
        assertEquals('D', game.active.currentSlot?.letter)
    }

    @Test
    fun `turn comes back to a player with their next slot`() {
        val game = duel().begin().pass().begin().pass().begin()

        assertEquals(0, game.state.activePlayer)
        assertEquals('B', game.active.currentSlot?.letter)
        assertTrue(game.lastEffects.isEmpty())
    }

    @Test
    fun `clock runs only on the active player's turn`() {
        val game = duel().begin().play(GameEvent.Tick(10_000L)).pass().begin().play(GameEvent.Tick(3_000L))

        assertEquals(140_000L, game.state.players[0].remainingMs)
        assertEquals(147_000L, game.state.players[1].remainingMs)
    }

    @Test
    fun `clock stands still during the handover`() {
        val game = duel().begin().pass().play(GameEvent.Tick(10_000L)).begin()

        assertEquals(150_000L, game.state.players[0].remainingMs)
        assertEquals(150_000L, game.state.players[1].remainingMs)
    }

    @Test
    fun `player whose clock runs out hands over to the other`() {
        val game = duel().begin().play(GameEvent.Tick(150_000L))

        assertEquals(GameOverReason.TIME_UP, game.state.players[0].summary?.reason)
        assertEquals(1, game.state.activePlayer)
        assertEquals(MatchPhase.HANDOVER, game.state.phase)
    }

    @Test
    fun `when one wheel is finished the other player keeps the turn without handovers`() {
        val game = duel().begin().answer().answer().answer()
        assertEquals(GameStatus.FINISHED, game.state.players[0].status)
        assertEquals(1, game.state.activePlayer)

        game.begin().pass()

        assertEquals(1, game.state.activePlayer)
        assertEquals(MatchPhase.PLAYING, game.state.phase)
        assertTrue(game.lastEffects.none { it is EngineEffect.TurnChanged })
        assertEquals('E', game.active.currentSlot?.letter)
    }

    @Test
    fun `match ends when both wheels are finished`() {
        val game = duel().begin().answer().answer().answer().begin().answer().answer().wrongAndWait()

        assertEquals(MatchPhase.FINISHED, game.state.phase)
        assertEquals(MatchResult(reason = MatchEndReason.ALL_FINISHED, winner = 0), game.state.result)
        assertEquals(
            EngineEffect.MatchOver(MatchResult(MatchEndReason.ALL_FINISHED, winner = 0)),
            game.lastEffects.last(),
        )
    }

    @Test
    fun `match ends when both clocks are at 0`() {
        val game = duel().begin().play(GameEvent.Tick(150_000L)).begin().answer().play(GameEvent.Tick(150_000L))

        assertEquals(MatchPhase.FINISHED, game.state.phase)
        assertEquals(1, game.state.result?.winner)
    }

    @Test
    fun `events after the match are ignored`() {
        val game = duel().begin().play(GameEvent.Tick(150_000L)).begin().play(GameEvent.Tick(150_000L))
        val before = game.state

        game.begin().play(GameEvent.Pass)

        assertSame(before, game.state)
    }

    @Test
    fun `forfeit ends the match and the other player wins`() {
        val game = duel().begin().answer().play(GameEvent.Forfeit)

        assertEquals(MatchPhase.FINISHED, game.state.phase)
        assertEquals(MatchResult(MatchEndReason.FORFEIT, winner = 1), game.state.result)
    }

    @Test
    fun `jokers are not allowed in duel`() {
        val game = duel().begin()
        val before = game.state

        game.play(GameEvent.UseJoker(JokerType.REVEAL_LETTER))

        assertSame(before, game.state)
    }

    @Test
    fun `start inside the match is ignored`() {
        val game = duel().begin()
        val before = game.state

        game.play(GameEvent.Start(1L))

        assertSame(before, game.state)
    }

    @Test
    fun `winner has more correct answers`() {
        assertEquals(1, MatchEngine.winnerOf(summary(correct = 3), summary(correct = 4)))
    }

    @Test
    fun `tie on correct goes to fewer wrong`() {
        assertEquals(0, MatchEngine.winnerOf(summary(correct = 3, wrong = 1), summary(correct = 3, wrong = 2)))
    }

    @Test
    fun `tie on wrong goes to more remaining time`() {
        val a = summary(correct = 3, wrong = 1, remainingMs = 10_000L)
        val b = summary(correct = 3, wrong = 1, remainingMs = 20_000L)

        assertEquals(1, MatchEngine.winnerOf(a, b))
    }

    @Test
    fun `full tie is a draw`() {
        assertNull(MatchEngine.winnerOf(summary(correct = 3), summary(correct = 3)))
    }

    @Test
    fun `match state survives a JSON round trip`() {
        val state = duel().begin().answer().pass().state

        val json = Json.encodeToString(MatchState.serializer(), state)

        assertEquals(state, Json.decodeFromString(MatchState.serializer(), json))
    }

    private fun summary(correct: Int, wrong: Int = 0, remainingMs: Long = 0L) = GameSummary(
        reason = GameOverReason.ALL_ANSWERED,
        score = 0,
        timeBonus = 0,
        completed = true,
        correct = correct,
        wrong = wrong,
        passed = 0,
        skipped = 0,
        unanswered = 0,
        longestStreak = 0,
        remainingMs = remainingMs,
        correctCards = emptyList(),
        missedCards = emptyList(),
    )
}
