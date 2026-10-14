package dev.talhabayhan.lexi.core.designsystem.modifier

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import dev.talhabayhan.lexi.core.designsystem.theme.LexiShadow

/**
 * Bauhaus hard shadow (CSS `box-shadow: 5px 5px 0 #111111`) from a token: `Modifier.hardShadow(LexiTheme.shadows.s5)`.
 * Preferred API. [shape] is filled with the shadow color, drawn behind the element and offset right and down by the
 * token's offset. No blur, no elevation, no layout space (like CSS, it may draw outside the bounds). Apply it before
 * `background`/`border` in the chain and on an opaque element: the part under the element is not cut out.
 */
public fun Modifier.hardShadow(shadow: LexiShadow, shape: Shape = RectangleShape): Modifier =
    hardShadow(shadow.offset, shadow.color, shape)

/**
 * [hardShadow] with an explicit offset and color, for values that are not a `LexiTheme.shadows` token yet (add the
 * token instead when a screen needs it).
 */
public fun Modifier.hardShadow(offset: Dp, color: Color, shape: Shape = RectangleShape): Modifier =
    hardShadow(offset, color, shape, visibleFraction = { 1f })

/**
 * [hardShadow] whose offset is scaled by [visibleFraction] (0 = collapsed under the element, 1 = full offset).
 * The fraction is read in the draw phase only, so animating it does not recompose.
 */
internal fun Modifier.hardShadow(offset: Dp, color: Color, shape: Shape, visibleFraction: () -> Float): Modifier =
    drawWithCache {
        val outline = shape.createOutline(size, layoutDirection, this)
        val offsetPx = offset.toPx()
        onDrawBehind {
            val shift = offsetPx * visibleFraction()
            if (shift > 0f) {
                translate(left = shift, top = shift) { drawOutline(outline, color) }
            }
        }
    }
