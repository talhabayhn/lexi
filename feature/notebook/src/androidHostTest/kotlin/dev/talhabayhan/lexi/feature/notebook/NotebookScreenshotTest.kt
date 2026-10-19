package dev.talhabayhan.lexi.feature.notebook

import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.LexiViewport
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.Level
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The stateless notebook screen at the size and scale of `docs/design/screens/08-notebook.png` (390 × 844dp,
 * xhdpi = 2×), in Turkish like the design.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class NotebookScreenshotTest {

    @Test
    fun designSample() {
        captureRoboImage("src/androidHostTest/screenshots/Notebook.png") {
            LexiTheme(reducedMotion = false) { NotebookScreen(state = NotebookSampleState, onIntent = {}) }
        }
    }

    @Test
    fun emptyNotebook() {
        captureRoboImage("src/androidHostTest/screenshots/NotebookEmpty.png") {
            LexiTheme(reducedMotion = false) {
                NotebookScreen(
                    state = NotebookUiState(tab = NotebookTab.All, isLoaded = true),
                    onIntent = {},
                )
            }
        }
    }

    @Test
    fun noReviewDue() {
        captureRoboImage("src/androidHostTest/screenshots/NotebookNothingDue.png") {
            LexiTheme(reducedMotion = false) {
                NotebookScreen(state = NotebookUiState(allCount = 12, isLoaded = true), onIntent = {})
            }
        }
    }

    /** Long English words and clues wrap or ellipsise inside the row; the mastered and many-days meta lines. */
    @Test
    @Config(qualifiers = "en-w390dp-h844dp-xhdpi")
    fun longTextEnglish() {
        captureRoboImage("src/androidHostTest/screenshots/NotebookLongTextEn.png") {
            LexiTheme(reducedMotion = false) {
                NotebookScreen(
                    state = NotebookUiState(
                        tab = NotebookTab.All,
                        words = persistentListOf(
                            WordUi(
                                CardId("c1-misunderstanding"),
                                "misunderstanding",
                                "yanlis anlama, anlasmazlik, iki tarafin birbirini yanlis anlamasi",
                                Level.C1,
                                4,
                                NextReview.Mastered,
                            ),
                            WordUi(
                                CardId("nat-break-the-ice"),
                                "break the ice",
                                "buzlari eritmek",
                                Level.NAT,
                                2,
                                NextReview.InDays(21),
                            ),
                            WordUi(CardId("a1-egg"), "egg", "yumurta", Level.A1, 1, NextReview.Today),
                        ),
                        dueCount = 1,
                        allCount = 412,
                        hardCount = 31,
                        isLoaded = true,
                    ),
                    onIntent = {},
                )
            }
        }
    }

    /** DESIGN-DEBT: the word detail sheet (no design) over the list. */
    @Test
    fun wordSheet() {
        captureRoboImage("src/androidHostTest/screenshots/NotebookWordSheet.png") {
            LexiTheme(reducedMotion = true) {
                NotebookScreen(state = NotebookSampleState.copy(detail = NotebookSampleDetail), onIntent = {})
            }
        }
    }

    @Test
    @Config(qualifiers = "en-w390dp-h844dp-xhdpi")
    fun wordSheetEnglish() {
        captureRoboImage("src/androidHostTest/screenshots/NotebookWordSheetEn.png") {
            LexiTheme(reducedMotion = true) {
                NotebookScreen(state = NotebookSampleState.copy(detail = NotebookSampleDetail), onIntent = {})
            }
        }
    }

    /** Design debt: the notebook could not be read; an empty state with a retry in place of the list. */
    @Test
    fun readFailed() {
        captureRoboImage("src/androidHostTest/screenshots/NotebookError.png") {
            LexiTheme(reducedMotion = false) {
                NotebookScreen(state = NotebookUiState(isLoaded = true, hasError = true), onIntent = {})
            }
        }
    }

    /** Expanded tier (`LexiViewport`): the design scaled, the column centred and at most 430dp (× scale) wide. */
    @Test
    @Config(qualifiers = "tr-w600dp-h1100dp-mdpi")
    fun expandedW600h1100() {
        captureRoboImage("src/androidHostTest/screenshots/Notebook_w600h1100.png") {
            LexiTheme(reducedMotion = false) {
                LexiViewport { NotebookScreen(state = NotebookSampleState, onIntent = {}) }
            }
        }
    }

    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi")
    fun w320h568() = capture("Notebook_w320h568")

    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi", fontScale = 1.3f)
    fun w320h568FontScale130() = capture("Notebook_w320h568_fontScale130")

    @Test
    @Config(qualifiers = "tr-w360dp-h640dp-xhdpi")
    fun w360h640() = capture("Notebook_w360h640")

    @Test
    @Config(qualifiers = "tr-w360dp-h640dp-xhdpi", fontScale = 1.3f)
    fun w360h640FontScale130() = capture("Notebook_w360h640_fontScale130")

    @Test
    @Config(qualifiers = "tr-w600dp-h960dp-mdpi")
    fun w600h960() = capture("Notebook_w600h960")

    @Test
    @Config(qualifiers = "tr-w600dp-h960dp-mdpi", fontScale = 1.3f)
    fun w600h960FontScale130() = capture("Notebook_w600h960_fontScale130")

    @Test
    @Config(qualifiers = "tr-w390dp-h844dp-xhdpi", fontScale = 2.0f)
    fun fontScale200() = capture("Notebook_fontScale200")

    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi", fontScale = 2.0f)
    fun w320h568FontScale200() = capture("Notebook_w320h568_fontScale200")

    private fun capture(name: String) {
        captureRoboImage("src/androidHostTest/screenshots/$name.png") {
            LexiTheme(reducedMotion = false) {
                LexiViewport {
                    NotebookScreen(state = NotebookSampleState, onIntent = {})
                }
            }
        }
    }
}
