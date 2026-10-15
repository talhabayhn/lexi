package dev.talhabayhan.lexi.core.domain.time

/**
 * Wall-clock time for the app layer (ADR 0019): `GameEvent.Start.timestampMs`, session records, SRS days. The engine
 * never reads it; pure domain functions take "now" and "today" as parameters, use cases get an [AppClock].
 * Implementation: `SystemAppClock` (`core:platform`); tests: `FakeAppClock` (`core:testing`).
 */
public interface AppClock {
    /** Milliseconds since the Unix epoch (UTC). */
    public fun nowMs(): Long

    /** Epoch day (days since 1970-01-01) of the instant [atMs] in the player's current time zone. */
    public fun epochDay(atMs: Long): Long
}
