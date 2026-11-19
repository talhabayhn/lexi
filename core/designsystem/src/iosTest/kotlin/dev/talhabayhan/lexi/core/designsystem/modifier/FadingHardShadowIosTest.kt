package dev.talhabayhan.lexi.core.designsystem.modifier

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LayerOutsets
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * The iOS renderer (Skia) side of the 1.1.1 late-shadow fix: a `graphicsLayer` with alpha < 1 clips a hard shadow that
 * lies outside its bounds, and [hardShadowOutsets] keeps it. Runs on the iOS simulator (`iosSimulatorArm64Test`).
 */
@OptIn(ExperimentalTestApi::class)
class FadingHardShadowIosTest {

    /** Pixel 3dp below the 40dp element, inside its 6dp shadow, with the element faded to half. */
    private fun shadowPixel(outsets: LayerOutsets): Color {
        var pixel = Color.Unspecified
        runComposeUiTest {
            setContent {
                Box(Modifier.background(Color.White).padding(10.dp)) {
                    Box(
                        Modifier
                            .graphicsLayer {
                                alpha = 0.5f
                                this.outsets = outsets
                            }
                            .size(40.dp)
                            .hardShadow(6.dp, Color.Black)
                            .background(Color.Red),
                    )
                }
            }
            val image = onRoot().captureToImage().toPixelMap()
            val x = with(density) { 30.dp.roundToPx() }
            val y = with(density) { 53.dp.roundToPx() }
            pixel = image[x, y]
        }
        return pixel
    }

    private fun Color.isWhite() = abs(red - 1f) < 0.03f && abs(green - 1f) < 0.03f && abs(blue - 1f) < 0.03f

    @Test
    fun aFadingLayerWithoutOutsetsClipsTheHardShadow() {
        assertTrue(shadowPixel(LayerOutsets.Zero).isWhite(), "expected the root cause: shadow clipped while fading")
    }

    @Test
    fun hardShadowOutsetsKeepTheShadowWhileFading() {
        val pixel = shadowPixel(hardShadowOutsets(6.dp))
        // Half-transparent black over white.
        assertTrue(abs(pixel.red - 0.5f) < 0.1f && pixel.blue < 0.6f, "shadow missing: $pixel")
    }
}
