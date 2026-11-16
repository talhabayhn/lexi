package dev.talhabayhan.lexi.core.designsystem.motion

import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import dev.talhabayhan.lexi.core.designsystem.theme.LexiMotion
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LetterMorphTimelineTest {
    private val flow = LexiMotion(reducedMotion = false).flow
    private val red = Color(0xFFE0534C)
    private val ink = Color(0xFF1F242D)
    private val source = Rect(20f, 700f, 370f, 780f)
    private val screen = Rect(0f, 0f, 390f, 844f)
    private val target = Rect(177f, 120f, 213f, 156f)

    /** Linear easings make the expected values easy to compute. */
    private val linear = LetterMorphTimeline(flow, LinearEasing, LinearEasing, red, ink)
    private val real = LexiMotion(reducedMotion = false).let {
        LetterMorphTimeline(flow, it.mechanical, it.pop, red, ink)
    }

    @Test
    fun `nothing is drawn while the button press plays`() {
        val frame = linear.frameAt(100, source, screen, target, shrinkStartMillis = null)

        assertFalse(frame.visible)
    }

    @Test
    fun `the ink starts on the button rect in red`() {
        val frame = linear.frameAt(flow.inkExpand.delayMillis.toLong(), source, screen, target, null)

        assertTrue(frame.visible)
        assertEquals(source, frame.inkRect)
        assertEquals(red, frame.inkColor)
        assertEquals(0f, frame.letterAlpha)
    }

    @Test
    fun `half way through the expand the rect is between button and screen`() {
        val t = flow.inkExpand.delayMillis + flow.inkExpand.durationMillis / 2.0
        val frame = linear.frameAt(t.toLong(), source, screen, target, null)

        assertEquals(lerpRect(source, screen, flow.inkExpand.progressAt(t.toLong())), frame.inkRect)
    }

    @Test
    fun `after the expand the ink covers the screen in ink and the letter is in at full size`() {
        val frame = real.frameAt(flow.inkShrink.delayMillis.toLong(), source, screen, target, null)

        assertEquals(screen, frame.inkRect)
        assertEquals(ink, frame.inkColor)
        assertEquals(1f, frame.letterAlpha)
        assertEquals(1f, frame.letterScale)
    }

    @Test
    fun `the letter pops in from 0_85`() {
        val frame = linear.frameAt(flow.letterIn.delayMillis.toLong(), source, screen, target, null)

        assertEquals(flow.letterStartScale, frame.letterScale)
    }

    @Test
    fun `without a shrink start the ink holds the full screen`() {
        val frame = real.frameAt(5_000, source, screen, target, shrinkStartMillis = null)

        assertTrue(frame.visible)
        assertEquals(screen, frame.inkRect)
        assertFalse(real.isFinished(5_000, null))
    }

    @Test
    fun `the shrink ends exactly on the marker with the letter at its final size`() {
        val start = 900L
        val end = start + flow.inkShrink.durationMillis
        val frame = real.frameAt(end, source, screen, target, start)

        assertEquals(target, frame.inkRect)
        assertEquals(flow.letterEndSize / flow.letterStartSize, frame.letterScale, absoluteTolerance = 1e-4f)
        assertTrue(real.isFinished(end, start))
        assertFalse(real.isFinished(end - 1, start))
    }

    @Test
    fun `half way through the shrink the rect is between screen and marker`() {
        val start = flow.inkShrink.delayMillis.toLong()
        val half = flow.inkShrink.durationMillis / 2
        val frame = linear.frameAt(start + half, source, screen, target, start)

        val expected = lerpRect(screen, target, half.toFloat() / flow.inkShrink.durationMillis)
        assertEquals(expected.left, frame.inkRect.left, absoluteTolerance = 0.01f)
        assertEquals(expected.bottom, frame.inkRect.bottom, absoluteTolerance = 0.01f)
    }

    @Test
    fun `the shrink never starts before its earliest time`() {
        assertEquals(flow.inkShrink.delayMillis.toLong(), linear.shrinkStartFor(readyAtMillis = 0))
        assertEquals(1_500L, linear.shrinkStartFor(readyAtMillis = 1_500))
    }

    @Test
    fun `alpha of an overshooting pop is clamped`() {
        val t = flow.letterIn.delayMillis + flow.letterIn.durationMillis / 2L
        val frame = real.frameAt(t, source, screen, target, null)

        assertTrue(frame.letterAlpha in 0f..1f)
    }
}
