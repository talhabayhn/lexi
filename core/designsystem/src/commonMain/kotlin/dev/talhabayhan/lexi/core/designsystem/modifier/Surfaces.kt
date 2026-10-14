package dev.talhabayhan.lexi.core.designsystem.modifier

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import dev.talhabayhan.lexi.core.designsystem.theme.LexiShadow
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * A static Bauhaus box with the CSS box model (`box-sizing: border-box`): optional hard [shadow], [color] fill,
 * optional [border] drawn inside the bounds, and the content inset by the border width so HTML paddings apply
 * unchanged. Tappable elements use `PressableSurface`, which applies the same model.
 */
public fun Modifier.lexiSurface(
    color: Color,
    border: BorderStroke? = null,
    shadow: LexiShadow? = null,
    shape: Shape = RectangleShape,
): Modifier = this
    .then(if (shadow != null) Modifier.hardShadow(shadow, shape) else Modifier)
    .clip(shape)
    .background(color)
    .then(if (border != null) Modifier.border(border, shape).padding(border.width) else Modifier)

/**
 * CSS `border: <width> dashed <color>` on a rectangle, as Chrome draws it: dashes twice as long as the border is wide,
 * gaps about one width, spread so that every side starts and ends with a full dash at the corners. Drawn inside the
 * bounds like a CSS border; combine with `padding(width)` for the box model.
 */
public fun Modifier.dashedBorder(width: Dp, color: Color): Modifier = drawWithCache {
    val w = width.toPx()
    val dash = 2 * w

    fun dashes(length: Float): List<Pair<Float, Float>> {
        if (length <= dash) return listOf(0f to length)
        val count = max(2, ((length + w) / (dash + w)).roundToInt())
        val gap = (length - count * dash) / (count - 1)
        return List(count) { i -> (i * (dash + gap)) to dash }
    }
    val horizontal = dashes(size.width)
    val vertical = dashes(size.height)
    onDrawBehind {
        horizontal.forEach { (start, len) ->
            drawRect(color, Offset(start, 0f), Size(len, w))
            drawRect(color, Offset(start, size.height - w), Size(len, w))
        }
        vertical.forEach { (start, len) ->
            drawRect(color, Offset(0f, start), Size(w, len))
            drawRect(color, Offset(size.width - w, start), Size(w, len))
        }
    }
}
