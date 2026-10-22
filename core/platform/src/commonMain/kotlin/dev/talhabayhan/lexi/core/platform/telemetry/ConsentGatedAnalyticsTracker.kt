package dev.talhabayhan.lexi.core.platform.telemetry

import dev.talhabayhan.lexi.core.domain.repository.SettingsRepository
import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsEvent
import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** The platform Firebase Analytics client: [setCollectionEnabled] switches the SDK's collection (and its queue). */
public interface AnalyticsSink : AnalyticsTracker {
    public fun setCollectionEnabled(enabled: Boolean)
}

/**
 * Analytics only with the player's opt-in (`Settings.analyticsConsent`, KVKK): without it nothing reaches [sink] and
 * the SDK's collection stays off. The collection switch follows every change of the stored consent (one source of
 * truth, [SettingsRepository.loaded], also read by [track]); it stays off until the store has delivered a value.
 */
public class ConsentGatedAnalyticsTracker(
    private val sink: AnalyticsSink,
    private val settings: SettingsRepository,
    scope: CoroutineScope,
) : AnalyticsTracker {
    private var applied: Boolean? = null

    init {
        apply(consent())
        scope.launch {
            settings.loaded
                .filterNotNull()
                .map { it.analyticsConsent }
                .distinctUntilChanged()
                .catch { apply(false) }
                .collect { apply(it) }
        }
    }

    override fun track(event: AnalyticsEvent) {
        if (consent()) sink.track(event)
    }

    private fun consent(): Boolean = settings.loaded.value?.analyticsConsent == true

    private fun apply(enabled: Boolean) {
        if (applied == enabled) return
        applied = enabled
        sink.setCollectionEnabled(enabled)
    }
}
