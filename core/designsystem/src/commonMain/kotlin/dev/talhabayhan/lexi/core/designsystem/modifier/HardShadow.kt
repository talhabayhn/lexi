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
import dev.talhabayhan.lexi.core.designsystem.theme.LightLexiColors

/**
 * Bauhaus hard shadow (CSS `box-shadow: 5px 5px 0 #111111`): [shape] filled with [color], drawn behind the element
 * and offset by [offset] right and down. No blur, no elevation, no layout space (like CSS, it may draw outside the
 * bounds). Apply it before `background`/`border` in the chain and on an opaque element: the part under the element
 * is not cut out.
 */
public fun Modifier.hardShadow(
    offset: Dp,
    color: Color = LightLexiColors.ink,
    shape: Shape = RectangleShape,
): Modifier = hardShadow(offset, color, shape, visibleFraction = { 1f })

/** [hardShadow] with a token from `LexiTheme.shadows`. */
public fun Modifier.hardShadow(shadow: LexiShadow, shape: Shape = RectangleShape): Modifier =
    hardShadow(shadow.offset, shadow.color, shape)

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
