package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.modifier.dashedBorder
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme

/** State of a letter key. Pressed (yellow) is not a state: it follows the touch. */
public enum class LetterKeyState {
    /** White key, s3 shadow. */
    Normal,

    /** Letter already placed: dashed `borderMuted`, muted letter, not tappable. */
    Used,

    /** Removed by the "Çeldiricileri temizle" joker: dashed `gridLine`, empty, not tappable. */
    RemovedByJoker,
}

/** Key geometry (`04-game.html`: 5-column grid, gap 8). */
public object LetterKeyDefaults {
    public val Height: Dp = 50.dp
    public val GridGap: Dp = 8.dp
    public const val COLUMNS: Int = 5
}

/**
 * A letter key: 50dp high ([height]; the game's compact tier uses 44), 3dp border, s3 shadow, 22sp/800; turns yellow while pressed. Width from [modifier].
 * A Normal key that is not [enabled] (input blocked, e.g. during a wrong-answer reveal) is drawn at 50 % opacity
 * without shadow, like the joker sheet's disabled rows (no design of its own).
 */
@Composable
public fun LetterKey(
    letter: Char,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    state: LetterKeyState = LetterKeyState.Normal,
    enabled: Boolean = true,
    height: Dp = LetterKeyDefaults.Height,
) {
    val colors = LexiTheme.colors
    val border = LexiTheme.borders.regular
    when (state) {
        LetterKeyState.Normal -> {
            val source = remember { MutableInteractionSource() }
            val pressed = source.isPressedOrForced()
            PressableSurface(
                onClick = onClick,
                modifier = modifier
                    .height(height)
                    .alpha(if (enabled) 1f else JokerRowDefaults.DISABLED_ALPHA),
                enabled = enabled,
                color = if (pressed && enabled) colors.yellow else colors.white,
                border = BorderStroke(border, colors.ink),
                shadow = if (enabled) LexiTheme.shadows.s3 else null,
                interactionSource = source,
            ) {
                Text(letter.toString(), style = LexiTheme.typography.key)
            }
        }

        LetterKeyState.Used -> Box(
            modifier = modifier
                .height(height)
                .semantics {
                    role = Role.Button
                    disabled()
                }
                .dashedBorder(border, colors.borderMuted),
            contentAlignment = Alignment.Center,
        ) {
            Text(letter.toString(), style = LexiTheme.typography.key, color = colors.borderMuted)
        }

        LetterKeyState.RemovedByJoker -> Box(
            modifier = modifier
                .height(height)
                .clearAndSetSemantics {}
                .dashedBorder(border, colors.gridLine),
        )
    }
}

// design sample, not user-facing
@Preview
@Composable
private fun LetterKeyPreview() {
    LexiTheme(reducedMotion = false) {
        Row(
            modifier = Modifier.background(LexiTheme.colors.cream).padding(LexiTheme.dimens.gap20),
            horizontalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap14),
        ) {
            val keyWidth = Modifier.width(LetterKeyDefaults.Height)
            LetterKey('E', onClick = {}, modifier = keyWidth)
            LetterKey('E', onClick = {}, modifier = keyWidth, state = LetterKeyState.Used)
            LetterKey('E', onClick = {}, modifier = keyWidth, state = LetterKeyState.RemovedByJoker)
        }
    }
}
