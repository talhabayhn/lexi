package dev.talhabayhan.lexi.feature.home

import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.component.HomeTierSize
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.LexiViewport
import dev.talhabayhan.lexi.core.designsystem.theme.ViewportDefaults
import dev.talhabayhan.lexi.core.domain.model.Level
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Home's layout (`02c-home-v2`, `home-v2-notes.md` §1): OYNA and the four footer keys are always fully on screen;
 * every text stays inside the screen's width; at 100 % font every text is on screen without scrolling (a tier fits),
 * while larger fonts may scroll the part above OYNA. Phone sizes 360 × 640, 390 × 844 and 412 × 915 at 100, 130 and
 * 200 % font, Turkish and English, plus the expanded tier.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
abstract class HomeLayoutChecks(private val pinned: List<String>, private val fontScaled: Boolean) {

    @Test
    fun `OYNA and the footer are on screen and every text fits`() = runComposeUiTest {
        setHome(HomeSampleState)
        assertLayout()
    }

    @Test
    fun `a long level name and a capped badge keep the layout`() = runComposeUiTest {
        setHome(HomeSampleState.copy(selectedLevel = Level.B2, dueReviews = 120))
        assertLayout()
    }

    @Test
    fun `the category sheet cards stay inside the screen`() = runComposeUiTest {
        setHome(HomeSampleState.copy(categorySheetOpen = true))
        val screen = onRoot().getBoundsInRoot()
        onAllNodes(
            SemanticsMatcher.expectValue(SemanticsProperties.Role, androidx.compose.ui.semantics.Role.RadioButton),
        )
            .fetchSemanticsNodes()
            .filter {
                it.config.getOrNull(SemanticsProperties.ContentDescription)?.firstOrNull()?.contains("/") == true
            }
            // Cards scrolled out of the sheet are clipped to an empty rect.
            .filter { it.boundsInRoot.width > 0f }
            .forEach { node ->
                val bounds = node.dpBounds(density.density)
                assertInside(
                    bounds,
                    screen,
                    node.config[SemanticsProperties.ContentDescription].first(),
                    vertical = false,
                )
                if (fontScaled && bounds.top < screen.bottom) {
                    // One column from 1.3×: every card spans the content width.
                    assertTrue(bounds.right - bounds.left > (screen.right - screen.left) / 2, "two columns at $bounds")
                }
            }
    }

    private fun ComposeUiTest.setHome(state: HomeUiState) {
        setContent { LexiTheme(reducedMotion = true) { LexiViewport { HomeScreen(state, onIntent = {}) } } }
    }

    private fun ComposeUiTest.assertLayout() {
        val screen = onRoot().getBoundsInRoot()
        pinned.forEach { label ->
            val node = placed(hasText(label) or hasContentDescription(label, substring = true))
            assertInside(node.dpBounds(density.density), screen, label, vertical = true)
        }
        var texts = 0
        onAllNodes(SemanticsMatcher("has bounds") { true }, useUnmergedTree = true).fetchSemanticsNodes()
            .filter {
                it.config.contains(SemanticsProperties.Text) ||
                    it.config.contains(SemanticsProperties.ContentDescription)
            }
            .forEach { node ->
                val label = node.config.getOrNull(SemanticsProperties.Text)?.joinToString { it.text }
                    ?: node.config.getOrNull(SemanticsProperties.ContentDescription)?.joinToString().orEmpty()
                if (label.isEmpty()) return@forEach
                texts++
                assertInside(node.dpBounds(density.density), screen, label, vertical = !fontScaled)
            }
        assertTrue(texts >= 20, "texts missing: $texts")
    }

    /** The placed node (the CTA caption fitting measures copies at 0, 0): the lowest match. */
    private fun ComposeUiTest.placed(matcher: SemanticsMatcher): SemanticsNode =
        onAllNodes(matcher, useUnmergedTree = true).fetchSemanticsNodes().maxBy { it.boundsInRoot.top }

    private fun SemanticsNode.dpBounds(density: Float): DpRect {
        val r = boundsInRoot
        return DpRect((r.left / density).dp, (r.top / density).dp, (r.right / density).dp, (r.bottom / density).dp)
    }

    private fun assertInside(bounds: DpRect, screen: DpRect, label: String, vertical: Boolean) {
        val tolerance: Dp = 1.dp
        val horizontal = bounds.left >= screen.left - tolerance && bounds.right <= screen.right + tolerance
        val inHeight = !vertical || (bounds.top >= screen.top - tolerance && bounds.bottom <= screen.bottom + tolerance)
        assertTrue(horizontal && inHeight, "'$label' $bounds outside $screen")
    }
}

private val TR = listOf("OYNA", "Defter", "İstatistik", "Sınavlar", "Düello")
private val EN = listOf("PLAY", "Notebook", "Statistics", "Exams", "Duel")

@Config(sdk = [36], qualifiers = "tr-w360dp-h640dp-xhdpi")
class HomeLayoutTr360x640Test : HomeLayoutChecks(TR, fontScaled = false)

@Config(sdk = [36], qualifiers = "tr-w360dp-h640dp-xhdpi", fontScale = 1.3f)
class HomeLayoutTr360x640Font130Test : HomeLayoutChecks(TR, fontScaled = true)

@Config(sdk = [36], qualifiers = "tr-w360dp-h640dp-xhdpi", fontScale = 2.0f)
class HomeLayoutTr360x640Font200Test : HomeLayoutChecks(TR, fontScaled = true)

@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class HomeLayoutTr390x844Test : HomeLayoutChecks(TR, fontScaled = false)

@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi", fontScale = 1.3f)
class HomeLayoutTr390x844Font130Test : HomeLayoutChecks(TR, fontScaled = true)

@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi", fontScale = 2.0f)
class HomeLayoutTr390x844Font200Test : HomeLayoutChecks(TR, fontScaled = true)

@Config(sdk = [36], qualifiers = "tr-w412dp-h915dp-xhdpi")
class HomeLayoutTr412x915Test : HomeLayoutChecks(TR, fontScaled = false)

@Config(sdk = [36], qualifiers = "tr-w412dp-h915dp-xhdpi", fontScale = 1.3f)
class HomeLayoutTr412x915Font130Test : HomeLayoutChecks(TR, fontScaled = true)

@Config(sdk = [36], qualifiers = "tr-w412dp-h915dp-xhdpi", fontScale = 2.0f)
class HomeLayoutTr412x915Font200Test : HomeLayoutChecks(TR, fontScaled = true)

@Config(sdk = [36], qualifiers = "en-w360dp-h640dp-xhdpi")
class HomeLayoutEn360x640Test : HomeLayoutChecks(EN, fontScaled = false)

@Config(sdk = [36], qualifiers = "en-w360dp-h640dp-xhdpi", fontScale = 1.3f)
class HomeLayoutEn360x640Font130Test : HomeLayoutChecks(EN, fontScaled = true)

@Config(sdk = [36], qualifiers = "en-w360dp-h640dp-xhdpi", fontScale = 2.0f)
class HomeLayoutEn360x640Font200Test : HomeLayoutChecks(EN, fontScaled = true)

@Config(sdk = [36], qualifiers = "en-w390dp-h844dp-xhdpi")
class HomeLayoutEn390x844Test : HomeLayoutChecks(EN, fontScaled = false)

@Config(sdk = [36], qualifiers = "en-w390dp-h844dp-xhdpi", fontScale = 1.3f)
class HomeLayoutEn390x844Font130Test : HomeLayoutChecks(EN, fontScaled = true)

@Config(sdk = [36], qualifiers = "en-w390dp-h844dp-xhdpi", fontScale = 2.0f)
class HomeLayoutEn390x844Font200Test : HomeLayoutChecks(EN, fontScaled = true)

@Config(sdk = [36], qualifiers = "en-w412dp-h915dp-xhdpi")
class HomeLayoutEn412x915Test : HomeLayoutChecks(EN, fontScaled = false)

@Config(sdk = [36], qualifiers = "en-w412dp-h915dp-xhdpi", fontScale = 1.3f)
class HomeLayoutEn412x915Font130Test : HomeLayoutChecks(EN, fontScaled = true)

@Config(sdk = [36], qualifiers = "en-w412dp-h915dp-xhdpi", fontScale = 2.0f)
class HomeLayoutEn412x915Font200Test : HomeLayoutChecks(EN, fontScaled = true)

/** Expanded tier (`LexiViewport`): the design scaled, centred, at most the content width; and the design's values. */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class HomeLayoutTest {

    @Test
    fun `the regular tier has the design's sizes at 390 x 844`() = runComposeUiTest {
        setHome()
        val play = bounds(hasText("OYNA"))
        val key = bounds(hasContentDescription("Defter, 24 tekrar bekliyor"))
        // OYNA: 68 high (the label row, the button is its parent); the key column: 56 key + 6 + the 12sp label.
        assertTrue(play.bottom - play.top <= HomeTierSize.Regular.playHeight, "OYNA label taller than the button")
        assertEquals(HomeTierSize.Regular.keySize.value, (key.right - key.left).value, 30f)
        val screen = onRoot().getBoundsInRoot()
        assertEquals(
            HomeTierSize.Regular.paddingBottom.value,
            (screen.bottom - key.bottom).value,
            2f,
            "footer must sit on the bottom padding",
        )
    }

    @Test
    @Config(qualifiers = "tr-w480dp-h1000dp-mdpi")
    fun w480h1000() = runComposeUiTest { setHome().also { assertCentred() } }

    @Test
    @Config(qualifiers = "tr-w600dp-h1100dp-mdpi")
    fun w600h1100() = runComposeUiTest { setHome().also { assertCentred() } }

    @Test
    @Config(qualifiers = "tr-w600dp-h960dp-mdpi")
    fun w600h960() = runComposeUiTest { setHome().also { assertCentred() } }

    private fun ComposeUiTest.setHome() {
        setContent { LexiTheme(reducedMotion = true) { LexiViewport { HomeScreen(HomeSampleState, onIntent = {}) } } }
    }

    private fun ComposeUiTest.assertCentred() {
        val screen = onRoot().getBoundsInRoot()
        val scale = ViewportDefaults.scale(screen.right - screen.left, screen.bottom - screen.top)
        val logo = bounds(hasContentDescription("Lexi"))
        val settings = bounds(hasContentDescription("Ayarlar"))
        val play = bounds(hasText("OYNA"))
        assertTrue(play.bottom <= screen.bottom, "OYNA off screen")
        assertEquals((logo.left - screen.left).value, (screen.right - settings.right).value, 1f, "not centred")
        val column = settings.right - logo.left + HomeTierSize.Regular.paddingHorizontal * 2 * scale
        assertTrue(column <= ViewportDefaults.ContentMaxWidth * scale + 1.dp, "too wide: $column")
    }

    private fun ComposeUiTest.bounds(matcher: SemanticsMatcher): DpRect {
        val r = onAllNodes(matcher).fetchSemanticsNodes().maxBy { it.boundsInRoot.top }.boundsInRoot
        val d = density.density
        return DpRect((r.left / d).dp, (r.top / d).dp, (r.right / d).dp, (r.bottom / d).dp)
    }
}
