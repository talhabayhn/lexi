package dev.talhabayhan.lexi.core.domain.reminder

import dev.talhabayhan.lexi.core.domain.joker.JokerGiftConfig
import dev.talhabayhan.lexi.core.domain.stats.Stats

/** What [ReminderPlanner] needs; all days are epoch days in the player's time zone. */
public data class ReminderInput(
    /** `Settings.dailyReminder`. */
    val enabled: Boolean,
    /** `Settings.reminderMinuteOfDay`; snapped to [ReminderTimes.optionsMinuteOfDay]. */
    val minuteOfDay: Int,
    /** `Settings.reviewReminders`: review variants are allowed. */
    val reviewReminders: Boolean,
    val today: Long,
    /** Local minute of "now"; a reminder at or before it is not planned for today. */
    val nowMinuteOfDay: Int,
    /** Days with ≥ 1 completed wheel (`StreakRepository.observeDays`); days after [today] are ignored. */
    val playDays: Collection<Long>,
    /** Due day of every notebook card that can fall due (`CardProgress.dueEpochDay` where `isDue` may become true). */
    val reviewDueDays: Collection<Long> = emptyList(),
    /** `JokerGiftConfig.streakBonusEvery`: `STREAK_3` the day before a streak reaches a multiple of it; 0 = off. */
    val streakBonusEvery: Int = JokerGiftConfig().streakBonusEvery,
)

/** One local notification: on [epochDay] at [minuteOfDay]; [count] fills the copy's `{n}` (null when it has none). */
public data class PlannedReminder(
    val epochDay: Long,
    val minuteOfDay: Int,
    val variant: ReminderVariant,
    val count: Int?,
)

/**
 * Daily reminder plan (owner decisions, M7; copy rules `docs/copy/COPY.md` §1). Pure and deterministic: no clock, no
 * randomness. Plans today .. today + 14 assuming the player doesn't play again; the app rebuilds the plan whenever an
 * input changes and on app start/resume, so playing replaces it.
 *
 * Rules, for a day D whose last play before D was L (gap = D − L):
 * - Disabled ⇒ nothing. At most one reminder per day, always at the chosen time.
 * - Played today ⇒ nothing today; the time already passed today ⇒ start tomorrow.
 * - gap 1–2: candidates in priority order — streak (gap 1 and streak ≥ [STREAK_MIN]: `STREAK_3` the day before a
 *   streak-bonus day, then `STREAK_1`/`STREAK_2` alternating by day parity), review (setting on; ≥ [REVIEW_MIN_DUE]
 *   due ⇒ `REVIEW_1`, ≥ [REVIEW_WHEEL_MIN_DUE] ⇒ `REVIEW_1`/`REVIEW_2` alternating by day parity), daily
 *   (`DAILY_{D mod 3 + 1}`). The first candidate that differs from the previous day's key wins, so a key never
 *   repeats on two consecutive days (the daily rotation always differs from yesterday's daily).
 * - gap ≥ 3: only `COMEBACK_3D` / `_7D` / `_14D` on gaps 3, 7 and 14; nothing on other days or after 14.
 * - Never completed a wheel: treated as a play yesterday (two daily reminders, then the comeback series), so an
 *   idle new player isn't reminded every day.
 *
 * To keep "no repeat" across rebuilds, the walk starts [LOOKBACK_DAYS] before today and assumes the key the same
 * rules give for those days (and for today before the chosen time or before playing) was shown.
 */
public object ReminderPlanner {
    /** today .. today + 14. */
    public const val PLAN_DAYS: Int = 15
    public const val LOOKBACK_DAYS: Int = 3
    public const val STREAK_MIN: Int = 2
    public const val REVIEW_MIN_DUE: Int = 5
    public const val REVIEW_WHEEL_MIN_DUE: Int = 10
    public const val COMEBACK_3_GAP: Int = 3
    public const val COMEBACK_7_GAP: Int = 7
    public const val COMEBACK_14_GAP: Int = 14

    private const val LAST_REGULAR_GAP = 2

    public fun plan(input: ReminderInput): List<PlannedReminder> {
        if (!input.enabled) return emptyList()
        val today = input.today
        val minute = ReminderTimes.normalize(input.minuteOfDay)
        val realDays = input.playDays.filter { it <= today }.distinct().sorted()
        val days = realDays.ifEmpty { listOf(today - 1) }
        val start = if (today in realDays || input.nowMinuteOfDay >= minute) today + 1 else today
        val dueDays = input.reviewDueDays.sorted()

        val plan = mutableListOf<PlannedReminder>()
        var previous: ReminderVariant? = null
        val walkFrom = if (realDays.isEmpty()) start else today - LOOKBACK_DAYS
        for (day in walkFrom until today + PLAN_DAYS) {
            val choice = choose(day, days, dueDays, input, previous)
            if (day >= start && choice != null) plan += PlannedReminder(day, minute, choice.variant, choice.count)
            previous = choice?.variant
        }
        return plan
    }

    private class Choice(val variant: ReminderVariant, val count: Int? = null)

    private fun choose(
        day: Long,
        days: List<Long>,
        dueDays: List<Long>,
        input: ReminderInput,
        previous: ReminderVariant?,
    ): Choice? {
        val last = days.lastOrNull { it < day } ?: return null
        val gap = day - last
        if (gap > LAST_REGULAR_GAP) {
            return when (gap) {
                COMEBACK_3_GAP.toLong() -> Choice(ReminderVariant.COMEBACK_3D)
                COMEBACK_7_GAP.toLong() -> Choice(ReminderVariant.COMEBACK_7D)
                COMEBACK_14_GAP.toLong() -> Choice(ReminderVariant.COMEBACK_14D)
                else -> null
            }
        }
        val even = day.mod(2) == 0
        val candidates = buildList {
            val streak = if (gap == 1L) Stats.currentStreak(days.filter { it < day }, last) else 0
            if (streak >= STREAK_MIN) {
                val every = input.streakBonusEvery
                if (every > 0 && (streak + 1) % every == 0) add(Choice(ReminderVariant.STREAK_3, streak + 1))
                val one = Choice(ReminderVariant.STREAK_1, streak)
                val two = Choice(ReminderVariant.STREAK_2)
                if (even) addAll(listOf(one, two)) else addAll(listOf(two, one))
            }
            val due = dueDays.count { it <= day }
            if (input.reviewReminders && due >= REVIEW_MIN_DUE) {
                val one = Choice(ReminderVariant.REVIEW_1, due)
                if (due >= REVIEW_WHEEL_MIN_DUE) {
                    val two = Choice(ReminderVariant.REVIEW_2, due)
                    if (even) addAll(listOf(one, two)) else addAll(listOf(two, one))
                } else {
                    add(one)
                }
            }
            add(Choice(DAILY[day.mod(DAILY.size)]))
        }
        return candidates.firstOrNull { it.variant != previous } ?: candidates.last()
    }

    private val DAILY = listOf(ReminderVariant.DAILY_1, ReminderVariant.DAILY_2, ReminderVariant.DAILY_3)
}
