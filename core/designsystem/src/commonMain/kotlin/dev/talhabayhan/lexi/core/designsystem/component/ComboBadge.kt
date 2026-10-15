package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import dev.talhabayhan.lexi.core.designsystem.modifier.lexiSurface
import dev.talhabayhan.lexi.core.designsystem.theme.LexiColors
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme

/** Combo badge and burst geometry (`source/15-motion.dc.html` #7). */
public object ComboBadgeDefaults {
    /** Badge: `padding: 8px 14px` inside the 3dp border. */
    public val Padding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 8.dp)

    /** Burst shape: `width: 26px` (content-box; the bordered shapes add 2dp on each side). */
    public val BurstShapeSize: Dp = 26.dp

    /**
     * The four burst shapes (`aB1`–`aB4`): red square, yellow circle, blue leaf (2dp ink border) and an ink square.
     * Offsets from the badge centre at the middle and the end keyframe; a shape without an end rotation turns back to 0.
     */
    public val BurstShapes: List<ComboBurstShape> = listOf(
        ComboBurstShape(
            kind = DecorShapeKind.Square,
            color = { it.red },
            bordered = true,
            mid = DpOffset((-80).dp, (-60).dp),
            midRotation = 90f,
            end = DpOffset((-100).dp, (-75).dp),
            endRotation = 120f,
        ),
        ComboBurstShape(
            kind = DecorShapeKind.Circle,
            color = { it.yellow },
            bordered = true,
            mid = DpOffset(80.dp, (-55).dp),
            midRotation = 0f,
            end = DpOffset(100.dp, (-70).dp),
            endRotation = 0f,
        ),
        ComboBurstShape(
            kind = DecorShapeKind.Leaf,
            color = { it.blue },
            bordered = true,
            mid = DpOffset((-70).dp, 60.dp),
            midRotation = -90f,
            end = DpOffset((-88).dp, 75.dp),
            endRotation = 0f,
        ),
        ComboBurstShape(
            kind = DecorShapeKind.Square,
            color = { it.ink },
            bordered = false,
            mid = DpOffset(75.dp, 62.dp),
            midRotation = 0f,
            end = DpOffset(92.dp, 78.dp),
            endRotation = 0f,
        ),
    )
}

/** One shape of the #7 burst: it grows from the centre to [mid] (scale 0 → 1), then drifts to [end] and fades. */
@Immutable
public class ComboBurstShape(
    public val kind: DecorShapeKind,
    public val color: (LexiColors) -> Color,
    public val bordered: Boolean,
    public val mid: DpOffset,
    public val midRotation: Float,
    public val end: DpOffset,
    public val endRotation: Float,
)

/**
 * The "×3 SERİ" badge (`15-motion` #7): yellow, 3dp ink border, s4 shadow, 24sp/800. Announced politely when it
 * appears. Motion (scale, rotation) is applied by the caller through [modifier].
 */
@Composable
public fun ComboBadge(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = LexiTheme.typography.heading24,
        modifier = modifier
            .semantics { liveRegion = LiveRegionMode.Polite }
            .lexiSurface(
                color = LexiTheme.colors.yellow,
                border = BorderStroke(LexiTheme.borders.regular, LexiTheme.colors.ink),
                shadow = LexiTheme.shadows.s4,
            )
            .padding(ComboBadgeDefaults.Padding),
    )
}

/**
 * The four #7 burst shapes around the centre of this box scope at [progress] (0..1 of `comboDurationMillis`, linear
 * time; each keyframe segment eases out like the CSS). Read in the layer phase only. Decorative (no semantics).
 */
@Composable
public fun BoxScope.ComboBurst(progress: () -> Float) {
    val colors = LexiTheme.colors
    val motion = LexiTheme.motion
    val outline = BorderStroke(LexiTheme.borders.thin, colors.ink)
    ComboBadgeDefaults.BurstShapes.forEach { shape ->
        val size = ComboBadgeDefaults.BurstShapeSize + if (shape.bordered) LexiTheme.borders.thin * 2 else 0.dp
        DecorShape(
            kind = shape.kind,
            color = shape.color(colors),
            border = if (shape.bordered) outline else null,
            modifier = Modifier
                .align(Alignment.Center)
                .size(size)
                .graphicsLayer {
                    val p = progress().coerceIn(0f, 1f)
                    val midAt = motion.comboBurstMidFraction
                    if (p < midAt) {
                        val q = motion.easeOut.transform(p / midAt)
                        translationX = shape.mid.x.toPx() * q
                        translationY = shape.mid.y.toPx() * q
                        rotationZ = shape.midRotation * q
                        scaleX = q
                        scaleY = q
                        alpha = 1f
                    } else {
                        val q = motion.easeOut.transform((p - midAt) / (1f - midAt))
                        translationX = lerp(shape.mid.x.toPx(), shape.end.x.toPx(), q)
                        translationY = lerp(shape.mid.y.toPx(), shape.end.y.toPx(), q)
                        rotationZ = lerp(shape.midRotation, shape.endRotation, q)
                        scaleX = 1f
                        scaleY = 1f
                        alpha = 1f - q
                    }
                },
        )
    }
}

// design sample, not user-facing
@Preview
@Composable
private fun ComboBadgePreview() {
    LexiTheme(reducedMotion = false) {
        Box(
            modifier = Modifier.background(LexiTheme.colors.cream).padding(LexiTheme.dimens.gap24),
            contentAlignment = Alignment.Center,
        ) {
            ComboBurst(progress = { 0.5f })
            val rest = LexiTheme.motion.comboBadgeRestRotation
            ComboBadge("×3 SERİ", Modifier.graphicsLayer { rotationZ = rest })
        }
    }
}
