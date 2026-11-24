package dev.talhabayhan.lexi.splash

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** When the iOS native splash leaves (ADR 0028): animation over AND first screen ready, or the 3 s limit. */
class NativeSplashGateTest {
    private val timers = mutableListOf<Pair<Long, () -> Unit>>()
    private val left = mutableListOf<SplashLeaveReason>()
    private val gate = NativeSplashGate(
        capMillis = NativeSplashGate.CAP_MILLIS,
        schedule = { delay, action -> timers += delay to action },
        onLeave = { left += it },
    )

    private fun fireCap() = timers.single { it.first == NativeSplashGate.CAP_MILLIS }.second()

    @Test
    fun readyBeforeTheAnimationEndsWaitsForTheAnimation() {
        gate.start()
        assertFalse(gate.contentReady())
        assertTrue(left.isEmpty(), "left before the animation ended")
        gate.animationEnded()
        assertEquals(listOf(SplashLeaveReason.Ready), left)
    }

    @Test
    fun theAnimationEndingFirstWaitsForTheFirstScreen() {
        gate.start()
        gate.animationEnded()
        assertTrue(left.isEmpty(), "left before Compose was ready")
        assertFalse(gate.contentReady())
        assertEquals(listOf(SplashLeaveReason.Ready), left)
    }

    @Test
    fun theLimitIsThreeSecondsAndEndsTheSplashWhateverHappens() {
        gate.start()
        assertEquals(listOf(3_000L), timers.map { it.first })
        assertNull(gate.leaveReason)
        fireCap()
        assertEquals(listOf(SplashLeaveReason.TimedOut), left)
        // A late first screen reports that the fail-safe fired; nothing leaves twice.
        gate.animationEnded()
        assertTrue(gate.contentReady())
        assertEquals(listOf(SplashLeaveReason.TimedOut), left)
    }

    @Test
    fun theLimitAfterANormalExitDoesNothing() {
        gate.start()
        gate.animationEnded()
        gate.contentReady()
        fireCap()
        assertEquals(listOf(SplashLeaveReason.Ready), left)
        assertEquals(SplashLeaveReason.Ready, gate.leaveReason)
    }

    @Test
    fun startIsIdempotent() {
        gate.start()
        gate.start()
        assertEquals(1, timers.size)
    }
}
