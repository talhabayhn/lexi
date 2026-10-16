package dev.talhabayhan.lexi.core.designsystem.theme

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The Expanded tier (`screen-map.md` § Responsive rules): the designs are drawn for a 390 × 844dp phone. When the
 * space inside the system bars is larger in **both** directions (e.g. "smallest display size": 480 × 1000dp at a low
 * density), every screen is drawn as the design scaled by `min(width / 390, height / 844)` (at most [MAX_SCALE]), so
 * the wheel, keys, tiles, texts, gaps, borders and shadows grow together instead of leaving holes. Phones at or below
 * the design size are untouched (scale 1). The scale never changes with the keyboard (IME insets are ignored).
 */
public object ViewportDefaults {
    /** The design canvas (`docs/design/screens`). */
    public val DesignWidth: Dp = 390.dp
    public val DesignHeight: Dp = 844.dp

    /** Upper bound of the Expanded scale (a 600 × 1300dp phone or a portrait tablet). */
    public const val MAX_SCALE: Float = 1.6f

    /** Content columns are centred and at most this wide (in design dp, so it grows with the scale). */
    public val ContentMaxWidth: Dp = 430.dp

    /** The Expanded scale for the space inside the system bars: 1 up to the design size, then proportional. */
    public fun scale(width: Dp, height: Dp): Float {
        val byWidth = width / DesignWidth
        val byHeight = height / DesignHeight
        return minOf(byWidth, byHeight, MAX_SCALE).coerceAtLeast(1f)
    }
}

/**
 * Hosts the whole app (and screen tests at large sizes): measures the space inside the system bars and display cutout
 * and provides a [LocalDensity] scaled by [ViewportDefaults.scale]. Insets stay in real pixels; screens keep adding
 * `WindowInsets.safeDrawing` on top of their design paddings.
 */
@Composable
public fun LexiViewport(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val direction = LocalLayoutDirection.current
        val insets = WindowInsets.systemBars.union(WindowInsets.displayCutout)
        val widthPx = constraints.maxWidth - insets.getLeft(density, direction) - insets.getRight(density, direction)
        val heightPx = constraints.maxHeight - insets.getTop(density) - insets.getBottom(density)
        val scale = if (constraints.hasBoundedWidth && constraints.hasBoundedHeight) {
            ViewportDefaults.scale(Dp(widthPx / density.density), Dp(heightPx / density.density))
        } else {
            1f
        }
        val scaled = remember(density, scale) {
            if (scale == 1f) density else Density(density.density * scale, density.fontScale)
        }
        CompositionLocalProvider(LocalDensity provides scaled, content = content)
    }
}

/** A screen's content column: centred and at most [ViewportDefaults.ContentMaxWidth] wide, full width below that. */
public fun Modifier.lexiContentWidth(): Modifier = this
    .wrapContentWidth(Alignment.CenterHorizontally)
    .widthIn(max = ViewportDefaults.ContentMaxWidth)
    .fillMaxWidth()
