package dev.talhabayhan.lexi.feature.game

import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.component.ReviewOutcome
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.feature.game.result.ResultScreen
import dev.talhabayhan.lexi.feature.game.result.ResultSamples
import dev.talhabayhan.lexi.feature.game.result.ReviewRowUi
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * English store stills, not regression tests: the English UI with English clues and meanings in place of the app's
 * Turkish ones (owner's choice: English listings show no Turkish text). Written to `build/store-screens/en/` for
 * `tools/store-graphics`; runs only with `LEXI_STORE_FOOTAGE=1`.
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
                GameScreen(state = englishClue(GameSamples.design), clock = { GameSamples.designClock }, onIntent = {})
            }
        }
    }

    @Test
    fun jokerSheet() {
        captureRoboImage("$DIR/JokerSheet.png") {
            LexiTheme(reducedMotion = false) {
                GameScreen(
                    state = englishClue(GameSamples.jokerSheet),
                    clock = { GameSamples.jokerSheetClock },
                    onIntent = {},
                )
            }
        }
    }

    @Test
    fun result() {
        captureRoboImage("$DIR/Result.png") {
            LexiTheme(reducedMotion = false) {
                ResultScreen(state = ResultSamples.design.copy(rows = ENGLISH_ROWS), onIntent = {})
            }
        }
    }

    private companion object {
        const val DIR = "build/store-screens/en"

        // Store sample content, not user-facing app strings.
        val ENGLISH_ROWS = persistentListOf(
            ReviewRowUi('C', "chair", "a seat for one person", "Sit on the chair.", ReviewOutcome.Passed),
            ReviewRowUi('E', "egg", "you can boil or fry it", "I eat an egg.", ReviewOutcome.Wrong),
            ReviewRowUi('H', "horse", "a large animal you can ride", "The horse runs.", ReviewOutcome.Passed),
            ReviewRowUi('M', "mirror", "you see yourself in it", "Look in the mirror.", ReviewOutcome.Wrong),
        )

        fun englishClue(state: GameUiState): GameUiState =
            state.copy(turn = state.turn?.copy(clue = "the room where you cook"))
    }
}
