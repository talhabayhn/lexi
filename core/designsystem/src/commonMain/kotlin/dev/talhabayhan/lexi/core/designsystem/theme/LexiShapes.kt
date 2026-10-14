package dev.talhabayhan.lexi.core.designsystem.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.min

/**
 * Shapes (`docs/design/tokens.md` § Shapes). Square is the default for everything; UI corners are never rounded
 * otherwise. A diamond is [square] rotated 45° (`Modifier.rotate(45f)`), not a shape of its own.
 */
@Immutable
public class LexiShapes internal constructor(
    /** Radius 0: buttons, cards, keys, slots, "current" and "wrong" wheel letters. */
    public val square: Shape,
    /** 50 %: "correct" and "todo" wheel letters, game action buttons. */
    public val circle: Shape,
    /** `border-radius: 50% 0 50% 0`: "pass" state. */
    public val leaf: Shape,
    /**
     * `border-radius: 0 80px 80px 0` on 80 × 160 (`0 60px 60px 0` on 60 × 120): decorative side shapes whose right
     * side is a half-disc; corner radius = min(width, height / 2) ([HalfPillShape]).
     */
    public val halfPill: Shape,
)

internal val DefaultLexiShapes: LexiShapes = LexiShapes(
    square = RectangleShape,
    circle = CircleShape,
    leaf = RoundedCornerShape(topStartPercent = 50, topEndPercent = 0, bottomEndPercent = 50, bottomStartPercent = 0),
    halfPill = HalfPillShape,
)

/**
 * Rectangle with both right corners rounded by r = min(width, height / 2), as CSS resolves `border-radius: 0 W W 0`.
 * Not a `RoundedCornerShape`: that clamps each corner to half the smaller side, which leaves a flat middle on 80 × 160.
 */
internal object HalfPillShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val radius = CornerRadius(min(size.width, size.height / 2))
        return Outline.Rounded(
            RoundRect(
                rect = Rect(Offset.Zero, size),
                topLeft = CornerRadius.Zero,
                topRight = radius,
                bottomRight = radius,
                bottomLeft = CornerRadius.Zero,
            ),
        )
    }

    override fun toString(): String = "HalfPillShape"
}
