package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.withCssLineHeight

/** Geometry of the category screen's cards (`07-categories`). */
public object CategoryCardDefaults {
    /** 92: category card (border-box, 3dp frame, `padding: 10px 12px`). */
    public val Height: Dp = 92.dp
    internal val Padding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 10.dp)

    /** Marker including its 2dp border (CSS border-box here). */
    internal val MarkerSize: Dp = 20.dp

    /** Gap between the progress bar and its caption. */
    internal val ProgressGap: Dp = 3.dp

    /** `line-height: 1.1` of the 15px name. */
    internal const val NAME_LINE_HEIGHT: Double = 1.1

    /** 64: the mixed-wheel banner; its circle is CSS content-box 30 + 2 × 2 border. */
    public val MixedHeight: Dp = 64.dp

    /** System font scale above which a category name may wrap onto more than two lines. */
    public const val WRAP_FONT_SCALE: Float = 1.5f

    internal val MixedPadding: Dp = 14.dp
    internal val MixedMarkerSize: Dp = 34.dp
}

/**
 * Category card of the 2-column grid: marker shape and name (15/800, up to two lines), a thin progress bar and the
 * [caption] ("88 / 120", "70 / 70 · tamam"). [complete] turns the bar blue (ink otherwise); completion is also in the
 * caption, never by color alone.
 */
@Composable
public fun CategoryCard(
    title: String,
    progress: Float,
    caption: String,
    marker: DecorShapeKind,
    markerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    complete: Boolean = false,
) {
    val colors = LexiTheme.colors
    val typography = LexiTheme.typography
    PressableSurface(
        onClick = onClick,
        modifier = modifier.heightIn(min = CategoryCardDefaults.Height),
        shadow = LexiTheme.shadows.s3,
        propagateMinConstraints = true,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(CategoryCardDefaults.Padding),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                // Keeps a tall name off the progress bar at the largest font sizes only; the design size is unchanged.
                modifier = Modifier.fillMaxWidth().padding(
                    bottom = if (LocalDensity.current.fontScale > CategoryCardDefaults.WRAP_FONT_SCALE) {
                        LexiTheme.dimens.gap6
                    } else {
                        0.dp
                    },
                ),
                horizontalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap8),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DecorShape(
                    kind = marker,
                    color = markerColor,
                    border = BorderStroke(LexiTheme.borders.thin, colors.ink),
                    modifier = Modifier.size(CategoryCardDefaults.MarkerSize),
                )
                Text(
                    title,
                    style = typography.bodyStrong15.withCssLineHeight(CategoryCardDefaults.NAME_LINE_HEIGHT),
                    // Two lines at the design and moderate font sizes; at the largest sizes the name may use more.
                    maxLines = if (LocalDensity.current.fontScale > CategoryCardDefaults.WRAP_FONT_SCALE) 4 else 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(CategoryCardDefaults.ProgressGap),
            ) {
                ProgressBar(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth().clearAndSetSemantics {},
                    height = ControlDefaults.ProgressHeightCompact,
                    fillColor = if (complete) colors.blue else colors.ink,
                )
                Text(caption, style = typography.caption12)
            }
        }
    }
}

/**
 * The red "mixed wheel" banner above the category grid: yellow circle, [title] (18/800) with its [subtitle] (13/500)
 * and the [trailing] count (14/800). s5 shadow, 64dp high (at least: it grows with large fonts).
 */
@Composable
public fun MixedWheelCard(
    title: String,
    subtitle: String,
    trailing: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = LexiTheme.colors
    val typography = LexiTheme.typography
    PressableSurface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().heightIn(min = CategoryCardDefaults.MixedHeight),
        enabled = enabled,
        color = colors.red,
        shadow = LexiTheme.shadows.s5,
        propagateMinConstraints = true,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = CategoryCardDefaults.MixedPadding, vertical = LexiTheme.dimens.gap4),
            horizontalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DecorShape(
                kind = DecorShapeKind.Circle,
                color = colors.yellow,
                border = BorderStroke(LexiTheme.borders.thin, colors.ink),
                modifier = Modifier.size(CategoryCardDefaults.MixedMarkerSize),
            )
            Column(Modifier.weight(1f)) {
                Text(title, style = typography.heading18, color = colors.white)
                Text(subtitle, style = typography.caption13, color = colors.white)
            }
            Text(trailing, style = typography.bodyStrong14, color = colors.white)
        }
    }
}

// design sample, not user-facing
@Preview(widthDp = 390)
@Composable
private fun CategoryCardsPreview() {
    LexiTheme(reducedMotion = false) {
        val colors = LexiTheme.colors
        Column(
            modifier = Modifier.background(colors.cream).padding(LexiTheme.dimens.gap20),
            verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap14),
        ) {
            MixedWheelCard("Karışık çark", "Tüm A1 kategorilerinden 26 kelime", "850", onClick = {})
            Row(horizontalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap10)) {
                CategoryCard(
                    "Günlük hayat",
                    0.73f,
                    "88 / 120",
                    DecorShapeKind.Square,
                    colors.red,
                    onClick = {},
                    modifier = Modifier.weight(1f),
                )
                CategoryCard(
                    "Aile ve insanlar",
                    1f,
                    "70 / 70 · tamam",
                    DecorShapeKind.Leaf,
                    colors.blue,
                    onClick = {},
                    complete = true,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
