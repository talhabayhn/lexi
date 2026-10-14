package dev.talhabayhan.lexi.core.domain.game

import dev.talhabayhan.lexi.core.domain.model.Level
import kotlinx.serialization.Serializable

/**
 * Every ⚙ number from `GAME_RULES.md`, with its documented default. Remote Config may override values later; tests
 * use the defaults. Multipliers are integer percentages so scoring stays exact on every platform.
 */
@Serializable
public data class GameConfig(
    /** §3: how long the correct answer is shown after a wrong one. */
    val revealMs: Long = 1_200L,
    /** §5: Classic total time; the player's setting (one of [classicTimeLimitOptionsSeconds]) overrides it. */
    val classicTimeLimitSeconds: Int = 180,
    val classicTimeLimitOptionsSeconds: List<Int> = listOf(120, 180, 240, 300),
    val hardcoreTimeLimitSeconds: Int = 180,
    val dailyTimeLimitSeconds: Int = 180,
    val levelTestTimeLimitSeconds: Int = 120,
    /** §5/§8: each duel player's own clock. */
    val duelClockSeconds: Int = 150,
    /** §12: `TimeWarning` is emitted every second while seconds left ≤ this. */
    val timeWarningSeconds: Int = 10,
    /** §6: points for a correct answer before the level multiplier. */
    val baseCorrectPoints: Int = 10,
    /** §6: level multiplier in percent (A1 1.0 … NAT 1.7). */
    val levelMultipliersPercent: Map<Level, Int> = mapOf(
        Level.A1 to 100,
        Level.A2 to 110,
        Level.B1 to 120,
        Level.B2 to 130,
        Level.C1 to 150,
        Level.NAT to 170,
    ),
    /** §6: streak bonus `step × (streak − 1)`, capped. */
    val streakBonusStep: Int = 2,
    val streakBonusCap: Int = 10,
    /** §6: streak lengths that trigger a combo celebration. */
    val comboMilestones: List<Int> = listOf(3, 5, 10),
    /** §6: added per remaining second when the wheel is completed. */
    val timeBonusPointsPerSecond: Int = 1,
    /** §6: Hardcore multiplier on the final total, in percent. */
    val hardcoreMultiplierPercent: Int = 150,
    /** §7 joker costs. */
    val revealLetterCostMs: Long = 10_000L,
    val revealLetterUntimedCostPoints: Int = 5,
    val removeDistractorsCostMs: Long = 15_000L,
    val exampleCostPoints: Int = 5,
    val listenCostPoints: Int = 10,
    val listenDisabledLevels: Set<Level> = setOf(Level.C1, Level.NAT),
    /** §7: modes where no joker may be used (fair comparison / adaptive test). */
    val jokerFreeModes: Set<GameMode> = setOf(GameMode.DAILY, GameMode.DUEL, GameMode.LEVEL_TEST),
    /** §7 inventory sources. */
    val jokerStartPack: JokerInventory = JokerInventory(
        mapOf(
            JokerType.REVEAL_LETTER to 2,
            JokerType.REMOVE_DISTRACTORS to 1,
            JokerType.EXAMPLE to 3,
            JokerType.LISTEN to 1,
        ),
    ),
    val dailyLoginJokerBonus: Int = 1,
    val rewardedAdJokerReward: Int = 1,
    val proDailyJokerRefill: Int = 3,
) {
    /** Total time for [mode] in ms (per player for [GameMode.DUEL]); null when the mode has no timer. */
    public fun timeLimitMs(mode: GameMode): Long? = when (mode) {
        GameMode.CLASSIC -> classicTimeLimitSeconds
        GameMode.HARDCORE -> hardcoreTimeLimitSeconds
        GameMode.DAILY -> dailyTimeLimitSeconds
        GameMode.LEVEL_TEST -> levelTestTimeLimitSeconds
        GameMode.DUEL -> duelClockSeconds
        GameMode.PRACTICE, GameMode.REVIEW -> null
    }?.let { it * MS_PER_SECOND }

    public fun levelMultiplierPercent(level: Level): Int = levelMultipliersPercent[level] ?: PERCENT

    /** Cost of [type] in [mode]; time costs become point costs (or free) in untimed modes. */
    public fun jokerCost(type: JokerType, mode: GameMode): JokerCost {
        val timed = timeLimitMs(mode) != null
        return when (type) {
            JokerType.REVEAL_LETTER ->
                if (timed) JokerCost.Time(revealLetterCostMs) else JokerCost.Points(revealLetterUntimedCostPoints)

            JokerType.REMOVE_DISTRACTORS -> if (timed) JokerCost.Time(removeDistractorsCostMs) else JokerCost.Free

            JokerType.EXAMPLE -> JokerCost.Points(exampleCostPoints)

            JokerType.LISTEN -> JokerCost.Points(listenCostPoints)
        }
    }

    /** Whether [type] may be used in [mode] at [level] (inventory and once-per-slot are checked by the engine). */
    public fun isJokerAllowed(type: JokerType, mode: GameMode, level: Level): Boolean =
        mode !in jokerFreeModes && !(type == JokerType.LISTEN && level in listenDisabledLevels)
}

private const val MS_PER_SECOND = 1_000L
private const val PERCENT = 100
