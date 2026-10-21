package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.talhabayhan.lexi.core.designsystem.modifier.lexiSurface
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.resized
import dev.talhabayhan.lexi.core.designsystem.theme.withCssLineHeight

/** Colors of a [LevelBadge] per level (`16-components` § SEVİYE ROZETLERİ). */
public enum class LevelBadgeStyle {
    /** Red, white text. */
    A1,

    /** White, ink text. */
    A2,

    /** Yellow, ink text. */
    B1,

    /** Blue, white text. */
    B2,

    /** White, ink text. */
    C1,

    /** Ink, yellow text. */
    Native,
}

/** Look of a [LevelCard]. */
public enum class LevelCardStyle {
    /** White, ink text, s3. */
    Normal,

    /** Red, white text, s6; progress ink on white. */
    Selected,

    /** Ink, cream text and lines (NAT). */
    Inverted,
}

/** Card and tile geometry from `03-level-select`, `02-home` and `16-components`. */
public object CardDefaults {
    /** Level badge: `padding: 4px 10px`, 2dp border. */
    public val BadgePadding: PaddingValues = PaddingValues(horizontal = 10.dp, vertical = 4.dp)

    /** 150: level card. */
    public val LevelCardHeight: Dp = 150.dp

    /** 110: menu tile. */
    public val MenuTileHeight: Dp = 110.dp

    /** 14: card and tile padding. */
    public val Padding: Dp = 14.dp

    /** Marker sizes including the 2dp border (the HTML sizes are CSS content-box: 24 + 2 × 2, 26 + 2 × 2). */
    public val LevelMarkerSize: Dp = 28.dp
    public val TileMarkerSize: Dp = 30.dp

    /** Compact menu tile for very short or narrow phones (DESIGN-DEBT, scaled from `02-home`). */
    public val CompactPadding: Dp = 10.dp
    internal val CompactTileTitle: TextUnit = 16.sp
    internal val CompactTileSubtitle: TextUnit = 12.sp

    /** Menu tile title/subtitle `line-height: 1.1` of the 18px title. */
    internal const val TILE_TITLE_LINE_HEIGHT: Double = 1.1
    internal const val TILE_SUBTITLE_LINE_HEIGHT: Double = 1.1 * 18 / 14
}

/** A level chip: square, 2dp ink border, 14sp/800, colored per [style]. */
@Composable
public fun LevelBadge(text: String, style: LevelBadgeStyle, modifier: Modifier = Modifier) {
    val colors = LexiTheme.colors
    val (fill, content) = when (style) {
        LevelBadgeStyle.A1 -> colors.red to colors.white
        LevelBadgeStyle.A2, LevelBadgeStyle.C1 -> colors.white to colors.ink
        LevelBadgeStyle.B1 -> colors.yellow to colors.ink
        LevelBadgeStyle.B2 -> colors.blue to colors.white
        LevelBadgeStyle.Native -> colors.ink to colors.yellow
    }
    Text(
        text = text,
        style = LexiTheme.typography.bodyStrong14,
        color = content,
        modifier = modifier
            .lexiSurface(fill, BorderStroke(LexiTheme.borders.thin, colors.ink))
            .padding(CardDefaults.BadgePadding),
    )
}

/**
 * Level card of the 2-column grid (150dp): title (34/800) with a marker shape, subtitle, progress bar and count.
 * Look per [LevelCardStyle]; radio-button semantics, [LevelCardStyle.Selected] being the selected one.
 */
@Composable
public fun LevelCard(
    title: String,
    subtitle: String,
    progress: Float,
    progressLabel: String,
    marker: DecorShapeKind,
    markerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: LevelCardStyle = LevelCardStyle.Normal,
) {
    val colors = LexiTheme.colors
    val typography = LexiTheme.typography
    val selected = style == LevelCardStyle.Selected
    val inverted = style == LevelCardStyle.Inverted
    val fill = when {
        selected -> colors.red
        inverted -> colors.ink
        else -> colors.white
    }
    val content = when {
        selected -> colors.white
        inverted -> colors.cream
        else -> colors.ink
    }
    val lines = if (inverted) colors.cream else colors.ink
    PressableSurface(
        onClick = onClick,
        modifier = modifier.heightIn(min = CardDefaults.LevelCardHeight).semantics { this.selected = selected },
        color = fill,
        shadow = if (selected) LexiTheme.shadows.s6 else LexiTheme.shadows.s3,
        role = Role.RadioButton,
        propagateMinConstraints = true,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(CardDefaults.Padding),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, style = typography.levelTitle, color = content)
                DecorShape(
                    kind = marker,
                    color = markerColor,
                    border = BorderStroke(LexiTheme.borders.thin, lines),
                    modifier = Modifier.size(CardDefaults.LevelMarkerSize),
                )
            }
            Text(subtitle, style = typography.caption14SemiBold, color = content)
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap4)) {
                // Decorative: the progress label below says the same in words and the card is one radio button.
                ProgressBar(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth().borderOverflow(LexiTheme.borders.thin).clearAndSetSemantics {},
                    borderColor = lines,
                    trackColor = if (selected) colors.white else Color.Transparent,
                    fillColor = if (selected) colors.ink else colors.blue,
                )
                Text(progressLabel, style = typography.caption12, color = content)
            }
        }
    }
}

/**
 * The level card's bar is CSS `box-sizing: content-box; width: 100%`: the track is as wide as the card's content and
 * its [border] comes on top, so it sticks out to the right of the label column by 2 × [border] (`03-level-select`).
 * Lays the bar out that much wider while reporting the column width.
 */
private fun Modifier.borderOverflow(border: Dp): Modifier = layout { measurable, constraints ->
    val width = constraints.maxWidth + (border * 2).roundToPx()
    val placeable = measurable.measure(Constraints(minWidth = width, maxWidth = width))
    layout(constraints.maxWidth, placeable.height) { placeable.place(0, 0) }
}

/**
 * Home menu tile (at least 110dp, grows with large font scales): marker shape (26dp, 2dp border), title (18/800) and subtitle (14/500), s4. [inverted] =
 * ink with cream text, cream marker border and the red shadow s4Red (exam packs).
 */
@Composable
public fun MenuTile(
    title: String,
    subtitle: String,
    marker: DecorShapeKind,
    markerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    inverted: Boolean = false,
    minHeight: Dp = CardDefaults.MenuTileHeight,
    compact: Boolean = false,
) {
    val colors = LexiTheme.colors
    val typography = LexiTheme.typography
    val content = if (inverted) colors.cream else colors.ink
    PressableSurface(
        onClick = onClick,
        modifier = modifier.heightIn(min = minHeight),
        color = if (inverted) colors.ink else colors.white,
        shadow = if (inverted) LexiTheme.shadows.s4Red else LexiTheme.shadows.s4,
        propagateMinConstraints = true,
    ) {
        MenuTileLayout(Modifier.padding(if (compact) CardDefaults.CompactPadding else CardDefaults.Padding)) {
            DecorShape(
                kind = marker,
                color = markerColor,
                border = BorderStroke(LexiTheme.borders.thin, if (inverted) colors.cream else colors.ink),
            )
            Column {
                Text(title, style = menuTileTitleStyle(compact), color = content)
                Text(
                    subtitle,
                    style = typography.caption14
                        .let { if (compact) it.resized(CardDefaults.CompactTileSubtitle) else it }
                        .withCssLineHeight(CardDefaults.TILE_SUBTITLE_LINE_HEIGHT),
                    color = content,
                )
            }
        }
    }
}

@Composable
private fun menuTileTitleStyle(compact: Boolean): TextStyle = LexiTheme.typography.heading18
    .let { if (compact) it.resized(CardDefaults.CompactTileTitle) else it }
    .withCssLineHeight(CardDefaults.TILE_TITLE_LINE_HEIGHT)

/**
 * The narrowest [MenuTile] whose title column still holds the longest word of every one of [titles] on one line. A grid
 * whose columns are narrower (large font sizes on narrow screens) should stack its tiles instead of breaking words.
 */
@Composable
public fun menuTileMinWidth(titles: List<String>, compact: Boolean = false): Dp {
    val measurer = rememberTextMeasurer()
    val style = menuTileTitleStyle(compact)
    val padding = if (compact) CardDefaults.CompactPadding else CardDefaults.Padding
    val density = LocalDensity.current
    val border = LexiTheme.borders.regular
    return remember(titles, style, padding, border, density, measurer) {
        val widest = titles.flatMap { it.split(' ') }.maxOfOrNull { word ->
            measurer.measure(word, style, softWrap = false, maxLines = 1).size.width
        } ?: 0
        // Text column = tile width − padding − the surface's border on both sides.
        with(density) { widest.toDp() } + padding * 2 + border * 2
    }
}

/**
 * The tile's `flex-direction: column; justify-content: space-between`: marker at the top, text at the bottom. When the
 * text needs more room than the tile's minimum height leaves, the marker shrinks in height first (CSS flex-shrink: a
 * circle turns into an ellipse, as in `02-home`); with no marker room left the tile grows (large font scales).
 */
@Composable
private fun MenuTileLayout(modifier: Modifier, content: @Composable () -> Unit) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        val markerSize = CardDefaults.TileMarkerSize.roundToPx()
        val width = constraints.maxWidth
        val text = measurables[1].measure(Constraints(maxWidth = width))
        val markerHeight = (constraints.minHeight - text.height).coerceIn(0, markerSize)
        val marker = measurables[0].measure(Constraints.fixed(markerSize, markerHeight))
        val height = maxOf(constraints.minHeight, markerHeight + text.height)
        layout(width, height) {
            marker.place(0, 0)
            text.place(0, height - text.height)
        }
    }
}

// design sample, not user-facing
@Preview(widthDp = 390)
@Composable
private fun CardsPreview() {
    LexiTheme(reducedMotion = false) {
        val colors = LexiTheme.colors
        Column(
            modifier = Modifier.background(colors.cream).padding(LexiTheme.dimens.gap20),
            verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap12),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap8)) {
                LevelBadge("A1", LevelBadgeStyle.A1)
                LevelBadge("B2", LevelBadgeStyle.B2)
                LevelBadge("NAT", LevelBadgeStyle.Native)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap12)) {
                LevelCard(
                    "A1", "Başlangıç", 0.48f, "412 / 850 kelime", DecorShapeKind.Square, colors.yellow,
                    onClick = {}, style = LevelCardStyle.Selected, modifier = Modifier.weight(1f),
                )
                LevelCard(
                    "NAT", "Deyim · phrasal verb", 0f, "0 / 800 kelime", DecorShapeKind.Leaf, colors.red,
                    onClick = {}, style = LevelCardStyle.Inverted, modifier = Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap12)) {
                MenuTile(
                    "Kelime Defterim",
                    "24 tekrar bekliyor",
                    DecorShapeKind.Circle,
                    colors.blue,
                    onClick = {},
                    modifier = Modifier.weight(1f),
                )
                MenuTile(
                    "Sınav paketleri",
                    "LGS · YKS-Dil · YDS",
                    DecorShapeKind.Diamond,
                    colors.yellow,
                    onClick = {},
                    inverted = true,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
