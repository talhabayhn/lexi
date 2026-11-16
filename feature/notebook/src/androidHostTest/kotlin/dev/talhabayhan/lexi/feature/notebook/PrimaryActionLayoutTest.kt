package dev.talhabayhan.lexi.feature.notebook

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onRoot
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
 * The notebook's review-wheel button is fully on screen and its empty states start on screen, without scrolling, on a
 * 320 × 568 phone at 100 %, 130 % and 200 % font scale, in Turkish and English (the pattern of `OnboardingLayoutTest`).
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
abstract class PrimaryActionLayoutChecks(private val labels: Labels) {
    class Labels(
        val review: String,
        val emptyAll: String,
        val emptyDue: String,
        val emptyHard: String,
        val speak: String,
        val remove: String,
    )

    @Test
    fun `notebook keeps the review wheel button on screen`() = assertVisible(labels.review, fullyVisible = true) {
        NotebookScreen(state = NotebookSampleState, onIntent = {})
    }

    @Test
    fun `empty notebook shows its message on screen`() = assertVisible(labels.emptyAll) {
        NotebookScreen(state = NotebookUiState(tab = NotebookTab.All, isLoaded = true), onIntent = {})
    }

    @Test
    fun `nothing due shows its message on screen`() = assertVisible(labels.emptyDue) {
        NotebookScreen(state = NotebookUiState(allCount = 12, isLoaded = true), onIntent = {})
    }

    @Test
    fun `empty hard tab shows its message on screen`() = assertVisible(labels.emptyHard) {
        NotebookScreen(state = NotebookUiState(tab = NotebookTab.Hard, allCount = 4, isLoaded = true), onIntent = {})
    }

    @Test
    fun `word sheet keeps its two buttons on screen`() {
        listOf(labels.speak, labels.remove).forEach { label ->
            assertVisible(label, fullyVisible = true) {
                NotebookScreen(state = NotebookSampleState.copy(detail = NotebookSampleDetail), onIntent = {})
            }
        }
    }

    private fun assertVisible(label: String, fullyVisible: Boolean = false, screen: @Composable () -> Unit) =
        runComposeUiTest {
            setContent { LexiTheme(reducedMotion = true) { LexiViewport { screen() } } }
            // The unclipped layout bounds: content scrolled out of its viewport still lies below the screen.
            val rootHeight = onRoot().fetchSemanticsNode().size.height
            val node = onAllNodes(hasText(label, substring = true)).fetchSemanticsNodes().maxBy { it.positionInRoot.y }
            val top = node.positionInRoot.y
            assertTrue(top >= 0f, "$label starts above the screen: $top")
            assertTrue(top < rootHeight, "$label starts below the screen: $top >= $rootHeight")
            if (fullyVisible) {
                val bottom = top + node.size.height
                assertTrue(bottom <= rootHeight, "$label is cut off: bottom $bottom > $rootHeight")
            }
        }
}

private val TR = PrimaryActionLayoutChecks.Labels(
    review = "Tekrar çarkını başlat",
    emptyAll = "Defterin boş",
    emptyDue = "Bugün tekrar edilecek kelime yok",
    emptyHard = "Henüz zor kelime yok",
    speak = "Dinle",
    remove = "Defterden çıkar",
)
private val EN = PrimaryActionLayoutChecks.Labels(
    review = "Start review wheel",
    emptyAll = "Your notebook is empty",
    emptyDue = "Nothing to review today",
    emptyHard = "No hard words yet",
    speak = "Listen",
    remove = "Remove from notebook",
)

@Config(sdk = [36], qualifiers = "tr-w320dp-h568dp-xhdpi")
class PrimaryActionLayoutTr100Test : PrimaryActionLayoutChecks(TR)

@Config(sdk = [36], qualifiers = "tr-w320dp-h568dp-xhdpi", fontScale = 1.3f)
class PrimaryActionLayoutTr130Test : PrimaryActionLayoutChecks(TR)

@Config(sdk = [36], qualifiers = "tr-w320dp-h568dp-xhdpi", fontScale = 2.0f)
class PrimaryActionLayoutTr200Test : PrimaryActionLayoutChecks(TR)

@Config(sdk = [36], qualifiers = "en-w320dp-h568dp-xhdpi")
class PrimaryActionLayoutEn100Test : PrimaryActionLayoutChecks(EN)

@Config(sdk = [36], qualifiers = "en-w320dp-h568dp-xhdpi", fontScale = 1.3f)
class PrimaryActionLayoutEn130Test : PrimaryActionLayoutChecks(EN)

@Config(sdk = [36], qualifiers = "en-w320dp-h568dp-xhdpi", fontScale = 2.0f)
class PrimaryActionLayoutEn200Test : PrimaryActionLayoutChecks(EN)
