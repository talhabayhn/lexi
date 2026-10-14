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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.generated.resources.Res
import dev.talhabayhan.lexi.core.designsystem.generated.resources.wheel_letter_description
import dev.talhabayhan.lexi.core.designsystem.generated.resources.wheel_letter_state_correct
import dev.talhabayhan.lexi.core.designsystem.generated.resources.wheel_letter_state_current
import dev.talhabayhan.lexi.core.designsystem.generated.resources.wheel_letter_state_passed
import dev.talhabayhan.lexi.core.designsystem.generated.resources.wheel_letter_state_todo
import dev.talhabayhan.lexi.core.designsystem.generated.resources.wheel_letter_state_wrong
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import org.jetbrains.compose.resources.StringResource
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

/** Wheel chip geometry (`04-game.html`). */
public object WheelLetterDefaults {
    /** 28: chip on the wheel. */
    public val Size: Dp = 28.dp

    /** Scale of the current letter on the wheel. */
    public const val CURRENT_SCALE: Float = 1.3f
}

/**
 * A wheel chip: 28dp, 2dp ink border, 13sp/800 letter; shape and color per [state] (`tokens.md` § Colors: correct =
 * circle, wrong = square, pass = leaf, current = black square). Announced as "K, doğru".
 *
 * @param scaleCurrent draw the [WheelLetterState.Current] chip at 1.3× (on the wheel; not in legends).
 */
@Composable
public fun WheelLetter(
    letter: Char,
    state: WheelLetterState,
    modifier: Modifier = Modifier,
    size: Dp = WheelLetterDefaults.Size,
    textStyle: TextStyle = LexiTheme.typography.wheelChip,
    scaleCurrent: Boolean = true,
) {
    val look = state.look()
    val description = stringResource(Res.string.wheel_letter_description, letter.toString(), stringResource(look.label))
    val scale = if (state == WheelLetterState.Current && scaleCurrent) WheelLetterDefaults.CURRENT_SCALE else 1f
    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clearAndSetSemantics { contentDescription = description }
            .background(look.fill, look.shape)
            .border(LexiTheme.borders.thin, LexiTheme.colors.ink, look.shape),
        contentAlignment = Alignment.Center,
    ) {
        Text(letter.toString(), style = textStyle, color = look.content)
    }
}

private class WheelLetterLook(val fill: Color, val content: Color, val shape: Shape, val label: StringResource)

@Composable
private fun WheelLetterState.look(): WheelLetterLook {
    val colors = LexiTheme.colors
    val shapes = LexiTheme.shapes
    return when (this) {
        WheelLetterState.Todo -> WheelLetterLook(
            colors.cream,
            colors.ink,
            shapes.circle,
            Res.string.wheel_letter_state_todo,
        )

        WheelLetterState.Current ->
            WheelLetterLook(colors.ink, colors.yellow, shapes.square, Res.string.wheel_letter_state_current)

        WheelLetterState.Correct ->
            WheelLetterLook(colors.blue, colors.white, shapes.circle, Res.string.wheel_letter_state_correct)

        WheelLetterState.Wrong -> WheelLetterLook(
            colors.red,
            colors.white,
            shapes.square,
            Res.string.wheel_letter_state_wrong,
        )

        WheelLetterState.Passed ->
            WheelLetterLook(colors.yellow, colors.ink, shapes.leaf, Res.string.wheel_letter_state_passed)
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
