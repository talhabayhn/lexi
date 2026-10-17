package dev.talhabayhan.lexi.core.domain.daily

import dev.talhabayhan.lexi.core.domain.random.Seeds

/**
 * Deterministic daily wheel seed calculation (`GAME_RULES.md` §5, §13).
 * Pure function of local date (epoch day) + contentVersion using SplitMix64 (Seeds.mix64) and FNV-1a 64 (Seeds.stableHash).
 * Never uses platform RNG or hashCode.
 */
public object DailySeed {
    public const val SEED_PREFIX: String = "daily"

    /**
     * Deterministic daily seed: function of [epochDay] (days since 1970-01-01 in player's local timezone)
     * + [contentVersion].
     */
    public fun seed(epochDay: Long, contentVersion: Long): Long {
        val key = "$SEED_PREFIX:$epochDay:$contentVersion"
        return Seeds.mix64(Seeds.stableHash(key))
    }
}
