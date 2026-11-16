package dev.talhabayhan.lexi.feature.levels

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
 * The level select's start button is fully on screen without scrolling on a 320 × 568 phone, at 100 %, 130 % and
 * 200 % font scale, in Turkish and English (the pattern of `OnboardingLayoutTest`).
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
abstract class PrimaryActionLayoutChecks(private val start: String) {
    @Test
    fun `level select keeps start on screen`() = runComposeUiTest {
        setContent {
            LexiTheme(reducedMotion = true) {
                LexiViewport { LevelSelectScreen(LevelSelectSampleState, onIntent = {}) }
            }
        }
        // The unclipped layout bounds: a button scrolled out of its viewport still lies below the screen.
        val rootHeight = onRoot().fetchSemanticsNode().size.height
        val button = onAllNodesWithText(start).fetchSemanticsNodes().maxBy { it.positionInRoot.y }
        val top = button.positionInRoot.y
        val bottom = top + button.size.height
        assertTrue(top >= 0f, "$start starts above the screen: $top")
        assertTrue(bottom <= rootHeight, "$start is cut off: bottom $bottom > $rootHeight")
    }
}

@Config(sdk = [36], qualifiers = "tr-w320dp-h568dp-xhdpi")
class PrimaryActionLayoutTr100Test : PrimaryActionLayoutChecks("BAŞLA")

@Config(sdk = [36], qualifiers = "tr-w320dp-h568dp-xhdpi", fontScale = 1.3f)
class PrimaryActionLayoutTr130Test : PrimaryActionLayoutChecks("BAŞLA")

@Config(sdk = [36], qualifiers = "tr-w320dp-h568dp-xhdpi", fontScale = 2.0f)
class PrimaryActionLayoutTr200Test : PrimaryActionLayoutChecks("BAŞLA")

@Config(sdk = [36], qualifiers = "en-w320dp-h568dp-xhdpi")
class PrimaryActionLayoutEn100Test : PrimaryActionLayoutChecks("START")

@Config(sdk = [36], qualifiers = "en-w320dp-h568dp-xhdpi", fontScale = 1.3f)
class PrimaryActionLayoutEn130Test : PrimaryActionLayoutChecks("START")

@Config(sdk = [36], qualifiers = "en-w320dp-h568dp-xhdpi", fontScale = 2.0f)
class PrimaryActionLayoutEn200Test : PrimaryActionLayoutChecks("START")
