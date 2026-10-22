@file:OptIn(ExperimentalNativeApi::class)

package dev.talhabayhan.lexi.telemetry

import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsEvent
import dev.talhabayhan.lexi.core.domain.telemetry.CrashReporter
import dev.talhabayhan.lexi.core.platform.telemetry.AnalyticsSink
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.setUnhandledExceptionHook
import kotlin.native.terminateWithUnhandledException

/**
 * Firebase Analytics + Crashlytics on iOS, implemented in Swift (`iosApp/iosApp/FirebaseTelemetry.swift`, ADR 0023):
 * Kotlin never links the Firebase SDK. Part of the Swift API: keep names and parameter types.
 */
interface IosTelemetry {
    /** `Analytics.logEvent`; [params] values are `String` or `Long` (`AnalyticsEvent`). */
    fun logEvent(name: String, params: Map<String, Any>)

    /** `Analytics.setAnalyticsCollectionEnabled`; Swift keeps it off whenever the build disallows telemetry. */
    fun setAnalyticsCollectionEnabled(enabled: Boolean)

    /** A Crashlytics log line, attached to the next report. */
    fun log(message: String)

    /** A non-fatal Crashlytics report with the Kotlin stack ([stackTrace], one frame per line). */
    fun recordException(type: String, message: String, stackTrace: List<String>)
}

internal class IosAnalyticsTracker(private val telemetry: IosTelemetry) : AnalyticsSink {
    override fun track(event: AnalyticsEvent) {
        telemetry.logEvent(event.name, event.params)
    }

    override fun setCollectionEnabled(enabled: Boolean) {
        telemetry.setAnalyticsCollectionEnabled(enabled)
    }
}

internal class IosCrashReporter(private val telemetry: IosTelemetry) : CrashReporter {
    override fun recordException(throwable: Throwable, message: String) {
        telemetry.log(message)
        telemetry.record(throwable)
    }
}

/**
 * Records an uncaught Kotlin exception with its Kotlin stack before the runtime terminates; the crash itself is then
 * caught by Crashlytics' signal handler like any native crash.
 */
internal fun installUncaughtExceptionReporting(telemetry: IosTelemetry) {
    setUnhandledExceptionHook { throwable ->
        runCatching {
            telemetry.log("uncaught Kotlin exception")
            telemetry.record(throwable)
        }
        terminateWithUnhandledException(throwable)
    }
}

private fun IosTelemetry.record(throwable: Throwable) {
    val type = throwable::class.qualifiedName ?: throwable::class.simpleName ?: "Throwable"
    val frames = throwable.stackTraceToString().lines().drop(1).map(String::trim).filter(String::isNotEmpty)
    recordException(type, throwable.message.orEmpty(), frames)
}
