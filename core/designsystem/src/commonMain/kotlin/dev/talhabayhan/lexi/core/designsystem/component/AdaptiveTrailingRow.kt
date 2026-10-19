package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp

/**
 * [main] and [trailing] side by side, [gap] apart, the way a row with a weighted text column and a trailing chip
 * lays out: [main] takes the width the chip leaves, both are centred vertically (or share their first baseline with
 * [alignBaselines]). When that width would be narrower than [main]'s longest unbreakable word (its min intrinsic
 * width), [trailing] moves under [main] instead ([stackedGap] apart, start-aligned), so large font sizes and narrow
 * screens wrap lines and never break words. Fills the incoming max width.
 */
@Composable
public fun AdaptiveTrailingRow(
    gap: Dp,
    modifier: Modifier = Modifier,
    stackedGap: Dp = gap,
    alignBaselines: Boolean = false,
    main: @Composable () -> Unit,
    trailing: @Composable () -> Unit,
) {
    Layout(contents = listOf({ Box { main() } }, { Box { trailing() } }), modifier = modifier) { slots, constraints ->
        val mainSlot = slots[0].single()
        val trailingSlot = slots[1].single()
        val width = constraints.maxWidth
        val trailingWidth = minOf(trailingSlot.maxIntrinsicWidth(Constraints.Infinity), width)
        // An empty trailing slot takes no gap, like a row whose trailing content emits nothing.
        val gapPx = if (trailingWidth > 0) gap.roundToPx() else 0
        val sideBySide = mainSlot.minIntrinsicWidth(Constraints.Infinity) + gapPx + trailingWidth <= width
        if (sideBySide) {
            val end = trailingSlot.measure(Constraints(maxWidth = trailingWidth))
            val mainWidth = width - gapPx - end.width
            val start = mainSlot.measure(Constraints(minWidth = mainWidth, maxWidth = mainWidth))
            val startBaseline = start[FirstBaseline]
            val endBaseline = end[FirstBaseline]
            val baselines = alignBaselines && startBaseline != AlignmentLine.Unspecified &&
                endBaseline != AlignmentLine.Unspecified
            val height: Int
            val startY: Int
            val endY: Int
            if (baselines) {
                val baseline = maxOf(startBaseline, endBaseline)
                startY = baseline - startBaseline
                endY = baseline - endBaseline
                height = maxOf(startY + start.height, endY + end.height, constraints.minHeight)
            } else {
                height = maxOf(start.height, end.height, constraints.minHeight)
                // Rounded like Row's Alignment.CenterVertically, so side by side matches a Row pixel for pixel.
                startY = Alignment.CenterVertically.align(start.height, height)
                endY = Alignment.CenterVertically.align(end.height, height)
            }
            layout(width, height) {
                start.place(0, startY)
                end.place(width - end.width, endY)
            }
        } else {
            val start = mainSlot.measure(Constraints(minWidth = width, maxWidth = width))
            val end = trailingSlot.measure(Constraints(maxWidth = width))
            val stackedGapPx = if (end.height > 0) stackedGap.roundToPx() else 0
            val height = maxOf(start.height + stackedGapPx + end.height, constraints.minHeight)
            layout(width, height) {
                start.place(0, 0)
                end.place(0, start.height + stackedGapPx)
            }
        }
    }
}
