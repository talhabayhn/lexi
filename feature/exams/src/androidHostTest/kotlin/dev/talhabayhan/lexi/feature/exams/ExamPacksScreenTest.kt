package dev.talhabayhan.lexi.feature.exams

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertAny
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.LexiViewport
import dev.talhabayhan.lexi.core.domain.model.ExamPackId
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class ExamPacksScreenTest {
    @Test
    fun `the pack shows real counts and progress and taps go to the view model`() = runComposeUiTest {
        val intents = mutableListOf<ExamPacksIntent>()
        setContent {
            LexiTheme(reducedMotion = true) { ExamPacksScreen(ExamPacksSampleState, onIntent = { intents += it }) }
        }

        onNodeWithText("8. sınıf · ünite bazlı · 573").assertIsDisplayed()
        onNodeWithContentDescription("Yüzde 34 öğrenildi").assertIsDisplayed()
        onNodeWithText("LGS İngilizce").performClick()
        onNodeWithContentDescription("Geri").performClick()

        assertEquals(listOf(ExamPacksIntent.OpenPack(ExamPackId("lgs")), ExamPacksIntent.Back), intents)
    }

    @Test
    fun `the coming soon rows are locked, not clickable and spoken as one description`() = runComposeUiTest {
        val intents = mutableListOf<ExamPacksIntent>()
        setContent {
            LexiTheme(reducedMotion = true) { ExamPacksScreen(ExamPacksSampleState, onIntent = { intents += it }) }
        }

        listOf("YDS / YÖKDİL", "YKS-DİL", "TOEFL", "IELTS").forEach { title ->
            val row = onNodeWithContentDescription("$title, yakında, kilitli")
            row.assertExists()
            row.assertHasNoClickAction()
            row.assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Role))
            row.performClick()
        }
        onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading)).assertAny(hasText("Yakında"))
        assertTrue(intents.isEmpty())
    }

    @Test
    @Config(qualifiers = "en-w390dp-h844dp-xhdpi")
    fun `the coming soon rows read in English`() = runComposeUiTest {
        setContent { LexiTheme(reducedMotion = true) { ExamPacksScreen(ExamPacksSampleState, onIntent = {}) } }

        onNodeWithText("Coming soon").assertIsDisplayed()
        onNodeWithContentDescription("YKS English, coming soon, locked").assertExists()
        onNodeWithContentDescription("TOEFL, coming soon, locked").assertHasNoClickAction()
    }

    @Test
    fun `the coming soon section is hidden while loading, on error and when there are no packs`() = runComposeUiTest {
        var state by mutableStateOf(ExamPacksUiState(isLoaded = false))
        setContent { LexiTheme(reducedMotion = true) { ExamPacksScreen(state, onIntent = {}) } }
        onNodeWithText("Yakında").assertDoesNotExist()

        state = ExamPacksUiState(isLoaded = true, hasError = true)
        waitForIdle()
        onNodeWithText("Yakında").assertDoesNotExist()

        state = ExamPacksUiState(isLoaded = true)
        waitForIdle()
        onNodeWithText("Yakında").assertDoesNotExist()
    }

    @Test
    fun `the title is a heading`() = runComposeUiTest {
        setContent { LexiTheme(reducedMotion = true) { ExamPacksScreen(ExamPacksSampleState, onIntent = {}) } }

        onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading)).assertAny(hasText("Sınav paketleri"))
    }

    @Test
    fun examPacks() {
        captureRoboImage("src/androidHostTest/screenshots/ExamPacks.png") {
            LexiTheme(reducedMotion = true) { ExamPacksScreen(ExamPacksSampleState, onIntent = {}) }
        }
    }

    @Test
    @Config(qualifiers = "en-w390dp-h844dp-xhdpi")
    fun examPacksEnglish() {
        captureRoboImage("src/androidHostTest/screenshots/ExamPacks_en.png") {
            LexiTheme(reducedMotion = true) { ExamPacksScreen(ExamPacksSampleState, onIntent = {}) }
        }
    }

    /** Long titles wrap inside the rows (narrow screen, large text) instead of overflowing. */
    @Test
    @Config(qualifiers = "tr-w320dp-h1500dp-xhdpi", fontScale = 1.5f)
    fun examPacksLongTitles() {
        captureRoboImage("src/androidHostTest/screenshots/ExamPacks_longTitles.png") {
            LexiTheme(reducedMotion = true) {
                ExamPacksScreen(
                    ExamPacksSampleState.copy(
                        packs = persistentListOf(
                            ExamPacksSampleState.packs.single().copy(
                                titleTr = "LGS İngilizce · 8. sınıf deneme sınavı hazırlık paketi",
                            ),
                        ),
                    ),
                    onIntent = {},
                )
            }
        }
    }

    @Test
    fun examPacksError() {
        captureRoboImage("src/androidHostTest/screenshots/ExamPacksError.png") {
            LexiTheme(reducedMotion = true) {
                ExamPacksScreen(ExamPacksUiState(isLoaded = true, hasError = true), onIntent = {})
            }
        }
    }

    /** Expanded tier (`LexiViewport`): the design scaled, the column centred and at most 430dp (× scale) wide. */
    @Test
    @Config(qualifiers = "tr-w600dp-h1100dp-mdpi")
    fun expandedW600h1100() {
        captureRoboImage("src/androidHostTest/screenshots/ExamPacks_w600h1100.png") {
            LexiTheme(reducedMotion = true) { LexiViewport { ExamPacksScreen(ExamPacksSampleState, onIntent = {}) } }
        }
    }

    /** Large text: rows grow instead of clipping; the list scrolls. */
    @Test
    @Config(qualifiers = "tr-w390dp-h844dp-xhdpi", fontScale = 2.0f)
    fun fontScale200() {
        captureRoboImage("src/androidHostTest/screenshots/ExamPacks_fontScale200.png") {
            LexiTheme(reducedMotion = true) { ExamPacksScreen(ExamPacksSampleState, onIntent = {}) }
        }
    }

    /** Small phone (`screen-map.md` compact tier). */
    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi")
    fun w320h568() {
        captureRoboImage("src/androidHostTest/screenshots/ExamPacks_w320h568.png") {
            LexiTheme(reducedMotion = true) { ExamPacksScreen(ExamPacksSampleState, onIntent = {}) }
        }
    }
}
