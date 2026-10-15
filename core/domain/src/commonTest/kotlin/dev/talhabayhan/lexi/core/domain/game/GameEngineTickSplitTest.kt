package dev.talhabayhan.lexi.core.domain.game

import dev.talhabayhan.lexi.core.domain.random.SeededRandom
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tick additivity (ADR 0019): `Tick(a)` then `Tick(b)` yields the same state as `Tick(a + b)`, so the ViewModel's
 * tick interval never matters and a saved event log may coalesce consecutive ticks. Effects may differ only as
 * `GAME_RULES.md` §12 allows: a big tick emits just the lowest `TimeWarning` (none at 0); all other effects match.
 */
class GameEngineTickSplitTest {
    private val engine = GameEngine()

    /** Named starting points that cover the clock, the time warning, the reveal and every way a tick can end a game. */
    private fun scenarios(): Map<String, GameState> = mapOf(
        "classic, full clock" to play("APPLE", "BREAD", "CHAIR").state,
        "classic, 12 s left" to play("APPLE", "BREAD").send(GameEvent.Tick(168_000L)).state,
        "classic, 10.5 s left (in the warning)" to play("APPLE", "BREAD").send(GameEvent.Tick(169_500L)).state,
        "classic, wrong answer revealing" to play("APPLE", "BREAD", "CHAIR").answerWrong().state,
        "classic, revealing with 3 s left" to
            play("APPLE", "BREAD", "CHAIR").send(GameEvent.Tick(177_000L)).answerWrong().state,
        "classic, revealing the last slot (reveal ends the wheel)" to play("APPLE").answerWrong().state,
        "classic, passed slot current again" to play("APPLE", "BREAD").send(GameEvent.Pass).answer().state,
        "classic, paused" to play("APPLE", "BREAD").send(GameEvent.Pause).state,
        "hardcore, wrong answer revealing (reveal ends the game)" to
            play("APPLE", "BREAD", mode = GameMode.HARDCORE).answerWrong().state,
        "practice (untimed), revealing" to play("APPLE", "BREAD", mode = GameMode.PRACTICE).answerWrong().state,
        "duel clock, 2 s left" to play("APPLE", "BREAD", mode = GameMode.DUEL).send(GameEvent.Tick(148_000L)).state,
        "not started" to play("APPLE", start = false).state,
        "finished" to play("APPLE").send(GameEvent.Forfeit).state,
    )

    /** Totals around the reveal end, the 10 s warning, whole-second borders and time up (classic 180 s). */
    private val edgeTotals = listOf(
        1L, 999L, 1_000L, 1_001L, 1_199L, 1_200L, 1_201L, 2_200L, 3_000L, 4_200L, 10_500L, 11_000L, 12_000L,
        13_201L, 170_000L, 170_001L, 171_000L, 179_999L, 180_000L, 181_200L, 400_000L,
    )

    @Test
    fun `tick a then tick b equals tick a plus b for every split of edge totals`() {
        for ((name, state) in scenarios()) {
            for (total in edgeTotals) {
                val splits = (listOf(1L, 999L, 1_000L, 1_200L, total / 2, total - 1) + edgeTotals)
                    .filter { it in 1 until total }
                    .distinct()
                for (a in splits) assertSplitInvariant(name, state, listOf(a, total - a))
            }
        }
    }

    @Test
    fun `any partition of a tick into many ticks yields the same state`() {
        val random = SeededRandom(19L)
        for ((name, state) in scenarios()) {
            repeat(PARTITIONS_PER_SCENARIO) {
                val total = random.nextInt(1, MAX_TOTAL_MS).toLong()
                val parts = partition(total, random)
                assertSplitInvariant(name, state, parts)
            }
        }
    }

    @Test
    fun `100 ms ticks reach the same state as one tick through the reveal the warning and time up`() {
        val state = play("APPLE", "BREAD", "CHAIR").send(GameEvent.Tick(165_000L)).answerWrong().state
        // 1.2 s reveal + 15 s left on the clock + 1 s extra: the game ends on TIME_UP either way.
        val ticks = List(174) { 100L }

        assertSplitInvariant("100 ms ticker", state, ticks)
        val final = ticks.fold(state) { s, ms -> engine.reduce(s, GameEvent.Tick(ms)).state }
        assertEquals(GameOverReason.TIME_UP, final.summary?.reason)
    }

    private fun partition(total: Long, random: SeededRandom): List<Long> {
        val parts = mutableListOf<Long>()
        var left = total
        while (left > 0) {
            val part = minOf(left, random.nextInt(1, MAX_PART_MS).toLong())
            parts += part
            left -= part
        }
        return parts
    }

    private fun assertSplitInvariant(name: String, state: GameState, parts: List<Long>) {
        val joined = engine.reduce(state, GameEvent.Tick(parts.sum()))
        val splitEffects = mutableListOf<EngineEffect>()
        val split = parts.fold(state) { s, ms ->
            engine.reduce(s, GameEvent.Tick(ms)).also { splitEffects += it.effects }.state
        }
        val message = "$name, ticks $parts"

        assertEquals(joined.state, split, message)
        assertEquals(joined.effects.withoutWarnings(), splitEffects.withoutWarnings(), message)
        val joinedWarning = joined.effects.filterIsInstance<EngineEffect.TimeWarning>()
        val splitWarnings = splitEffects.filterIsInstance<EngineEffect.TimeWarning>()
        assertTrue(joinedWarning.size <= 1, message)
        // The big tick's single warning is the lowest second the split ticks reached too.
        joinedWarning.singleOrNull()?.let { assertEquals(it, splitWarnings.lastOrNull(), message) }
    }

    private fun List<EngineEffect>.withoutWarnings(): List<EngineEffect> = filterNot { it is EngineEffect.TimeWarning }

    private companion object {
        const val PARTITIONS_PER_SCENARIO = 60
        const val MAX_TOTAL_MS = 200_000
        const val MAX_PART_MS = 20_000
    }
}
