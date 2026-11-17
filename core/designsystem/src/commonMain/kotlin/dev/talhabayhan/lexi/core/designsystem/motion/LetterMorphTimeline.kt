package dev.talhabayhan.lexi.core.designsystem.motion

import androidx.compose.animation.core.Easing
import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color

/** What the letter-morph overlay draws at one moment (rects in the overlay's coordinates). */
@Immutable
public data class LetterMorphFrame(
    /** False while the button's own press plays and after the ink has landed on the marker. */
    val visible: Boolean,
    val inkRect: Rect,
    val inkColor: Color,
    /** Scale of the big letter, drawn once at [FlowMotion.letterStartSize]. */
    val letterScale: Float,
    val letterAlpha: Float,
    /** Opacity of the whole overlay: 1, fading out when the morph is cancelled. */
    val overlayAlpha: Float = 1f,
)

/**
 * `motion.md` #16 as a pure function of time (ms since the OYNA tap): expand button → screen, red → ink, letter pop,
 * hold, then shrink screen → marker once [shrinkStartFor] says so. The shrink waits for the game: until the caller has
 * a shrink start the ink holds the full screen with the letter.
 */
public class LetterMorphTimeline(
    private val flow: FlowMotion,
    private val mechanical: Easing,
    private val pop: Easing,
    private val startColor: Color,
    private val endColor: Color,
) {
    /** When the shrink starts if the game (data + marker position) is ready at [readyAtMillis]. */
    public fun shrinkStartFor(readyAtMillis: Long): Long = maxOf(readyAtMillis, flow.inkShrink.delayMillis.toLong())

    public fun isFinished(elapsedMillis: Long, shrinkStartMillis: Long?): Boolean =
        shrinkStartMillis != null && elapsedMillis >= shrinkStartMillis + flow.inkShrink.durationMillis

    /**
     * [letterPop] false (tier Lite): the letter only fades in, at full size. [pulse] multiplies the letter's scale
     * ([LetterMorphMachine.pulseScale]); [overlayAlpha] fades everything ([LetterMorphMachine.overlayAlpha]).
     */
    public fun frameAt(
        elapsedMillis: Long,
        source: Rect,
        screen: Rect,
        target: Rect?,
        shrinkStartMillis: Long?,
        letterPop: Boolean = true,
        pulse: Float = 1f,
        overlayAlpha: Float = 1f,
    ): LetterMorphFrame {
        val expand = mechanical.transform(flow.inkExpand.progressAt(elapsedMillis))
        val letterProgress = flow.letterIn.progressAt(elapsedMillis)
        val letterIn = if (letterPop) pop.transform(letterProgress) else letterProgress
        val startScale = if (letterPop) flow.letterStartScale else 1f
        val shrinkProgress = if (shrinkStartMillis == null || target == null) {
            0f
        } else {
            val step = flow.inkShrink.copy(delayMillis = 0)
            mechanical.transform(step.progressAt(elapsedMillis - shrinkStartMillis))
        }
        val expanded = lerpRect(source, screen, expand)
        val endRatio = flow.letterEndSize / flow.letterStartSize
        val letterScale = (startScale + (1f - startScale) * letterIn) *
            (1f + (endRatio - 1f) * shrinkProgress) * pulse
        return LetterMorphFrame(
            visible = elapsedMillis >= flow.inkExpand.delayMillis && !isFinished(elapsedMillis, shrinkStartMillis),
            inkRect = if (target == null) expanded else lerpRect(expanded, target, shrinkProgress),
            inkColor = lerpSrgb(startColor, endColor, flow.inkColor.progressAt(elapsedMillis)),
            letterScale = letterScale,
            letterAlpha = letterIn.coerceIn(0f, 1f),
            overlayAlpha = overlayAlpha,
        )
    }
}
