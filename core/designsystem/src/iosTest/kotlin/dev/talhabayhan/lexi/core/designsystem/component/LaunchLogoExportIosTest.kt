package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runSkikoComposeUiTest
import androidx.compose.ui.unit.Density
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.AppInfo
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.IRect
import org.jetbrains.skia.Image
import platform.Foundation.NSData
import platform.Foundation.NSProcessInfo
import platform.Foundation.create
import platform.Foundation.writeToFile
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Renders the iOS launch screen's `LaunchLogo` image (`iosApp/iosApp/Assets.xcassets/LaunchLogo.imageset`,
 * `docs/PERF.md` § Launch logo) from the real composable ([LexiSplashLogo], the splash's held pose) on the iOS
 * simulator's Skia renderer, at 1x, 2x and 3x. The image is cropped symmetrically around the screen centre, so
 * `UILaunchScreen` (which centres it on the `LaunchBackground` cream) shows the blocks exactly where the first Compose
 * frame draws them. Only runs when `LEXI_LAUNCH_LOGO_DIR` is set (a no-op in CI):
 *
 * ```
 * SIMCTL_CHILD_LEXI_LAUNCH_LOGO_DIR=$PWD/iosApp/iosApp/Assets.xcassets/LaunchLogo.imageset \
 *   ./gradlew :core:designsystem:iosSimulatorArm64Test --tests '*LaunchLogoExportIosTest*'
 * ```
 */
@OptIn(ExperimentalTestApi::class)
class LaunchLogoExportIosTest {
    @Test
    fun exportTheLaunchLogo() {
        val dir = NSProcessInfo.processInfo.environment["LEXI_LAUNCH_LOGO_DIR"] as String? ?: return
        for (scale in 1..3) {
            val png = render(scale.toFloat())
            assertTrue(png.write("$dir/launch-logo@${scale}x.png"), "could not write the $scale x image")
        }
    }

    /** The held logo in a [BOX_WIDTH] x [BOX_HEIGHT] (points) box at [scale], cropped symmetrically to its blocks. */
    private fun render(scale: Float): ByteArray {
        var png = ByteArray(0)
        runSkikoComposeUiTest(size = Size(BOX_WIDTH * scale, BOX_HEIGHT * scale), density = Density(scale, 1f)) {
            setContent {
                LexiTheme(reducedMotion = false) {
                    Box(Modifier.fillMaxSize().testTag(BOX_TAG)) { LexiSplashLogo(tagline = TAGLINE) }
                }
            }
            waitForIdle()
            val logo = onAllNodesWithContentDescription(AppInfo.NAME).fetchSemanticsNodes().first().boundsInRoot
            val bitmap = onNodeWithTag(BOX_TAG).captureToImage().asSkiaBitmap()
            val left = logo.left.roundToInt()
            val top = logo.top.roundToInt()
            val crop = IRect.makeLTRB(left, top, bitmap.width - left, bitmap.height - top)
            val subset = Bitmap()
            check(bitmap.extractSubset(subset, crop)) { "crop $crop outside ${bitmap.width} x ${bitmap.height}" }
            png = Image.makeFromBitmap(subset).encodeToData(EncodedImageFormat.PNG)?.bytes ?: error("PNG encoding")
        }
        return png
    }

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    private fun ByteArray.write(path: String): Boolean = usePinned {
        NSData.create(bytes = it.addressOf(0), length = size.toULong()).writeToFile(path, atomically = true)
    }

    private companion object {
        // Any box larger than the splash's column works: the crop is symmetric around its centre.
        const val BOX_WIDTH = 340f
        const val BOX_HEIGHT = 240f
        const val BOX_TAG = "launchLogoBox"

        // Laid out invisibly at font scale 1; its text does not change the column's height (one line in both locales).
        const val TAGLINE = "A'DAN Z'YE İNGİLİZCE"
    }
}
