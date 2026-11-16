package dev.talhabayhan.lexi.core.designsystem.motion

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Downgrade rule: 3 consecutive frames > 24 ms during a registered transition (fake frame clock). */
class FrameMonitorTest {
    private val frame = 16_667_000L
    private val slow = 30_000_000L

    /** Feeds frames that are [gaps] ns apart, starting at [startNanos]. */
    private fun FrameMonitor.frames(vararg gaps: Long, startNanos: Long = 1_000_000_000L) {
        var t = startNanos
        onFrame(t)
        for (gap in gaps) {
            t += gap
            onFrame(t)
        }
    }

    @Test
    fun `three consecutive slow frames during a transition report a slow streak`() {
        val monitor = FrameMonitor()
        monitor.beginTransition()

        monitor.frames(frame, slow, slow, slow)

        assertTrue(monitor.slowStreakDetected)
    }

    @Test
    fun `two slow frames are tolerated and a fast frame resets the count`() {
        val monitor = FrameMonitor()
        monitor.beginTransition()

        monitor.frames(slow, slow, frame, slow, slow, frame)

        assertFalse(monitor.slowStreakDetected)
    }

    @Test
    fun `a frame of exactly 24 ms is not slow`() {
        val monitor = FrameMonitor()
        monitor.beginTransition()

        monitor.frames(24_000_000L, 24_000_000L, 24_000_000L)

        assertFalse(monitor.slowStreakDetected)
    }

    @Test
    fun `frames outside a transition are ignored`() {
        val monitor = FrameMonitor()

        monitor.frames(slow, slow, slow, slow)

        assertFalse(monitor.slowStreakDetected)
    }

    @Test
    fun `the gap before the first frame of a transition is not counted`() {
        val monitor = FrameMonitor()
        monitor.frames(frame)
        monitor.beginTransition()

        // The first frame of the transition comes 2 s after the last idle frame: not a slow frame.
        monitor.frames(slow, slow, startNanos = 3_000_000_000L)

        assertFalse(monitor.slowStreakDetected)
    }

    @Test
    fun `the count restarts with each transition`() {
        val monitor = FrameMonitor()
        monitor.beginTransition()
        monitor.frames(slow, slow)
        monitor.endTransition()
        monitor.beginTransition()

        monitor.frames(slow, startNanos = 5_000_000_000L)

        assertFalse(monitor.slowStreakDetected)
    }

    @Test
    fun `overlapping transitions stay active until the last one ends`() {
        val monitor = FrameMonitor()
        monitor.beginTransition()
        monitor.beginTransition()
        monitor.endTransition()

        assertTrue(monitor.isWatching)
        monitor.endTransition()
        assertFalse(monitor.isWatching)
        monitor.endTransition()
        assertEquals(false, monitor.isWatching)
    }

    @Test
    fun `the streak sticks once detected`() {
        val monitor = FrameMonitor()
        monitor.beginTransition()
        monitor.frames(slow, slow, slow)
        monitor.endTransition()
        monitor.beginTransition()

        monitor.frames(frame, frame, startNanos = 9_000_000_000L)

        assertTrue(monitor.slowStreakDetected)
    }
}
