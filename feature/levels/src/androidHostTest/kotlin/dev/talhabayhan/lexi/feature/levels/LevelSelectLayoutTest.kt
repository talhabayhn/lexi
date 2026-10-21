package dev.talhabayhan.lexi.feature.levels

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.swipeUp
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

    /** `03-level-select` at 390 × 844: the Regular tier, 150dp cards, the design's top offset, the row above BAŞLA. */
    @Test
    fun w390h844IsTheDesign() = runComposeUiTest {
        setScreen()
        val d = density.density
        val cards = onAllNodes(
            SemanticsMatcher("radio") {
                it.config.getOrNull(SemanticsProperties.Role) ==
                    Role.RadioButton
            },
        )
            .fetchSemanticsNodes().filter { it.boundsInRoot.top > 0f }
        assertEquals(6, cards.size, "placed level cards")
        cards.forEach { assertEquals(150f, it.boundsInRoot.height / d, 1f, "card height") }
        assertEquals(86f, cards.minOf { it.boundsInRoot.top } / d, 1f, "grid top (24 + 44 + 18)")
        val category = bounds(hasContentDescription("Kategori seç", substring = true))
        assertTrue(category.height / d >= 48f - 1f, "touch target ${category.height / d}")
        assertTrue(bounds(hasText("BAŞLA")).top - category.bottom >= 12f * d - 1f, "gap to BAŞLA")
    }

    /** Tiny screen: scrolled to the end, the mode picker and the category row clear BAŞLA. */
    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi")
    fun w320h568ScrollsToTheCategoryRow() = runComposeUiTest {
        setScreen()
        val scrollables = onAllNodes(hasScrollAction())
        val nodes = scrollables.fetchSemanticsNodes()
        val placed = nodes.indexOf(nodes.maxBy { it.boundsInRoot.bottom })
        scrollables[placed].performTouchInput { swipeUp() }
        waitForIdle()
        val start = bounds(hasText("BAŞLA"))
        val modeLabel = bounds(hasText("OYUN MODU"))
        val category = bounds(hasContentDescription("Kategori seç", substring = true))
        assertTrue(modeLabel.bottom <= start.top, "mode label under BAŞLA: $modeLabel / $start")
        assertTrue(category.bottom <= start.top && category.top >= 0f, "category row not reachable: $category / $start")
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
