package dev.talhabayhan.lexi.feature.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.LexiViewport
import dev.talhabayhan.lexi.core.designsystem.theme.ViewportDefaults
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.math.abs
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The game layout is computed once (`screen-map.md`): clues of one, two and three lines (and a 15-key turn) leave
 * every other row where it is, and at 390 × 844 the rows sit where `04-game.png` draws them.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class GameLayoutTest {

    @Test
    fun `clues of one to three lines do not move the other rows`() = assertStableAcrossClues()

    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi")
    fun `clues do not move the other rows on a 320 x 568 phone`() = assertStableAcrossClues()

    @Test
    @Config(qualifiers = "tr-w360dp-h640dp-xhdpi")
    fun `clues do not move the other rows on a 360 x 640 phone`() = assertStableAcrossClues()

    @Test
    @Config(qualifiers = "tr-w412dp-h915dp-xhdpi", fontScale = 1.3f)
    fun `clues do not move the other rows at font scale 1_3`() = assertStableAcrossClues()

    /** `04-game.html`: HUD at 20, wheel 84–404, clue card from 420, boxes from 518, keys from 582, actions from 760. */
    @Test
    fun `the design size keeps the design's rows`() = runComposeUiTest {
        setGame(GameSamples.design)

        assertBounds(DpRect(20.dp, 20.dp, 370.dp, 68.dp), bounds(GameTestTags.HUD))
        assertBounds(DpRect(35.dp, 84.dp, 355.dp, 404.dp), bounds(GameTestTags.WHEEL))
        assertEquals(420f, bounds(GameTestTags.CLUE).top.value, TOLERANCE)
        assertEquals(518f, bounds(GameTestTags.ANSWER).top.value, TOLERANCE)
        assertEquals(582f, bounds(GameTestTags.KEYS).top.value, TOLERANCE)
        assertEquals(760f, bounds(GameTestTags.ACTIONS).top.value, TOLERANCE)
        assertEquals(816f, bounds(GameTestTags.ACTIONS).bottom.value, TOLERANCE)
    }

    @Test
    @Config(qualifiers = "tr-w480dp-h1000dp-mdpi")
    fun `a 480 x 1000 viewport draws the design scaled`() = assertDesignScaled()

    @Test
    @Config(qualifiers = "tr-w600dp-h1100dp-mdpi")
    fun `a 600 x 1100 viewport draws the design scaled`() = assertDesignScaled()

    @Test
    @Config(qualifiers = "tr-w600dp-h960dp-mdpi")
    fun `a 600 x 960 viewport draws the design scaled`() = assertDesignScaled()

    @Test
    @Config(qualifiers = "tr-w600dp-h1100dp-mdpi")
    fun `clues do not move the other rows at 600 x 1100`() = assertStableAcrossClues()

    /** Taller than the design's proportions: the actions stay 12dp under the keys, the rest is shared out. */
    @Test
    @Config(qualifiers = "tr-w400dp-h1000dp-mdpi")
    fun `the actions stay attached to the keys on a tall narrow viewport`() = runComposeUiTest {
        setGame(GameSamples.design)
        val scale = ViewportDefaults.scale(400.dp, 1000.dp)
        val keys = screenBounds(GameTestTags.KEYS)
        val actions = screenBounds(GameTestTags.ACTIONS)
        val keysBottom = keys.top + (KEY_ROWS_HEIGHT * scale).dp
        assertEquals(KEYS_ACTIONS_GAP, (actions.top - keysBottom).value / scale, SCALED_TOLERANCE)
        val bottomSpace = onRoot().getBoundsInRoot().bottom - actions.bottom
        val topSpace = screenBounds(GameTestTags.HUD).top
        assertEquals(topSpace.value + BOTTOM_MINUS_TOP_PADDING * scale, bottomSpace.value, SCALED_TOLERANCE)
    }

    /**
     * `04-game` × min(w / 390, h / 844), the column centred: every row at the design's top and height (scaled), the
     * HUD inset by the design padding from the centred column.
     */
    private fun assertDesignScaled() = runComposeUiTest {
        setGame(GameSamples.design)
        val screen = onRoot().getBoundsInRoot()
        val scale = ViewportDefaults.scale(screen.right - screen.left, screen.bottom - screen.top)
        mapOf(
            GameTestTags.HUD to (20f to 68f),
            GameTestTags.WHEEL to (84f to 404f),
            GameTestTags.ANSWER to (518f to 566f),
            GameTestTags.ACTIONS to (760f to 816f),
        ).forEach { (tag, rows) ->
            val bounds = screenBounds(tag)
            assertEquals(rows.first, bounds.top.value / scale, SCALED_TOLERANCE, "$tag top")
            assertEquals(rows.second, bounds.bottom.value / scale, SCALED_TOLERANCE, "$tag bottom")
        }
        assertEquals(420f, screenBounds(GameTestTags.CLUE).top.value / scale, SCALED_TOLERANCE, "clue top")
        assertEquals(582f, screenBounds(GameTestTags.KEYS).top.value / scale, SCALED_TOLERANCE, "keys top")
        val hud = screenBounds(GameTestTags.HUD)
        assertEquals(hud.left.value, (screen.right - hud.right).value, SCALED_TOLERANCE, "not centred")
        val column = hud.right - hud.left + (40 * scale).dp
        assertTrue(column <= ViewportDefaults.ContentMaxWidth * scale + 1.dp, "too wide: $column")
        val wheel = screenBounds(GameTestTags.WHEEL)
        assertEquals(320f, (wheel.right - wheel.left).value / scale, SCALED_TOLERANCE, "wheel size")
    }

    /** Bounds in screen dp (`getBoundsInRoot` reports a node in its own, scaled density). */
    private fun ComposeUiTest.screenBounds(tag: String): DpRect {
        val r = onNodeWithTag(tag).fetchSemanticsNode().boundsInRoot
        val d = density.density
        return DpRect((r.left / d).dp, (r.top / d).dp, (r.right / d).dp, (r.bottom / d).dp)
    }

    private fun assertStableAcrossClues() = runComposeUiTest {
        var state by mutableStateOf(GameLayoutFixtures.withClue(GameLayoutFixtures.ONE_LINE))
        setGame { state }
        val reference = rows()
        listOf(
            GameLayoutFixtures.withClue(GameLayoutFixtures.TWO_LINES),
            GameLayoutFixtures.withClue(GameLayoutFixtures.THREE_LINES),
            GameLayoutFixtures.withClue(GameLayoutFixtures.THREE_LINES, example = "A thunderstorm _______ the town."),
            GameLayoutFixtures.fifteenKeys,
        ).forEach { next ->
            state = next
            waitForIdle()
            val rows = rows()
            reference.forEach { (name, rect) -> assertBounds(rect, rows.getValue(name), name) }
        }
        val wheel = reference.getValue(GameTestTags.WHEEL)
        val actions = reference.getValue(GameTestTags.ACTIONS)
        assertTrue(wheel.bottom - wheel.top > 0.dp, "wheel collapsed")
        assertTrue(actions.bottom <= onRoot().getBoundsInRoot().bottom, "actions off screen")
    }

    /** The rows that never move with the clue, plus the wheel's rule label and the card's level column. */
    private fun ComposeUiTest.rows(): Map<String, DpRect> = mapOf(
        GameTestTags.HUD to bounds(GameTestTags.HUD),
        GameTestTags.WHEEL to bounds(GameTestTags.WHEEL),
        GameTestTags.CLUE to bounds(GameTestTags.CLUE),
        GameTestTags.ANSWER to bounds(GameTestTags.ANSWER),
        GameTestTags.KEYS + ".top" to bounds(GameTestTags.KEYS).let { DpRect(it.left, it.top, it.right, it.top) },
        GameTestTags.ACTIONS to bounds(GameTestTags.ACTIONS),
        RULE to onNodeWithText(RULE, useUnmergedTree = true).getBoundsInRoot(),
        LEVEL to onNodeWithText(LEVEL, useUnmergedTree = true).getBoundsInRoot(),
    )

    private fun ComposeUiTest.bounds(tag: String): DpRect = onNodeWithTag(tag).getBoundsInRoot()

    private fun ComposeUiTest.setGame(state: GameUiState) = setGame { state }

    private fun ComposeUiTest.setGame(state: () -> GameUiState) {
        setContent {
            LexiTheme(reducedMotion = true) {
                LexiViewport {
                    GameScreen(state = state(), clock = { GameSamples.designClock }, onIntent = {})
                }
            }
        }
    }

    private fun assertBounds(expected: DpRect, actual: DpRect, name: String = "") {
        val close = abs(expected.left.value - actual.left.value) <= TOLERANCE &&
            abs(expected.top.value - actual.top.value) <= TOLERANCE &&
            abs(expected.right.value - actual.right.value) <= TOLERANCE &&
            abs(expected.bottom.value - actual.bottom.value) <= TOLERANCE
        assertTrue(close, "$name: expected $expected, was $actual")
    }

    private companion object {
        /** Positions within 1dp (`04-game.png` is a 2× raster). */
        const val TOLERANCE = 1f

        /** 1dp, plus the pixel rounding of a scaled layout at mdpi. */
        const val SCALED_TOLERANCE = 2f

        /** Three key rows of 50 with two 8dp gaps, then the 12dp gap to the actions. */
        const val KEY_ROWS_HEIGHT = 166f
        const val KEYS_ACTIONS_GAP = 12f

        /** Design paddings: 28 bottom − 20 top. */
        const val BOTTOM_MINUS_TOP_PADDING = 8f
        const val RULE = "İLE BAŞLAR"
        const val LEVEL = "A1"
    }
}
