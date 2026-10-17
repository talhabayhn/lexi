package dev.talhabayhan.lexi.core.domain.daily

import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.random.Seeds
import dev.talhabayhan.lexi.core.domain.time.CivilDate
import dev.talhabayhan.lexi.core.domain.time.civilDateOfEpochDay

/**
 * Deterministic daily wheel seed calculation (`GAME_RULES.md` §5, §13).
 * Pure function of local date + level + contentVersion using SplitMix64 (Seeds.mix64) and FNV-1a 64 (Seeds.stableHash).
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

    /** Deterministic daily seed for a [CivilDate] + [contentVersion]. */
    public fun seed(date: CivilDate, contentVersion: Long): Long {
        val key = "$SEED_PREFIX:${date.year}-${date.month.toString().padStart(
            2,
            '0',
        )}-${date.day.toString().padStart(2, '0')}:$contentVersion"
        return Seeds.mix64(Seeds.stableHash(key))
    }

    /** Backward-compatible overload ignoring level. */
    public fun seed(epochDay: Long, level: Level, contentVersion: Long): Long = seed(epochDay, contentVersion)

    /** Backward-compatible overload ignoring level. */
    public fun seed(date: CivilDate, level: Level, contentVersion: Long): Long = seed(date, contentVersion)
}
