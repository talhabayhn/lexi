package dev.talhabayhan.lexi.core.domain.game

import kotlinx.serialization.Serializable

/**
 * Game modes (`GAME_RULES.md` §5). Timers and other tunables live in [GameConfig]; the flags here are fixed rules.
 */
@Serializable
public enum class GameMode(
    /** Points are counted (and may reach a leaderboard). */
    public val isScored: Boolean,
    /** The first WRONG answer ends the game. */
    public val endsOnFirstWrong: Boolean,
    /**
     * The wheel prefers cards the player hasn't seen recently. False where every player must get the same wheel
     * for the same seed (daily, duel) and for the level test.
     */
    public val usesCardHistory: Boolean,
) {
    CLASSIC(isScored = true, endsOnFirstWrong = false, usesCardHistory = true),
    PRACTICE(isScored = false, endsOnFirstWrong = false, usesCardHistory = true),
    HARDCORE(isScored = true, endsOnFirstWrong = true, usesCardHistory = true),
    DAILY(isScored = true, endsOnFirstWrong = false, usesCardHistory = false),
    REVIEW(isScored = false, endsOnFirstWrong = false, usesCardHistory = true),
    LEVEL_TEST(isScored = false, endsOnFirstWrong = false, usesCardHistory = false),

    /** Hot-seat duel (v1.0); online rooms reuse the same rules (v1.1). */
    DUEL(isScored = true, endsOnFirstWrong = false, usesCardHistory = false),
}
