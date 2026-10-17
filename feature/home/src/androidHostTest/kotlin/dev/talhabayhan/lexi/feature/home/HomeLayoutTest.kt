package dev.talhabayhan.lexi.feature.home

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
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

/** The home never scrolls (`screen-map.md`): at every phone size and font scale all content stays on screen. */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class HomeLayoutTest {

    @Test
    fun w390h844() = runComposeUiTest { setHome().also { assertContentOnScreen() } }

    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi")
    fun w320h568() = runComposeUiTest { setHome().also { assertContentOnScreen() } }

    @Test
    @Config(qualifiers = "tr-w360dp-h640dp-xhdpi")
    fun w360h640() = runComposeUiTest { setHome().also { assertContentOnScreen() } }

    @Test
    @Config(qualifiers = "tr-w360dp-h780dp-xhdpi")
    fun w360h780() = runComposeUiTest { setHome().also { assertContentOnScreen() } }

    @Test
    @Config(qualifiers = "tr-w412dp-h915dp-xhdpi")
    fun w412h915() = runComposeUiTest { setHome().also { assertContentOnScreen() } }

    @Test
    @Config(fontScale = 1.15f)
    fun fontScale115() = runComposeUiTest { setHome().also { assertContentOnScreen() } }

    @Test
    @Config(fontScale = 1.3f)
    fun fontScale130() = runComposeUiTest { setHome().also { assertContentOnScreen() } }

    @Test
    @Config(qualifiers = "tr-w360dp-h640dp-xhdpi", fontScale = 1.3f)
    fun w360h640FontScale130() = runComposeUiTest { setHome().also { assertContentOnScreen() } }

    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi", fontScale = 1.3f)
    fun w320h568FontScale130() = runComposeUiTest { setHome().also { assertContentOnScreen() } }

    /** Expanded tier: the design scaled by min(w / 390, h / 844), centred, no hole larger than the design's. */
    @Test
    @Config(qualifiers = "tr-w480dp-h1000dp-mdpi")
    fun w480h1000() = runComposeUiTest { setHome().also { assertContentOnScreen() }.also { assertExpanded() } }

    @Test
    @Config(qualifiers = "tr-w600dp-h1100dp-mdpi")
    fun w600h1100() = runComposeUiTest { setHome().also { assertContentOnScreen() }.also { assertExpanded() } }

    @Test
    @Config(qualifiers = "tr-w600dp-h960dp-mdpi")
    fun w600h960() = runComposeUiTest { setHome().also { assertContentOnScreen() }.also { assertExpanded() } }

    /** Taller than the design's proportions (scaled by the width): the tiles grow before the OYNA–tiles hole. */
    @Test
    @Config(qualifiers = "tr-w400dp-h1000dp-mdpi")
    fun w400h1000() = runComposeUiTest {
        setHome()
        assertContentOnScreen()
        val scale = ViewportDefaults.scale(400.dp, 1000.dp)
        val tile = bounds(hasText("Kelime Defterim", substring = true))
        assertEquals(TILE_HEIGHT + TILE_GROWTH, (tile.bottom - tile.top).value / scale, SCALED_TOLERANCE)
    }

    @Test
    fun designGaps() = runComposeUiTest { setHome().also { assertExpanded() } }

    private fun androidx.compose.ui.test.ComposeUiTest.setHome() {
        setContent { LexiTheme(reducedMotion = true) { LexiViewport { HomeScreen(HomeSampleState, onIntent = {}) } } }
    }

    /**
     * The design scaled: the OYNA–tiles hole and the tiles as in `02-home` (× scale), the content column centred and at
     * most [ViewportDefaults.ContentMaxWidth] (× scale) wide.
     */
    private fun androidx.compose.ui.test.ComposeUiTest.assertExpanded() {
        val screen = onRoot().getBoundsInRoot()
        val scale = ViewportDefaults.scale(screen.right - screen.left, screen.bottom - screen.top)
        val play = bounds(hasText("OYNA"))
        val tile = bounds(hasText("Kelime Defterim", substring = true))
        val streak = bounds(hasText("7 gün seri"))
        val settings = bounds(hasContentDescription("Ayarlar"))
        assertEquals(DESIGN_PLAY_TO_TILES, (tile.top - play.bottom).value / scale, SCALED_TOLERANCE, "OYNA–tiles hole")
        assertEquals(TILE_HEIGHT, (tile.bottom - tile.top).value / scale, SCALED_TOLERANCE, "tile height")
        val left = streak.left - screen.left
        val right = screen.right - settings.right
        assertEquals(left.value, right.value, 1f, "not centred")
        val column = settings.right - streak.left + SCREEN_PADDING.dp * 2 * scale
        assertTrue(column <= ViewportDefaults.ContentMaxWidth * scale + 1.dp, "too wide: $column")
    }

    /** Bounds in screen dp of the placed node matching [matcher] (the CTA caption fitting measures copies at 0, 0). */
    private fun androidx.compose.ui.test.ComposeUiTest.bounds(matcher: SemanticsMatcher): DpRect {
        val r = onAllNodes(matcher).fetchSemanticsNodes().maxBy { it.boundsInRoot.top }.boundsInRoot
        val d = density.density
        return DpRect((r.left / d).dp, (r.top / d).dp, (r.right / d).dp, (r.bottom / d).dp)
    }

    private fun androidx.compose.ui.test.ComposeUiTest.assertContentOnScreen() {
        val screen = onRoot().getBoundsInRoot()
        val nodes = onAllNodes(SemanticsMatcher("has bounds") { true }, useUnmergedTree = true).fetchSemanticsNodes()
        assertTrue(nodes.isNotEmpty())
        var texts = 0
        nodes.filter {
            it.config.contains(SemanticsProperties.Text) ||
                it.config.contains(SemanticsProperties.ContentDescription)
        }.forEach { node ->
            val r = node.boundsInRoot
            val d = density.density
            val bounds = DpRect((r.left / d).dp, (r.top / d).dp, (r.right / d).dp, (r.bottom / d).dp)
            val label = node.config.getOrNull(SemanticsProperties.Text)?.joinToString { it.text }.orEmpty()
            if (label.isNotEmpty()) texts++
            assertTrue(
                bounds.left >= screen.left - 1.dp && bounds.top >= screen.top - 1.dp &&
                    bounds.right <= screen.right + 1.dp && bounds.bottom <= screen.bottom + 1.dp,
                "'$label' $bounds outside $screen",
            )
        }
        assertTrue(texts >= 8, "texts missing: $texts")
    }

    private companion object {
        /** `02-home` at 390 × 844: OYNA's bottom edge to the tiles (the 20dp gap and the free space). */
        const val DESIGN_PLAY_TO_TILES = 161f
        const val TILE_HEIGHT = 110f
        const val TILE_GROWTH = 55f
        const val SCREEN_PADDING = 20

        /** 1dp, plus the pixel rounding of a scaled layout at mdpi. */
        const val SCALED_TOLERANCE = 2f
    }
}
