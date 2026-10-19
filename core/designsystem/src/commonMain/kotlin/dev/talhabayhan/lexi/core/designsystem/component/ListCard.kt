package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.feedback.LocalHaptics
import dev.talhabayhan.lexi.core.designsystem.feedback.LocalSoundPlayer
import dev.talhabayhan.lexi.core.designsystem.icon.LexiIcons
import dev.talhabayhan.lexi.core.designsystem.modifier.lexiSurface
import dev.talhabayhan.lexi.core.designsystem.theme.LexiShadow
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.feedback.HapticKind
import dev.talhabayhan.lexi.core.domain.feedback.SoundEffect

/** List and stat geometry from `11-settings`, `08-notebook`, `09-stats`, `06-result`. */
public object ListDefaults {
    /** 52: settings row minimum height. */
    public val RowMinHeight: Dp = 52.dp

    /** `padding: 0 12px`, gap 10. */
    public val RowPadding: PaddingValues = PaddingValues(horizontal = 12.dp)
    public val RowGap: Dp = 10.dp

    /** Value + chevron (`gap: 6px`, chevron 14). */
    public val ValueGap: Dp = 6.dp
    public val ChevronSize: Dp = 14.dp

    /** Stat tile: padding 12, 16dp marker, gap 4 (`09-stats`); result tiles use 26dp markers and gap 6. */
    public val StatPadding: Dp = 12.dp
    public val StatMarkerSize: Dp = 16.dp
    public val StatGap: Dp = 4.dp
}

/**
 * White card with a 3dp ink frame whose children are separated by [dividerWidth] ink lines (rows of settings,
 * notebook, result lists). Like the CSS `border-top` of each row, a divider belongs to the row below it.
 */
@Composable
public fun ListCard(
    modifier: Modifier = Modifier,
    shadow: LexiShadow? = null,
    dividerWidth: Dp = LexiTheme.borders.thin,
    content: @Composable () -> Unit,
) {
    DividedGrid(
        columns = 1,
        dividerWidth = dividerWidth,
        modifier = modifier.lexiSurface(
            color = LexiTheme.colors.white,
            border = BorderStroke(LexiTheme.borders.regular, LexiTheme.colors.ink),
            shadow = shadow,
        ),
        content = content,
    )
}

/**
 * A row of a [ListCard]: min 52dp, padding 0 12, gap 10, label 16/600 filling the width, then [trailing] (a
 * [ListRowValue], chip or speaker button; for a switch use the toggle overload). With [onClick] the whole row is a button (light haptic + tap).
 */
@Composable
public fun ListRow(
    label: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    ListRow(modifier = modifier, onClick = onClick) {
        // Large fonts on narrow screens: the value or chip moves under the label instead of breaking words.
        AdaptiveTrailingRow(
            gap = ListDefaults.RowGap,
            stackedGap = LexiTheme.dimens.gap4,
            modifier = Modifier.weight(1f),
            main = { Text(label, style = LexiTheme.typography.body16SemiBold) },
            trailing = { Row(verticalAlignment = Alignment.CenterVertically, content = trailing) },
        )
    }
}

/**
 * A settings row whose whole area is the switch: one toggleable node (Role.Switch, label as its text) with the
 * [LexiSwitch] as display; light haptic + tap on toggle.
 */
@Composable
public fun ListRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val haptics = LocalHaptics.current
    val sounds = LocalSoundPlayer.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = ListDefaults.RowMinHeight)
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                interactionSource = null,
                indication = null,
            ) {
                haptics.perform(HapticKind.Light)
                sounds.play(SoundEffect.Tap)
                onCheckedChange(it)
            }
            .padding(ListDefaults.RowPadding),
        horizontalArrangement = Arrangement.spacedBy(ListDefaults.RowGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = LexiTheme.typography.body16SemiBold, modifier = Modifier.weight(1f))
        LexiSwitch(checked = checked, onCheckedChange = null, enabled = enabled)
    }
}

/** A [ListCard] row with free content (e.g. the result list: letter chip, word, speaker). */
@Composable
public fun ListRow(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    minHeight: Dp = ListDefaults.RowMinHeight,
    contentPadding: PaddingValues = ListDefaults.RowPadding,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(ListDefaults.RowGap),
    content: @Composable RowScope.() -> Unit,
) {
    val haptics = LocalHaptics.current
    val sounds = LocalSoundPlayer.current
    val click = if (onClick != null) {
        Modifier.clickable(role = Role.Button, interactionSource = null, indication = null) {
            haptics.perform(HapticKind.Light)
            sounds.play(SoundEffect.Tap)
            onClick()
        }
    } else {
        Modifier.semantics(mergeDescendants = true) {}
    }
    Row(
        modifier = modifier.fillMaxWidth().heightIn(min = minHeight).then(click).padding(contentPadding),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

/** Trailing value of a settings row: 15/800 text and a 14dp chevron. */
@Composable
public fun ListRowValue(value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(ListDefaults.ValueGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(value, style = LexiTheme.typography.bodyStrong15)
        Icon(LexiIcons.ChevronRight, contentDescription = null, modifier = Modifier.size(ListDefaults.ChevronSize))
    }
}

/**
 * Grid of [StatTile]s in a 3dp framed white card with [dividerWidth] ink lines between cells (2×2 with 2dp in
 * stats, 1×3 with 3dp on the result screen).
 */
@Composable
public fun StatGrid(
    columns: Int,
    modifier: Modifier = Modifier,
    dividerWidth: Dp = LexiTheme.borders.thin,
    content: @Composable () -> Unit,
) {
    DividedGrid(
        columns = columns,
        dividerWidth = dividerWidth,
        modifier = modifier.lexiSurface(
            LexiTheme.colors.white,
            BorderStroke(LexiTheme.borders.regular, LexiTheme.colors.ink),
        ),
        content = content,
    )
}

/** Stat cell: marker shape, big number (30/800) and label (13/600). */
@Composable
public fun StatTile(
    value: String,
    label: String,
    marker: DecorShapeKind,
    markerColor: Color,
    modifier: Modifier = Modifier,
    markerSize: Dp = ListDefaults.StatMarkerSize,
    gap: Dp = ListDefaults.StatGap,
) {
    Column(
        modifier = modifier.semantics(mergeDescendants = true) {}.padding(ListDefaults.StatPadding),
        verticalArrangement = Arrangement.spacedBy(gap),
    ) {
        DecorShape(
            kind = marker,
            color = markerColor,
            border = BorderStroke(LexiTheme.borders.thin, LexiTheme.colors.ink),
            modifier = Modifier.size(markerSize),
        )
        Text(value, style = LexiTheme.typography.statValue)
        Text(label, style = LexiTheme.typography.caption13SemiBold)
    }
}

/**
 * Equal-width grid (CSS `repeat(n, minmax(0, 1fr))`) whose cells carry their dividers inside, like `border-left` /
 * `border-top` on every cell but the first column/row; rows are as tall as their tallest cell.
 */
@Composable
private fun DividedGrid(columns: Int, dividerWidth: Dp, modifier: Modifier, content: @Composable () -> Unit) {
    val color = LexiTheme.colors.ink
    val lines = remember { DividerLines() }
    Layout(
        content = content,
        modifier = modifier.drawBehind {
            val w = dividerWidth.toPx()
            lines.columnStarts.forEach { x -> drawRect(color, Offset(x, 0f), Size(w, size.height)) }
            lines.rowStarts.forEach { y -> drawRect(color, Offset(0f, y), Size(size.width, w)) }
        },
    ) { measurables, constraints ->
        val divider = dividerWidth.roundToPx()
        val width = if (constraints.hasBoundedWidth) {
            constraints.maxWidth
        } else {
            val widest = measurables.maxOfOrNull { it.maxIntrinsicWidth(Constraints.Infinity) } ?: 0
            maxOf(constraints.minWidth, widest * columns + divider * (columns - 1))
        }
        val cellWidths = IntArray(columns) { col -> (width * (col + 1)) / columns - (width * col) / columns }
        val cellStarts = IntArray(columns) { col -> (width * col) / columns }
        val placeables = measurables.mapIndexed { i, measurable ->
            val col = i % columns
            val inner = cellWidths[col] - if (col > 0) divider else 0
            measurable.measure(Constraints(minWidth = inner, maxWidth = inner))
        }
        val rows = placeables.chunked(columns)
        val rowHeights = rows.mapIndexed { r, row -> row.maxOf { it.height } + if (r > 0) divider else 0 }
        val height = rowHeights.sum().coerceIn(constraints.minHeight, constraints.maxHeight)
        lines.columnStarts = (1 until columns).map { cellStarts[it].toFloat() }
        lines.rowStarts = rowHeights.runningFold(0) { acc, h -> acc + h }.drop(1).dropLast(1).map { it.toFloat() }
        layout(width, height) {
            var y = 0
            rows.forEachIndexed { r, row ->
                val top = y + if (r > 0) divider else 0
                row.forEachIndexed { col, placeable ->
                    placeable.place(cellStarts[col] + if (col > 0) divider else 0, top)
                }
                y += rowHeights[r]
            }
        }
    }
}

/** Divider positions found during layout, read in the draw pass of the same frame. */
private class DividerLines {
    var columnStarts: List<Float> = emptyList()
    var rowStarts: List<Float> = emptyList()
}

// design sample, not user-facing
@Preview(widthDp = 390)
@Composable
private fun ListCardPreview() {
    LexiTheme(reducedMotion = false) {
        val colors = LexiTheme.colors
        Column(
            modifier = Modifier.background(colors.cream).padding(LexiTheme.dimens.gap20),
            verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap14),
        ) {
            ListCard(Modifier.fillMaxWidth()) {
                ListRow("Ses efektleri", checked = true, onCheckedChange = {})
                ListRow("Klasik mod süresi", onClick = {}) { ListRowValue("3:00") }
            }
            StatGrid(columns = 2, modifier = Modifier.fillMaxWidth()) {
                StatTile("412", "öğrenilen kelime", DecorShapeKind.Circle, colors.blue)
                StatTile("%78", "doğruluk", DecorShapeKind.Square, colors.red)
                StatTile("12 gün", "en uzun seri", DecorShapeKind.Leaf, colors.yellow)
                StatTile("63", "tamamlanan çark", DecorShapeKind.Square, colors.ink)
            }
        }
    }
}
