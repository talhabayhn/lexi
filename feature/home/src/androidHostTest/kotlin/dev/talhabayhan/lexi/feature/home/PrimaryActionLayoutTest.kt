package dev.talhabayhan.lexi.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runComposeUiTest
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.LexiViewport
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertTrue

/**
 * The primary actions of this screen are fully on screen without scrolling on a 320 × 568 phone, at 100 %, 130 % and
 * 200 % font scale, in Turkish and English (the pattern of `OnboardingLayoutTest`).
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
abstract class PrimaryActionLayoutChecks(private val labels: List<String>) {
    @Test
    fun `home keeps play on screen`() = assertVisible { HomeScreen(HomeSampleState, onIntent = {}) }

    private fun assertVisible(screen: @Composable () -> Unit) = runComposeUiTest {
        setContent { LexiTheme(reducedMotion = true) { LexiViewport { screen() } } }
        // The unclipped layout bounds: a button scrolled out of its viewport still lies below the screen.
        val rootHeight = onRoot().fetchSemanticsNode().size.height
        labels.forEach { label ->
            val node = onAllNodesWithText(label).fetchSemanticsNodes().maxBy { it.positionInRoot.y }
            val top = node.positionInRoot.y
            val bottom = top + node.size.height
            assertTrue(top >= 0f, "$label starts above the screen: $top")
            assertTrue(bottom <= rootHeight, "$label is cut off: bottom $bottom > $rootHeight")
        }
    }
}

private val TR = listOf("OYNA")
private val EN = listOf("PLAY")

@Config(sdk = [36], qualifiers = "tr-w320dp-h568dp-xhdpi")
class PrimaryActionLayoutTr100Test : PrimaryActionLayoutChecks(TR)

@Config(sdk = [36], qualifiers = "tr-w320dp-h568dp-xhdpi", fontScale = 1.3f)
class PrimaryActionLayoutTr130Test : PrimaryActionLayoutChecks(TR)

@Config(sdk = [36], qualifiers = "tr-w320dp-h568dp-xhdpi", fontScale = 2.0f)
class PrimaryActionLayoutTr200Test : PrimaryActionLayoutChecks(TR)

@Config(sdk = [36], qualifiers = "en-w320dp-h568dp-xhdpi")
class PrimaryActionLayoutEn100Test : PrimaryActionLayoutChecks(EN)

@Config(sdk = [36], qualifiers = "en-w320dp-h568dp-xhdpi", fontScale = 1.3f)
class PrimaryActionLayoutEn130Test : PrimaryActionLayoutChecks(EN)

@Config(sdk = [36], qualifiers = "en-w320dp-h568dp-xhdpi", fontScale = 2.0f)
class PrimaryActionLayoutEn200Test : PrimaryActionLayoutChecks(EN)
