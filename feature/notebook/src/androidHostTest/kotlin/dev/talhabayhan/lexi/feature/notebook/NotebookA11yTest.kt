package dev.talhabayhan.lexi.feature.notebook

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertAny
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Screen-reader semantics: the screen title is a heading; the "Zor" rule is readable text. */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class NotebookA11yTest {
    @Test
    fun `the title is a heading`() = runComposeUiTest {
        setContent { LexiTheme(reducedMotion = true) { NotebookScreen(NotebookSampleState, onIntent = {}) } }

        onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading)).assertAny(hasText("Kelime Defterim"))
    }

    @Test
    fun `the hard tab states its rule and its empty state explains it`() = runComposeUiTest {
        setContent {
            LexiTheme(reducedMotion = true) {
                NotebookScreen(NotebookUiState(tab = NotebookTab.Hard, allCount = 3, isLoaded = true), onIntent = {})
            }
        }

        onNodeWithText(
            "Yanlış yaptığın ya da pas geçtiğin kelimeler deftere kaydedilir. 2 veya daha fazla kez yanlış " +
                "yaptıkların Zor sayılır; paslar sayılmaz.",
        ).assertExists()
        onNodeWithText(
            "Henüz zor kelime yok. 2 veya daha fazla kez yanlış yaptığın kelimeler burada görünür; paslar sayılmaz.",
        ).assertExists()
    }

    @Test
    fun `the other tabs have no hard rule line`() = runComposeUiTest {
        setContent { LexiTheme(reducedMotion = true) { NotebookScreen(NotebookSampleState, onIntent = {}) } }

        onNodeWithText("yaptıkların Zor sayılır", substring = true).assertDoesNotExist()
    }
}
