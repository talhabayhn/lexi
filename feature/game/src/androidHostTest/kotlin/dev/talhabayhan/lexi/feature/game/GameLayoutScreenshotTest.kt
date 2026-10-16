package dev.talhabayhan.lexi.feature.game

import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
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

    private fun capture(name: String, state: GameUiState) {
        captureRoboImage("src/androidHostTest/screenshots/$name.png") {
            LexiTheme(reducedMotion = false) {
                GameScreen(state = state, clock = { GameSamples.designClock }, onIntent = {})
            }
        }
    }
}
