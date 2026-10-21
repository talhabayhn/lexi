package dev.talhabayhan.lexi.feature.game.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import kotlin.math.max
import kotlin.math.min

/**
 * The rows in screen-reader order (TalkBack / VoiceOver): the clue is read before the wheel, the question before its
 * context; then answer, keys and actions. The ordinal is the row's traversal index.
 */
private enum class GameColumnSlot { Hud, Clue, Wheel, Answer, Keys, Actions }

/**
 * The game screen's column (`04-game.html`: flex column, [gap] between rows, [answerTopMargin] extra above the answer
 * slots, actions at the bottom with `margin-top: auto`). The game screen never scrolls (`screen-map.md`): the wheel
 * gets what is left, up to [maxWheelSize]. The keys always take [keysHeight] (three rows, reserved even when a turn
 * has two) with at least [keysActionsGap] before the actions, so with fixed-height rows (the clue card has a fixed clue
 * area) no row moves between turns. At the design size (390 × 844) the wheel is 320dp.
 *
 * Screen readers go through the rows in [GameColumnSlot] order (HUD, clue, wheel, answer, keys, actions), not top to
 * bottom: the column is a traversal group and each row a group with its index.
 */
@Composable
internal fun GameColumn(
    gap: Dp,
    answerTopMargin: Dp,
    keysHeight: Dp,
    keysActionsGap: Dp,
    maxWheelSize: Dp,
    hud: @Composable () -> Unit,
    wheel: @Composable (size: Dp) -> Unit,
    clue: @Composable () -> Unit,
    answer: @Composable () -> Unit,
    keys: @Composable () -> Unit,
    actions: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    SubcomposeLayout(modifier.semantics { isTraversalGroup = true }) { constraints ->
        val width = constraints.maxWidth
        val stretch = Constraints(minWidth = width, maxWidth = width)
        fun measure(slot: GameColumnSlot, content: @Composable () -> Unit) =
            subcompose(slot) { ReadingOrderGroup(slot, content) }.map { it.measure(stretch) }

        val hudPlaceables = measure(GameColumnSlot.Hud, hud)
        val cluePlaceables = measure(GameColumnSlot.Clue, clue)
        val answerPlaceables = measure(GameColumnSlot.Answer, answer)
        val keyPlaceables = measure(GameColumnSlot.Keys, keys)
        val actionPlaceables = measure(GameColumnSlot.Actions, actions)
        fun height(placeables: List<Placeable>) = placeables.maxOfOrNull { it.height } ?: 0

        val gapPx = gap.roundToPx()
        val answerGapPx = gapPx + answerTopMargin.roundToPx()
        val keysPx = max(keysHeight.roundToPx(), height(keyPlaceables))
        // hud | wheel | clue | answer | keys | actions: three gaps, the answer one with its margin, and the minimum gap
        // between the keys and the actions (`margin-top: auto`).
        val fixed = height(hudPlaceables) + height(cluePlaceables) + height(answerPlaceables) + keysPx +
            height(actionPlaceables) + 3 * gapPx + answerGapPx + keysActionsGap.roundToPx()
        val maxWheelPx = min(maxWheelSize.roundToPx(), width)
        val wheelPx = if (constraints.hasBoundedHeight) {
            min(maxWheelPx, max(0, constraints.maxHeight - fixed))
        } else {
            maxWheelPx
        }
        val wheelSize = wheelPx.toDp()
        val wheelPlaceables = subcompose(GameColumnSlot.Wheel) {
            ReadingOrderGroup(GameColumnSlot.Wheel) { wheel(wheelSize) }
        }.map { it.measure(Constraints.fixed(wheelPx, wheelPx)) }

        val contentHeight = fixed + wheelPx
        // The column fills its bounded height (actions at the bottom) and never reports more than the constraints
        // allow; content that still doesn't fit with a 0dp wheel overflows at the bottom instead.
        val layoutHeight = if (constraints.hasBoundedHeight) {
            constraints.maxHeight
        } else {
            max(contentHeight, constraints.minHeight)
        }
        layout(width, layoutHeight) {
            var y = 0
            hudPlaceables.forEach { it.place(0, y) }
            y += height(hudPlaceables) + gapPx
            wheelPlaceables.forEach { it.place((width - wheelPx) / 2, y) }
            y += wheelPx + gapPx
            cluePlaceables.forEach { it.place(0, y) }
            y += height(cluePlaceables) + answerGapPx
            answerPlaceables.forEach { it.place(0, y) }
            y += height(answerPlaceables) + gapPx
            keyPlaceables.forEach { it.place(0, y) }
            y += keysPx + keysActionsGap.roundToPx()
            val actionsY = max(y, layoutHeight - height(actionPlaceables))
            actionPlaceables.forEach { it.place(0, actionsY) }
        }
    }
}

/** One row as a traversal group at its [slot]'s place in the reading order; the layout passes through unchanged. */
@Composable
private fun ReadingOrderGroup(slot: GameColumnSlot, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.semantics {
            isTraversalGroup = true
            traversalIndex = slot.ordinal.toFloat()
        },
        propagateMinConstraints = true,
    ) {
        content()
    }
}
