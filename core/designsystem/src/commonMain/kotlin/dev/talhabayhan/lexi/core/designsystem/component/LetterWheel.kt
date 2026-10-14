package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import kotlin.math.PI
import kotlin.math.cos
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
 * 180dp yellow centre with the big current letter and the rule label ("İLE BAŞLAR" / "İÇİNDE GEÇER").
 * Motion (#3, #5, #6) is driven by the game screen in M4.
 */
@Composable
public fun LetterWheel(
    letters: List<WheelLetterUi>,
    centerLetter: Char,
    ruleLabel: String,
    modifier: Modifier = Modifier,
    size: Dp = LetterWheelDefaults.Size,
) {
    val colors = LexiTheme.colors
    val scale = size.value / LetterWheelDefaults.DESIGN_SIZE
    val chip = WheelLetterDefaults.Size * scale
    val radius = LetterWheelDefaults.CHIP_RADIUS * scale
    val half = size.value / 2
    Box(modifier.size(size)) {
        Box(
            Modifier
                .padding((LetterWheelDefaults.RING_INSET * scale).dp)
                .size(size - (2 * LetterWheelDefaults.RING_INSET * scale).dp)
                .border(LexiTheme.borders.regular, colors.ink, LexiTheme.shapes.circle),
        )
        val centerSize = (LetterWheelDefaults.CENTER_SIZE * scale).dp
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
            Text(centerLetter.toString(), style = LexiTheme.typography.wheelLetter)
            Text(ruleLabel, style = LexiTheme.typography.wheelRule)
        }
        letters.forEachIndexed { i, item ->
            val angle = -PI / 2 + i * 2 * PI / letters.size
            val x = half + radius * cos(angle).toFloat() - chip.value / 2
            val y = half + radius * sin(angle).toFloat() - chip.value / 2
            WheelLetter(
                letter = item.letter,
                state = item.state,
                size = chip,
                modifier = Modifier.offset(x.dp, y.dp),
            )
        }
    }
}

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
