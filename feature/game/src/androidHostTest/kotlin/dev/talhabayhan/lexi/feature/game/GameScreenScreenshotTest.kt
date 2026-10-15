package dev.talhabayhan.lexi.feature.game

import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The stateless game screen at the size and scale of `docs/design/screens/04-game.png` (390 × 844dp, xhdpi = 2×), in
 * Turkish like the design. Warning and wrong-reveal use reduced motion so the #8 pulse loop does not keep the capture
 * busy (colours are the same).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class GameScreenScreenshotTest {

    @Test
    fun designSample() {
        captureRoboImage("src/androidHostTest/screenshots/GameScreen.png") {
            LexiTheme(reducedMotion = false) {
                GameScreen(state = GameSamples.design, clock = { GameSamples.designClock }, onIntent = {})
            }
        }
    }

    @Test
    fun timeWarning() {
        captureRoboImage("src/androidHostTest/screenshots/GameScreenTimeWarning.png") {
            LexiTheme(reducedMotion = true) {
                GameScreen(state = GameSamples.design, clock = { GameSamples.warningClock }, onIntent = {})
            }
        }
    }

    @Test
    fun wrongReveal() {
        captureRoboImage("src/androidHostTest/screenshots/GameScreenWrongReveal.png") {
            LexiTheme(reducedMotion = true) {
                GameScreen(state = GameSamples.wrongReveal, clock = { GameSamples.designClock }, onIntent = {})
            }
        }
    }
}
