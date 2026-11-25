package dev.talhabayhan.lexi.feature.levels.picker

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.runComposeUiTest
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.LexiViewport
import dev.talhabayhan.lexi.core.domain.model.Level
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * "Başa dön" on "Çarkını seç" scrolls all the way to the top (S8): from the last exam card of the longest level (B1,
 * 15 categories) and of the shortest, the title-side of the grid ends where it started, the first card (Karışık) at
 * the same position as on opening. The scroll used to stop half way: the pill left the composition once the grid was
 * back within its first screen, and took the scroll's coroutine scope (and with it the scroll) along.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
abstract class CategoryPickerScrollTopChecks {
    @Test
    @Config(qualifiers = "+w320dp-h568dp")
    fun w320h568() = check()

    @Test
    @Config(qualifiers = "+w390dp-h844dp")
    fun w390h844() = check()

    private fun check() {
        var pilled = 0
        for ((level, categories) in ShippedPicker.levels.filter { it.first == Level.B1 || it.first == Level.C1 }) {
            runComposeUiTest {
                setContent {
                    LexiTheme(reducedMotion = false) {
                        LexiViewport { CategoryPickerScreen(ShippedPicker.state(level, categories), {}) }
                    }
                }
                val mixed = SemanticsMatcher("Karışık card") { node ->
                    node.config.getOrNull(SemanticsProperties.ContentDescription).orEmpty()
                        .any { it.startsWith("Karışık, ") || it.startsWith("Mixed, ") }
                }
                val topOnOpen = onNode(mixed).fetchSemanticsNode().positionInRoot.y
                onNode(hasScrollToNodeAction()).performScrollToNode(hasContentDescription("IELTS", substring = true))
                waitForIdle()
                val pill = onAllNodes(hasText("Başa dön", substring = true) or hasText("Back to top", substring = true))
                // A short list (C1 on a tall screen) never scrolls past its first screen: no pill, nothing to test.
                if (pill.fetchSemanticsNodes().isEmpty()) return@runComposeUiTest
                pilled++
                pill[0].performClick()
                // The distance covered, with the real 400 ms ease-out.
                mainClock.advanceTimeBy(1_000)
                waitForIdle()
                val nodes = onAllNodes(mixed).fetchSemanticsNodes()
                assertTrue(nodes.isNotEmpty(), "$level: the first card is not on screen after the scroll")
                assertEquals(topOnOpen, nodes.single().positionInRoot.y, "$level: the grid did not reach its top")
            }
        }
        assertTrue(pilled > 0, "no level showed the pill: the test does not exercise the scroll")
    }
}

@Config(sdk = [36], qualifiers = "tr-xhdpi")
class CategoryPickerScrollTop100Test : CategoryPickerScrollTopChecks()

@Config(sdk = [36], qualifiers = "tr-xhdpi", fontScale = 1.3f)
class CategoryPickerScrollTop130Test : CategoryPickerScrollTopChecks()

@Config(sdk = [36], qualifiers = "tr-xhdpi", fontScale = 2.0f)
class CategoryPickerScrollTop200Test : CategoryPickerScrollTopChecks()
