package dev.talhabayhan.lexi.core.designsystem.motion

import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.unit.Dp

/**
 * Sets the layer's camera so a `rotationX` / `rotationY` looks like CSS `perspective(<distance>)`. `cameraDistance` is
 * in Skia camera units of 72 px (Android's RenderNode and Compose's Skiko layers both multiply it by 72), so the
 * distance in pixels is divided by that.
 */
public fun GraphicsLayerScope.perspective(distance: Dp) {
    cameraDistance = distance.toPx() / CAMERA_UNIT_PX
}

private const val CAMERA_UNIT_PX = 72f
