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

    /** Milliseconds until the next local day starts (local midnight) from instant [atMs]. */
    public fun msUntilNextDay(atMs: Long = nowMs()): Long

    /**
     * Local wall-clock minute after midnight (0..1439) of the instant [atMs] (reminder planning). The default derives
     * it from [msUntilNextDay], which is off by the DST shift on a 23/25-hour day; real clocks override it.
     */
    public fun minuteOfDay(atMs: Long = nowMs()): Int {
        val msIntoDay = (MS_PER_DAY - msUntilNextDay(atMs)).coerceIn(0L, MS_PER_DAY - 1)
        return (msIntoDay / MS_PER_MINUTE).toInt()
    }
}

private const val MS_PER_DAY: Long = 86_400_000L
private const val MS_PER_MINUTE: Long = 60_000L
