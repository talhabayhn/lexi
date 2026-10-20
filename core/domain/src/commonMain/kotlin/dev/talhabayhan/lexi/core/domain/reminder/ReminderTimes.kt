package dev.talhabayhan.lexi.core.domain.reminder

/**
 * Daily reminder times the player can pick (owner decision, M7): 08:00 to 21:30 local time in 30-minute steps, so a
 * reminder never falls inside the quiet hours 22:00–08:00 (`docs/copy/COPY.md` §1). Settings UI lists
 * [optionsMinuteOfDay]; the planner snaps any other stored value with [normalize].
 */
public object ReminderTimes {
    public const val FIRST_MINUTE: Int = 8 * 60
    public const val LAST_MINUTE: Int = 21 * 60 + 30
    public const val STEP_MINUTES: Int = 30

    /** 20:00, the design's default. */
    public const val DEFAULT_MINUTE: Int = 20 * 60

    /** Minutes after local midnight, ascending: 480, 510, …, 1290. */
    public val optionsMinuteOfDay: List<Int> = (FIRST_MINUTE..LAST_MINUTE step STEP_MINUTES).toList()

    /** The option nearest to [minuteOfDay] (ties go to the later option; values outside the range are clamped). */
    public fun normalize(minuteOfDay: Int): Int {
        val clamped = minuteOfDay.coerceIn(FIRST_MINUTE, LAST_MINUTE)
        return FIRST_MINUTE + (clamped - FIRST_MINUTE + STEP_MINUTES / 2) / STEP_MINUTES * STEP_MINUTES
    }
}
