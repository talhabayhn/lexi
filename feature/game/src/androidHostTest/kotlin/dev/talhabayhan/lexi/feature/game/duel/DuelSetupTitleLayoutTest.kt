package dev.talhabayhan.lexi.feature.game.duel

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.text.TextLayoutResult
import dev.talhabayhan.lexi.core.designsystem.component.DuelDefaults
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The duel setup headline (`17-duel-setup`) is one line, side by side, at 320 and 390dp and up to 200 % font, in
 * Turkish and English, and stays clear of the blue circle on the right.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
abstract class DuelSetupTitleChecks(private val title: String) {
    @Test
    fun `the headline is one line beside the circle`() = runComposeUiTest {
        setContent { LexiTheme(reducedMotion = true) { DuelSetupScreen(DuelSetupSample, onIntent = {}) } }
        // Read as one heading.
        onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading) and hasTitle(title)).assertExists()
        val node = onNode(hasTitle(title), useUnmergedTree = true)
        val layouts = mutableListOf<TextLayoutResult>()
        node.fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action?.invoke(layouts)
        assertEquals(1, layouts.single().lineCount, "'$title' wraps")
        val screen = onRoot().getBoundsInRoot()
        val bounds = node.getBoundsInRoot()
        // The circle shows its last 80dp at the right edge: the title ends before it.
        val circleLeft = screen.right - (DuelDefaults.SetupCircleSize - DuelDefaults.SetupCircleBleed)
        assertTrue(bounds.left >= screen.left, "starts off screen")
        assertTrue(
            layoutRight(layouts.single(), bounds.left) <= circleLeft.value + 1f,
            "'$title' runs under the circle",
        )
    }

    private fun hasTitle(text: String) = SemanticsMatcher("text = $text") { node ->
        node.config.getOrElse(SemanticsProperties.Text) { emptyList() }.any { it.text == text }
    }

    private fun ComposeUiTest.layoutRight(layout: TextLayoutResult, left: androidx.compose.ui.unit.Dp): Float =
        left.value + layout.getLineRight(0) / density.density
}

private const val TR = "Sırayla yarışın"
private const val EN = "Take turns"

@Config(sdk = [36], qualifiers = "tr-w320dp-h640dp-xhdpi")
class DuelSetupTitleTr320Test : DuelSetupTitleChecks(TR)

@Config(sdk = [36], qualifiers = "tr-w320dp-h640dp-xhdpi", fontScale = 2.0f)
class DuelSetupTitleTr320Font200Test : DuelSetupTitleChecks(TR)

@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi", fontScale = 2.0f)
class DuelSetupTitleTr390Font200Test : DuelSetupTitleChecks(TR)

@Config(sdk = [36], qualifiers = "en-w320dp-h640dp-xhdpi")
class DuelSetupTitleEn320Test : DuelSetupTitleChecks(EN)

@Config(sdk = [36], qualifiers = "en-w320dp-h640dp-xhdpi", fontScale = 2.0f)
class DuelSetupTitleEn320Font200Test : DuelSetupTitleChecks(EN)

@Config(sdk = [36], qualifiers = "en-w390dp-h844dp-xhdpi")
class DuelSetupTitleEn390Test : DuelSetupTitleChecks(EN)
