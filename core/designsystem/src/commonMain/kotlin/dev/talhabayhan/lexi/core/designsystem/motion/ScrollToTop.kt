package dev.talhabayhan.lexi.core.designsystem.motion

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.grid.LazyGridState
import kotlin.math.min

/**
 * Motion #19 ("Başa dön ↑"): scrolls a grid to its top in [durationMillis] whatever the distance (an ease-out
 * [easing]); the distance beyond [maxStretchPx] is skipped in one step first, so only the last stretch is animated.
 * Runs at the default scroll priority: the user's own drag takes over and cancels it (nothing snaps afterwards).
 * Reads the grid's layout only (no recomposition per frame); the position is the grid's own scroll.
 */
public suspend fun LazyGridState.animateScrollToTop(durationMillis: Int, easing: Easing, maxStretchPx: Float) {
    val distance = distanceToTopPx()
    if (distance <= 0f) {
        if (firstVisibleItemIndex != 0 || firstVisibleItemScrollOffset != 0) scrollToItem(0)
        return
    }
    val stretch = min(distance, maxStretchPx)
    scroll {
        // The part of a very long list that would fly past in a few frames: skipped at once, then the stretch.
        if (distance > stretch) scrollBy(-(distance - stretch))
        val clock = Animatable(0f)
        var done = 0f
        clock.animateTo(stretch, tween(durationMillis, easing = easing)) {
            val step = value - done
            done = value
            // Negative = towards the top. An estimate that overshoots just has nothing left to consume (0 returned).
            if (step > 0f) scrollBy(-step)
        }
    }
    // The distance is an estimate (rows can differ in height): land exactly on the top.
    if (firstVisibleItemIndex != 0 || firstVisibleItemScrollOffset != 0) scrollToItem(0)
}

/** Pixels from the first visible position to the top, from the rows laid out now (a row's pitch × the rows above). */
internal fun LazyGridState.distanceToTopPx(): Float {
    val visible = layoutInfo.visibleItemsInfo
    val first = visible.firstOrNull() ?: return 0f
    val nextRow = visible.firstOrNull { it.row > first.row }
    val pitch = if (nextRow != null) {
        (nextRow.offset.y - first.offset.y).toFloat()
    } else {
        (first.size.height + layoutInfo.mainAxisItemSpacing).toFloat()
    }
    return firstVisibleItemScrollOffset + first.row * pitch
}
