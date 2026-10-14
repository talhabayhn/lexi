package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/**
 * Renders the iOS App Store icon from [LexiAppIcon]: the home-screen variant of option 1 (no leaf, `13-logo-and-icon`)
 * without the frame, since iOS draws the mask. At mdpi 1dp = 1px, so 1024dp is the 1024×1024 single-size AppIcon.
 * Written as an opaque RGB PNG (App Store icons must have no alpha channel) to [OUTPUT]; copy it into the asset
 * catalog with `./gradlew :core:designsystem:exportIosAppIcon`.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w1100dp-h1200dp-mdpi")
class AppIconRenderTest {

    @Test
    fun rendersOpaqueIosAppIcon() = runComposeUiTest {
        setContent {
            LexiTheme(reducedMotion = false) {
                Box(Modifier.testTag(TAG)) {
                    LexiAppIcon(size = SIZE.dp, showLeaf = false, framed = false)
                }
            }
        }
        val bitmap = onNodeWithTag(TAG).captureToImage().asAndroidBitmap()
        assertEquals(SIZE, bitmap.width)
        assertEquals(SIZE, bitmap.height)

        val pixels = IntArray(SIZE * SIZE)
        bitmap.getPixels(pixels, 0, SIZE, 0, 0, SIZE, SIZE)
        assertEquals("icon must be fully opaque", 0, pixels.count { it ushr 24 != OPAQUE })

        val image = BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB)
        image.setRGB(0, 0, SIZE, SIZE, pixels, 0, SIZE)
        OUTPUT.parentFile.mkdirs()
        ImageIO.write(image, "png", OUTPUT)
    }

    private companion object {
        const val TAG = "appIcon"
        const val SIZE = 1024
        const val OPAQUE = 0xFF

        /** Relative to the module directory (the test working directory); see `exportIosAppIcon`. */
        val OUTPUT = File("build/app-icon/app-icon-1024.png")
    }
}
