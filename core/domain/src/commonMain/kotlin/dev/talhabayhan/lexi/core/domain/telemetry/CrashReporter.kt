package dev.talhabayhan.lexi.core.domain.telemetry

/**
 * Crash reports (Firebase Crashlytics on both platforms, ADR 0023). Uncaught crashes are reported by the SDK itself;
 * this is for caught, unexpected failures the app recovers from (`ARCHITECTURE.md` §10). Never throws.
 */
public interface CrashReporter {
    /** Records [throwable] as a non-fatal; [message] is a fixed description of what failed, never user input. */
    public fun recordException(throwable: Throwable, message: String)
}
