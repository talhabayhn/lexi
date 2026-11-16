package dev.talhabayhan.lexi.core.designsystem.motion

import kotlin.test.Test
import kotlin.test.assertEquals

class MotionPolicyTest {
    private val normal = MotionSignals(reducedMotion = false, lowRamDevice = false, lowPowerMode = false)

    @Test
    fun `a normal device gets the full tier`() {
        assertEquals(MotionTier.Full, MotionPolicy().tier(normal))
    }

    @Test
    fun `reduced motion always wins with the minimal tier`() {
        val policy = MotionPolicy()

        assertEquals(MotionTier.Minimal, policy.tier(normal.copy(reducedMotion = true)))
        assertEquals(MotionTier.Minimal, policy.tier(MotionSignals(true, lowRamDevice = true, lowPowerMode = true)))
    }

    @Test
    fun `low ram android and ios low power mode start in lite`() {
        val policy = MotionPolicy()

        assertEquals(MotionTier.Lite, policy.tier(normal.copy(lowRamDevice = true)))
        assertEquals(MotionTier.Lite, policy.tier(normal.copy(lowPowerMode = true)))
    }

    @Test
    fun `leaving low power mode returns to full`() {
        val policy = MotionPolicy()
        policy.tier(normal.copy(lowPowerMode = true))

        assertEquals(MotionTier.Full, policy.tier(normal))
    }

    @Test
    fun `a slow streak downgrades to lite for the rest of the session`() {
        val monitor = FrameMonitor()
        val policy = MotionPolicy(monitor)
        monitor.beginTransition()
        var t = 0L
        repeat(4) {
            monitor.onFrame(t)
            t += 40_000_000L
        }
        monitor.endTransition()

        assertEquals(MotionTier.Lite, policy.tier(normal))
        // Reduced motion still wins over the downgrade.
        assertEquals(MotionTier.Minimal, policy.tier(normal.copy(reducedMotion = true)))
    }
}
