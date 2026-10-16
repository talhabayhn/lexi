package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.talhabayhan.lexi.core.designsystem.icon.LexiIcons
import dev.talhabayhan.lexi.core.designsystem.modifier.lexiSurface
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme

/** Geometry of the home screen's own cards (`02-home`). */
public object HomeDefaults {
    /** Streak chip: CSS content-box `height: 40px` + 2 × 3dp border = 46dp. */
    public val StreakChipHeight: Dp = 46.dp
    internal val StreakChipPadding: Dp = 12.dp
    internal val StreakMarkerSize: Dp = 14.dp

    /** Daily card: padding 16, gap 14, date circle 64 (3dp border), chevron 22. */
    internal val DailyPadding: Dp = 16.dp
    internal val DailyGap: Dp = 14.dp
    internal val DailyCircleSize: Dp = 64.dp
    internal val DailyChevronSize: Dp = 22.dp

    /** Background blue circle 140 at `right: -70; top: 210`: half of it leaves the screen. */
    public val CircleSize: Dp = 140.dp
    public val CircleBleed: Dp = 70.dp

    /** Background red half-pill 60 × 120 at `left: -30; bottom: 210`. */
    public val PillWidth: Dp = 60.dp
    public val PillHeight: Dp = 120.dp
    public val PillBleed: Dp = 30.dp

    /** 210 from the top (circle) and from the bottom (pill). */
    public val DecorOffset: Dp = 210.dp

    /** Short phones (DESIGN-DEBT, derived from `02-home`): smaller daily card. */
    internal val DailyCompactPadding: Dp = 12.dp
    internal val DailyCompactCircleSize: Dp = 48.dp

    /** The home screen honours the system font size up to this scale; it never scrolls (`screen-map.md`). */
    public const val MAX_FONT_SCALE: Float = 1.15f

    /** Layout tiers by available height (DESIGN-DEBT below regular): paddings, section gap, CTA and tile heights. */
    public val RegularPlayHeight: Dp = 76.dp
    public val CompactPaddingTop: Dp = 12.dp
    public val CompactPaddingBottom: Dp = 16.dp
    public val CompactGap: Dp = 12.dp
    public val CompactPlayHeight: Dp = 60.dp
    public val CompactTileHeight: Dp = 84.dp
    public val CompactTileGap: Dp = 10.dp
    public val TightPaddingTop: Dp = 8.dp
    public val TightPaddingBottom: Dp = 12.dp
    public val TightGap: Dp = 8.dp
    public val TightPlayHeight: Dp = 52.dp
    public val TightTileHeight: Dp = 0.dp
    public val TightTileGap: Dp = 8.dp

    /** Smallest scale a CTA caption shrinks to before it is cut with an ellipsis. */
    internal const val CAPTION_MIN_SCALE: Float = 0.7f
    internal val CAPTION_STEP: TextUnit = 0.5.sp
}

/** "7 gün seri": white chip with a red square, 3dp ink frame, 15/800. Informational, one node for accessibility. */
@Composable
public fun StreakChip(text: String, modifier: Modifier = Modifier) {
    val colors = LexiTheme.colors
    Row(
        modifier = modifier
            .height(HomeDefaults.StreakChipHeight)
            .semantics(mergeDescendants = true) {}
            .lexiSurface(colors.white, BorderStroke(LexiTheme.borders.regular, colors.ink))
            .padding(horizontal = HomeDefaults.StreakChipPadding),
        horizontalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(HomeDefaults.StreakMarkerSize).clearAndSetSemantics {}.background(colors.red))
        Text(text, style = LexiTheme.typography.bodyStrong15)
    }
}

/**
 * "Günün çarkı" card: yellow, 3dp frame, s6 shadow; white date circle with the day of the month, a [label] line
 * ("GÜNÜN ÇARKI · EKİM", uppercase in the resource), the [title] and a chevron. The whole card is the button.
 */
@Composable
public fun DailyCard(
    day: String,
    label: String,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val colors = LexiTheme.colors
    PressableSurface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        color = colors.yellow,
        shadow = LexiTheme.shadows.s6,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(
                if (compact) HomeDefaults.DailyCompactPadding else HomeDefaults.DailyPadding,
            ),
            horizontalArrangement = Arrangement.spacedBy(HomeDefaults.DailyGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(if (compact) HomeDefaults.DailyCompactCircleSize else HomeDefaults.DailyCircleSize)
                    .clearAndSetSemantics {}
                    .lexiSurface(
                        color = colors.white,
                        border = BorderStroke(LexiTheme.borders.regular, colors.ink),
                        shape = LexiTheme.shapes.circle,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(day, style = LexiTheme.typography.heading22)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap2)) {
                Text(label, style = LexiTheme.typography.label12)
                Text(title, style = LexiTheme.typography.heading22)
            }
            Icon(
                LexiIcons.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(HomeDefaults.DailyChevronSize),
            )
        }
    }
}

/**
 * OYNA (`02-home`): the red 76dp CTA with s6 shadow; [badge] ("A1 · KLASİK") is a small white chip on the right. A long
 * badge shrinks (then ends in an ellipsis) instead of touching the label.
 */
@Composable
public fun PlayButton(
    label: String,
    badge: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = LexiButtonDefaults.PlayHeight,
) {
    val colors = LexiTheme.colors
    LexiButton(
        onClick = onClick,
        height = height,
        shadow = LexiTheme.shadows.s6,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(label, style = LexiTheme.typography.button30, color = colors.white, maxLines = 1)
        ShrinkingText(
            text = badge,
            style = LexiTheme.typography.badge14,
            color = colors.ink,
            modifier = Modifier
                .padding(start = LexiTheme.dimens.gap12)
                .weight(1f, fill = false)
                .lexiSurface(colors.white, BorderStroke(LexiTheme.borders.thin, colors.ink))
                .padding(CardDefaults.BadgePadding),
        )
    }
}

/**
 * BAŞLA (`03-level-select`): the red 68dp CTA with s6 shadow and a plain [caption] ("A1 · KLASİK · 26 HARF"), right
 * aligned on one line with at least a 12dp gap to the label; a long caption shrinks (then ends in an ellipsis).
 */
@Composable
public fun StartButton(label: String, caption: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LexiTheme.colors
    LexiButton(
        onClick = onClick,
        height = LexiButtonDefaults.StartHeight,
        shadow = LexiTheme.shadows.s6,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(label, style = LexiTheme.typography.button24, color = colors.white, maxLines = 1)
        ShrinkingText(
            text = caption,
            style = LexiTheme.typography.ctaCaption14,
            color = colors.white,
            modifier = Modifier.padding(start = LexiTheme.dimens.gap12).weight(1f, fill = false),
        )
    }
}

/** One line, right aligned; when it does not fit, the font shrinks to a minimum, then it is ellipsized. */
@Composable
private fun ShrinkingText(text: String, style: TextStyle, color: Color, modifier: Modifier = Modifier) {
    BasicText(
        text = text,
        modifier = modifier,
        style = style.merge(TextStyle(textAlign = TextAlign.End)),
        color = { color },
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        autoSize = TextAutoSize.StepBased(
            minFontSize = style.fontSize * HomeDefaults.CAPTION_MIN_SCALE,
            maxFontSize = style.fontSize,
            stepSize = HomeDefaults.CAPTION_STEP,
        ),
    )
}

// design sample, not user-facing
@Preview(widthDp = 390)
@Composable
private fun HomeCardsPreview() {
    LexiTheme(reducedMotion = false) {
        Column(
            modifier = Modifier.background(LexiTheme.colors.cream).padding(LexiTheme.dimens.gap20),
            verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap12),
        ) {
            StreakChip("7 gün seri")
            DailyCard("14", "GÜNÜN ÇARKI · EKİM", "Herkes aynı çarkı çözüyor", onClick = {})
            PlayButton("OYNA", "A1 · KLASİK", onClick = {})
            StartButton("BAŞLA", "A1 · KLASİK · 26 HARF", onClick = {})
        }
    }
}
