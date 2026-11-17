package dev.talhabayhan.lexi.core.designsystem.motion

import dev.talhabayhan.lexi.core.designsystem.theme.LexiMotion
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** `motion.md` #13–#15 and the interruption rules of the dialog flip, with a fake frame clock. */
class DialogFlipTest {
    private val motion = LexiMotion(reducedMotion = false)
    private val flow = motion.flow
    private val choreography = DialogFlipChoreography(flow, motion.mechanical, motion.turn, motion.pop)
    private val machine = DialogFlipMachine(choreography)

    private val openEnd get() = flow.dialogOpenSettledMillis.toLong()
    private val closeEnd get() = flow.dialogCloseSettledMillis.toLong()

    private fun run(millis: Long, step: Long = 16L) {
        var left = millis
        while (left > 0) {
            machine.tick(minOf(step, left))
            left -= step
        }
    }

    @Test
    fun `at rest closed nothing of the dialog shows and the card is down`() {
        assertEquals(DialogFlipPhase.Closed, machine.phase)
        val frame = machine.frame()
        assertEquals(0f, frame.scrimAlpha)
        assertEquals(0f, frame.dialogAlpha)
        assertEquals(0f, frame.cardLift, absoluteTolerance = 1e-4f)
        assertEquals(0f, frame.cardTurnDegrees)
        assertEquals(1f, frame.cardAlpha)
    }

    @Test
    fun `opening lifts and turns the card away then flips the dialog in from the card rect`() {
        machine.open(MotionTier.Full)
        assertEquals(DialogFlipPhase.Opening, machine.phase)

        run(flow.cardLift.endMillis.toLong())
        assertEquals(1f, machine.frame().cardLift, absoluteTolerance = 1e-2f)

        run(flow.dialogFlipIn.delayMillis - flow.cardLift.endMillis.toLong())
        val flipStart = machine.frame()
        assertEquals(1f, flipStart.dialogAlpha)
        assertEquals(
            0f,
            flipStart.dialogGrow,
            absoluteTolerance = 1e-3f,
            message = "the dialog starts on the card rect",
        )
        assertEquals(-flow.flipDegrees, flipStart.dialogTurnDegrees, absoluteTolerance = 0.5f)

        run(flow.cardTurnAway.endMillis - flow.dialogFlipIn.delayMillis.toLong())
        assertEquals(0f, machine.frame().cardAlpha, "the card is edge-on and hidden")
        assertFalse(machine.dialogTakesInput)

        run(openEnd)
        assertEquals(DialogFlipPhase.Open, machine.phase)
        assertTrue(machine.dialogTakesInput)
        val open = machine.frame()
        assertEquals(1f, open.dialogGrow)
        assertEquals(0f, open.dialogTurnDegrees, absoluteTolerance = 1e-3f)
        assertEquals(1f, open.scrimAlpha)
        assertEquals(1f, open.contentAlpha)
        assertEquals(1f, machine.itemProgress(0))
    }

    @Test
    fun `the rows rise one after another`() {
        machine.open(MotionTier.Full)
        run(flow.dialogItemIn.delayMillis + flow.dialogItemStaggerMillis.toLong())

        assertTrue(machine.itemProgress(0) > machine.itemProgress(1))
        assertEquals(0f, machine.itemProgress(2))
    }

    @Test
    fun `the open end and the close start look the same and so do the close end and the open start`() {
        val openEndFrame = choreography.frameAt(DialogFlipTimeline.Open, openEnd, MotionTier.Full)
        val closeStart = choreography.frameAt(DialogFlipTimeline.Close, 0, MotionTier.Full)
        assertFramesClose(openEndFrame, closeStart)

        val closeEndFrame = choreography.frameAt(DialogFlipTimeline.Close, closeEnd, MotionTier.Full)
        val openStart = choreography.frameAt(DialogFlipTimeline.Open, 0, MotionTier.Full)
        assertFramesClose(closeEndFrame, openStart)
    }

    @Test
    fun `closing collapses the dialog to the card which turns back and lands`() {
        machine.open(MotionTier.Full)
        run(openEnd)
        machine.close(MotionTier.Full)
        assertEquals(DialogFlipPhase.Closing, machine.phase)
        assertFalse(machine.dialogTakesInput)

        run(flow.dialogContentOut.endMillis.toLong())
        assertEquals(0f, machine.frame().contentAlpha)

        run(closeEnd)
        assertEquals(DialogFlipPhase.Closed, machine.phase)
        val frame = machine.frame()
        assertEquals(0f, frame.cardTurnDegrees, absoluteTolerance = 1e-3f)
        assertEquals(0f, frame.cardLift, absoluteTolerance = 1e-3f)
        assertEquals(0f, frame.scrimAlpha)
        assertFalse(machine.dialogShown)
    }

    @Test
    fun `a close mid-open plays the open backwards from where it is without a jump`() {
        machine.open(MotionTier.Full)
        run(400)
        val before = machine.frame()
        machine.close(MotionTier.Full)
        assertEquals(DialogFlipPhase.Closing, machine.phase)
        assertEquals(DialogFlipTimeline.Open, machine.timeline, "the same timeline, backwards")
        assertEquals(before, machine.frame(), "no jump at the reversal")

        run(200)
        assertEquals(200L, machine.positionMillis)
        run(400)
        assertEquals(DialogFlipPhase.Closed, machine.phase)
        assertFramesClose(choreography.frameAt(DialogFlipTimeline.Open, 0, MotionTier.Full), machine.frame())
    }

    @Test
    fun `a reopen mid-close plays the close backwards back to the open dialog`() {
        machine.open(MotionTier.Full)
        run(openEnd)
        machine.close(MotionTier.Full)
        run(300)
        val before = machine.frame()
        machine.open(MotionTier.Full)
        assertEquals(DialogFlipPhase.Opening, machine.phase)
        assertEquals(before, machine.frame())

        run(300)
        assertEquals(DialogFlipPhase.Open, machine.phase)
        assertTrue(machine.dialogTakesInput)
        assertFramesClose(choreography.frameAt(DialogFlipTimeline.Open, openEnd, MotionTier.Full), machine.frame())
    }

    @Test
    fun `repeated requests in the running direction change nothing`() {
        machine.open(MotionTier.Full)
        run(100)
        machine.open(MotionTier.Full)
        assertEquals(100L, machine.positionMillis)
        machine.close(MotionTier.Full)
        machine.close(MotionTier.Full)
        run(100)
        assertEquals(DialogFlipPhase.Closed, machine.phase)
    }

    @Test
    fun `lite keeps the card still and scales the dialog in from 0_92 with a fade`() {
        machine.open(MotionTier.Lite)
        run(flow.dialogGrow.delayMillis.toLong())
        val start = machine.frame()
        assertEquals(0f, start.cardLift)
        assertEquals(0f, start.cardTurnDegrees)
        assertEquals(1f, start.cardAlpha)
        assertEquals(flow.liteFlipStartScale, start.dialogScale)
        assertEquals(0f, start.dialogAlpha)
        assertEquals(1f, start.dialogGrow)

        run(openEnd)
        assertEquals(1f, machine.frame().dialogScale)
        assertEquals(machine.itemProgress(0), machine.itemProgress(3), "rows enter together")
    }

    @Test
    fun `minimal is a short cross-fade both ways and the tier stays latched through a reversal`() {
        machine.open(MotionTier.Minimal)
        run(flow.reducedCrossfadeMillis / 2L, step = flow.reducedCrossfadeMillis / 2L)
        assertEquals(0.5f, machine.frame().dialogAlpha, absoluteTolerance = 0.01f)
        machine.close(MotionTier.Full)
        assertEquals(MotionTier.Minimal, machine.tier)
        run(flow.reducedCrossfadeMillis.toLong())
        assertEquals(DialogFlipPhase.Closed, machine.phase)

        machine.open(MotionTier.Minimal)
        run(flow.reducedCrossfadeMillis.toLong())
        assertTrue(machine.dialogTakesInput)
        assertEquals(0f, machine.frame().dialogTurnDegrees)
    }

    @Test
    fun `snapping to open shows the dialog at rest without running`() {
        machine.snapTo(open = true)
        assertEquals(DialogFlipPhase.Open, machine.phase)
        assertFalse(machine.isRunning)
        machine.snapTo(open = false)
        assertEquals(DialogFlipPhase.Closed, machine.phase)
    }

    private fun assertFramesClose(expected: DialogFlipFrame, actual: DialogFlipFrame) {
        val tolerance = 1e-3f
        assertEquals(expected.cardLift, actual.cardLift, tolerance, "cardLift")
        assertEquals(expected.cardTurnDegrees, actual.cardTurnDegrees, 0.05f, "cardTurn")
        assertEquals(expected.cardAlpha, actual.cardAlpha, tolerance, "cardAlpha")
        assertEquals(expected.scrimAlpha, actual.scrimAlpha, tolerance, "scrim")
        assertEquals(expected.dialogTurnDegrees, actual.dialogTurnDegrees, 0.05f, "dialogTurn")
        assertEquals(expected.dialogGrow, actual.dialogGrow, tolerance, "grow")
        assertEquals(expected.dialogAlpha, actual.dialogAlpha, tolerance, "dialogAlpha")
        assertEquals(expected.contentAlpha, actual.contentAlpha, tolerance, "content")
    }
}
