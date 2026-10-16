package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.generated.resources.Res
import dev.talhabayhan.lexi.core.designsystem.generated.resources.word_row_strength
import dev.talhabayhan.lexi.core.designsystem.icon.LexiIcons
import dev.talhabayhan.lexi.core.designsystem.modifier.lexiSurface
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.withCssLineHeight
import org.jetbrains.compose.resources.stringResource

/** Word row geometry (`08-notebook`, `source/08-notebook.dc.html`). */
public object WordRowDefaults {
    /** `padding: 8px 10px 8px 12px`, gap 10. */
    public val Padding: PaddingValues = PaddingValues(start = 12.dp, top = 8.dp, end = 10.dp, bottom = 8.dp)

    /** Memory-strength bars: 8 wide, 10/15/20/25 high (+ 2dp border each side, CSS content-box), gap 3. */
    internal val BarWidth: Dp = 8.dp
    internal val BarBaseHeight: Dp = 10.dp
    internal val BarStep: Dp = 5.dp
    internal val BarGap: Dp = 3.dp
    public const val MAX_STRENGTH: Int = 4

    /** Word line `line-height: 1.15`. */
    internal const val WORD_LINE_HEIGHT: Double = 1.15
}

/**
 * Notebook word row (inside a [ListCard]): "word · translation" (18/800 + 14/500), meta line (level · next review),
 * 4 memory-strength bars (filled up to [strength]: red at 1, yellow at 2, blue from 3) and the speaker button.
 */
@Composable
public fun WordRow(
    word: String,
    translation: String,
    meta: String,
    strength: Int,
    speakContentDescription: String,
    onSpeak: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LexiTheme.colors
    val typography = LexiTheme.typography
    Row(
        modifier = modifier.fillMaxWidth().padding(WordRowDefaults.Padding),
        horizontalArrangement = Arrangement.spacedBy(ListDefaults.RowGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            val wordStyle = typography.heading18.withCssLineHeight(WordRowDefaults.WORD_LINE_HEIGHT)
            Text(
                text = buildAnnotatedString {
                    append(word)
                    withStyle(
                        SpanStyle(
                            fontSize = typography.caption14.fontSize,
                            fontWeight = typography.caption14.fontWeight,
                        ),
                    ) {
                        append("· ")
                        append(translation)
                    }
                },
                style = wordStyle,
            )
            Text(meta, style = typography.caption12)
        }
        val fill = when {
            strength <= 1 -> colors.red
            strength == 2 -> colors.yellow
            else -> colors.blue
        }
        val description = stringResource(Res.string.word_row_strength, strength)
        Row(
            modifier = Modifier.clearAndSetSemantics { contentDescription = description },
            horizontalArrangement = Arrangement.spacedBy(WordRowDefaults.BarGap),
            verticalAlignment = Alignment.Bottom,
        ) {
            val border = LexiTheme.borders.thin
            repeat(WordRowDefaults.MAX_STRENGTH) { k ->
                val height = WordRowDefaults.BarBaseHeight + WordRowDefaults.BarStep * k
                Box(
                    Modifier
                        .size(WordRowDefaults.BarWidth + border * 2, height + border * 2)
                        .lexiSurface(if (k < strength) fill else colors.white, BorderStroke(border, colors.ink)),
                )
            }
        }
        IconSquareButton(
            icon = LexiIcons.Speaker,
            contentDescription = speakContentDescription,
            onClick = onSpeak,
            size = IconButtonDefaults.NavSize,
            iconSize = IconButtonDefaults.IconSize,
            containerColor = colors.cream,
            borderWidth = LexiTheme.borders.thin,
        )
    }
}

// design sample, not user-facing
@Preview(widthDp = 390)
@Composable
private fun WordRowPreview() {
    LexiTheme(reducedMotion = false) {
        Column(Modifier.background(LexiTheme.colors.cream).padding(LexiTheme.dimens.gap20)) {
            ListCard(Modifier.fillMaxWidth()) {
                WordRow("egg", "yumurta", "A1 · tekrar: bugün", 1, onSpeak = {}, speakContentDescription = "egg")
                WordRow("chair", "sandalye", "A1 · tekrar: bugün", 2, onSpeak = {}, speakContentDescription = "chair")
                WordRow("yesterday", "dün", "A1 · tekrar: yarın", 3, onSpeak = {
                }, speakContentDescription = "yesterday")
            }
        }
    }
}
