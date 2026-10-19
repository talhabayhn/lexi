package dev.talhabayhan.lexi.core.domain.game

import dev.talhabayhan.lexi.core.domain.card
import dev.talhabayhan.lexi.core.domain.model.Card
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.random.SeededRandom
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

/**
 * Golden tests (ARCHITECTURE.md §4, ADR 0002): seed + event log ⇒ exact final state, compared as pretty JSON
 * against the snapshots in `GoldenSnapshots.kt`. A failure means game behaviour changed for every device and
 * every stored replay. Update a snapshot only deliberately: paste the "actual" JSON from the failure message and
 * explain the rule change in the commit.
 */
class GoldenTest {
    private val json = Json { prettyPrint = true }

    private val pool: List<Card> =
        listOf("APPLE", "ARM", "BREAD", "BOOK", "CHAIR", "CAT", "DOG", "DESK", "EGG", "EYE").map { card(it) }

    @Test
    fun `classic game with passes laps jokers wrong answers and time up matches the golden snapshot`() {
        val seed = 2024L
        val wheel = WheelGenerator.generate(
            pool = pool,
            level = Level.A1,
            category = CategoryId.MIXED,
            mode = GameMode.CLASSIC,
            random = SeededRandom(seed).derive(WheelGenerator.SEED_PURPOSE),
        )
        val engine = GameEngine()
        // Recorded before the given first letter (GAME_RULES.md §3): replays with the old rule.
        val initial = engine.newGame(
            seed,
            GameMode.CLASSIC,
            Level.A1,
            wheel,
            GameConfig().jokerStartPack,
            prefillsFirstLetter = false,
        )

        val final = CLASSIC_EVENTS.fold(initial) { state, event -> engine.reduce(state, event).state }

        assertGolden("classic", GOLDEN_CLASSIC, json.encodeToString(GameState.serializer(), final))
        assertEquals(final, json.decodeFromString(GameState.serializer(), GOLDEN_CLASSIC))
    }

    @Test
    fun `classic game with the given first letter matches the golden snapshot`() {
        val seed = 2024L
        val wheel = WheelGenerator.generate(
            pool = pool,
            level = Level.A1,
            category = CategoryId.MIXED,
            mode = GameMode.CLASSIC,
            random = SeededRandom(seed).derive(WheelGenerator.SEED_PURPOSE),
        )
        val engine = GameEngine()
        val initial = engine.newGame(seed, GameMode.CLASSIC, Level.A1, wheel, GameConfig().jokerStartPack)

        val final = GIVEN_LETTER_EVENTS.fold(initial) { state, event -> engine.reduce(state, event).state }

        assertGolden("given-letter", GOLDEN_GIVEN_LETTER, json.encodeToString(GameState.serializer(), final))
        assertEquals(final, json.decodeFromString(GameState.serializer(), GOLDEN_GIVEN_LETTER))
    }

    @Test
    fun `hot-seat duel matches the golden snapshot`() {
        val seed = 77L
        val wheels = (0 until MatchEngine.PLAYERS).map { player ->
            WheelGenerator.generate(
                pool = pool.filter { it.letter <= 'C' },
                level = Level.A1,
                category = CategoryId.MIXED,
                mode = GameMode.DUEL,
                random = SeededRandom(MatchEngine.playerSeed(seed, player)).derive(WheelGenerator.SEED_PURPOSE),
            )
        }
        val engine = MatchEngine()
        // Recorded before the given first letter (GAME_RULES.md §3): replays with the old rule.
        val initial = engine.newMatch(seed, Level.A1, wheels, prefillsFirstLetter = false)

        val final = DUEL_EVENTS.fold(initial) { state, event -> engine.reduce(state, event).state }

        assertGolden("duel", GOLDEN_DUEL, json.encodeToString(MatchState.serializer(), final))
        assertEquals(final, json.decodeFromString(MatchState.serializer(), GOLDEN_DUEL))
    }

    /** Line-by-line comparison that names the first differing line and prints the full actual JSON. */
    private fun assertGolden(name: String, expected: String, actual: String) {
        if (expected == actual) return
        val expectedLines = expected.lines()
        val actualLines = actual.lines()
        val line = (0 until maxOf(expectedLines.size, actualLines.size))
            .first { expectedLines.getOrNull(it) != actualLines.getOrNull(it) }
        fail(
            """
            |Golden snapshot '$name' differs at line ${line + 1}:
            |  expected: ${expectedLines.getOrNull(line) ?: "<end>"}
            |  actual:   ${actualLines.getOrNull(line) ?: "<end>"}
            |Update GoldenSnapshots.kt only if the rule change is intended. Actual JSON:
            |$actual
            """.trimMargin(),
        )
    }

    internal companion object {
        /** Wheel (seed 2024): A=APPLE, B=BREAD, C=CAT, D=DOG, E=EGG, F–Z skipped. Key indices from that seed. */
        val CLASSIC_EVENTS: List<GameEvent> = listOf(
            GameEvent.Start(timestampMs = 1_000L),
            // A: A, distractor R, Backspace, P, Reveal letter (P), L, E → correct with joker.
            GameEvent.KeyTapped(6),
            GameEvent.KeyTapped(1),
            GameEvent.Backspace,
            GameEvent.KeyTapped(0),
            GameEvent.UseJoker(JokerType.REVEAL_LETTER),
            GameEvent.KeyTapped(9),
            GameEvent.KeyTapped(5),
            // B: pass.
            GameEvent.Tick(4_000L),
            GameEvent.Pass,
            // C: "ATC" → wrong, reveal.
            GameEvent.KeyTapped(0),
            GameEvent.KeyTapped(7),
            GameEvent.KeyTapped(2),
            GameEvent.Tick(1_200L),
            // D: Example joker, DOG → correct.
            GameEvent.UseJoker(JokerType.EXAMPLE),
            GameEvent.KeyTapped(7),
            GameEvent.KeyTapped(2),
            GameEvent.KeyTapped(6),
            // E: EGG → correct, streak 2; wraps to B on lap 2.
            GameEvent.KeyTapped(5),
            GameEvent.KeyTapped(4),
            GameEvent.KeyTapped(8),
            // B (lap 2): Remove distractors, B, pause (tick ignored), pass (clears the typed B) → lap 3, time up.
            GameEvent.UseJoker(JokerType.REMOVE_DISTRACTORS),
            GameEvent.KeyTapped(2),
            GameEvent.Pause,
            GameEvent.Tick(3_000L),
            GameEvent.Resume,
            GameEvent.Tick(500L),
            GameEvent.Pass,
            GameEvent.Tick(200_000L),
        )

        /**
         * Same wheel and keys as [CLASSIC_EVENTS], with the given first letter (A, B, D, E are STARTS slots; key 6 A,
         * 2 B, 7 D and 5 E are used from the start).
         */
        val GIVEN_LETTER_EVENTS: List<GameEvent> = listOf(
            GameEvent.Start(timestampMs = 1_000L),
            // A: P, distractor R, Backspace, Reveal letter (second P, key 8), L, E → correct with joker.
            GameEvent.KeyTapped(0),
            GameEvent.KeyTapped(1),
            GameEvent.Backspace,
            GameEvent.UseJoker(JokerType.REVEAL_LETTER),
            GameEvent.KeyTapped(9),
            GameEvent.KeyTapped(5),
            // B: pass.
            GameEvent.Tick(4_000L),
            GameEvent.Pass,
            // C: "CTA" → wrong, reveal.
            GameEvent.KeyTapped(7),
            GameEvent.KeyTapped(0),
            GameEvent.Tick(1_200L),
            // D: Example joker, OG → correct.
            GameEvent.UseJoker(JokerType.EXAMPLE),
            GameEvent.KeyTapped(2),
            GameEvent.KeyTapped(6),
            // E: GG → correct, streak 2; wraps to B on lap 2.
            GameEvent.KeyTapped(4),
            GameEvent.KeyTapped(8),
            // B (lap 2): Remove distractors, R, Backspace, Backspace on the given B (ignored), pause, pass → time up.
            GameEvent.UseJoker(JokerType.REMOVE_DISTRACTORS),
            GameEvent.KeyTapped(6),
            GameEvent.Backspace,
            GameEvent.Backspace,
            GameEvent.Pause,
            GameEvent.Tick(3_000L),
            GameEvent.Resume,
            GameEvent.Tick(500L),
            GameEvent.Pass,
            GameEvent.Tick(200_000L),
        )

        /** Player 1 (seed 77): ARM, BREAD, CHAIR; player 2: APPLE, BREAD, CHAIR. */
        val DUEL_EVENTS: List<MatchEvent> = listOf(
            MatchEvent.BeginTurn(timestampMs = 1_000L),
            // P1: ARM correct, BREAD typed "RBEAD" → wrong, reveal ends the turn.
            play(GameEvent.KeyTapped(5)),
            play(GameEvent.KeyTapped(1)),
            play(GameEvent.KeyTapped(6)),
            play(GameEvent.KeyTapped(3)),
            play(GameEvent.KeyTapped(9)),
            play(GameEvent.KeyTapped(8)),
            play(GameEvent.KeyTapped(1)),
            play(GameEvent.KeyTapped(5)),
            play(GameEvent.Tick(1_200L)),
            MatchEvent.BeginTurn(timestampMs = 2_000L),
            // P2: 3 s on the clock, APPLE correct, pass on BREAD.
            play(GameEvent.Tick(3_000L)),
            play(GameEvent.KeyTapped(2)),
            play(GameEvent.KeyTapped(5)),
            play(GameEvent.KeyTapped(7)),
            play(GameEvent.KeyTapped(8)),
            play(GameEvent.KeyTapped(1)),
            play(GameEvent.Pass),
            MatchEvent.BeginTurn(timestampMs = 3_000L),
            // P1: CHAIR correct → wheel finished, hands over.
            play(GameEvent.KeyTapped(3)),
            play(GameEvent.KeyTapped(4)),
            play(GameEvent.KeyTapped(1)),
            play(GameEvent.KeyTapped(6)),
            play(GameEvent.KeyTapped(9)),
            MatchEvent.BeginTurn(timestampMs = 4_000L),
            // P2: CHAIR correct, BREAD (lap 2) "RBEAD" → wrong; 2-1 each, P1 wins on remaining time.
            play(GameEvent.KeyTapped(1)),
            play(GameEvent.KeyTapped(8)),
            play(GameEvent.KeyTapped(6)),
            play(GameEvent.KeyTapped(9)),
            play(GameEvent.KeyTapped(3)),
            play(GameEvent.KeyTapped(7)),
            play(GameEvent.KeyTapped(3)),
            play(GameEvent.KeyTapped(1)),
            play(GameEvent.KeyTapped(4)),
            play(GameEvent.KeyTapped(8)),
            play(GameEvent.Tick(1_200L)),
        )

        private fun play(event: GameEvent): MatchEvent = MatchEvent.Play(event)
    }
}
