package dev.talhabayhan.lexi.feature.game

import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The stateless game screen at the size and scale of `docs/design/screens/04-game.png` / `05-joker-sheet.png`
 * (390 × 844dp, xhdpi = 2×), in Turkish like the design. Warning and wrong-reveal use reduced motion so the #8 pulse
 * loop does not keep the capture busy (colours are the same).
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

    /** `05-joker-sheet.png`. */
    @Test
    fun jokerSheet() {
        captureRoboImage("src/androidHostTest/screenshots/JokerSheet.png") {
            LexiTheme(reducedMotion = false) {
                GameScreen(state = GameSamples.jokerSheet, clock = { GameSamples.jokerSheetClock }, onIntent = {})
            }
        }
    }

    /** The sheet as `05-joker-sheet.png` draws it, with the ad button (`FeatureFlags.rewardedAdJokers`, on from M7). */
    @Test
    fun jokerSheetWithAd() {
        captureRoboImage("src/androidHostTest/screenshots/JokerSheetWithAd.png") {
            LexiTheme(reducedMotion = false) {
                GameScreen(
                    state = GameSamples.jokerSheet.copy(showJokerAd = true),
                    clock = { GameSamples.jokerSheetClock },
                    onIntent = {},
                )
            }
        }
    }

    /** English copy is longer: titles and descriptions wrap, costs stay on one line. */
    @Test
    @Config(qualifiers = "en-w390dp-h844dp-xhdpi")
    fun jokerSheetEnglish() {
        captureRoboImage("src/androidHostTest/screenshots/JokerSheetEnglish.png") {
            LexiTheme(reducedMotion = false) {
                GameScreen(state = GameSamples.jokerSheet, clock = { GameSamples.jokerSheetClock }, onIntent = {})
            }
        }
    }

    /** Design debt: the blanked example of the "Örnek cümle" joker under the clue, shrunk into the fixed clue area. */
    @Test
    fun exampleJoker() {
        captureRoboImage("src/androidHostTest/screenshots/GameScreenExampleJoker.png") {
            LexiTheme(reducedMotion = false) {
                GameScreen(state = GameSamples.exampleJoker, clock = { GameSamples.designClock }, onIntent = {})
            }
        }
    }

    /** Design debt: pause dialog from `16-components` (no design). */
    @Test
    fun pauseDialog() {
        captureRoboImage("src/androidHostTest/screenshots/GamePauseDialog.png") {
            LexiTheme(reducedMotion = false) {
                GameScreen(state = GameSamples.paused, clock = { GameSamples.designClock }, onIntent = {})
            }
        }
    }

    @Test
    @Config(qualifiers = "en-w390dp-h844dp-xhdpi")
    fun quitConfirmationEnglish() {
        captureRoboImage("src/androidHostTest/screenshots/GameQuitConfirmationEnglish.png") {
            LexiTheme(reducedMotion = false) {
                GameScreen(state = GameSamples.quitConfirmation, clock = { GameSamples.designClock }, onIntent = {})
            }
        }
    }

    /** Design debt: Practice has no clock ("SÜRESİZ") and the badge counts correct answers. */
    @Test
    fun practice() {
        captureRoboImage("src/androidHostTest/screenshots/GameScreenPractice.png") {
            LexiTheme(reducedMotion = false) {
                GameScreen(state = GameSamples.practice, clock = { GameClockUi.Untimed }, onIntent = {})
            }
        }
    }

    /** Design debt: the content could not be read; retry or go back. */
    @Test
    fun loadError() {
        captureRoboImage("src/androidHostTest/screenshots/GameScreenLoadError.png") {
            LexiTheme(reducedMotion = false) {
                GameScreen(state = GameSamples.loadError, clock = { GameClockUi.Untimed }, onIntent = {})
            }
        }
    }

    /** Design debt: no card for the chosen level / category; only back. English copy is longer. */
    @Test
    @Config(qualifiers = "en-w390dp-h844dp-xhdpi")
    fun noCardsEnglish() {
        captureRoboImage("src/androidHostTest/screenshots/GameScreenNoCardsEnglish.png") {
            LexiTheme(reducedMotion = false) {
                GameScreen(state = GameSamples.noCards, clock = { GameClockUi.Untimed }, onIntent = {})
            }
        }
    }
}
