package dev.talhabayhan.lexi.core.designsystem.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

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
    /** `border-radius: 0 80px 80px 0`: decorative side shapes. */
    public val halfPill: Shape,
)

internal val DefaultLexiShapes: LexiShapes = LexiShapes(
    square = RectangleShape,
    circle = CircleShape,
    leaf = RoundedCornerShape(topStartPercent = 50, topEndPercent = 0, bottomEndPercent = 50, bottomStartPercent = 0),
    halfPill = RoundedCornerShape(topStart = 0.dp, topEnd = 80.dp, bottomEnd = 80.dp, bottomStart = 0.dp),
)
