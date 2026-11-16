package dev.talhabayhan.lexi.core.designsystem.motion

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color

/** Linear interpolation of every edge; [fraction] may overshoot 0..1 (pop easing). */
public fun lerpRect(from: Rect, to: Rect, fraction: Float): Rect = Rect(
    left = from.left + (to.left - from.left) * fraction,
    top = from.top + (to.top - from.top) * fraction,
    right = from.right + (to.right - from.right) * fraction,
    bottom = from.bottom + (to.bottom - from.bottom) * fraction,
)

/**
 * Per-channel sRGB mix like the prototype's `rgb(v + (c2 − v) · e)`; Compose's `lerp(Color, Color, …)` mixes in
 * Oklab, which passes through a different middle colour. [fraction] is clamped to 0..1.
 */
public fun lerpSrgb(from: Color, to: Color, fraction: Float): Color {
    val f = fraction.coerceIn(0f, 1f)
    return Color(
        red = from.red + (to.red - from.red) * f,
        green = from.green + (to.green - from.green) * f,
        blue = from.blue + (to.blue - from.blue) * f,
        alpha = from.alpha + (to.alpha - from.alpha) * f,
    )
}
