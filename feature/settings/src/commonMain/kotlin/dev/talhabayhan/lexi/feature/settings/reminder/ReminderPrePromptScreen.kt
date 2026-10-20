package dev.talhabayhan.lexi.feature.settings.reminder

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import dev.talhabayhan.lexi.core.designsystem.component.DecorShape
import dev.talhabayhan.lexi.core.designsystem.component.DecorShapeKind
import dev.talhabayhan.lexi.core.designsystem.component.LexiButton
import dev.talhabayhan.lexi.core.designsystem.component.LexiButtonDefaults
import dev.talhabayhan.lexi.core.designsystem.component.LexiButtonVariant
import dev.talhabayhan.lexi.core.designsystem.component.OverlayDefaults
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.lexiContentWidth
import dev.talhabayhan.lexi.feature.settings.generated.resources.Res
import dev.talhabayhan.lexi.feature.settings.generated.resources.reminder_preprompt_body
import dev.talhabayhan.lexi.feature.settings.generated.resources.reminder_preprompt_no
import dev.talhabayhan.lexi.feature.settings.generated.resources.reminder_preprompt_title
import dev.talhabayhan.lexi.feature.settings.generated.resources.reminder_preprompt_yes
import org.jetbrains.compose.resources.stringResource

/**
 * DESIGN-DEBT (no design; onboarding-style layout from `16-components`): the pre-prompt before the OS notification
 * dialog (`COPY.md` §1 `reminder_preprompt_*`). Three Bauhaus shapes, title, body, "Evet, hatırlat" and "Şimdi değil".
 */
@Composable
internal fun ReminderPrePromptScreen(
    @Suppress("UNUSED_PARAMETER") state: ReminderPrePromptUiState,
    onIntent: (ReminderPrePromptIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = LexiTheme.dimens
    val colors = LexiTheme.colors
    val outline = BorderStroke(LexiTheme.borders.thick, colors.ink)
    val shape = Modifier.size(OverlayDefaults.PrePromptShapeSize)
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.cream)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .lexiContentWidth()
            .verticalScroll(rememberScrollState())
            .padding(
                start = dimens.screenPaddingHorizontal,
                top = dimens.screenPaddingTop,
                end = dimens.screenPaddingHorizontal,
                bottom = dimens.screenPaddingBottom,
            ),
        verticalArrangement = Arrangement.spacedBy(dimens.gap20),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(dimens.gap16)) {
            DecorShape(DecorShapeKind.Circle, colors.yellow, shape, outline)
            DecorShape(DecorShapeKind.Square, colors.blue, shape, outline)
            DecorShape(DecorShapeKind.Leaf, colors.red, shape, outline)
        }
        Column(verticalArrangement = Arrangement.spacedBy(dimens.gap10)) {
            Text(
                text = stringResource(Res.string.reminder_preprompt_title),
                style = LexiTheme.typography.display,
                modifier = Modifier.semantics { heading() },
            )
            Text(text = stringResource(Res.string.reminder_preprompt_body), style = LexiTheme.typography.body16)
        }
        Spacer(Modifier.weight(1f))
        Column(verticalArrangement = Arrangement.spacedBy(dimens.gap10)) {
            LexiButton(
                text = stringResource(Res.string.reminder_preprompt_yes),
                onClick = { onIntent(ReminderPrePromptIntent.Accept) },
                textStyle = LexiTheme.typography.button20,
                height = LexiButtonDefaults.HeroHeight,
                shadow = LexiTheme.shadows.s6,
                modifier = Modifier.fillMaxWidth(),
            )
            LexiButton(
                text = stringResource(Res.string.reminder_preprompt_no),
                onClick = { onIntent(ReminderPrePromptIntent.Decline) },
                variant = LexiButtonVariant.Secondary,
                shadow = null,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// design sample, not user-facing
@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun ReminderPrePromptPreview() {
    LexiTheme(reducedMotion = false) { ReminderPrePromptScreen(state = ReminderPrePromptUiState(), onIntent = {}) }
}
