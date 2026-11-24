package dev.talhabayhan.lexi.core.designsystem.motion

import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsEvent
import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsEvent.FrameTimeBucket
import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsEvent.MotionTierCode
import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsEvent.PerfTransition
import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsEvent.TransitionFrames
import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsTracker
import kotlin.test.Test
import kotlin.test.assertEquals

/** `transition_frames`: at most once per named transition per process, bucketed, with tier and first-launch flag. */
class TransitionTelemetryTest {
    private val events = mutableListOf<AnalyticsEvent>()
    private val telemetry = TransitionTelemetry(
        object : AnalyticsTracker {
            override fun track(event: AnalyticsEvent) {
                events += event
            }
        },
        TransitionSession(),
    )

    @Test
    fun `each named transition is sent once per session with its bucket tier and first-launch flag`() {
        telemetry.firstLaunchOfVersion = true
        telemetry.onTransitionMeasured(setOf(TransitionKeys.SPLASH), 40_000_000L, MotionTier.Full)
        telemetry.onTransitionMeasured(setOf(TransitionKeys.SPLASH), 8_000_000L, MotionTier.Full)
        telemetry.onTransitionMeasured(setOf(TransitionKeys.DIALOG_OPEN), 8_000_000L, MotionTier.Lite)

        assertEquals(
            listOf<AnalyticsEvent>(
                TransitionFrames(PerfTransition.SPLASH, FrameTimeBucket.LE_50, MotionTierCode.FULL, true),
                TransitionFrames(PerfTransition.DIALOG_OPEN, FrameTimeBucket.LE_16, MotionTierCode.LITE, true),
            ),
            events,
        )
    }

    @Test
    fun `a recreated reporter on the same session sends nothing twice`() {
        val session = TransitionSession()
        val tracker = object : AnalyticsTracker {
            override fun track(event: AnalyticsEvent) {
                events += event
            }
        }
        TransitionTelemetry(
            tracker,
            session,
        ).onTransitionMeasured(setOf(TransitionKeys.MORPH_EXPAND), 1L, MotionTier.Full)
        TransitionTelemetry(
            tracker,
            session,
        ).onTransitionMeasured(setOf(TransitionKeys.MORPH_EXPAND), 1L, MotionTier.Full)

        assertEquals(1, events.size)
    }

    @Test
    fun `every documented transition has a frame monitor key`() {
        val keys = mapOf(
            TransitionKeys.SPLASH to PerfTransition.SPLASH,
            TransitionKeys.ONBOARDING_TO_HOME to PerfTransition.ONBOARDING_TO_HOME,
            TransitionKeys.PICKER_GRID_ENTRANCE to PerfTransition.PICKER_OPEN,
            TransitionKeys.DIALOG_OPEN to PerfTransition.DIALOG_OPEN,
            TransitionKeys.DIALOG_CLOSE to PerfTransition.DIALOG_CLOSE,
            TransitionKeys.MORPH_EXPAND to PerfTransition.MORPH,
            TransitionKeys.MORPH_SHRINK to PerfTransition.GAME_START,
        )
        keys.forEach { (key, _) -> telemetry.onTransitionMeasured(setOf(key), 120_000_000L, MotionTier.Minimal) }

        assertEquals(keys.values.toList(), events.map { (it as TransitionFrames).transition })
        events.forEach {
            it as TransitionFrames
            assertEquals(FrameTimeBucket.GT_100, it.worstFrame)
            assertEquals(MotionTierCode.MINIMAL, it.motionTier)
            assertEquals(false, it.firstLaunchOfVersion)
        }
    }

    @Test
    fun `a span with several keys reports each named one and ignores unknown keys`() {
        telemetry.onTransitionMeasured(
            setOf("debug.other", TransitionKeys.MORPH_SHRINK, TransitionKeys.PICKER_GRID_ENTRANCE),
            20_000_000L,
            MotionTier.Full,
        )

        assertEquals(
            setOf(PerfTransition.GAME_START, PerfTransition.PICKER_OPEN),
            events.map { (it as TransitionFrames).transition }.toSet(),
        )
        assertEquals(2, events.size)
    }

    @Test
    fun `it is wired to a frame monitor as its listener`() {
        val monitor = FrameMonitor()
        monitor.onTransitionMeasured = telemetry.listener { MotionTier.Full }
        monitor.beginTransition(TransitionKeys.DIALOG_CLOSE)
        monitor.onFrame(1_000_000_000L)
        monitor.onFrame(1_030_000_000L)
        monitor.endTransition()

        assertEquals(
            listOf<AnalyticsEvent>(
                TransitionFrames(PerfTransition.DIALOG_CLOSE, FrameTimeBucket.LE_33, MotionTierCode.FULL, false),
            ),
            events,
        )
    }
}
