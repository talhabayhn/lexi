package dev.talhabayhan.lexi.core.designsystem.motion

import dev.talhabayhan.lexi.core.designsystem.theme.LexiMotion
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** `motion.md` #16 sequencing with a fake clock: every time below is ms of frame time; the tap is at [T0]. */
class LetterMorphMachineTest {
    private val flow = LexiMotion(reducedMotion = false).flow
    private val machine = LetterMorphMachine(flow)

    private fun at(elapsed: Long) = machine.tick(T0 + elapsed)

    private val expandEnd get() = flow.inkExpand.endMillis.toLong()
    private val holdEnd get() = flow.inkShrink.delayMillis.toLong()
    private val shrink get() = flow.inkShrink.durationMillis.toLong()
    private val settled get() = flow.boardSettledMillis.toLong()

    @Test
    fun `the game ready before the hold ends shrinks at the hold end and frees the clock when the board settled`() {
        assertTrue(machine.start(T0, MotionTier.Full))
        assertEquals(LetterMorphPhase.Expanding, at(0))
        assertFalse(machine.covered)

        assertEquals(LetterMorphPhase.Holding, at(expandEnd))
        assertTrue(machine.covered, "the game may be opened under the ink")
        machine.markReady()
        machine.setTargetLaidOut(true)

        assertEquals(LetterMorphPhase.Holding, at(holdEnd - 1))
        assertEquals(LetterMorphPhase.Shrinking, at(holdEnd))
        assertEquals(holdEnd, machine.shrinkStartMillis)
        assertFalse(machine.covered)

        assertEquals(LetterMorphPhase.Settling, at(holdEnd + shrink))
        assertFalse(machine.clockMayStart, "the board is still coming in")
        assertEquals(LetterMorphPhase.Settling, at(holdEnd + settled - 1))
        assertEquals(LetterMorphPhase.Finished, at(holdEnd + settled))
        assertTrue(machine.clockMayStart)
        assertFalse(machine.isActive)
    }

    @Test
    fun `the whole ink motion takes about 0_93 s when the game is ready in time`() {
        machine.start(T0, MotionTier.Full)
        machine.markReady()
        machine.setTargetLaidOut(true)
        at(holdEnd)
        at(holdEnd + shrink)

        assertEquals(LetterMorphPhase.Settling, machine.phase)
        assertEquals(flow.letterMorphMillis.toLong(), holdEnd + shrink - flow.inkExpand.delayMillis)
    }

    @Test
    fun `a game ready after the hold makes the letter pulse until then and shrinks right away`() {
        machine.start(T0, MotionTier.Full)
        machine.setTargetLaidOut(true)
        assertEquals(LetterMorphPhase.Waiting, at(holdEnd + 1))
        assertEquals(1f, machine.pulseScale(), absoluteTolerance = 1e-3f, message = "the pulse starts from rest")

        val quarter = holdEnd + flow.letterPulseMillis / 2
        at(quarter)
        assertEquals(flow.letterPulseScale, machine.pulseScale(), absoluteTolerance = 1e-4f)
        assertEquals(LetterMorphPhase.Waiting, at(2_000))
        assertTrue(machine.covered)

        machine.markReady()
        val before = machine.pulseScale()
        assertEquals(LetterMorphPhase.Shrinking, at(2_000))
        assertEquals(2_000L, machine.shrinkStartMillis)
        assertEquals(before, machine.pulseScale(), "no jump when the shrink starts")
        at(2_000 + shrink)
        assertEquals(1f, machine.pulseScale(), "the pulse has faded out with the shrink")
    }

    @Test
    fun `the shrink waits for the marker to be laid out`() {
        machine.start(T0, MotionTier.Full)
        machine.markReady()

        assertEquals(LetterMorphPhase.Waiting, at(holdEnd + 100))
        assertNull(machine.shrinkStartMillis)
        machine.setTargetLaidOut(true)
        assertEquals(LetterMorphPhase.Shrinking, at(holdEnd + 150))
    }

    @Test
    fun `a cancel fades the overlay out and frees the clock`() {
        machine.start(T0, MotionTier.Full)
        at(300)
        machine.cancel()
        assertEquals(LetterMorphPhase.Cancelling, at(300))
        assertEquals(1f, machine.overlayAlpha())
        assertFalse(machine.clockMayStart)

        at(300 + flow.reducedCrossfadeMillis / 2L)
        assertEquals(0.5f, machine.overlayAlpha(), absoluteTolerance = 0.01f)
        machine.markReady()
        machine.setTargetLaidOut(true)
        assertNull(machine.shrinkStartMillis, "a cancelled morph never shrinks")

        assertEquals(LetterMorphPhase.Cancelled, at(300 + flow.reducedCrossfadeMillis.toLong()))
        assertTrue(machine.clockMayStart)
        assertFalse(machine.isActive)
    }

    @Test
    fun `a second cancel and late events change nothing`() {
        machine.start(T0, MotionTier.Full)
        at(900)
        machine.cancel()
        at(950)
        machine.cancel()
        assertEquals(900L, machine.cancelStartMillis)
        at(5_000)
        machine.cancel()
        machine.markReady()
        assertEquals(LetterMorphPhase.Cancelled, at(6_000))
    }

    @Test
    fun `a second start while one runs is refused and minimal never morphs`() {
        assertFalse(machine.start(T0, MotionTier.Minimal))
        assertEquals(LetterMorphPhase.Idle, machine.phase)

        assertTrue(machine.start(T0, MotionTier.Full))
        assertFalse(machine.start(T0 + 10, MotionTier.Full))
    }

    @Test
    fun `after a finished run reset allows the next one`() {
        machine.start(T0, MotionTier.Full)
        machine.markReady()
        machine.setTargetLaidOut(true)
        at(holdEnd)
        at(holdEnd + settled)
        assertFalse(machine.isActive)

        machine.reset()
        assertEquals(LetterMorphPhase.Idle, machine.phase)
        assertTrue(machine.start(T0 + 10_000, MotionTier.Full))
        assertEquals(LetterMorphPhase.Expanding, machine.tick(T0 + 10_000))
        assertNull(machine.shrinkStartMillis)
    }

    @Test
    fun `the tier is latched at the start for the letter and at the shrink for the board`() {
        machine.start(T0, MotionTier.Full)
        assertTrue(machine.letterPop)
        at(400)
        machine.setTier(MotionTier.Lite)
        assertTrue(machine.letterPop, "a downgrade mid-expand keeps the letter's pop: no jump")

        machine.markReady()
        machine.setTargetLaidOut(true)
        at(holdEnd)
        assertFalse(machine.boardStagger, "the board enters in Lite once the downgrade happened")
    }

    @Test
    fun `lite drops the letter pop and the board stagger from the start`() {
        machine.start(T0, MotionTier.Lite)
        assertFalse(machine.letterPop)
        machine.markReady()
        machine.setTargetLaidOut(true)
        at(holdEnd)
        assertFalse(machine.boardStagger)
    }

    @Test
    fun `time never runs backwards`() {
        machine.start(T0, MotionTier.Full)
        at(600)
        at(500)
        assertEquals(600L, machine.elapsedMillis)
    }

    private companion object {
        const val T0 = 1_000_000L
    }
}
