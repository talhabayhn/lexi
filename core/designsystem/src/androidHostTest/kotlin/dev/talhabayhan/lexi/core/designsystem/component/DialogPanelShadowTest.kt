package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test
import kotlin.test.assertEquals

/** Regression: the dialog's scroll viewport clipped the primary button's hard shadow (s4) in every dialog. */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class DialogPanelShadowTest {

    @Test
    fun `the primary dialog button keeps its hard shadow`() = runComposeUiTest {
        var ink = 0
        setContent {
            LexiTheme(reducedMotion = true) {
                ink = LexiTheme.colors.ink.toArgb()
                DialogPanel(title = "Başlık", message = "Mesaj") {
                    DialogButton("Evet", onClick = {}, primary = true)
                }
            }
        }

        val button = onNodeWithText("Evet").getBoundsInRoot()
        val image = onRoot().captureToImage().asAndroidBitmap()
        // 2dp into the 4dp shadow, below the button's bottom edge, halfway across.
        val x = with(density) { ((button.left + button.right) / 2).roundToPx() }
        val y = with(density) { (button.bottom + 2.dp).roundToPx() }
        assertEquals(ink, image.getPixel(x, y), "no shadow below the primary button")
    }
}
