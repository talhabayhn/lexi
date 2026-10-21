package dev.talhabayhan.lexi.feature.levels

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.model.Level
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The level select fits the screen (`screen-map.md`): at every phone size BAŞLA is fully visible without scrolling and
 * the parts above it do not overlap.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class LevelSelectLayoutTest {

    @Test
    fun w390h844() = runComposeUiTest { setScreen().also { assertFits() } }

    @Test
    @Config(qualifiers = "tr-w360dp-h640dp-xhdpi")
    fun w360h640() = runComposeUiTest { setScreen().also { assertFits() } }

    @Test
    @Config(qualifiers = "tr-w412dp-h915dp-xhdpi")
    fun w412h915() = runComposeUiTest { setScreen().also { assertFits() } }

    /** Last resort (tiny screen or huge font): the middle part scrolls, BAŞLA stays pinned and visible. */
    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi")
    fun w320h568() = runComposeUiTest { setScreen().also { assertStartVisible() } }

    @Test
    @Config(qualifiers = "tr-w360dp-h640dp-xhdpi", fontScale = 2.0f)
    fun w360h640FontScale200() = runComposeUiTest { setScreen().also { assertStartVisible() } }

    private fun ComposeUiTest.assertStartVisible() {
        val screen = onRoot().fetchSemanticsNode().boundsInRoot
        val start = bounds(hasText("BAŞLA"))
        assertTrue(start.top >= 0f && start.bottom <= screen.bottom + 1f, "BAŞLA $start outside $screen")
    }

    @Test
    fun categoryRowOpensCategories() = runComposeUiTest {
        val intents = mutableListOf<LevelSelectIntent>()
        setContent { LexiTheme(reducedMotion = true) { LevelSelectScreen(LevelSelectSampleState, intents::add) } }
        // A tier measured but not chosen stays composed, unplaced; the placed one is listed first.
        onAllNodes(hasContentDescription("Kategori seç", substring = true)).onFirst().performClick()
        assertEquals(listOf<LevelSelectIntent>(LevelSelectIntent.OpenCategories), intents)
    }

    @Test
    fun natHasNoCategoryRow() = runComposeUiTest {
        setContent {
            LexiTheme(reducedMotion = true) {
                LevelSelectScreen(LevelSelectSampleState.copy(selectedLevel = Level.NAT), onIntent = {})
            }
        }
        onNode(hasContentDescription("Kategori seç", substring = true)).assertDoesNotExist()
    }

    private fun ComposeUiTest.setScreen() {
        setContent {
            LexiTheme(reducedMotion = true) { LevelSelectScreen(LevelSelectSampleState, onIntent = {}) }
        }
    }

    private fun ComposeUiTest.bounds(matcher: SemanticsMatcher): Rect =
        onAllNodes(matcher).fetchSemanticsNodes().maxBy { it.boundsInRoot.top }.boundsInRoot

    private fun ComposeUiTest.assertFits() {
        val screen = onRoot().fetchSemanticsNode().boundsInRoot
        val start = bounds(hasText("BAŞLA"))
        assertTrue(start.top >= 0f && start.bottom <= screen.bottom + 1f, "BAŞLA $start outside $screen")
        val title = bounds(hasText("Seviyeni seç"))
        val firstCard = bounds(hasText("Başlangıç"))
        val lastCard = bounds(hasText("Deyim", substring = true))
        val modeLabel = bounds(hasText("OYUN MODU"))
        val category = bounds(hasContentDescription("Kategori seç", substring = true))
        val ordered = listOf(
            "title" to title,
            "first card" to firstCard,
            "last card" to lastCard,
            "mode label" to modeLabel,
            "category" to category,
            "start" to start,
        )
        ordered.zipWithNext().forEach { (above, below) ->
            assertTrue(above.second.top < below.second.top, "${above.first} is not above ${below.first}: $ordered")
        }
        assertTrue(lastCard.bottom <= modeLabel.top + 1f, "cards overlap the mode picker: $lastCard / $modeLabel")
        assertTrue(modeLabel.bottom <= category.top, "mode picker overlaps the category row: $modeLabel / $category")
        assertTrue(category.bottom <= start.top, "category row overlaps BAŞLA: $category / $start")
        val texts = onAllNodes(SemanticsMatcher("text") { it.config.getOrNull(SemanticsProperties.Text) != null })
            .fetchSemanticsNodes()
        texts.forEach {
            val r = it.boundsInRoot
            assertTrue(r.top >= -1f && r.bottom <= screen.bottom + 1f, "text outside the screen: $r")
        }
    }
}
