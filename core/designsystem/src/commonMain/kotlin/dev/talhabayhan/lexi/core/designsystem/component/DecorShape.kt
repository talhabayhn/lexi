package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.modifier.lexiSurface
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme

/** The Bauhaus shape vocabulary of markers and background decoration. */
public enum class DecorShapeKind {
    Square,
    Circle,
    Leaf,

    /** A square rotated 45° (CSS `transform: rotate(45deg)`, layout size unchanged). */
    Diamond,

    /** Right side fully rounded (`border-radius: 0 W W 0`), for shapes entering from the left edge. */
    HalfPill,
}

/** The [Shape] drawn for [kind] (a diamond is drawn as a rotated square). */
@Composable
public fun DecorShapeKind.shape(): Shape = when (this) {
    DecorShapeKind.Square, DecorShapeKind.Diamond -> LexiTheme.shapes.square
    DecorShapeKind.Circle -> LexiTheme.shapes.circle
    DecorShapeKind.Leaf -> LexiTheme.shapes.leaf
    DecorShapeKind.HalfPill -> LexiTheme.shapes.halfPill
}

/**
 * A filled Bauhaus shape: the markers on tiles, cards, stats and toasts (with a 2dp [border]) and the large
 * background shapes of the screens (size and position from the screen HTML, partly off-screen, edge-to-edge behind
 * content). Decorative: hidden from accessibility. Size comes from [modifier] and includes the border: most marker
 * divs in the HTML are CSS content-box, so `width: 26px; border: 2px` is a 30dp shape.
 */
@Composable
public fun DecorShape(
    kind: DecorShapeKind,
    color: Color,
    modifier: Modifier = Modifier,
    border: BorderStroke? = null,
) {
    // Only the diamond needs a layer (its rotation); the other shapes draw straight into their parent.
    val rotation = if (kind ==
        DecorShapeKind.Diamond
    ) {
        Modifier.graphicsLayer { rotationZ = DIAMOND_ROTATION }
    } else {
        Modifier
    }
    Box(
        modifier
            .clearAndSetSemantics {}
            .then(rotation)
            .lexiSurface(color = color, border = border, shape = kind.shape()),
    )
}

private const val DIAMOND_ROTATION = 45f

@Preview
@Composable
private fun DecorShapePreview() {
    LexiTheme(reducedMotion = false) {
        Row(
            modifier = Modifier.background(LexiTheme.colors.cream).padding(LexiTheme.dimens.gap20),
            horizontalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap16),
        ) {
            val colors = LexiTheme.colors
            val border = BorderStroke(LexiTheme.borders.thin, colors.ink)
            val marker = Modifier.size(30.dp)
            DecorShape(DecorShapeKind.Circle, colors.blue, marker, border)
            DecorShape(DecorShapeKind.Square, colors.red, marker, border)
            DecorShape(DecorShapeKind.Leaf, colors.yellow, marker, border)
            DecorShape(
                DecorShapeKind.Diamond,
                colors.yellow,
                marker,
                BorderStroke(LexiTheme.borders.thin, colors.cream),
            )
            DecorShape(DecorShapeKind.HalfPill, colors.blue, Modifier.size(40.dp, 80.dp))
        }
    }
}
