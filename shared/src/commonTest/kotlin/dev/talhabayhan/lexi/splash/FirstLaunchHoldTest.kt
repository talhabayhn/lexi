package dev.talhabayhan.lexi.splash

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The first launch of a version (`docs/PERF.md` § Launch logo): the splash holds its logo while the hidden warm-up
 * runs, at most [FIRST_LAUNCH_HOLD_CAP_MILLIS]; the version is stored only after a completed warm-up.
 */
class FirstLaunchHoldTest {
    @Test
    fun `a warm-up that completes ends the hold at once`() = runTest {
        val outcome = holdForWarmUp(FIRST_LAUNCH_HOLD_CAP_MILLIS) { delay(300) }

        assertEquals(WarmUpOutcome.Completed, outcome)
        assertEquals(300, currentTime)
    }

    @Test
    fun `a warm-up that takes too long is cut at the 2 second cap`() = runTest {
        var cancelled = false
        val outcome = holdForWarmUp(FIRST_LAUNCH_HOLD_CAP_MILLIS) {
            try {
                awaitCancellation()
            } finally {
                cancelled = true
            }
        }

        assertEquals(WarmUpOutcome.CappedOut, outcome)
        assertEquals(2_000, currentTime)
        assertTrue(cancelled, "the warm-up kept running after the cap")
    }

    @Test
    fun `the cap is 2 seconds`() {
        assertEquals(2_000L, FIRST_LAUNCH_HOLD_CAP_MILLIS)
    }

    @Test
    fun `only a stored version that differs from the running one is a first launch`() {
        assertTrue(FirstLaunchWarmUp.isFirstLaunchOfVersion(stored = 0, running = 10_103))
        assertTrue(FirstLaunchWarmUp.isFirstLaunchOfVersion(stored = 10_102, running = 10_103))
        assertFalse(FirstLaunchWarmUp.isFirstLaunchOfVersion(stored = 10_103, running = 10_103))
        // Not read in time: unknown, so not held (the next launch tries again).
        assertFalse(FirstLaunchWarmUp.isFirstLaunchOfVersion(stored = null, running = 10_103))
    }

    @Test
    fun `reduced motion never holds or warms up`() {
        assertTrue(FirstLaunchWarmUp.shouldWarmUp(stored = 0, running = 2, reducedMotion = false))
        assertFalse(FirstLaunchWarmUp.shouldWarmUp(stored = 0, running = 2, reducedMotion = true))
        assertFalse(FirstLaunchWarmUp.shouldWarmUp(stored = 2, running = 2, reducedMotion = false))
        assertFalse(FirstLaunchWarmUp.shouldWarmUp(stored = null, running = 2, reducedMotion = false))
    }

    @Test
    fun `the version is stored only after a completed warm-up`() {
        assertTrue(FirstLaunchWarmUp.shouldRecord(WarmUpOutcome.Completed))
        assertFalse(FirstLaunchWarmUp.shouldRecord(WarmUpOutcome.CappedOut))
    }
}
