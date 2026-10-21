package dev.talhabayhan.lexi.core.platform.telemetry

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsEvent
import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsTracker
import dev.talhabayhan.lexi.core.domain.telemetry.CrashReporter

/**
 * Firebase Analytics (ADR 0023). Collection (release on, debug off) and the privacy defaults come from the app
 * manifest; the SDK is looked up on the first event, so building the Koin graph never touches Firebase.
 */
public class FirebaseAnalyticsTracker(private val context: Context) : AnalyticsTracker {
    private val analytics by lazy { FirebaseAnalytics.getInstance(context) }

    override fun track(event: AnalyticsEvent) {
        runCatching { analytics.logEvent(event.name, event.toBundle()) }
    }
}

/** Firebase Crashlytics (ADR 0023): [message] becomes a log line of the non-fatal report. */
public class FirebaseCrashReporter : CrashReporter {
    override fun recordException(throwable: Throwable, message: String) {
        runCatching {
            val crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.log(message)
            crashlytics.recordException(throwable)
        }
    }
}

/** Strings stay strings, counts become longs (`AnalyticsEvent` allows nothing else). */
internal fun AnalyticsEvent.toBundle(): Bundle = Bundle().apply {
    params.forEach { (key, value) ->
        when (value) {
            is Long -> putLong(key, value)
            is Int -> putLong(key, value.toLong())
            else -> putString(key, value.toString())
        }
    }
}
