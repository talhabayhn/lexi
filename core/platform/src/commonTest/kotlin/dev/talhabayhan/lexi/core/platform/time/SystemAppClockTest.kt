package dev.talhabayhan.lexi.core.platform.time

import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Instant

class SystemAppClockTest {
    private val fixed = object : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(NOW_MS)
    }

    @Test
    fun `nowMs reads the clock in epoch milliseconds`() {
        assertEquals(NOW_MS, SystemAppClock(fixed) { TimeZone.UTC }.nowMs())
    }

    @Test
    fun `epochDay is the day in the player's time zone`() {
        // 2026-10-14T22:30Z is still the 14th in UTC but already the 15th in Istanbul (UTC+3).
        val atMs = Instant.parse("2026-10-14T22:30:00Z").toEpochMilliseconds()

        assertEquals(DAY_2026_10_14, SystemAppClock(fixed) { TimeZone.UTC }.epochDay(atMs))
        assertEquals(DAY_2026_10_14 + 1, SystemAppClock(fixed) { TimeZone.of("Europe/Istanbul") }.epochDay(atMs))
    }

    @Test
    fun `epochDay counts days before 1970 as negative`() {
        assertEquals(-1L, SystemAppClock(fixed) { TimeZone.UTC }.epochDay(-1L))
    }

    @Test
    fun `minuteOfDay is the local wall-clock minute`() {
        val atMs = Instant.parse("2026-10-14T22:30:00Z").toEpochMilliseconds()

        assertEquals(22 * 60 + 30, SystemAppClock(fixed) { TimeZone.UTC }.minuteOfDay(atMs))
        assertEquals(1 * 60 + 30, SystemAppClock(fixed) { TimeZone.of("Europe/Istanbul") }.minuteOfDay(atMs))
    }

    @Test
    fun `minuteOfDay follows the DST shift`() {
        // 2026-03-29 01:00Z: Berlin jumps from 02:00 to 03:00 local; 01:30Z is 03:30 local.
        val atMs = Instant.parse("2026-03-29T01:30:00Z").toEpochMilliseconds()

        assertEquals(3 * 60 + 30, SystemAppClock(fixed) { TimeZone.of("Europe/Berlin") }.minuteOfDay(atMs))
    }

    private companion object {
        const val NOW_MS = 1_792_000_000_000L

        /** `LocalDate(2026, 10, 14).toEpochDays()`. */
        const val DAY_2026_10_14 = 20_740L
    }
}
