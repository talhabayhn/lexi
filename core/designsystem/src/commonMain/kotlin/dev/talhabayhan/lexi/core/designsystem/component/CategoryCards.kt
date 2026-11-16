package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.icon.LexiIcons
import dev.talhabayhan.lexi.core.designsystem.modifier.lexiSurface
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.withCssLineHeight

/** Geometry of the category cards (`07-categories`, now Home's category sheet in `02c-home-v2`). */
public object CategoryCardDefaults {
    /** 92: category card (border-box, 3dp frame, `padding: 10px 12px`); at least the 84dp touch height asked for. */
    public val Height: Dp = 92.dp
    internal val Padding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 10.dp)

    /** Marker including its 2dp border (CSS border-box here). */
    internal val MarkerSize: Dp = 20.dp

    /** Gap between the progress bar and its caption. */
    internal val ProgressGap: Dp = 3.dp

    /** `line-height: 1.1` of the 15px name. */
    internal const val NAME_LINE_HEIGHT: Double = 1.1

    /** 64: the mixed-wheel card; its circle is CSS content-box 30 + 2 × 2 border. */
    public val MixedHeight: Dp = 64.dp

    /** System font scale above which a category name may wrap onto more than two lines. */
    public const val WRAP_FONT_SCALE: Float = 1.5f

    internal val MixedPadding: Dp = 14.dp
    internal val MixedMarkerSize: Dp = 34.dp

    /** Grid gap of `07-categories`. */
    public val GridGap: Dp = 10.dp

    /** One column from this font scale on, or when a card of two columns would be narrower than [MinColumnWidth]. */
    public const val ONE_COLUMN_FONT_SCALE: Float = 1.3f
    public val MinColumnWidth: Dp = 140.dp

    /** Selected: a yellow 20dp check badge (2dp ink frame, 12dp check) 6dp out of the top-right corner. */
    internal val CheckBadgeSize: Dp = 20.dp
    internal val CheckBadgeOffset: Dp = 6.dp
    internal val CheckIconSize: Dp = 12.dp
}

/**
 * Category card of the 2-column grid: marker shape and name (15/800, up to two lines), a thin progress bar and the
 * [caption] ("88 / 120", "70 / 70 · tamam"). [complete] turns the bar blue (ink otherwise); completion is also in the
 * caption, never by color alone. [selected] (Home's category sheet): ink surface, yellow name and fill, white bar
 * outline and a yellow check badge. A radio button read as [description] with its selected state.
 */
@Composable
public fun CategoryCard(
    title: String,
    progress: Float,
    caption: String,
    marker: DecorShapeKind,
    markerColor: Color,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    complete: Boolean = false,
    selected: Boolean = false,
) {
    val colors = LexiTheme.colors
    val typography = LexiTheme.typography
    val wrap = LocalDensity.current.fontScale > CategoryCardDefaults.WRAP_FONT_SCALE
    SelectedBadgeBox(selected, modifier) {
        PressableSurface(
            onClick = onClick,
            modifier = Modifier
                .fillMaxSize()
                .heightIn(min = CategoryCardDefaults.Height)
                .semantics {
                    contentDescription = description
                    this.selected = selected
                },
            color = if (selected) colors.ink else colors.white,
            shadow = LexiTheme.shadows.s3,
            role = Role.RadioButton,
            propagateMinConstraints = true,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clearAndSetSemantics {}
                    .padding(CategoryCardDefaults.Padding),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    // Keeps a tall name off the progress bar at the largest font sizes only.
                    modifier = Modifier.fillMaxWidth().padding(bottom = if (wrap) LexiTheme.dimens.gap6 else 0.dp),
                    horizontalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap8),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DecorShape(
                        kind = marker,
                        color = markerColor,
                        border = BorderStroke(LexiTheme.borders.thin, if (selected) colors.cream else colors.ink),
                        modifier = Modifier.size(CategoryCardDefaults.MarkerSize),
                    )
                    Text(
                        title,
                        style = typography.bodyStrong15.withCssLineHeight(CategoryCardDefaults.NAME_LINE_HEIGHT),
                        color = if (selected) colors.yellow else colors.ink,
                        // Two lines at the design and moderate font sizes; at the largest sizes the name may use more.
                        maxLines = if (wrap) 4 else 2,
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
                        modifier = Modifier.fillMaxWidth(),
                        height = ControlDefaults.ProgressHeightCompact,
                        borderColor = if (selected) colors.white else colors.ink,
                        trackColor = if (selected) colors.ink else colors.white,
                        fillColor = when {
                            selected -> colors.yellow
                            complete -> colors.blue
                            else -> colors.ink
                        },
                    )
                    Text(caption, style = typography.caption12, color = if (selected) colors.cream else colors.ink)
                }
            }
        }
    }
}

/**
 * The "mixed wheel" card above the category grid: yellow circle, [title] (18/800) with its [subtitle] (13/500) and
 * the [trailing] count (14/800). Red, ink when [selected] (with the check badge); s5 shadow, at least 64dp high. A
 * radio button read as [description] with its selected state.
 */
@Composable
public fun MixedWheelCard(
    title: String,
    subtitle: String,
    trailing: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
) {
    val colors = LexiTheme.colors
    val typography = LexiTheme.typography
    SelectedBadgeBox(selected, modifier.fillMaxWidth()) {
        PressableSurface(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = CategoryCardDefaults.MixedHeight)
                .semantics {
                    contentDescription = description
                    this.selected = selected
                },
            color = if (selected) colors.ink else colors.red,
            shadow = LexiTheme.shadows.s5,
            role = Role.RadioButton,
            propagateMinConstraints = true,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clearAndSetSemantics {}
                    .padding(horizontal = CategoryCardDefaults.MixedPadding, vertical = LexiTheme.dimens.gap4),
                horizontalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap12),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DecorShape(
                    kind = DecorShapeKind.Circle,
                    color = colors.yellow,
                    border = BorderStroke(LexiTheme.borders.thin, if (selected) colors.cream else colors.ink),
                    modifier = Modifier.size(CategoryCardDefaults.MixedMarkerSize),
                )
                Column(Modifier.weight(1f)) {
                    Text(title, style = typography.heading18, color = if (selected) colors.yellow else colors.white)
                    Text(subtitle, style = typography.caption13, color = if (selected) colors.cream else colors.white)
                }
                Text(trailing, style = typography.bodyStrong14, color = if (selected) colors.cream else colors.white)
            }
        }
    }
}

/** [content] with, when [selected], a yellow check badge over its top-right corner (decorative). */
@Composable
private fun SelectedBadgeBox(selected: Boolean, modifier: Modifier, content: @Composable () -> Unit) {
    val colors = LexiTheme.colors
    Box(modifier) {
        content()
        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = CategoryCardDefaults.CheckBadgeOffset, y = -CategoryCardDefaults.CheckBadgeOffset)
                    .size(CategoryCardDefaults.CheckBadgeSize)
                    .clearAndSetSemantics {}
                    .lexiSurface(
                        colors.yellow,
                        BorderStroke(LexiTheme.borders.thin, colors.ink),
                        shape = LexiTheme.shapes.circle,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    LexiIcons.Check,
                    contentDescription = null,
                    tint = colors.ink,
                    modifier = Modifier.size(CategoryCardDefaults.CheckIconSize),
                )
            }
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
            verticalArrangement = Arrangement.spacedBy(CategoryCardDefaults.GridGap),
        ) {
            MixedWheelCard("Karışık çark", "Tüm A1 kategorilerinden 26 kelime", "850", "Karışık çark", onClick = {})
            Row(horizontalArrangement = Arrangement.spacedBy(CategoryCardDefaults.GridGap)) {
                CategoryCard(
                    "Günlük hayat", 88f / 120, "88 / 120", DecorShapeKind.Square, colors.red, "Günlük hayat",
                    onClick = {}, modifier = Modifier.weight(1f), selected = true,
                )
                CategoryCard(
                    "Aile ve insanlar", 1f, "70 / 70 · tamam", DecorShapeKind.Leaf, colors.blue, "Aile ve insanlar",
                    onClick = {}, modifier = Modifier.weight(1f), complete = true,
                )
            }
        }
    }
}
