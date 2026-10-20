package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.talhabayhan.lexi.core.designsystem.icon.LexiIcons
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.descenderOverflowPx
import dev.talhabayhan.lexi.core.designsystem.theme.resized
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/** Wheel geometry from `04-game.html` / `source/04-game.dc.html` for the 320dp wheel; everything scales with size. */
public object LetterWheelDefaults {
    /** 320: wheel box; `min(320, screenWidth − 40)` on narrow phones, 280 on short ones (`screen-map.md`). */
    public val Size: Dp = 320.dp
    internal const val DESIGN_SIZE: Float = 320f
    internal const val RING_INSET: Float = 16f
    internal const val CHIP_RADIUS: Float = 144f
    internal const val CENTER_SIZE: Float = 180f
}

/**
 * Passaparola wheel: N [letters] on a circle (angle = −90° + i·360°/N, radius 144 of 320), a 288dp ring (3dp) and the
 * 180dp yellow centre with the big current letter and the rule label ("İLE BAŞLAR" / "İÇİNDE GEÇER"). The letter's
 * line box is tighter than the font (line-height .9), so the space its descender (J, Q, Y, G, P) hangs into is reserved
 * under it and never overlaps the label. When large font sizes make that stack (letter, reserve, label) too big for the
 * centre, i.e. its bounding box's corners leave the circle, letter and label shrink together until they touch it.
 *
 * Motion (`motion.md`): #6 the black current square jumps with the state (a step, no easing); the centre letter slides
 * down and the next one follows from above (`wheelAdvanceDurationMillis`, no fade; in place under reduced motion).
 * [chipMotion] gives a chip's transient #3 / #5 motion; a moving chip is drawn above its neighbours and its #5 arrow
 * slides clockwise along the wheel, towards the next letter.
 */
@Composable
public fun LetterWheel(
    letters: List<WheelLetterUi>,
    centerLetter: Char,
    ruleLabel: String,
    modifier: Modifier = Modifier,
    size: Dp = LetterWheelDefaults.Size,
    chipMotion: (Char) -> WheelChipMotion? = { null },
) {
    val colors = LexiTheme.colors
    val motion = LexiTheme.motion
    val scale = size.value / LetterWheelDefaults.DESIGN_SIZE
    val chip = WheelLetterDefaults.Size * scale
    val radius = LetterWheelDefaults.CHIP_RADIUS * scale
    val half = size.value / 2
    val typography = LexiTheme.typography

    // Smaller wheels (narrow / short phones) scale their texts with the geometry.
    fun TextStyle.scaled(): TextStyle = if (scale < 1f) resized(fontSize * scale) else this
    val chipStyle = typography.wheelChip.scaled()
    val centerSize = (LetterWheelDefaults.CENTER_SIZE * scale).dp
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val letterBase = typography.wheelLetter.scaled()
    val ruleBase = typography.wheelRule.scaled()
    val fit = remember(measurer, density, centerLetter, ruleLabel, letterBase, ruleBase, centerSize) {
        with(density) {
            val letter = measurer.measure(centerLetter.toString(), letterBase).size
            val rule = measurer.measure(ruleLabel, ruleBase).size
            val width = maxOf(letter.width, rule.width).toFloat()
            val height = letter.height + letterBase.descenderOverflowPx(density) + rule.height
            minOf(1f, centerSize.toPx() / hypot(width, height))
        }
    }

    // Shrunk in px, then back to sp: non-linear font scaling would not shrink every size by the same factor.
    fun TextStyle.fitted(): TextStyle =
        if (fit < 1f) resized(with(density) { (fontSize.toPx() * fit).toSp() }) else this
    val letterStyle = letterBase.fitted()
    val ruleStyle = ruleBase.fitted()
    val descenderReserve = with(density) { letterStyle.descenderOverflowPx(density).toDp() }
    val chipLabels = rememberWheelLetterLabels()
    Box(modifier.size(size)) {
        Box(
            Modifier
                .padding((LetterWheelDefaults.RING_INSET * scale).dp)
                .size(size - (2 * LetterWheelDefaults.RING_INSET * scale).dp)
                .border(LexiTheme.borders.regular, colors.ink, LexiTheme.shapes.circle),
        )
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .size(centerSize)
                .semantics(mergeDescendants = true) { traversalIndex = -1f }
                .background(colors.yellow, LexiTheme.shapes.circle)
                .border(LexiTheme.borders.regular, colors.ink, LexiTheme.shapes.circle),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            AnimatedContent(
                targetState = centerLetter,
                transitionSpec = {
                    // A blank initial letter is the pre-game state: the first letter appears in place.
                    if (motion.reducedMotion || initialState == ' ') {
                        EnterTransition.None togetherWith ExitTransition.None
                    } else {
                        val slide = tween<IntOffset>(motion.wheelAdvanceDurationMillis, easing = motion.mechanical)
                        slideInVertically(slide) { -it } togetherWith slideOutVertically(slide) { it }
                    }
                },
                contentAlignment = Alignment.Center,
            ) { letter ->
                Text(letter.toString(), style = letterStyle, modifier = Modifier.padding(bottom = descenderReserve))
            }
            Text(ruleLabel, style = ruleStyle, maxLines = 1)
        }
        letters.forEachIndexed { i, item ->
            val angle = -PI / 2 + i * 2 * PI / letters.size
            val centerX = half + radius * cos(angle).toFloat()
            val centerY = half + radius * sin(angle).toFloat()
            val itemMotion = chipMotion(item.letter)
            if (itemMotion != null) {
                // Under the chips: the arrow comes out from under the passed chip.
                PassArrow(itemMotion, centerX, centerY, tangent = angle + PI / 2, scale = scale)
            }
            WheelLetter(
                letter = item.letter,
                state = item.state,
                size = chip,
                textStyle = chipStyle,
                motion = itemMotion,
                labels = chipLabels,
                modifier = Modifier
                    .offset((centerX - chip.value / 2).dp, (centerY - chip.value / 2).dp)
                    .zIndex(if (itemMotion != null) 1f else 0f),
            )
        }
    }
}

/** #5: the arrow centred on the chip, turned to [tangent] (radians) and moved along it by the arrow progress. */
@Composable
private fun PassArrow(motion: WheelChipMotion, centerX: Float, centerY: Float, tangent: Double, scale: Float) {
    val width = WheelLetterDefaults.ArrowWidth * scale
    val height = WheelLetterDefaults.ArrowHeight * scale
    val distance = LexiTheme.motion.passArrowDistance * scale
    val dx = cos(tangent).toFloat()
    val dy = sin(tangent).toFloat()
    Icon(
        imageVector = LexiIcons.ArrowRight,
        contentDescription = null,
        tint = LexiTheme.colors.ink,
        modifier = Modifier
            .offset((centerX - width.value / 2).dp, (centerY - height.value / 2).dp)
            .size(width, height)
            .zIndex(1f)
            .graphicsLayer {
                val travelled = distance.toPx() * motion.arrowProgress
                alpha = motion.arrowAlpha.coerceIn(0f, 1f)
                rotationZ = (tangent * DEGREES_PER_RADIAN).toFloat()
                translationX = dx * travelled
                translationY = dy * travelled
            },
    )
}

private const val DEGREES_PER_RADIAN: Double = 180.0 / PI

private val PreviewLetters: List<WheelLetterUi> = ('A'..'Z').mapIndexed { i, c ->
    val state = when {
        i == 2 || i == 7 -> WheelLetterState.Passed
        i == 4 -> WheelLetterState.Wrong
        i < 10 -> WheelLetterState.Correct
        i == 10 -> WheelLetterState.Current
        else -> WheelLetterState.Todo
    }
    WheelLetterUi(c, state)
}

// design sample, not user-facing
@Preview
@Composable
private fun LetterWheelPreview() {
    LexiTheme(reducedMotion = false) {
        Box(Modifier.background(LexiTheme.colors.cream).padding(LexiTheme.dimens.gap20)) {
            LetterWheel(letters = PreviewLetters, centerLetter = 'K', ruleLabel = "İLE BAŞLAR")
        }
    }
}
