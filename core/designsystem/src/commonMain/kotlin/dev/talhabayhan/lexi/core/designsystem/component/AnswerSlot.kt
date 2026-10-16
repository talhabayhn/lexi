package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.generated.resources.Res
import dev.talhabayhan.lexi.core.designsystem.generated.resources.answer_slot_active
import dev.talhabayhan.lexi.core.designsystem.generated.resources.answer_slot_empty
import dev.talhabayhan.lexi.core.designsystem.generated.resources.answer_slot_joker
import dev.talhabayhan.lexi.core.designsystem.generated.resources.wheel_letter_description
import dev.talhabayhan.lexi.core.designsystem.generated.resources.wheel_letter_state_wrong
import dev.talhabayhan.lexi.core.designsystem.modifier.lexiSurface
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import org.jetbrains.compose.resources.stringResource

/** State of one answer box. */
public enum class AnswerSlotState {
    /** Ink box, cream letter. */
    Filled,

    /** White box with a red border: the next box to fill. */
    Active,

    /** White box with an ink border. */
    Empty,

    /** Red box, white letter (wrong answer reveal). */
    Wrong,

    /** Blue box, white letter (opened by the "Harf aç" joker). */
    JokerFilled,
}

/** Answer box geometry (`04-game.html`). */
public object AnswerSlotDefaults {
    public val Width: Dp = 40.dp
    public val Height: Dp = 48.dp

    /** Gap between slots. */
    public val Gap: Dp = 6.dp
}

/** One answer box (40 × 48dp, [height] 40 in the game's compact tier; letter 26sp/800). Empty states ignore [letter]. */
@Composable
public fun AnswerSlot(
    letter: Char?,
    state: AnswerSlotState,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LexiTheme.typography.slotLetter,
    height: Dp = AnswerSlotDefaults.Height,
) {
    val colors = LexiTheme.colors
    val border = LexiTheme.borders.regular
    val (fill, content, borderStroke) = when (state) {
        AnswerSlotState.Filled -> Triple(colors.ink, colors.cream, null)
        AnswerSlotState.Active -> Triple(colors.white, Color.Unspecified, BorderStroke(border, colors.red))
        AnswerSlotState.Empty -> Triple(colors.white, Color.Unspecified, BorderStroke(border, colors.ink))
        AnswerSlotState.Wrong -> Triple(colors.red, colors.white, null)
        AnswerSlotState.JokerFilled -> Triple(colors.blue, colors.white, null)
    }
    val shown = letter?.takeIf { content != Color.Unspecified }?.toString()
    val description = when (state) {
        AnswerSlotState.Empty -> stringResource(Res.string.answer_slot_empty)

        AnswerSlotState.Active -> stringResource(Res.string.answer_slot_active)

        AnswerSlotState.Filled -> shown.orEmpty()

        AnswerSlotState.Wrong ->
            stringResource(
                Res.string.wheel_letter_description,
                shown.orEmpty(),
                stringResource(Res.string.wheel_letter_state_wrong),
            )

        AnswerSlotState.JokerFilled ->
            stringResource(
                Res.string.wheel_letter_description,
                shown.orEmpty(),
                stringResource(Res.string.answer_slot_joker),
            )
    }
    Box(
        modifier = modifier
            .size(AnswerSlotDefaults.Width, height)
            .clearAndSetSemantics { contentDescription = description }
            .lexiSurface(fill, borderStroke),
        contentAlignment = Alignment.Center,
    ) {
        if (shown != null) Text(shown, style = textStyle, color = content)
    }
}

// design sample, not user-facing
@Preview
@Composable
private fun AnswerSlotPreview() {
    LexiTheme(reducedMotion = false) {
        Row(
            modifier = Modifier.background(LexiTheme.colors.cream).padding(LexiTheme.dimens.gap20),
            horizontalArrangement = Arrangement.spacedBy(AnswerSlotDefaults.Gap),
        ) {
            AnswerSlot('K', AnswerSlotState.Filled)
            AnswerSlot(null, AnswerSlotState.Active)
            AnswerSlot(null, AnswerSlotState.Empty)
            AnswerSlot('S', AnswerSlotState.Wrong)
            AnswerSlot('N', AnswerSlotState.JokerFilled)
        }
    }
}
