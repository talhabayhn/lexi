package dev.talhabayhan.lexi.feature.levels.picker

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.runComposeUiTest
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.LexiViewport
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertTrue

/**
 * "Çarkını seç" and its dialog at 320 × 568, 360 × 640, 390 × 844 and 412 × 915, font scale 1.0 / 1.3 / 2.0, Turkish
 * and English: back, the level chips and the first card are on screen without scrolling; the dialog's OYNA can always
 * be scrolled fully into view (never cut off); every button is at least 48dp high.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
abstract class CategoryPickerLayoutChecks(
    private val back: String,
    private val chip: String,
    private val mixed: String,
    private val play: String,
) {
    @Test
    @Config(qualifiers = "+w320dp-h568dp")
    fun w320h568() = check()

    @Test
    @Config(qualifiers = "+w360dp-h640dp")
    fun w360h640() = check()

    @Test
    @Config(qualifiers = "+w390dp-h844dp")
    fun w390h844() = check()

    @Test
    @Config(qualifiers = "+w412dp-h915dp")
    fun w412h915() = check()

    private fun check() {
        runComposeUiTest {
            setContent {
                LexiTheme(reducedMotion = true) { LexiViewport { CategoryPickerScreen(PickerSampleState, {}) } }
            }
            val height = onRoot().fetchSemanticsNode().size.height.toFloat()
            listOf(back, chip, mixed).forEach { label ->
                assertOnScreen(
                    onNode(hasContentDescription(label, substring = true)).fetchSemanticsNode(),
                    label,
                    height,
                )
            }
            assertButtonsTallEnough()
        }
        runComposeUiTest {
            setContent {
                LexiTheme(reducedMotion = true) {
                    LexiViewport { CategoryPickerScreen(PickerSampleState.withDialog(), {}) }
                }
            }
            val height = onRoot().fetchSemanticsNode().size.height.toFloat()
            val button = onNode(hasText(play)).performScrollTo()
            assertOnScreen(button.fetchSemanticsNode(), play, height)
            assertButtonsTallEnough()
        }
    }

    private fun assertOnScreen(node: SemanticsNode, label: String, bottomLimit: Float) {
        val top = node.boundsInRoot.top
        val bottom = node.boundsInRoot.bottom
        assertTrue(top >= 0f, "$label starts above the screen: $top")
        assertTrue(bottom <= bottomLimit, "$label is cut off: bottom $bottom > $bottomLimit")
    }

    private fun ComposeUiTest.assertButtonsTallEnough() {
        val minPx = MIN_TOUCH_DP * onRoot().fetchSemanticsNode().layoutInfo.density.density
        onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsActions.OnClick)).fetchSemanticsNodes().forEach { node ->
            val label = node.config.getOrNull(SemanticsProperties.ContentDescription)
                ?: node.config.getOrNull(SemanticsProperties.Text)
            assertTrue(node.size.height >= minPx - 1f, "$label is only ${node.size.height}px high")
        }
    }

    private companion object {
        const val MIN_TOUCH_DP = 48f
    }
}

private val TR = arrayOf("Geri", "B2 seviyesi", "Karışık", "OYNA")
private val EN = arrayOf("Back", "Level B2", "Mixed", "PLAY")

@Config(sdk = [36], qualifiers = "tr-xhdpi")
class CategoryPickerLayoutTr100Test : CategoryPickerLayoutChecks(TR[0], TR[1], TR[2], TR[3])

@Config(sdk = [36], qualifiers = "tr-xhdpi", fontScale = 1.3f)
class CategoryPickerLayoutTr130Test : CategoryPickerLayoutChecks(TR[0], TR[1], TR[2], TR[3])

@Config(sdk = [36], qualifiers = "tr-xhdpi", fontScale = 2.0f)
class CategoryPickerLayoutTr200Test : CategoryPickerLayoutChecks(TR[0], TR[1], TR[2], TR[3])

@Config(sdk = [36], qualifiers = "en-xhdpi")
class CategoryPickerLayoutEn100Test : CategoryPickerLayoutChecks(EN[0], EN[1], EN[2], EN[3])

@Config(sdk = [36], qualifiers = "en-xhdpi", fontScale = 1.3f)
class CategoryPickerLayoutEn130Test : CategoryPickerLayoutChecks(EN[0], EN[1], EN[2], EN[3])

@Config(sdk = [36], qualifiers = "en-xhdpi", fontScale = 2.0f)
class CategoryPickerLayoutEn200Test : CategoryPickerLayoutChecks(EN[0], EN[1], EN[2], EN[3])
