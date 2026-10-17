package dev.talhabayhan.lexi.feature.game

import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.feature.game.result.ResultSamples
import dev.talhabayhan.lexi.feature.game.result.ResultScreen
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * English store stills, not regression tests: the real screens and sample cards in the English UI, Turkish clues and
 * meanings exactly as the app shows them. Written to `build/store-screens/en/` for `tools/store-graphics`; runs only
 * with `LEXI_STORE_FOOTAGE=1`.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "en-w390dp-h844dp-xhdpi")
class StoreScreensCapture {

    @Before
    fun onlyForStore() = assumeTrue(System.getenv("LEXI_STORE_FOOTAGE") == "1")

    @Test
    fun game() {
        captureRoboImage("$DIR/Game.png") {
            LexiTheme(reducedMotion = false) {
                GameScreen(state = GameSamples.design, clock = { GameSamples.designClock }, onIntent = {})
            }
        }
    }

    @Test
    fun result() {
        captureRoboImage("$DIR/Result.png") {
            LexiTheme(reducedMotion = false) { ResultScreen(state = ResultSamples.design, onIntent = {}) }
        }
    }

    private companion object {
        const val DIR = "build/store-screens/en"
    }
}
