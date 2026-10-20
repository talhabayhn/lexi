package dev.talhabayhan.lexi.feature.game

import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The level test run on the game screen (DESIGN-DEBT: no design; compared with `04-game.png` for consistency) at
 * 390 × 844dp, xhdpi, Turkish. The reveal uses reduced motion like the game's wrong-reveal golden.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class LevelTestScreenScreenshotTest {

    @Test
    fun midTest() {
        captureRoboImage("src/androidHostTest/screenshots/LevelTestMidTest.png") {
            LexiTheme(reducedMotion = false) {
                GameScreen(state = LevelTestSamples.midTest, clock = { LevelTestSamples.midTestClock }, onIntent = {})
            }
        }
    }

    @Test
    fun reveal() {
        captureRoboImage("src/androidHostTest/screenshots/LevelTestReveal.png") {
            LexiTheme(reducedMotion = true) {
                GameScreen(state = LevelTestSamples.reveal, clock = { LevelTestSamples.midTestClock }, onIntent = {})
            }
        }
    }

    @Test
    fun unavailable() {
        captureRoboImage("src/androidHostTest/screenshots/LevelTestUnavailable.png") {
            LexiTheme(reducedMotion = false) {
                GameScreen(state = LevelTestSamples.unavailable, clock = { GameClockUi.Untimed }, onIntent = {})
            }
        }
    }
}
