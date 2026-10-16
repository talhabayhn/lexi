package dev.talhabayhan.lexi.feature.home

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
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

    private fun androidx.compose.ui.test.ComposeUiTest.setHome() {
        setContent { LexiTheme(reducedMotion = true) { HomeScreen(HomeSampleState, onIntent = {}) } }
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
}
