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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
public fun DailyCard(day: String, label: String, title: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LexiTheme.colors
    PressableSurface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        color = colors.yellow,
        shadow = LexiTheme.shadows.s6,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(HomeDefaults.DailyPadding),
            horizontalArrangement = Arrangement.spacedBy(HomeDefaults.DailyGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(HomeDefaults.DailyCircleSize)
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
 * OYNA (`02-home`): the red 76dp CTA with s6 shadow; [badge] ("A1 · KLASİK") is a small white chip on the right.
 */
@Composable
public fun PlayButton(label: String, badge: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LexiTheme.colors
    LexiButton(
        onClick = onClick,
        height = LexiButtonDefaults.PlayHeight,
        shadow = LexiTheme.shadows.s6,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(label, style = LexiTheme.typography.button30, color = colors.white)
        Text(
            badge,
            style = LexiTheme.typography.badge14,
            color = colors.ink,
            modifier = Modifier
                .lexiSurface(colors.white, BorderStroke(LexiTheme.borders.thin, colors.ink))
                .padding(CardDefaults.BadgePadding),
        )
    }
}

/** BAŞLA (`03-level-select`): the red 68dp CTA with s6 shadow and a plain [caption] ("A1 · KLASİK · 26 HARF"). */
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
        Text(label, style = LexiTheme.typography.button24, color = colors.white)
        Text(caption, style = LexiTheme.typography.ctaCaption14, color = colors.white)
    }
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
