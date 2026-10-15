package dev.talhabayhan.lexi.core.designsystem.format

private const val SECONDS_PER_MINUTE = 60

/** "3:00", "1:47": minutes and two-digit seconds (digits only, the same in every language). */
public fun formatClock(wholeSeconds: Int): String {
    val minutes = wholeSeconds / SECONDS_PER_MINUTE
    val seconds = wholeSeconds % SECONDS_PER_MINUTE
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}
