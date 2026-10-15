package dev.talhabayhan.lexi.feature.levels

import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
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

    /** NAT and Hardcore selected: the states the design does not show. */
    @Test
    fun otherSelection() {
        captureRoboImage("src/androidHostTest/screenshots/LevelSelectNatHardcore.png") {
            LexiTheme(reducedMotion = false) {
                LevelSelectScreen(
                    state = LevelSelectSampleState.copy(selectedLevel = Level.NAT, selectedMode = GameMode.HARDCORE),
                    onIntent = {},
                )
            }
        }
    }
}
