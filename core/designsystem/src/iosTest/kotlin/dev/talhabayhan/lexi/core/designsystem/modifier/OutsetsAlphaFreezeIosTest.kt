package dev.talhabayhan.lexi.core.designsystem.modifier

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
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
 * Root cause of the 1.1.2 freeze (empty picker grid, hidden game board) on the iOS simulator's Skia renderer: a
 * `graphicsLayer` with outsets whose alpha changes after its parent was recorded keeps the old alpha, because Compose
 * 1.12's Skia layer bakes it into a `saveLayer` in the **parent's** picture. [fadeWithShadowRoom] replaces it.
 */
@OptIn(ExperimentalTestApi::class)
class OutsetsAlphaFreezeIosTest {

    private val room = LayerOutsets(right = 8.dp, bottom = 8.dp)

    /** Centre of a 40dp red square whose fade goes 0 → 0.5 → 1 in three idle frames. */
    private fun centerAfterFadeIn(fade: (alpha: () -> Float) -> Modifier): Color {
        var pixel = Color.Unspecified
        runComposeUiTest {
            var alpha by mutableFloatStateOf(0f)
            setContent {
                Box(Modifier.background(Color.White).size(60.dp)) {
                    Box(fade { alpha }.size(40.dp).background(Color.Red))
                }
            }
            waitForIdle()
            alpha = 0.5f
            waitForIdle()
            alpha = 1f
            waitForIdle()
            val c = with(density) { 20.dp.roundToPx() }
            pixel = onRoot().captureToImage().toPixelMap()[c, c]
        }
        return pixel
    }

    private fun Color.isRed() = red > 0.9f && green < 0.1f && blue < 0.1f

    private fun Color.isWhite() = abs(red - 1f) < 0.03f && abs(green - 1f) < 0.03f && abs(blue - 1f) < 0.03f

    @Test
    fun aLayerWithoutOutsetsEndsItsFadeInVisible() {
        val p = centerAfterFadeIn { alpha -> Modifier.graphicsLayer { this.alpha = alpha() } }
        assertTrue(p.isRed(), "control: $p")
    }

    @Test
    fun aLayerWithOutsetsKeepsItsFirstAlpha() {
        val p = centerAfterFadeIn { alpha ->
            Modifier.graphicsLayer {
                this.alpha = alpha()
                outsets = room
            }
        }
        // If this ever fails, Compose fixed the bug: outsets could be used again (`fadeWithShadowRoom`).
        assertTrue(p.isWhite(), "expected the Compose 1.12 Skia bug (alpha frozen at 0): $p")
    }

    @Test
    fun fadeWithShadowRoomEndsItsFadeInVisible() {
        val p = centerAfterFadeIn { alpha -> Modifier.fadeWithShadowRoom(room) { alpha() } }
        assertTrue(p.isRed(), "fade-in frozen: $p")
    }

    @Test
    fun fadeWithShadowRoomKeepsTheHardShadowWhileFading() {
        var pixel = Color.Unspecified
        runComposeUiTest {
            setContent {
                Box(Modifier.background(Color.White).padding(10.dp)) {
                    Box(
                        Modifier
                            .fadeWithShadowRoom(hardShadowOutsets(6.dp)) { 0.5f }
                            .size(40.dp)
                            .hardShadow(6.dp, Color.Black)
                            .background(Color.Red),
                    )
                }
            }
            // 3dp below the element, inside its 6dp shadow.
            val x = with(density) { 30.dp.roundToPx() }
            val y = with(density) { 53.dp.roundToPx() }
            pixel = onRoot().captureToImage().toPixelMap()[x, y]
        }
        assertTrue(abs(pixel.red - 0.5f) < 0.1f && pixel.blue < 0.6f, "shadow missing: $pixel")
    }
}
