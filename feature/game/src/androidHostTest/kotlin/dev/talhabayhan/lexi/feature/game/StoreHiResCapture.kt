package dev.talhabayhan.lexi.feature.game

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runComposeUiTest
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
 * High-resolution sources for the store images (xxxhdpi = 4x, 1560 x 3376 px), not regression tests: the same screens
 * as the Roborazzi goldens, rendered at 4x so `tools/store-graphics` downsamples instead of upscaling. Written to
 * `build/store-hires/<lang>/`; runs only with `LEXI_STORE_FOOTAGE=1`. The goldens in `src/androidHostTest/screenshots`
 * stay at 2x and are not touched.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xxxhdpi")
class StoreHiResCapture {
    @Before
    fun onlyForStore() = assumeTrue(System.getenv("LEXI_STORE_FOOTAGE") == "1")

    @Test
    fun gameTr() = game("tr/GameScreen")

    @Test
    @Config(qualifiers = "en-w390dp-h844dp-xxxhdpi")
    fun gameEn() = game("en/Game")

    @Test
    fun resultTr() = result("tr/ResultScreen")

    @Test
    @Config(qualifiers = "en-w390dp-h844dp-xxxhdpi")
    fun resultEn() = result("en/Result")

    @Test
    fun jokerSheetTr() = jokerSheet("tr/JokerSheet")

    @Test
    @Config(qualifiers = "en-w390dp-h844dp-xxxhdpi")
    fun jokerSheetEn() = jokerSheet("en/JokerSheetEnglish")

    @Test
    fun comboBurstTr() = comboBurst("tr/GameMotionComboBurst")

    @Test
    @Config(qualifiers = "en-w390dp-h844dp-xxxhdpi")
    fun comboBurstEn() = comboBurst("en/GameMotionComboBurst")

    private fun game(name: String) {
        captureRoboImage("build/store-hires/$name.png") {
            LexiTheme(reducedMotion = false) {
                GameScreen(state = GameSamples.design, clock = { GameSamples.designClock }, onIntent = {})
            }
        }
    }

    private fun result(name: String) {
        captureRoboImage("build/store-hires/$name.png") {
            LexiTheme(reducedMotion = false) { ResultScreen(state = ResultSamples.design, onIntent = {}) }
        }
    }

    private fun jokerSheet(name: String) {
        captureRoboImage("build/store-hires/$name.png") {
            LexiTheme(reducedMotion = false) {
                GameScreen(state = GameSamples.jokerSheet, clock = { GameSamples.jokerSheetClock }, onIntent = {})
            }
        }
    }

    /** Same still as GameMotionScreenshotTest.comboBurst (#7 at its middle keyframe). */
    private fun comboBurst(name: String) = runComposeUiTest {
        val harness = setGameScreen(GameSamples.design)
        play(harness, GameEffect.ComboReached(3))
        mainClock.advanceTimeBy(COMBO_MID_MS)
        onRoot().captureRoboImage("build/store-hires/$name.png")
    }

    private companion object {
        const val COMBO_MID_MS = 210L
    }
}
