package dev.talhabayhan.lexi.feature.game.result

import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
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
}
