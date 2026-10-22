package dev.talhabayhan.lexi.core.platform.telemetry

import dev.talhabayhan.lexi.core.domain.repository.SettingsRepository
import dev.talhabayhan.lexi.core.domain.settings.Settings
import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConsentGatedAnalyticsTrackerTest {
    private class RecordingSink : AnalyticsSink {
        val events = mutableListOf<AnalyticsEvent>()
        val collection = mutableListOf<Boolean>()

        override fun track(event: AnalyticsEvent) {
            events += event
        }

        override fun setCollectionEnabled(enabled: Boolean) {
            collection += enabled
        }
    }

    /** [loaded] can lag [settings] and start null, like the real store. */
    private class Store(consent: Boolean?) : SettingsRepository {
        private val state = MutableStateFlow(Settings(analyticsConsent = consent == true))
        override val settings: StateFlow<Settings> = state.asStateFlow()
        private val loadedState = MutableStateFlow<Settings?>(consent?.let { Settings(analyticsConsent = it) })
        override val loaded: StateFlow<Settings?> = loadedState.asStateFlow()

        fun set(consent: Boolean) {
            state.update { it.copy(analyticsConsent = consent) }
            loadedState.value = state.value
        }

        override suspend fun update(transform: (Settings) -> Settings) {
            state.update(transform)
            loadedState.value = state.value
        }
    }

    private val sink = RecordingSink()

    private fun tracker(store: Store) =
        ConsentGatedAnalyticsTracker(sink, store, CoroutineScope(Job() + Dispatchers.Unconfined))

    @Test
    fun `nothing reaches the SDK before consent and collection is switched off`() {
        val tracker = tracker(Store(consent = false))

        tracker.track(AnalyticsEvent.AppOpen)
        tracker.track(AnalyticsEvent.Share)

        assertTrue(sink.events.isEmpty())
        assertEquals(listOf(false), sink.collection)
    }

    @Test
    fun `opting in lets events through and opting out stops them`() {
        val store = Store(consent = false)
        val tracker = tracker(store)

        store.set(true)
        tracker.track(AnalyticsEvent.AppOpen)
        store.set(false)
        tracker.track(AnalyticsEvent.Share)

        assertEquals(listOf<AnalyticsEvent>(AnalyticsEvent.AppOpen), sink.events)
        assertEquals(listOf(false, true, false), sink.collection)
    }

    @Test
    fun `nothing is sent and collection stays off until the store has loaded`() {
        val store = Store(consent = null)
        val tracker = tracker(store)

        tracker.track(AnalyticsEvent.AppOpen)

        assertTrue(sink.events.isEmpty())
        assertEquals(listOf(false), sink.collection)
    }
}
