package dev.talhabayhan.lexi.feature.levels

import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.LexiViewport
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.model.Level
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The stateless level select at the size and scale of `docs/design/screens/03-level-select.png` (390 × 844dp, xhdpi =
 * 2×), in Turkish like the design.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class LevelSelectScreenshotTest {

    @Test
    fun designSample() {
        captureRoboImage("src/androidHostTest/screenshots/LevelSelect.png") {
            LexiTheme(reducedMotion = false) { LevelSelectScreen(state = LevelSelectSampleState, onIntent = {}) }
        }
    }

    /** Longest start caption in Turkish: NAT · HARDCORE · 26 HARF must stay on one line. */
    @Test
    fun longestCaptionTurkish() {
        captureRoboImage("src/androidHostTest/screenshots/LevelSelectNatHardcore.png") {
            LexiTheme(reducedMotion = false) {
                LevelSelectScreen(
                    state = LevelSelectSampleState.copy(selectedLevel = Level.NAT, selectedMode = GameMode.HARDCORE),
                    onIntent = {},
                )
            }
        }
    }

    /** Longest start caption in English: NAT · PRACTICE · 26 LETTERS. */
    @Test
    @Config(qualifiers = "en-w390dp-h844dp-xhdpi")
    fun longestCaptionEnglish() {
        captureRoboImage("src/androidHostTest/screenshots/LevelSelectNatPracticeEn.png") {
            LexiTheme(reducedMotion = false) {
                LevelSelectScreen(
                    state = LevelSelectSampleState.copy(selectedLevel = Level.NAT, selectedMode = GameMode.PRACTICE),
                    onIntent = {},
                )
            }
        }
    }

    /** Design debt: the card totals could not be read; an empty state with a retry above the 0 / 0 level cards. */
    @Test
    fun readFailed() {
        captureRoboImage("src/androidHostTest/screenshots/LevelSelectError.png") {
            LexiTheme(reducedMotion = false) {
                LevelSelectScreen(state = LevelSelectUiState(hasError = true), onIntent = {})
            }
        }
    }

    /** Expanded tier (`LexiViewport`): the design scaled, the column centred and at most 430dp (× scale) wide. */
    @Test
    @Config(qualifiers = "tr-w600dp-h1100dp-mdpi")
    fun expandedW600h1100() {
        captureRoboImage("src/androidHostTest/screenshots/LevelSelect_w600h1100.png") {
            LexiTheme(reducedMotion = false) {
                LexiViewport { LevelSelectScreen(state = LevelSelectSampleState, onIntent = {}) }
            }
        }
    }

    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi")
    fun w320h568() = capture("LevelSelect_w320h568")

    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi", fontScale = 1.3f)
    fun w320h568FontScale130() = capture("LevelSelect_w320h568_fontScale130")

    @Test
    @Config(qualifiers = "tr-w360dp-h640dp-xhdpi")
    fun w360h640() = capture("LevelSelect_w360h640")

    @Test
    @Config(qualifiers = "tr-w360dp-h640dp-xhdpi", fontScale = 1.3f)
    fun w360h640FontScale130() = capture("LevelSelect_w360h640_fontScale130")

    @Test
    @Config(qualifiers = "tr-w600dp-h960dp-mdpi")
    fun w600h960() = capture("LevelSelect_w600h960")

    @Test
    @Config(qualifiers = "tr-w600dp-h960dp-mdpi", fontScale = 1.3f)
    fun w600h960FontScale130() = capture("LevelSelect_w600h960_fontScale130")

    /** Largest system font size (accessibility): nothing may clip or overlap. */
    @Test
    @Config(qualifiers = "tr-w390dp-h844dp-xhdpi", fontScale = 2.0f)
    fun fontScale200() = capture("LevelSelect_fontScale200")

    private fun capture(name: String) {
        captureRoboImage("src/androidHostTest/screenshots/$name.png") {
            LexiTheme(reducedMotion = false) {
                LexiViewport {
                    LevelSelectScreen(state = LevelSelectSampleState, onIntent = {})
                }
            }
        }
    }
}
