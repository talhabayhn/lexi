package dev.talhabayhan.lexi.core.platform.telemetry

import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsEvent
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class FirebaseTelemetryTest {
    @Test
    fun `parameters keep their types in the bundle`() {
        val bundle = AnalyticsEvent.GameFinish(
            GameMode.CLASSIC,
            Level.B2,
            correct = 21,
            durationSeconds = 140,
        ).toBundle()

        assertEquals(setOf("mode", "level", "correct", "duration_s"), bundle.keySet())
        assertEquals("classic", bundle.getString("mode"))
        assertEquals("B2", bundle.getString("level"))
        assertEquals(21L, bundle.getLong("correct"))
        assertEquals(140L, bundle.getLong("duration_s"))
    }

    @Test
    fun `an event without parameters gives an empty bundle`() {
        assertTrue(AnalyticsEvent.AppOpen.toBundle().isEmpty)
    }
}
