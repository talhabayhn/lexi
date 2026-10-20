package dev.talhabayhan.lexi.core.designsystem.format

private const val SECONDS_PER_MINUTE = 60

/** "3:00", "1:47": minutes and two-digit seconds (digits only, the same in every language). */
public fun formatClock(wholeSeconds: Int): String {
    val minutes = wholeSeconds / SECONDS_PER_MINUTE
    val seconds = wholeSeconds % SECONDS_PER_MINUTE
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}

private const val MINUTES_PER_HOUR = 60

/** "08:00", "20:30": a time of day from its minutes after local midnight (24-hour, digits only). */
public fun formatTimeOfDay(minuteOfDay: Int): String {
    val hours = minuteOfDay / MINUTES_PER_HOUR
    val minutes = minuteOfDay % MINUTES_PER_HOUR
    return "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}"
}
