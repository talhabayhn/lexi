package dev.talhabayhan.lexi.feature.onboarding

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import dev.talhabayhan.lexi.core.designsystem.component.LexiButton
import dev.talhabayhan.lexi.core.designsystem.component.LexiButtonDefaults
import dev.talhabayhan.lexi.core.designsystem.component.LexiButtonVariant
import dev.talhabayhan.lexi.core.designsystem.theme.LexiShadow
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme

/**
 * Full-width CTA of `01-onboarding`: 64dp, red, s6, 20/800 +0.06em. [fitted] labels shrink to stay on one line
 * (narrow phones, large fonts: "SEVİYE TESTİNE BAŞLA"); otherwise the label is cut with an ellipsis.
 */
@Composable
internal fun HeroButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: LexiButtonVariant = LexiButtonVariant.Primary,
    shadow: LexiShadow? = LexiTheme.shadows.s6,
    fitted: Boolean = true,
) {
    LexiButton(
        onClick = onClick,
        variant = variant,
        height = LexiButtonDefaults.HeroHeight,
        shadow = shadow,
        modifier = modifier.fillMaxWidth(),
    ) {
        if (fitted) {
            FittedLabel(text = text, style = LexiTheme.typography.button20)
        } else {
            Text(
                text = text,
                style = LexiTheme.typography.button20,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** One line in [style] and the content color, shrunk in 0.5sp steps (down to half) when it doesn't fit. */
@Composable
internal fun FittedLabel(text: String, style: TextStyle, modifier: Modifier = Modifier) {
    val color = LocalContentColor.current
    BasicText(
        text = text,
        style = style,
        color = { color },
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        autoSize = TextAutoSize.StepBased(
            minFontSize = style.fontSize * FITTED_LABEL_MIN_SCALE,
            maxFontSize = style.fontSize,
            stepSize = FITTED_LABEL_STEP,
        ),
        modifier = modifier,
    )
}

private const val FITTED_LABEL_MIN_SCALE = 0.5f
private val FITTED_LABEL_STEP = 0.5.sp
