package dev.talhabayhan.lexi.core.domain.time

import kotlin.test.Test
import kotlin.test.assertEquals

class AppClockTest {
    private val utcClock = object : AppClock {
        override fun nowMs(): Long = 0L

        override fun epochDay(atMs: Long): Long = atMs / MS_PER_DAY

        override fun msUntilNextDay(atMs: Long): Long = MS_PER_DAY - atMs % MS_PER_DAY
    }

    @Test
    fun `default minuteOfDay derives the local minute from the time until midnight`() {
        assertEquals(0, utcClock.minuteOfDay(0L))
        assertEquals(20 * 60 + 5, utcClock.minuteOfDay((20 * 60 + 5) * 60_000L + 59_999L))
        assertEquals(24 * 60 - 1, utcClock.minuteOfDay(MS_PER_DAY - 1))
    }

    private companion object {
        const val MS_PER_DAY = 86_400_000L
    }
}
