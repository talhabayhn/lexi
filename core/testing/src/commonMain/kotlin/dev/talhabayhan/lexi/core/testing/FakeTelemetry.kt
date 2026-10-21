package dev.talhabayhan.lexi.core.testing

import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsEvent
import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsTracker
import dev.talhabayhan.lexi.core.domain.telemetry.CrashReporter

/** Records tracked events; also a silent [AnalyticsTracker] for previews and tests. */
public class FakeAnalyticsTracker : AnalyticsTracker {
    private val _events = mutableListOf<AnalyticsEvent>()

    /** Every event passed to [track], in order. */
    public val events: List<AnalyticsEvent> get() = _events.toList()

    override fun track(event: AnalyticsEvent) {
        _events += event
    }
}

/** Records reported failures; also a silent [CrashReporter] for previews and tests. */
public class FakeCrashReporter : CrashReporter {
    private val _recorded = mutableListOf<Pair<Throwable, String>>()

    /** Every (throwable, message) passed to [recordException], in order. */
    public val recorded: List<Pair<Throwable, String>> get() = _recorded.toList()

    override fun recordException(throwable: Throwable, message: String) {
        _recorded += throwable to message
    }
}
