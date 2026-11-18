package dev.talhabayhan.lexi.core.designsystem.motion

import kotlin.test.Test
import kotlin.test.assertEquals

class MotionPolicyTest {
    private val normal = MotionSignals(reducedMotion = false, lowRamDevice = false, lowPowerMode = false)
    private val reduced = normal.copy(reducedMotion = true)
    private val lowRam = normal.copy(lowRamDevice = true)
    private val lowPower = normal.copy(lowPowerMode = true)
    private var clock = 0L

    /** One transition of [key] whose frames are [gapNanos] apart ([frames] intervals). */
    private fun FrameMonitor.transition(key: String, gapNanos: Long, frames: Int = 6) {
        beginTransition(key)
        clock += 1_000_000_000L
        onFrame(clock)
        repeat(frames) {
            clock += gapNanos
            onFrame(clock)
        }
        endTransition()
    }

    /** A policy whose monitor is past the warm-up and downgraded by 2 slow transitions. */
    private fun downgradedPolicy(): MotionPolicy {
        val monitor = FrameMonitor()
        monitor.transition(KEY, GOOD) // warm-up
        repeat(2) { monitor.transition(KEY, SLOW, frames = 3) }
        return MotionPolicy(monitor)
    }

    private fun MotionPolicy.recover() = repeat(3) { frameMonitor.transition(KEY, GOOD) }

    @Test
    fun `a normal device gets the full tier`() {
        val policy = MotionPolicy()

        assertEquals(MotionTier.Full, policy.tier(normal))
        assertEquals(MotionTierReason.Default, policy.tierReason(normal))
    }

    @Test
    fun `a slow cold start keeps the full tier`() {
        val policy = MotionPolicy()

        // First-ever flip and morph of the process: very slow (JIT, class loading), not counted.
        policy.frameMonitor.transition(KEY, SLOW, frames = 10)
        policy.frameMonitor.transition("morph", SLOW, frames = 10)

        assertEquals(MotionTier.Full, policy.tier(normal))
    }

    @Test
    fun `reduced motion always wins with the minimal tier`() {
        val policy = MotionPolicy()

        assertEquals(MotionTier.Minimal, policy.tier(reduced))
        assertEquals(MotionTier.Minimal, policy.tier(MotionSignals(true, lowRamDevice = true, lowPowerMode = true)))
        assertEquals(MotionTierReason.ReducedMotion, policy.tierReason(reduced))
    }

    @Test
    fun `low ram android and ios low power mode start in lite`() {
        val policy = MotionPolicy()

        assertEquals(MotionTier.Lite, policy.tier(lowRam))
        assertEquals(MotionTier.Lite, policy.tier(lowPower))
        assertEquals(MotionTierReason.LowRamDevice, policy.tierReason(lowRam))
        assertEquals(MotionTierReason.LowPowerMode, policy.tierReason(lowPower))
    }

    @Test
    fun `leaving low power mode returns to full`() {
        val policy = MotionPolicy()
        policy.tier(lowPower)

        assertEquals(MotionTier.Full, policy.tier(normal))
    }

    @Test
    fun `slow transitions downgrade to lite and good ones bring full back`() {
        val policy = downgradedPolicy()
        assertEquals(MotionTier.Lite, policy.tier(normal))
        assertEquals(MotionTierReason.SlowFrames, policy.tierReason(normal))

        policy.recover()

        assertEquals(MotionTier.Full, policy.tier(normal))
        assertEquals(MotionTierReason.Default, policy.tierReason(normal))
    }

    @Test
    fun `the frame rules never override reduced motion`() {
        val policy = downgradedPolicy()
        assertEquals(MotionTier.Minimal, policy.tier(reduced))

        policy.recover()

        assertEquals(MotionTier.Minimal, policy.tier(reduced))
        assertEquals(MotionTierReason.ReducedMotion, policy.tierReason(reduced))
    }

    @Test
    fun `a recovery never lifts low ram or low power mode out of lite`() {
        val policy = downgradedPolicy()

        policy.recover()

        assertEquals(MotionTier.Lite, policy.tier(lowRam))
        assertEquals(MotionTier.Lite, policy.tier(lowPower))
        assertEquals(MotionTierReason.LowRamDevice, policy.tierReason(lowRam))
        assertEquals(MotionTierReason.LowPowerMode, policy.tierReason(lowPower))
    }

    @Test
    fun `a forced tier wins over every signal and the frame rules`() {
        val policy = downgradedPolicy()
        policy.forcedTier = MotionTier.Full

        assertEquals(MotionTier.Full, policy.tier(reduced))
        assertEquals(MotionTier.Full, policy.tier(lowRam))
        assertEquals(MotionTier.Full, policy.tier(normal))
        assertEquals(MotionTierReason.Forced, policy.tierReason(normal))

        policy.forcedTier = MotionTier.Lite
        policy.recover()
        assertEquals(MotionTier.Lite, policy.tier(normal))

        policy.forcedTier = null
        assertEquals(MotionTier.Minimal, policy.tier(reduced))
        assertEquals(MotionTier.Full, policy.tier(normal))
    }

    private companion object {
        const val KEY = "flip"
        const val GOOD = 16_667_000L
        const val SLOW = 40_000_000L
    }
}
