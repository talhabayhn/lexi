package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.modifier.lexiSurface
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme

/** Geometry of the statistics screen's level rows and hardest-letter chips (`09-stats.html`). */
public object StatsDefaults {
    public val LevelLabelWidth: Dp = 34.dp
    public val LevelCountWidth: Dp = 64.dp
    public val LevelRowGap: Dp = 10.dp
    public val LetterTileSize: Dp = 30.dp
    public val LetterChipPadding: Dp = 8.dp
    public val LetterChipGap: Dp = 8.dp
}

/** Level progress row: level name (15/800, 34dp), 12dp ink-filled bar, `learned / total` (13/600, 64dp, right). */
@Composable
public fun LevelProgressRow(level: String, progress: Float, count: String, modifier: Modifier = Modifier) {
    val typography = LexiTheme.typography
    Row(
        modifier = modifier.semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.spacedBy(StatsDefaults.LevelRowGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(level, style = typography.bodyStrong15, modifier = Modifier.width(StatsDefaults.LevelLabelWidth))
        ProgressBar(
            progress = progress,
            height = ControlDefaults.ProgressHeightThick,
            fillColor = LexiTheme.colors.ink,
            modifier = Modifier.weight(1f),
        )
        Text(
            count,
            style = typography.caption13SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier.width(StatsDefaults.LevelCountWidth),
        )
    }
}

/** Hardest-letter chip: white 3dp card, red 30dp tile with the white letter, accuracy (13/700) over [label] (13/500). */
@Composable
public fun HardLetterChip(letter: String, percent: String, label: String, modifier: Modifier = Modifier) {
    val colors = LexiTheme.colors
    val typography = LexiTheme.typography
    Row(
        modifier = modifier
            .semantics(mergeDescendants = true) {}
            .lexiSurface(colors.white, BorderStroke(LexiTheme.borders.regular, colors.ink))
            .padding(StatsDefaults.LetterChipPadding),
        horizontalArrangement = Arrangement.spacedBy(StatsDefaults.LetterChipGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(StatsDefaults.LetterTileSize).background(colors.red),
            contentAlignment = Alignment.Center,
        ) {
            Text(letter, style = typography.bodyStrong16, color = colors.white)
        }
        Column {
            Text(percent, style = typography.letterChipValue)
            Text(label, style = typography.letterChipLabel)
        }
    }
}

// design sample, not user-facing
@Preview(widthDp = 390)
@Composable
private fun StatsComponentsPreview() {
    LexiTheme(reducedMotion = false) {
        Column(
            modifier = Modifier.background(LexiTheme.colors.cream).padding(LexiTheme.dimens.gap20),
            verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap8),
        ) {
            LevelProgressRow("A1", 0.48f, "412 / 850")
            HardLetterChip("X", "%32", "doğru")
        }
    }
}
