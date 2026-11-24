package dev.talhabayhan.lexi.core.domain.telemetry

import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsEvent.FrameTimeBucket
import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsEvent.MotionTierCode
import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsEvent.PerfTransition
import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsEvent.TransitionFrames
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** `transition_frames` (`docs/ANALYTICS.md`): the worst frame of one named transition, bucketed. */
class TransitionFramesEventTest {
    @Test
    fun `the worst frame time falls into the documented buckets`() {
        fun bucket(millis: Double) = FrameTimeBucket.of((millis * NANOS_PER_MILLI).toLong()).code
        // One 60 Hz vsync (16.67 ms) is a smooth frame; so are 120 Hz frames.
        assertEquals("le_16", bucket(0.0))
        assertEquals("le_16", bucket(8.3))
        assertEquals("le_16", bucket(16.7))
        assertEquals("le_24", bucket(16.8))
        assertEquals("le_24", bucket(24.0))
        assertEquals("le_33", bucket(24.1))
        // Two 60 Hz vsyncs (33.33 ms): one dropped frame.
        assertEquals("le_33", bucket(33.4))
        assertEquals("le_50", bucket(33.5))
        assertEquals("le_50", bucket(50.0))
        assertEquals("le_100", bucket(50.1))
        assertEquals("le_100", bucket(100.0))
        assertEquals("gt_100", bucket(100.1))
        assertEquals("gt_100", bucket(4_000.0))
        assertEquals("le_16", FrameTimeBucket.of(-1L).code)
    }

    @Test
    fun `the event carries only the transition and the bucket the tier and the first-launch flag`() {
        val event = TransitionFrames(
            transition = PerfTransition.ONBOARDING_TO_HOME,
            worstFrame = FrameTimeBucket.LE_33,
            motionTier = MotionTierCode.LITE,
            firstLaunchOfVersion = true,
        )
        assertEquals("transition_frames", event.name)
        assertEquals(
            mapOf<String, Any>(
                "transition" to "onboarding_to_home",
                "worst_frame" to "le_33",
                "motion_tier" to "lite",
                "first_launch_of_version" to 1L,
            ),
            event.params,
        )
        val later = event.copy(firstLaunchOfVersion = false)
        assertEquals(0L, later.params["first_launch_of_version"])
    }

    @Test
    fun `codes match docs ANALYTICS and fit Firebase limits`() {
        assertEquals(
            listOf("splash", "onboarding_to_home", "picker_open", "dialog_open", "dialog_close", "morph", "game_start"),
            PerfTransition.entries.map { it.code },
        )
        assertEquals(listOf("full", "lite", "minimal"), MotionTierCode.entries.map { it.code })
        assertEquals(
            listOf("le_16", "le_24", "le_33", "le_50", "le_100", "gt_100"),
            FrameTimeBucket.entries.map { it.code },
        )
        val snake = Regex("[a-z][a-z0-9_]{0,39}")
        PerfTransition.entries.forEach { transition ->
            val event = TransitionFrames(transition, FrameTimeBucket.GT_100, MotionTierCode.FULL, false)
            assertTrue(snake.matches(event.name), event.name)
            event.params.forEach { (key, value) ->
                assertTrue(snake.matches(key), key)
                assertTrue((value is String && value.length <= 100) || value is Long, "$key=$value")
            }
        }
    }

    private companion object {
        const val NANOS_PER_MILLI = 1_000_000.0
    }
}
