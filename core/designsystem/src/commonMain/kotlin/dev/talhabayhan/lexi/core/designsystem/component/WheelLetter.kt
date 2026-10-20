package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.generated.resources.Res
import dev.talhabayhan.lexi.core.designsystem.generated.resources.wheel_letter_description
import dev.talhabayhan.lexi.core.designsystem.generated.resources.wheel_letter_state_correct
import dev.talhabayhan.lexi.core.designsystem.generated.resources.wheel_letter_state_current
import dev.talhabayhan.lexi.core.designsystem.generated.resources.wheel_letter_state_passed
import dev.talhabayhan.lexi.core.designsystem.generated.resources.wheel_letter_state_todo
import dev.talhabayhan.lexi.core.designsystem.generated.resources.wheel_letter_state_wrong
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import org.jetbrains.compose.resources.stringResource

/** State of one letter on the wheel; never color-only (shape carries it too). */
public enum class WheelLetterState {
    /** Cream circle. */
    Todo,

    /** Black square with yellow letter, scaled 1.3 on the wheel. */
    Current,

    /** Blue circle. */
    Correct,

    /** Red square. */
    Wrong,

    /** Yellow leaf. */
    Passed,
}

/** One letter of the wheel with its state. */
@Immutable
public data class WheelLetterUi(val letter: Char, val state: WheelLetterState)

/**
 * The chips' accessibility texts ("K, doğru"), resolved once for a whole wheel ([rememberWheelLetterLabels]) instead of
 * two string lookups per chip. [describe] is exactly `wheel_letter_description` formatted with letter and state label.
 */
@Immutable
public class WheelLetterLabels internal constructor(
    private val template: String,
    private val states: Map<WheelLetterState, String>,
) {
    public fun describe(letter: Char, state: WheelLetterState): String =
        template.replace(LETTER_ARG, letter.toString()).replace(STATE_ARG, states.getValue(state))

    private companion object {
        const val LETTER_ARG = "%1\$s"
        const val STATE_ARG = "%2\$s"
    }
}

/** [WheelLetterLabels] in the current language. */
@Composable
public fun rememberWheelLetterLabels(): WheelLetterLabels {
    val template = stringResource(Res.string.wheel_letter_description)
    val todo = stringResource(Res.string.wheel_letter_state_todo)
    val current = stringResource(Res.string.wheel_letter_state_current)
    val correct = stringResource(Res.string.wheel_letter_state_correct)
    val wrong = stringResource(Res.string.wheel_letter_state_wrong)
    val passed = stringResource(Res.string.wheel_letter_state_passed)
    return remember(template, todo, current, correct, wrong, passed) {
        WheelLetterLabels(
            template,
            mapOf(
                WheelLetterState.Todo to todo,
                WheelLetterState.Current to current,
                WheelLetterState.Correct to correct,
                WheelLetterState.Wrong to wrong,
                WheelLetterState.Passed to passed,
            ),
        )
    }
}

/** Wheel chip geometry (`04-game.html`). */
public object WheelLetterDefaults {
    /** 28: chip on the wheel. */
    public val Size: Dp = 28.dp

    /** Scale of the current letter on the wheel. */
    public const val CURRENT_SCALE: Float = 1.3f

    /** #3 ring: 4dp blue stroke at the 28dp chip size (`15-motion`, `aRing`). */
    public val RingWidth: Dp = 4.dp

    /** #3 ring diameter at scale 1 relative to the chip (70 on the 56 demo chip). */
    public const val RING_SIZE_RATIO: Float = 70f / 56f

    /** #5 arrow (`LexiIcons.ArrowRight`, 24 x 16 at the 28dp chip size). */
    public val ArrowWidth: Dp = 24.dp
    public val ArrowHeight: Dp = 16.dp
}

/**
 * Transient motion of one chip (`motion.md` #3, #5), read in the layer / draw phase only. The game screen drives it;
 * at the neutral values (scale 1, no ring, pass done, no arrow) the chip shows its resting look from [WheelLetterState].
 */
@Stable
public interface WheelChipMotion {
    /** #3 pop: multiplies the chip's resting scale. */
    public val popScale: Float

    /** #3 ring scale (times the ring base size); no ring at [ringAlpha] 0. */
    public val ringScale: Float

    /** #3 ring opacity. */
    public val ringAlpha: Float

    /**
     * #5 pass, 0..1: the chip shape turns clockwise by `LexiMotion.passRotationDegrees` while it morphs from a circle
     * into a leaf; at 1 the turned shape is exactly the resting leaf. The letter stays upright.
     */
    public val passProgress: Float

    /** #5 arrow: travelled part of `LexiMotion.passArrowDistance`, 0..1 (drawn by `LetterWheel`). */
    public val arrowProgress: Float

    /** #5 arrow opacity; no arrow at 0. */
    public val arrowAlpha: Float
}

/**
 * A wheel chip: 28dp, 2dp ink border, 13sp/800 letter; shape and color per [state] (`tokens.md` § Colors: correct =
 * circle, wrong = square, pass = leaf, current = black square). Announced as "K, doğru".
 *
 * @param scaleCurrent draw the [WheelLetterState.Current] chip at 1.3× (on the wheel; not in legends).
 * @param motion transient #3 pop + ring and #5 turn + morph, or null at rest.
 * @param labels the accessibility texts, resolved once by a wheel; null resolves them here.
 */
@Composable
public fun WheelLetter(
    letter: Char,
    state: WheelLetterState,
    modifier: Modifier = Modifier,
    size: Dp = WheelLetterDefaults.Size,
    textStyle: TextStyle = LexiTheme.typography.wheelChip,
    scaleCurrent: Boolean = true,
    motion: WheelChipMotion? = null,
    labels: WheelLetterLabels? = null,
) {
    val look = state.look()
    val resolvedLabels = labels ?: rememberWheelLetterLabels()
    val scale = if (state == WheelLetterState.Current && scaleCurrent) WheelLetterDefaults.CURRENT_SCALE else 1f
    // A layer only where it scales (the current chip) or a motion runs; resting chips draw without one.
    val scaleLayer = if (scale != 1f || motion != null) {
        Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    } else {
        Modifier
    }
    val border = LexiTheme.borders.thin
    val ink = LexiTheme.colors.ink
    val surface = if (motion == null) {
        Modifier.background(look.fill, look.shape).border(border, ink, look.shape)
    } else {
        val ringColor = LexiTheme.colors.blue
        val rotation = LexiTheme.motion.passRotationDegrees
        val chipScale = size / WheelLetterDefaults.Size
        // The ring is drawn outside the chip's pop layer, so the pop does not scale it (a sibling in `15-motion`).
        Modifier
            .drawBehind { drawCorrectRing(motion, ringColor, chipScale) }
            .graphicsLayer {
                scaleX = motion.popScale
                scaleY = motion.popScale
            }
            .drawBehind { drawPassingChip(motion.passProgress, rotation, look, border.toPx(), ink) }
    }
    Box(
        modifier = modifier
            .size(size)
            .then(scaleLayer)
            .clearAndSetSemantics { contentDescription = resolvedLabels.describe(letter, state) }
            .then(surface),
        contentAlignment = Alignment.Center,
    ) {
        Text(letter.toString(), style = textStyle, color = look.content)
    }
}

private class WheelLetterLook(val fill: Color, val content: Color, val shape: Shape)

/** #3: the blue ring around the chip centre (stroke inside its diameter, like the CSS border). */
private fun DrawScope.drawCorrectRing(motion: WheelChipMotion, color: Color, chipScale: Float) {
    val alpha = motion.ringAlpha.coerceIn(0f, 1f)
    if (alpha <= 0f) return
    val stroke = WheelLetterDefaults.RingWidth.toPx() * chipScale
    val diameter = size.minDimension * WheelLetterDefaults.RING_SIZE_RATIO * motion.ringScale
    drawCircle(color = color, radius = (diameter - stroke) / 2, center = center, alpha = alpha, style = Stroke(stroke))
}

/**
 * The chip surface with motion: during #5 it is turned by [rotationDegrees] x [progress] while its outline goes from a
 * circle to the leaf turned back by [rotationDegrees] (for 90°: rounded top-right and bottom-left), so at [progress] 1
 * it is the resting leaf. Otherwise the resting shape is drawn unturned.
 */
private fun DrawScope.drawPassingChip(
    progress: Float,
    rotationDegrees: Float,
    look: WheelLetterLook,
    borderPx: Float,
    ink: Color,
) {
    if (progress >= 1f) {
        drawSurface(look.shape, look.fill, borderPx, ink)
        return
    }
    val p = progress.coerceIn(0f, 1f)
    rotate(rotationDegrees * p) { drawSurface(PassMorphShape(p), look.fill, borderPx, ink) }
}

/** Fill plus an inside border of [borderPx], as `background(shape)` + `border(shape)`. */
private fun DrawScope.drawSurface(shape: Shape, fill: Color, borderPx: Float, ink: Color) {
    drawOutline(shape.createOutline(size, layoutDirection, this), fill)
    val inner = shape.createOutline(Size(size.width - borderPx, size.height - borderPx), layoutDirection, this)
    translate(borderPx / 2, borderPx / 2) { drawOutline(inner, ink, style = Stroke(borderPx)) }
}

/** #5 outline at [progress]: the top-left and bottom-right corners go from round to square (CSS `50%` radii). */
private class PassMorphShape(private val progress: Float) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val round = CornerRadius(size.width / 2, size.height / 2)
        val closing = CornerRadius(round.x * (1f - progress), round.y * (1f - progress))
        return Outline.Rounded(
            RoundRect(
                rect = Rect(Offset.Zero, size),
                topLeft = closing,
                topRight = round,
                bottomRight = closing,
                bottomLeft = round,
            ),
        )
    }
}

@Composable
private fun WheelLetterState.look(): WheelLetterLook {
    val colors = LexiTheme.colors
    val shapes = LexiTheme.shapes
    return when (this) {
        WheelLetterState.Todo -> WheelLetterLook(colors.cream, colors.ink, shapes.circle)
        WheelLetterState.Current -> WheelLetterLook(colors.ink, colors.yellow, shapes.square)
        WheelLetterState.Correct -> WheelLetterLook(colors.blue, colors.white, shapes.circle)
        WheelLetterState.Wrong -> WheelLetterLook(colors.red, colors.white, shapes.square)
        WheelLetterState.Passed -> WheelLetterLook(colors.yellow, colors.ink, shapes.leaf)
    }
}

@Preview
@Composable
private fun WheelLetterPreview() {
    LexiTheme(reducedMotion = false) {
        Row(
            modifier = Modifier.background(LexiTheme.colors.cream).padding(LexiTheme.dimens.gap20),
            horizontalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap14),
        ) {
            WheelLetterState.entries.forEachIndexed { i, state -> WheelLetter('A' + i, state) }
        }
    }
}
