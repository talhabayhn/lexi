package dev.talhabayhan.lexi.feature.game.result

import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.LexiViewport
import dev.talhabayhan.lexi.core.domain.game.GameMode
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** `docs/design/screens/06-result.png` at its size and scale (390 × 844dp, xhdpi = 2×), Turkish like the design. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class ResultScreenScreenshotTest {

    @Test
    fun designSample() {
        captureRoboImage("src/androidHostTest/screenshots/ResultScreen.png") {
            LexiTheme(reducedMotion = false) { ResultScreen(state = ResultSamples.design, onIntent = {}) }
        }
    }

    /** English UI with long answers, clues and examples: rows wrap, the screen scrolls instead of clipping. */
    @Test
    @Config(qualifiers = "en-w390dp-h844dp-xhdpi")
    fun longTextEnglish() {
        captureRoboImage("src/androidHostTest/screenshots/ResultScreenLongTextEnglish.png") {
            LexiTheme(reducedMotion = false) { ResultScreen(state = ResultSamples.longText, onIntent = {}) }
        }
    }

    /** Daily wheel: one scored attempt per day, so TEKRAR OYNA is hidden and Paylaş fills the row. */
    @Test
    fun daily() {
        captureRoboImage("src/androidHostTest/screenshots/ResultScreenDaily.png") {
            LexiTheme(reducedMotion = false) {
                ResultScreen(
                    state = ResultSamples.design.copy(mode = GameMode.DAILY),
                    onIntent = {},
                )
            }
        }
    }

    /** Design debt: a wheel without misses shows the component library's empty state. */
    @Test
    fun noMisses() {
        captureRoboImage("src/androidHostTest/screenshots/ResultScreenNoMisses.png") {
            LexiTheme(reducedMotion = false) {
                ResultScreen(
                    state = ResultSamples.design.copy(
                        wrong = 0,
                        passed = 0,
                        correct = 26,
                        addedToNotebook = 0,
                        rows = kotlinx.collections.immutable.persistentListOf(),
                        hiddenRows = 0,
                    ),
                    onIntent = {},
                )
            }
        }
    }

    /** Design debt: Practice is unscored, so the header shows the correct answers instead of "0 puan"; no time left. */
    @Test
    fun practice() {
        captureRoboImage("src/androidHostTest/screenshots/ResultScreenPractice.png") {
            LexiTheme(reducedMotion = false) { ResultScreen(state = ResultSamples.practice, onIntent = {}) }
        }
    }

    /** Design debt: the review cards could not be read; the list offers a retry. */
    @Test
    fun reviewError() {
        captureRoboImage("src/androidHostTest/screenshots/ResultScreenReviewError.png") {
            LexiTheme(reducedMotion = false) { ResultScreen(state = ResultSamples.reviewError, onIntent = {}) }
        }
    }

    /** Expanded tier (`LexiViewport`): the design scaled, the column centred and at most 430dp (× scale) wide. */
    @Test
    @Config(qualifiers = "tr-w600dp-h1100dp-mdpi")
    fun expandedW600h1100() {
        captureRoboImage("src/androidHostTest/screenshots/ResultScreen_w600h1100.png") {
            LexiTheme(reducedMotion = false) {
                LexiViewport { ResultScreen(state = ResultSamples.design, onIntent = {}) }
            }
        }
    }
}
