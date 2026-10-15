package dev.talhabayhan.lexi.feature.game.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import kotlin.math.max
import kotlin.math.min

private enum class GameColumnSlot { Hud, Wheel, Clue, Answer, Keys, Actions }

/**
 * The game screen's column (`04-game.html`: flex column, [gap] between rows, [answerTopMargin] extra above the answer
 * slots, actions at the bottom with `margin-top: auto`). The game screen never scrolls (`screen-map.md`): the wheel
 * gets what is left, up to [maxWheelSize], so long answers (3 key rows), short phones and system bars shrink the wheel
 * instead of pushing the actions off screen. At the design size (390 × 844, 2 key rows) the wheel is 320dp.
 */
@Composable
internal fun GameColumn(
    gap: Dp,
    answerTopMargin: Dp,
    maxWheelSize: Dp,
    hud: @Composable () -> Unit,
    wheel: @Composable (size: Dp) -> Unit,
    clue: @Composable () -> Unit,
    answer: @Composable () -> Unit,
    keys: @Composable () -> Unit,
    actions: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    SubcomposeLayout(modifier) { constraints ->
        val width = constraints.maxWidth
        val stretch = Constraints(minWidth = width, maxWidth = width)
        fun measure(slot: GameColumnSlot, content: @Composable () -> Unit) =
            subcompose(slot, content).map { it.measure(stretch) }

        val hudPlaceables = measure(GameColumnSlot.Hud, hud)
        val cluePlaceables = measure(GameColumnSlot.Clue, clue)
        val answerPlaceables = measure(GameColumnSlot.Answer, answer)
        val keyPlaceables = measure(GameColumnSlot.Keys, keys)
        val actionPlaceables = measure(GameColumnSlot.Actions, actions)
        fun height(placeables: List<Placeable>) = placeables.maxOfOrNull { it.height } ?: 0

        val gapPx = gap.roundToPx()
        val answerGapPx = gapPx + answerTopMargin.roundToPx()
        // hud | wheel | clue | answer | keys | actions: five gaps, the clue | answer one with the answer margin
        // (keys | actions is the minimum gap before `margin-top: auto`).
        val fixed = height(hudPlaceables) + height(cluePlaceables) + height(answerPlaceables) +
            height(keyPlaceables) + height(actionPlaceables) + 4 * gapPx + answerGapPx
        val maxWheelPx = min(maxWheelSize.roundToPx(), width)
        val wheelPx = if (constraints.hasBoundedHeight) {
            min(maxWheelPx, max(0, constraints.maxHeight - fixed))
        } else {
            maxWheelPx
        }
        val wheelSize = wheelPx.toDp()
        val wheelPlaceables = subcompose(GameColumnSlot.Wheel) { wheel(wheelSize) }
            .map { it.measure(Constraints.fixed(wheelPx, wheelPx)) }

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
            y += height(keyPlaceables) + gapPx
            val actionsY = max(y, layoutHeight - height(actionPlaceables))
            actionPlaceables.forEach { it.place(0, actionsY) }
        }
    }
}
