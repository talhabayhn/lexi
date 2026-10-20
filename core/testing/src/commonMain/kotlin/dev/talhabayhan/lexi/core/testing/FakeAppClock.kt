package dev.talhabayhan.lexi.core.testing

import dev.talhabayhan.lexi.core.domain.time.AppClock

/**
 * Settable [AppClock]. Days are computed with a fixed [utcOffsetMs] (default UTC), floor-divided so instants before
 * 1970 get negative days like the real clock.
 */
public class FakeAppClock(public var currentMs: Long = DEFAULT_NOW_MS, public val utcOffsetMs: Long = 0L) : AppClock {
    override fun nowMs(): Long = currentMs

    override fun epochDay(atMs: Long): Long = (atMs + utcOffsetMs).floorDiv(MS_PER_DAY)

    override fun msUntilNextDay(atMs: Long): Long {
        val msInDay = (atMs + utcOffsetMs).mod(MS_PER_DAY)
        return (MS_PER_DAY - msInDay).coerceAtLeast(0L)
    }

    override fun minuteOfDay(atMs: Long): Int = ((atMs + utcOffsetMs).mod(MS_PER_DAY) / MS_PER_MINUTE).toInt()

    public fun advanceBy(ms: Long) {
        currentMs += ms
    }

    public companion object {
        public const val MS_PER_DAY: Long = 86_400_000L
        public const val MS_PER_MINUTE: Long = 60_000L

        /** 2026-10-15T12:00:00Z (epoch day 20 741). */
        public const val DEFAULT_NOW_MS: Long = 20_741L * MS_PER_DAY + MS_PER_DAY / 2
    }
}
