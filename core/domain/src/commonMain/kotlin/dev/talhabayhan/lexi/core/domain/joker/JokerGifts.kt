package dev.talhabayhan.lexi.core.domain.joker

import dev.talhabayhan.lexi.core.domain.game.JokerInventory
import dev.talhabayhan.lexi.core.domain.game.JokerType
import dev.talhabayhan.lexi.core.domain.random.SeededRandom
import dev.talhabayhan.lexi.core.domain.random.Seeds
import dev.talhabayhan.lexi.core.domain.stats.Stats
import kotlinx.serialization.Serializable

/** ⚙ Free joker gifts (`GAME_RULES.md` §7, owner decision); part of `GameConfig.jokerGifts`. */
@Serializable
public data class JokerGiftConfig(
    /** Jokers given on the first app open of each local calendar day. */
    val dailyGift: Int = 1,
    /** Gifts only pick joker types whose count is below this (per type). */
    val cap: Int = 5,
    /** A streak bonus is given each time the current streak reaches a multiple of this many days; 0 = off. */
    val streakBonusEvery: Int = 7,
    /** Jokers per streak bonus. */
    val streakBonus: Int = 1,
) {
    init {
        require(dailyGift >= 0 && cap >= 0 && streakBonusEvery >= 0 && streakBonus >= 0) {
            "joker gift values must not be negative: $this"
        }
    }
}

/** Why a joker was given (for a notice in the UI). */
public enum class JokerGrantReason { DAILY_GIFT, STREAK_BONUS }

public data class JokerGrant(val type: JokerType, val reason: JokerGrantReason)

/**
 * Joker inventory plus gift bookkeeping, read and written together (`JokerRepository.updateWallet`). Days are epoch
 * days in the player's time zone and only ever move forward.
 */
public data class JokerWallet(
    val inventory: JokerInventory = JokerInventory(),
    /** Day of the last daily gift (also set when every type was at the cap); null = never. */
    val lastDailyGiftDay: Long? = null,
    /** Day on which the last rewarded streak milestone was reached; null = never. */
    val lastStreakBonusDay: Long? = null,
) {
    /** Throws [IllegalArgumentException] when a gift day of this wallet is cleared or earlier than in [previous]. */
    public fun requireDaysNotBefore(previous: JokerWallet) {
        require(notBefore(lastDailyGiftDay, previous.lastDailyGiftDay)) {
            "lastDailyGiftDay must not move back: ${previous.lastDailyGiftDay} -> $lastDailyGiftDay"
        }
        require(notBefore(lastStreakBonusDay, previous.lastStreakBonusDay)) {
            "lastStreakBonusDay must not move back: ${previous.lastStreakBonusDay} -> $lastStreakBonusDay"
        }
    }

    private fun notBefore(day: Long?, previous: Long?): Boolean = previous == null || (day != null && day >= previous)
}

public data class JokerGiftOutcome(val wallet: JokerWallet, val grants: List<JokerGrant>)

/**
 * Daily joker gift and streak bonus (`GAME_RULES.md` §7). Pure: "today" and the streak days are parameters, the
 * picked types come from [SeededRandom] seeded by the day (§13 purposes `joker-gift:daily`, `joker-gift:streak`).
 */
public object JokerGifts {
    public const val DAILY_PURPOSE: String = "joker-gift:daily"
    public const val STREAK_PURPOSE: String = "joker-gift:streak"

    /**
     * Applies today's gifts to [wallet]: the daily gift when [today] is after the last gift day (never when the clock
     * went back), then the streak bonus when the current run of [streakDays] reached a milestone after the last
     * rewarded one. Each joker goes to a uniformly picked type below the cap; with every type at the cap the
     * remaining jokers are dropped, and the day or milestone is used up all the same.
     */
    public fun grant(
        wallet: JokerWallet,
        today: Long,
        streakDays: Collection<Long>,
        config: JokerGiftConfig,
    ): JokerGiftOutcome {
        var inventory = wallet.inventory
        var result = wallet
        val grants = mutableListOf<JokerGrant>()

        fun give(amount: Int, seedDay: Long, purpose: String, reason: JokerGrantReason) {
            val random = SeededRandom(Seeds.derive(seedDay, purpose))
            repeat(amount) {
                val eligible = JokerType.entries.filter { inventory.count(it) < config.cap }
                if (eligible.isEmpty()) return
                val type = random.pick(eligible)
                inventory = inventory.plus(type, 1)
                grants += JokerGrant(type, reason)
            }
        }

        val lastGift = wallet.lastDailyGiftDay
        if (lastGift == null || today > lastGift) {
            give(config.dailyGift, today, DAILY_PURPOSE, JokerGrantReason.DAILY_GIFT)
            result = result.copy(lastDailyGiftDay = today)
        }

        val milestone = streakMilestoneDay(streakDays, today, config.streakBonusEvery)
        val lastBonus = wallet.lastStreakBonusDay
        if (milestone != null && (lastBonus == null || milestone > lastBonus)) {
            give(config.streakBonus, milestone, STREAK_PURPOSE, JokerGrantReason.STREAK_BONUS)
            result = result.copy(lastStreakBonusDay = milestone)
        }

        return JokerGiftOutcome(result.copy(inventory = inventory), grants)
    }

    /**
     * The day on which the current streak ([Stats.currentStreak]: the run ending today or yesterday) last reached a
     * multiple of [every] days, or null when it is shorter than [every] (or [every] is 0).
     */
    public fun streakMilestoneDay(days: Collection<Long>, today: Long, every: Int): Long? {
        if (every <= 0) return null
        val length = Stats.currentStreak(days, today)
        if (length < every) return null
        val end = if (today in days) today else today - 1
        val start = end - length + 1
        return start + (length / every).toLong() * every - 1
    }
}
