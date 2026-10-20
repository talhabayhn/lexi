package dev.talhabayhan.lexi.core.platform.time

import dev.talhabayhan.lexi.core.domain.time.AppClock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * [AppClock] on the device clock (ADR 0019). The time zone is read on every call, so a zone change while the app
 * runs (travel, DST settings) moves the day boundary at once.
 */
public class SystemAppClock internal constructor(private val clock: Clock, private val timeZone: () -> TimeZone) :
    AppClock {
    public constructor() : this(Clock.System, { TimeZone.currentSystemDefault() })

    override fun nowMs(): Long = clock.now().toEpochMilliseconds()

    override fun epochDay(atMs: Long): Long =
        Instant.fromEpochMilliseconds(atMs).toLocalDateTime(timeZone()).date.toEpochDays()

    override fun msUntilNextDay(atMs: Long): Long {
        val tz = timeZone()
        val instant = Instant.fromEpochMilliseconds(atMs)
        val ldt = instant.toLocalDateTime(tz)
        val tomorrow = ldt.date.plus(1, DateTimeUnit.DAY)
        val nextMidnight = LocalDateTime(tomorrow, LocalTime(0, 0, 0, 0))
        return (nextMidnight.toInstant(tz).toEpochMilliseconds() - atMs).coerceAtLeast(0L)
    }

    override fun minuteOfDay(atMs: Long): Int {
        val time = Instant.fromEpochMilliseconds(atMs).toLocalDateTime(timeZone()).time
        return time.hour * 60 + time.minute
    }
}
