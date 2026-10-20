package dev.talhabayhan.lexi.core.designsystem.capture

import android.graphics.BitmapFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w390dp-h844dp-xxhdpi")
class OffscreenCaptureTest {

    @Test
    fun `the content is captured at the fixed scale whatever the screen density and stays invisible`() =
        runComposeUiTest {
            val captured = mutableListOf<ByteArray?>()
            setContent {
                Box(Modifier.fillMaxSize().background(Color.White)) {
                    OffscreenCapture(size = DpSize(40.dp, 20.dp), scale = 2.5f, onCaptured = { captured += it }) {
                        Box(Modifier.size(40.dp, 20.dp).background(Color.Red)) { Text("hidden") }
                    }
                }
            }

            // Robolectric draws the window only when asked (a device draws every frame): draw it while waiting.
            waitUntil(timeoutMillis = 5_000) {
                onRoot().captureToImage()
                captured.isNotEmpty()
            }
            val png = assertNotNull(captured.single(), "no png")
            val bitmap = assertNotNull(BitmapFactory.decodeByteArray(png, 0, png.size), "not decoded: ${png.size}")
            assertEquals(100 to 50, bitmap.width to bitmap.height)
            assertEquals(Color.Red.toArgb(), bitmap.getPixel(bitmap.width - 1, bitmap.height - 1))
            onNodeWithText("hidden").assertDoesNotExist()
            assertEquals(Color.White.toArgb(), onRoot().captureToImage().asAndroidBitmap().getPixel(1, 1))
        }
}
