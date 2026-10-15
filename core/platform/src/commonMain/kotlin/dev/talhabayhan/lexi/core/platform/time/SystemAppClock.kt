package dev.talhabayhan.lexi.core.platform.time

import dev.talhabayhan.lexi.core.domain.time.AppClock
import kotlinx.datetime.TimeZone
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
}
