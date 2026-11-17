package dev.talhabayhan.lexi.feature.levels.picker

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.runComposeUiTest
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.ExamPackId
import dev.talhabayhan.lexi.core.domain.model.Level
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals

/** "Çarkını seç" and its dialog send their intents with radio-group semantics (`video-flow-notes.md` §2–§3). */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class CategoryPickerScreenTest {

    private fun run(state: CategoryPickerUiState, block: ComposeUiTest.() -> Unit): List<CategoryPickerIntent> {
        val intents = mutableListOf<CategoryPickerIntent>()
        runComposeUiTest {
            setContent { LexiTheme(reducedMotion = true) { CategoryPickerScreen(state, onIntent = { intents += it }) } }
            block()
        }
        return intents
    }

    @Test
    fun `level chips are radio buttons and a chip sends its level`() {
        val intents = run(PickerSampleState) {
            onNodeWithContentDescription("B2 seviyesi").assertIsSelected()
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton))
            onNodeWithContentDescription("NAT seviyesi").performClick()
        }
        assertEquals(listOf<CategoryPickerIntent>(CategoryPickerIntent.SelectLevel(Level.NAT)), intents)
    }

    @Test
    fun `a category card is a radio button that opens its dialog, the selected one is marked`() {
        val state = PickerSampleState.copy(selectedCategory = CategoryId("daily-life"))
        val intents = run(state) {
            onNodeWithContentDescription("Günlük hayat, 120 / 240 kelime öğrenildi")
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton))
                .assertIsSelected()
            onNodeWithContentDescription("Karışık, 412 / 850 kelime öğrenildi").performClick()
        }
        assertEquals(listOf<CategoryPickerIntent>(CategoryPickerIntent.OpenCategory(CategoryId.MIXED)), intents)
    }

    @Test
    fun `the open pack starts and the locked exams are information only`() {
        val intents = run(PickerSampleState) {
            val grid = onNode(hasScrollToNodeAction())
            grid.performScrollToNode(hasContentDescription("TOEFL, yakında"))
            onNodeWithContentDescription("TOEFL, yakında")
                .assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnClick))
            grid.performScrollToNode(hasContentDescription("LGS İngilizce", substring = true))
            onNodeWithContentDescription("LGS İngilizce", substring = true)
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
                .performClick()
        }
        assertEquals(listOf<CategoryPickerIntent>(CategoryPickerIntent.OpenPack(ExamPackId("lgs"))), intents)
    }

    @Test
    fun `NAT shows only the mixed card before the exam packs`() {
        val nat = PickerSampleState.copy(
            level = Level.NAT,
            categories = persistentListOf(PickerSampleState.categories[0]),
        )
        run(nat) {
            onNodeWithContentDescription("Karışık", substring = true).assertExists()
            onNodeWithContentDescription("Günlük hayat", substring = true).assertDoesNotExist()
        }
    }

    @Test
    fun `the dialog shows the card, the mode sentence and sends mode, time and play`() {
        val intents = run(PickerSampleState.withDialog(GameMode.HARDCORE)) {
            onNodeWithText("Yiyecek ve içecek").assertExists()
            onNodeWithText("B2 · 20 kelime · 6 öğrenildi").assertExists()
            onNodeWithText("Tek hata hakkın var. İlk yanlışta oyun biter.").assertExists()
            onNodeWithText("Hardcore").assertIsSelected()
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton))
            onNodeWithText("Pratik").performClick()
            onNodeWithText("7:00").performClick()
            onNodeWithText("OYNA").performClick()
            // The grid behind the dialog is not read while it is open.
            onNodeWithContentDescription("Karışık", substring = true).assertDoesNotExist()
        }
        assertEquals(
            listOf(
                CategoryPickerIntent.SelectMode(GameMode.PRACTICE),
                CategoryPickerIntent.SelectTime(420),
                CategoryPickerIntent.Play,
            ),
            intents,
        )
    }

    @Test
    fun `in practice the time row is disabled and says so`() {
        val intents = run(PickerSampleState.withDialog(GameMode.PRACTICE)) {
            onNodeWithText("5:00").assertIsNotEnabled().performClick()
            onNodeWithText("Pratik modda süre yok.").assertExists()
            onNodeWithText("Süre yok, istediğin kadar rahat çalış.").assertExists()
        }
        assertEquals(emptyList(), intents)
    }

    @Test
    fun `the dialog's back arrow closes it`() {
        val intents = run(PickerSampleState.withDialog()) { onNodeWithContentDescription("Kapat").performClick() }
        assertEquals(listOf<CategoryPickerIntent>(CategoryPickerIntent.CloseDialog), intents)
    }

    @Test
    fun `the grid opens at the top once the cards load and not scrolled to the exams`() {
        runComposeUiTest {
            var state by mutableStateOf(PickerSampleState.copy(categories = persistentListOf(), isLoaded = false))
            setContent { LexiTheme(reducedMotion = true) { CategoryPickerScreen(state, onIntent = {}) } }
            onNodeWithContentDescription("TOEFL, yakında").assertDoesNotExist()

            state = PickerSampleState
            waitForIdle()
            onNodeWithContentDescription("Karışık", substring = true).assertIsDisplayed()
            onNodeWithText("Başa dön").assertDoesNotExist()
        }
    }
}
