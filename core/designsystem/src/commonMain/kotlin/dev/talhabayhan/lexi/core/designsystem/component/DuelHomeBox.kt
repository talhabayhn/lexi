package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import dev.talhabayhan.lexi.core.designsystem.icon.LexiIcons
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.withCssLineHeight

/**
 * The full-width "İki kişi, tek telefon" box of `02b-home-duel`: white, 3dp frame, s4, a red square overlapping a blue
 * circle, [title] / [subtitle] and a chevron. The whole box is one button ([onClickLabel] names its action). [height]
 * is a minimum: the box grows with large fonts. With [showSubtitle] off (DESIGN-DEBT: the tightest Home tier on very
 * short phones) only the title is drawn; the subtitle stays in the accessibility description.
 */
@Composable
public fun DuelHomeBox(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onClickLabel: String? = null,
    height: Dp = DuelDefaults.HomeBoxHeight,
    showSubtitle: Boolean = true,
) {
    val colors = LexiTheme.colors
    val border = BorderStroke(LexiTheme.borders.thin, colors.ink)
    PressableSurface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().heightIn(min = height),
        shadow = LexiTheme.shadows.s4,
        onClickLabel = onClickLabel,
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = DuelDefaults.HomeBoxPaddingHorizontal),
            horizontalArrangement = Arrangement.spacedBy(DuelDefaults.HomeBoxGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(DuelDefaults.HomeBoxMarkerWidth, DuelDefaults.HomeBoxMarkerHeight)) {
                val marker = Modifier.size(DuelDefaults.HomeBoxMarkerSize)
                DecorShape(
                    DecorShapeKind.Square,
                    colors.red,
                    marker.offset(y = DuelDefaults.HomeBoxMarkerTop),
                    border,
                )
                DecorShape(
                    DecorShapeKind.Circle,
                    colors.blue,
                    marker.offset(x = DuelDefaults.HomeBoxMarkerCircleStart, y = DuelDefaults.HomeBoxMarkerTop),
                    border,
                )
            }
            Column(Modifier.weight(1f)) {
                val titleStyle = LexiTheme.typography.heading18.withBoxLineHeight()
                if (showSubtitle) {
                    Text(title, style = titleStyle)
                    // The 14sp line sits in the parent's 18sp × 1.1 line box (the HTML span nests it).
                    Text(subtitle, style = LexiTheme.typography.caption14.withCssLineHeight(SUBTITLE_LINE_HEIGHT))
                } else {
                    // One line that shrinks (then ends in an ellipsis) instead of wrapping.
                    BasicText(
                        text = title,
                        style = titleStyle,
                        color = { colors.ink },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        autoSize = TextAutoSize.StepBased(
                            minFontSize = DuelDefaults.HomeBoxMinTitleSize,
                            maxFontSize = titleStyle.fontSize,
                            stepSize = DuelDefaults.HomeBoxTitleStep,
                        ),
                    )
                    Box(Modifier.semantics { contentDescription = subtitle })
                }
            }
            Icon(
                LexiIcons.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(DuelDefaults.HomeBoxChevronSize).clearAndSetSemantics {},
            )
        }
    }
}

// The HTML box has `line-height: 1.1` on the whole text span (both lines).
private fun TextStyle.withBoxLineHeight() = withCssLineHeight(BOX_LINE_HEIGHT)

private const val BOX_LINE_HEIGHT = 1.1

/** 18 × 1.1 / 14: the title's line box expressed in the subtitle's font size. */
private const val SUBTITLE_LINE_HEIGHT = 18 * BOX_LINE_HEIGHT / 14

// design sample, not user-facing
@Preview(widthDp = 390)
@Composable
private fun DuelHomeBoxPreview() {
    LexiTheme(reducedMotion = false) {
        Box(Modifier.padding(LexiTheme.dimens.gap20)) {
            DuelHomeBox("İki kişi, tek telefon", "Oyuncu 2la sırayla yarış", onClick = {})
        }
    }
}
