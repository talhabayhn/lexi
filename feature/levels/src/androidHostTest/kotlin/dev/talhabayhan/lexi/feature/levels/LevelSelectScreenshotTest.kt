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
}
