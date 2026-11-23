package dev.talhabayhan.lexi.core.designsystem.modifier

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.LayerOutsets
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.InspectorInfo

/**
 * Fades this element and everything drawn after it in the chain by [alpha] (read in the draw phase, with the element's
 * size: an animation redraws without recomposing), as one group, in an offscreen layer that reaches [room] beyond its
 * bounds: hard shadows (`hardShadow`, right of and below the element) stay drawn through the fade instead of popping in
 * at the end. Alpha 1 draws plainly (no offscreen buffer); alpha 0 draws nothing.
 *
 * Use this instead of `graphicsLayer { alpha = …; outsets = … }`. Compose 1.12's Skia renderer (iOS) applies a layer
 * with outsets through a `saveLayer` recorded into its **parent's** picture with the alpha of that moment, so a later
 * alpha change is never drawn until the parent re-records: a fade-in from 0 stayed invisible (1.1.2's empty picker grid
 * and board; `OutsetsAlphaFreezeIosTest`). Here the alpha is part of this node's own drawing, so every change re-records
 * it. Keep moves and scales in a `graphicsLayer` before this modifier (layer properties only, no offscreen buffer).
 */
public fun Modifier.fadeWithShadowRoom(room: LayerOutsets, alpha: DrawScope.() -> Float): Modifier =
    this then FadeWithShadowRoomElement(room, alpha)

private class FadeWithShadowRoomElement(val room: LayerOutsets, val alpha: DrawScope.() -> Float) :
    ModifierNodeElement<FadeWithShadowRoomNode>() {
    override fun create(): FadeWithShadowRoomNode = FadeWithShadowRoomNode(room, alpha)

    override fun update(node: FadeWithShadowRoomNode) {
        node.room = room
        node.alpha = alpha
        node.invalidateDraw()
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "fadeWithShadowRoom"
        properties["room"] = room
    }

    override fun equals(other: Any?): Boolean =
        other is FadeWithShadowRoomElement && other.room == room && other.alpha === alpha

    override fun hashCode(): Int = 31 * room.hashCode() + alpha.hashCode()
}

private class FadeWithShadowRoomNode(var room: LayerOutsets, var alpha: DrawScope.() -> Float) :
    Modifier.Node(),
    DrawModifierNode {
    private val paint = Paint()

    override fun ContentDrawScope.draw() {
        val a = alpha().coerceIn(0f, 1f)
        when {
            a >= 1f -> drawContent()

            a <= 0f -> Unit

            else -> {
                paint.alpha = a
                val bounds = Rect(
                    left = -room.left.toPx(),
                    top = -room.top.toPx(),
                    right = size.width + room.right.toPx(),
                    bottom = size.height + room.bottom.toPx(),
                )
                drawIntoCanvas { it.saveLayer(bounds, paint) }
                drawContent()
                drawIntoCanvas { it.restore() }
            }
        }
    }
}
