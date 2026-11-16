package dev.talhabayhan.lexi.core.designsystem.motion

import dev.talhabayhan.lexi.core.designsystem.theme.LexiMotion
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** The catalogue values of `docs/design/motion.md` #13–#17 (prototype ÷ 0.86). */
class FlowMotionTest {
    private val flow = LexiMotion(reducedMotion = false).flow

    @Test
    fun `card lift is 140 ms and the flip open steps follow the prototype`() {
        assertEquals(MotionStep(0, 140), flow.cardLift)
        assertEquals(MotionStep(116, 186), flow.cardTurnAway)
        assertEquals(MotionStep(291, 233), flow.dialogFlipIn)
        assertEquals(MotionStep(291, 349), flow.dialogGrow)
        assertEquals(MotionStep(465, 163), flow.dialogContentIn)
        assertEquals(41, flow.dialogItemStaggerMillis)
    }

    @Test
    fun `close collapses in 279 ms and the card lands with a pop`() {
        assertEquals(MotionStep(0, 105), flow.dialogContentOut)
        assertEquals(MotionStep(70, 279), flow.dialogCollapse)
        assertEquals(MotionStep(326, 209), flow.cardTurnBack)
        assertEquals(MotionStep(488, 209), flow.cardLand)
    }

    @Test
    fun `letter morph lasts about 0_93 s from expand to the end of the shrink`() {
        assertEquals(930, flow.letterMorphMillis)
        assertEquals(MotionStep(186, 349), flow.inkExpand)
        assertEquals(MotionStep(767, 349), flow.inkShrink)
        assertTrue(flow.letterIn.endMillis < flow.inkShrink.delayMillis, "letter must land before the shrink")
    }

    @Test
    fun `board entrance staggers are 9 35 and 23 ms`() {
        assertEquals(9, flow.boardRingChipStaggerMillis)
        assertEquals(35, flow.boardSlotStaggerMillis)
        assertEquals(23, flow.boardKeyStaggerMillis)
        assertEquals(907, flow.boardSettledMillis)
    }

    @Test
    fun `reduced motion is a 120 ms cross-fade`() {
        assertEquals(120, flow.reducedCrossfadeMillis)
    }
}
