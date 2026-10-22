package dev.talhabayhan.lexi.telemetry

import dev.talhabayhan.lexi.core.domain.game.JokerType
import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsEvent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IosTelemetryTest {
    private class RecordingTelemetry : IosTelemetry {
        val events = mutableListOf<Pair<String, Map<String, Any>>>()
        val logs = mutableListOf<String>()
        val exceptions = mutableListOf<Triple<String, String, List<String>>>()

        override fun logEvent(name: String, params: Map<String, Any>) {
            events += name to params
        }

        override fun setAnalyticsCollectionEnabled(enabled: Boolean) = Unit

        override fun log(message: String) {
            logs += message
        }

        override fun recordException(type: String, message: String, stackTrace: List<String>) {
            exceptions += Triple(type, message, stackTrace)
        }
    }

    @Test
    fun `events reach Swift with their name and parameters`() {
        val telemetry = RecordingTelemetry()

        IosAnalyticsTracker(telemetry).track(AnalyticsEvent.JokerUsed(JokerType.LISTEN))

        assertEquals(listOf("joker_used" to mapOf<String, Any>("type" to "listen")), telemetry.events)
    }

    @Test
    fun `a reported exception logs the message and sends its type and Kotlin frames`() {
        val telemetry = RecordingTelemetry()

        IosCrashReporter(telemetry).recordException(IllegalStateException("boom"), "game not loaded")

        assertEquals(listOf("game not loaded"), telemetry.logs)
        val (type, message, frames) = telemetry.exceptions.single()
        assertEquals("kotlin.IllegalStateException", type)
        assertEquals("boom", message)
        assertTrue(frames.isNotEmpty())
        assertTrue(frames.none { it.isBlank() || it.startsWith("kotlin.IllegalStateException") }, "$frames")
    }
}
