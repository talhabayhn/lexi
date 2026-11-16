package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.talhabayhan.lexi.core.designsystem.theme.resized

/**
 * A headline kept on one line: [style]'s size, or smaller in 0.5sp steps down to [minFontSize] until [text] fits the
 * width. [minFontSize] is the size at font scale 1 and does not grow with the system font scale (a larger font must
 * not make the floor larger than the line). Never ellipsized: if the text is still too wide at the minimum, it wraps.
 */
@Composable
public fun FitOneLineText(text: String, style: TextStyle, minFontSize: TextUnit, modifier: Modifier = Modifier) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val fonts = LocalFontFamilyResolver.current
    val direction = LocalLayoutDirection.current
    // One node with whatever [modifier] adds (e.g. a heading).
    BoxWithConstraints(modifier.semantics(mergeDescendants = true) {}) {
        val maxWidth = constraints.maxWidth
        val fitted = remember(text, style, minFontSize, maxWidth, density, fonts, direction) {
            val floorPx = with(density) { minFontSize.value.dp.toPx() }
            var size = style.fontSize.value
            fun width(sp: Float) = measurer.measure(
                text = text,
                style = style.resized(sp.sp),
                softWrap = false,
                maxLines = 1,
                constraints = Constraints(),
                density = density,
                layoutDirection = direction,
                fontFamilyResolver = fonts,
            ).size.width
            if (maxWidth != Constraints.Infinity) {
                while (width(size) > maxWidth && with(density) { (size - STEP).sp.toPx() } >= floorPx) size -= STEP
            }
            size
        }
        Text(text, style = style.resized(fitted.sp))
    }
}

private const val STEP = 0.5f
