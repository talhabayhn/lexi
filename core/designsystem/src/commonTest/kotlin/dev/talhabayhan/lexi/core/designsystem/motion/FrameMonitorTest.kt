package dev.talhabayhan.lexi.core.designsystem.motion

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Downgrade rules (fake frame clock): the first transition of each key is a warm-up; Lite after 2 transitions with a
 * slow streak (3 frames > 24 ms) or one streak of 5; back to Full after 3 good transitions in a row.
 */
class FrameMonitorTest {
    private val frame = 16_667_000L
    private val slow = 30_000_000L
    private var clock = 1_000_000_000L

    /** Feeds frames that are [gaps] ns apart, one idle second after the previous feed. */
    private fun FrameMonitor.frames(vararg gaps: Long) {
        clock += 1_000_000_000L
        onFrame(clock)
        for (gap in gaps) {
            clock += gap
            onFrame(clock)
        }
    }

    /** One registered transition of [key] with frames [gaps] apart. */
    private fun FrameMonitor.transition(key: String, vararg gaps: Long) {
        beginTransition(key)
        frames(*gaps)
        endTransition()
    }

    private fun FrameMonitor.slowTransition(key: String = FLIP) = transition(key, frame, slow, slow, slow, frame)

    private fun FrameMonitor.goodTransition(key: String = FLIP) = transition(key, frame, frame, frame, frame, frame)

    /** A monitor whose [keys] are past their warm-up. */
    private fun warmMonitor(vararg keys: String = arrayOf(FLIP)): FrameMonitor =
        FrameMonitor().apply { keys.forEach { goodTransition(it) } }

    private fun clockAfter(gap: Long): Long {
        clock += gap
        return clock
    }

    @Test
    fun `the first transition of a key is a warm-up and never counts`() {
        val monitor = FrameMonitor()

        // Cold start: a very slow first flip (JIT, class loading), far beyond the 5-frame rule.
        monitor.transition(FLIP, slow, slow, slow, slow, slow, slow, slow, slow)
        monitor.slowTransition(MORPH) // the first morph is a warm-up of its own

        assertNull(monitor.downgrade)
        assertEquals(0, monitor.slowTransitions)
    }

    @Test
    fun `a warm-up cancelled too early to judge leaves the key cold`() {
        val monitor = FrameMonitor()
        monitor.transition(FLIP, frame) // cancelled after one frame

        // Still the warm-up: a slow, still-cold run is not judged.
        monitor.transition(FLIP, slow, slow, slow, slow, slow, slow)
        monitor.slowTransition()

        assertNull(monitor.downgrade)
        assertEquals(1, monitor.slowTransitions)
    }

    @Test
    fun `a transition overlapping a first-ever one is part of the warm-up`() {
        val monitor = warmMonitor(FLIP)
        monitor.beginTransition(FLIP)
        monitor.beginTransition(GRID)

        monitor.frames(slow, slow, slow, slow, slow)
        monitor.endTransition()
        monitor.endTransition()

        assertNull(monitor.downgrade)
        assertEquals(0, monitor.slowTransitions)
    }

    @Test
    fun `one transition with a 3-frame streak is not enough`() {
        val monitor = warmMonitor()

        monitor.slowTransition()

        assertNull(monitor.downgrade)
        assertEquals(1, monitor.slowTransitions)
    }

    @Test
    fun `two separate transitions with a slow streak downgrade to lite`() {
        val monitor = warmMonitor(FLIP, MORPH)

        monitor.slowTransition(FLIP)
        monitor.goodTransition(FLIP)
        monitor.slowTransition(MORPH)

        assertEquals(FrameDowngrade.SlowTransitions(2), monitor.downgrade)
    }

    @Test
    fun `two streaks inside one transition count as one`() {
        val monitor = warmMonitor()

        monitor.transition(FLIP, slow, slow, slow, frame, slow, slow, slow)

        assertNull(monitor.downgrade)
        assertEquals(1, monitor.slowTransitions)
    }

    @Test
    fun `five consecutive slow frames in one transition downgrade at once`() {
        val monitor = warmMonitor()
        monitor.beginTransition(FLIP)

        monitor.frames(frame, slow, slow, slow, slow)
        assertNull(monitor.downgrade)
        monitor.onFrame(clockAfter(slow))

        assertEquals(FrameDowngrade.LongStreak(5), monitor.downgrade)
    }

    @Test
    fun `slow evidence fades after three good transitions`() {
        val monitor = warmMonitor()
        monitor.slowTransition()

        repeat(3) { monitor.goodTransition() }
        monitor.slowTransition()

        assertNull(monitor.downgrade)
        assertEquals(1, monitor.slowTransitions)
    }

    @Test
    fun `three good transitions in a row return to full`() {
        val monitor = warmMonitor()
        monitor.slowTransition()
        monitor.slowTransition()
        assertTrue(monitor.isDowngraded)

        monitor.goodTransition()
        monitor.goodTransition()
        assertTrue(monitor.isDowngraded)
        assertEquals(2, monitor.goodTransitionsInARow)
        monitor.goodTransition()

        assertFalse(monitor.isDowngraded)
        assertEquals(1, monitor.recoveries)
        assertEquals(0, monitor.slowTransitions)
    }

    @Test
    fun `a slow transition during recovery restarts the good count`() {
        val monitor = warmMonitor()
        monitor.slowTransition()
        monitor.slowTransition()

        monitor.goodTransition()
        monitor.goodTransition()
        monitor.slowTransition()
        monitor.goodTransition()
        monitor.goodTransition()

        assertTrue(monitor.isDowngraded)
        monitor.goodTransition()
        assertFalse(monitor.isDowngraded)
    }

    @Test
    fun `after a recovery a new downgrade needs fresh evidence`() {
        val monitor = warmMonitor()
        repeat(2) { monitor.slowTransition() }
        repeat(3) { monitor.goodTransition() }

        monitor.slowTransition()
        assertFalse(monitor.isDowngraded)
        monitor.slowTransition()

        assertEquals(FrameDowngrade.SlowTransitions(2), monitor.downgrade)
    }

    @Test
    fun `a transition too short to judge is neither good nor slow`() {
        val monitor = warmMonitor()
        repeat(2) { monitor.slowTransition() }

        repeat(5) { monitor.transition(FLIP, frame) }

        assertTrue(monitor.isDowngraded)
        assertEquals(0, monitor.goodTransitionsInARow)
    }

    @Test
    fun `two slow frames are tolerated and a fast frame resets the run`() {
        val monitor = warmMonitor()

        monitor.transition(FLIP, slow, slow, frame, slow, slow, frame)
        monitor.transition(FLIP, slow, slow, frame, slow, slow, frame)

        assertNull(monitor.downgrade)
        assertEquals(0, monitor.slowTransitions)
    }

    @Test
    fun `a frame of exactly 24 ms is not slow`() {
        val monitor = warmMonitor()
        val limit = FrameMonitor.DEFAULT_SLOW_FRAME_NANOS

        repeat(2) { monitor.transition(FLIP, limit, limit, limit, limit, limit) }

        assertNull(monitor.downgrade)
        assertEquals(0, monitor.slowTransitions)
    }

    @Test
    fun `frames outside a transition are ignored`() {
        val monitor = warmMonitor()

        monitor.frames(slow, slow, slow, slow, slow, slow)

        assertNull(monitor.downgrade)
    }

    @Test
    fun `the gap before the first frame of a transition is not counted`() {
        val monitor = warmMonitor()
        monitor.frames(frame)
        monitor.beginTransition(FLIP)

        // The first frame of the transition comes 1 s after the last idle frame: not a slow frame.
        monitor.frames(slow, slow)
        monitor.endTransition()

        assertEquals(0, monitor.slowTransitions)
    }

    @Test
    fun `the run restarts with each transition`() {
        val monitor = warmMonitor()

        monitor.transition(FLIP, frame, frame, slow, slow)
        monitor.transition(FLIP, slow, frame, frame, frame)

        assertEquals(0, monitor.slowTransitions)
    }

    @Test
    fun `overlapping transitions stay active until the last one ends`() {
        val monitor = FrameMonitor()
        monitor.beginTransition(FLIP)
        monitor.beginTransition(MORPH)
        monitor.endTransition()

        assertTrue(monitor.isWatching)
        monitor.endTransition()
        assertFalse(monitor.isWatching)
        monitor.endTransition()
        assertEquals(false, monitor.isWatching)
    }

    @Test
    fun `the same frame reported twice is counted once`() {
        val monitor = warmMonitor()
        monitor.beginTransition(FLIP)
        var t = clockAfter(frame)
        repeat(6) {
            monitor.onFrame(t)
            monitor.onFrame(t)
            t += slow
        }

        assertEquals(FrameDowngrade.LongStreak(5), monitor.downgrade)
    }

    private companion object {
        const val FLIP = "flip"
        const val MORPH = "morph"
        const val GRID = "grid"
    }
}
