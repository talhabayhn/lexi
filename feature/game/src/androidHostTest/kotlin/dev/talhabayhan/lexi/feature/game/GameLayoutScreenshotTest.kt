package dev.talhabayhan.lexi.feature.game

import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.LexiViewport
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The responsive rules of `screen-map.md` on the game screen: phone sizes from 320 × 568 to 412 × 915 and the system
 * font size at 1.15× and 1.3× (capped at 1.15×). The board never scrolls; the clue area keeps its height.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class GameLayoutScreenshotTest {

    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi")
    fun w320h568() = capture("GameLayout_w320h568", GameLayoutFixtures.threeLineClue)

    @Test
    @Config(qualifiers = "tr-w360dp-h640dp-xhdpi")
    fun w360h640() = capture("GameLayout_w360h640", GameLayoutFixtures.twoLineClue)

    @Test
    @Config(qualifiers = "tr-w360dp-h780dp-xhdpi")
    fun w360h780() = capture("GameLayout_w360h780", GameLayoutFixtures.threeLineClue)

    @Test
    fun w390h844() = capture("GameLayout_w390h844", GameLayoutFixtures.twoLineClue)

    @Test
    fun w390h844FifteenKeys() = capture("GameLayout_w390h844_15keys", GameLayoutFixtures.fifteenKeys)

    @Test
    @Config(qualifiers = "tr-w412dp-h915dp-xhdpi")
    fun w412h915() = capture("GameLayout_w412h915", GameLayoutFixtures.threeLineClue)

    @Test
    @Config(fontScale = 1.15f)
    fun fontScale115() = capture("GameLayout_fontScale115", GameSamples.design)

    @Test
    @Config(fontScale = 1.3f)
    fun fontScale130() = capture("GameLayout_fontScale130", GameLayoutFixtures.threeLineClue)

    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi", fontScale = 1.3f)
    fun w320h568FontScale130() = capture("GameLayout_w320h568_fontScale130", GameLayoutFixtures.fifteenKeys)

    /** Expanded tier (`LexiViewport`): large viewports ("smallest display size") draw the design scaled. */
    @Test
    @Config(qualifiers = "tr-w480dp-h1000dp-mdpi")
    fun w480h1000() = capture("GameLayout_w480h1000", GameSamples.design)

    @Test
    @Config(qualifiers = "tr-w600dp-h1100dp-mdpi")
    fun w600h1100() = capture("GameLayout_w600h1100", GameLayoutFixtures.twoLineClue)

    @Test
    @Config(qualifiers = "tr-w600dp-h960dp-mdpi")
    fun w600h960() = capture("GameLayout_w600h960", GameLayoutFixtures.fifteenKeys)

    /** Taller than the design's proportions: the gaps grow and the column is centred, actions stay on the keys. */
    @Test
    @Config(qualifiers = "tr-w400dp-h1000dp-mdpi")
    fun w400h1000() = capture("GameLayout_w400h1000", GameSamples.design)

    @Test
    @Config(qualifiers = "tr-w360dp-h640dp-xhdpi", fontScale = 1.3f)
    fun w360h640FontScale130() = capture("GameLayout_w360h640_fontScale130", GameLayoutFixtures.twoLineClue)

    @Test
    @Config(qualifiers = "tr-w600dp-h960dp-mdpi", fontScale = 1.3f)
    fun w600h960FontScale130() = capture("GameLayout_w600h960_fontScale130", GameLayoutFixtures.fifteenKeys)

    // Joker sheet layout tests
    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi")
    fun jokerSheet_w320h568() =
        capture("JokerSheet_w320h568", GameSamples.jokerSheet, clock = { GameSamples.jokerSheetClock })

    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi", fontScale = 1.3f)
    fun jokerSheet_w320h568FontScale130() =
        capture("JokerSheet_w320h568_fontScale130", GameSamples.jokerSheet, clock = { GameSamples.jokerSheetClock })

    @Test
    @Config(qualifiers = "tr-w360dp-h640dp-xhdpi")
    fun jokerSheet_w360h640() =
        capture("JokerSheet_w360h640", GameSamples.jokerSheet, clock = { GameSamples.jokerSheetClock })

    @Test
    @Config(qualifiers = "tr-w360dp-h640dp-xhdpi", fontScale = 1.3f)
    fun jokerSheet_w360h640FontScale130() =
        capture("JokerSheet_w360h640_fontScale130", GameSamples.jokerSheet, clock = { GameSamples.jokerSheetClock })

    @Test
    @Config(qualifiers = "tr-w600dp-h960dp-mdpi")
    fun jokerSheet_w600h960() =
        capture("JokerSheet_w600h960", GameSamples.jokerSheet, clock = { GameSamples.jokerSheetClock })

    @Test
    @Config(qualifiers = "tr-w600dp-h960dp-mdpi", fontScale = 1.3f)
    fun jokerSheet_w600h960FontScale130() =
        capture("JokerSheet_w600h960_fontScale130", GameSamples.jokerSheet, clock = { GameSamples.jokerSheetClock })

    // Pause dialog layout tests
    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi")
    fun pauseDialog_w320h568() = capture("GamePauseDialog_w320h568", GameSamples.paused)

    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi", fontScale = 1.3f)
    fun pauseDialog_w320h568FontScale130() = capture("GamePauseDialog_w320h568_fontScale130", GameSamples.paused)

    @Test
    @Config(qualifiers = "tr-w360dp-h640dp-xhdpi")
    fun pauseDialog_w360h640() = capture("GamePauseDialog_w360h640", GameSamples.paused)

    @Test
    @Config(qualifiers = "tr-w360dp-h640dp-xhdpi", fontScale = 1.3f)
    fun pauseDialog_w360h640FontScale130() = capture("GamePauseDialog_w360h640_fontScale130", GameSamples.paused)

    @Test
    @Config(qualifiers = "tr-w600dp-h960dp-mdpi")
    fun pauseDialog_w600h960() = capture("GamePauseDialog_w600h960", GameSamples.paused)

    @Test
    @Config(qualifiers = "tr-w600dp-h960dp-mdpi", fontScale = 1.3f)
    fun pauseDialog_w600h960FontScale130() = capture("GamePauseDialog_w600h960_fontScale130", GameSamples.paused)

    @Test
    @Config(qualifiers = "tr-w390dp-h844dp-xhdpi", fontScale = 2.0f)
    fun jokerSheetFontScale200() = capture("JokerSheet_fontScale200", GameSamples.jokerSheet, clock = {
        GameSamples.jokerSheetClock
    })

    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi", fontScale = 2.0f)
    fun jokerSheet_w320h568FontScale200() =
        capture("JokerSheet_w320h568_fontScale200", GameSamples.jokerSheet, clock = {
            GameSamples.jokerSheetClock
        })

    private fun capture(name: String, state: GameUiState, clock: () -> GameClockUi = { GameSamples.designClock }) {
        captureRoboImage("src/androidHostTest/screenshots/$name.png") {
            LexiTheme(reducedMotion = false) {
                LexiViewport {
                    GameScreen(state = state, clock = clock, onIntent = {})
                }
            }
        }
    }
}
