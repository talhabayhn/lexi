package dev.talhabayhan.lexi.feature.exams

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertAny
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
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class ExamPacksScreenTest {
    @Test
    fun `rows show real counts, progress and the locked pro pack, and taps go to the view model`() = runComposeUiTest {
        val intents = mutableListOf<ExamPacksIntent>()
        setContent {
            LexiTheme(reducedMotion = true) { ExamPacksScreen(ExamPacksSampleState, onIntent = { intents += it }) }
        }

        onNodeWithText("8. sınıf · ünite bazlı · 573").assertIsDisplayed()
        onNodeWithContentDescription("Yüzde 34 öğrenildi").assertIsDisplayed()
        onNodeWithText("PRO").assertIsDisplayed()
        onNodeWithText("Yakında").assertIsDisplayed()
        onNodeWithText("YDS / YÖKDİL").performClick()
        onNodeWithText("LGS İngilizce").performClick()
        onNodeWithContentDescription("Geri").performClick()

        assertEquals(
            listOf(
                ExamPacksIntent.OpenPack(ExamPackId("yds-yokdil")),
                ExamPacksIntent.OpenPack(ExamPackId("lgs")),
                ExamPacksIntent.Back,
            ),
            intents,
        )
    }

    @Test
    fun `the title is a heading and the locked toast is shown`() = runComposeUiTest {
        setContent {
            LexiTheme(reducedMotion = true) {
                ExamPacksScreen(ExamPacksSampleState.copy(lockedNotice = true), onIntent = {})
            }
        }

        onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading)).assertAny(hasText("Sınav paketleri"))
        onNodeWithText("Pro paketler çok yakında geliyor. Şimdilik ücretsiz paketlerle çalışabilirsin.")
            .assertIsDisplayed()
    }

    @Test
    fun examPacks() {
        captureRoboImage("src/androidHostTest/screenshots/ExamPacks.png") {
            LexiTheme(reducedMotion = true) { ExamPacksScreen(ExamPacksSampleState, onIntent = {}) }
        }
    }

    @Test
    fun examPacksLockedToast() {
        captureRoboImage("src/androidHostTest/screenshots/ExamPacksLockedToast.png") {
            LexiTheme(reducedMotion = true) {
                ExamPacksScreen(ExamPacksSampleState.copy(lockedNotice = true), onIntent = {})
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
