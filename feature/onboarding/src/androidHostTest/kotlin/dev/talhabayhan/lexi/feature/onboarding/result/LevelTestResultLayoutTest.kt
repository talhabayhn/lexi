package dev.talhabayhan.lexi.feature.onboarding.result

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runComposeUiTest
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.LexiViewport
import dev.talhabayhan.lexi.core.domain.model.Level
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertTrue

/**
 * The level test result's two buttons are fully on screen without scrolling on a 320 × 568 phone, at 100 %, 130 % and
 * 200 % font scale, in Turkish and English (the pattern of `OnboardingLayoutTest`).
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
abstract class LevelTestResultLayoutChecks(private val labels: List<String>) {
    @Test
    fun `result keeps both buttons on screen`() = runComposeUiTest {
        setContent {
            LexiTheme(reducedMotion = true) {
                LexiViewport {
                    LevelTestResultScreen(
                        state = LevelTestResultUiState(level = Level.A1),
                        onIntent = {},
                    )
                }
            }
        }
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

private val TR = listOf("A1 ile başla", "Seviyeyi değiştir")
private val EN = listOf("Start at A1", "Change level")

@Config(sdk = [36], qualifiers = "tr-w320dp-h568dp-xhdpi")
class LevelTestResultLayoutTr100Test : LevelTestResultLayoutChecks(TR)

@Config(sdk = [36], qualifiers = "tr-w320dp-h568dp-xhdpi", fontScale = 1.3f)
class LevelTestResultLayoutTr130Test : LevelTestResultLayoutChecks(TR)

@Config(sdk = [36], qualifiers = "tr-w320dp-h568dp-xhdpi", fontScale = 2.0f)
class LevelTestResultLayoutTr200Test : LevelTestResultLayoutChecks(TR)

@Config(sdk = [36], qualifiers = "en-w320dp-h568dp-xhdpi")
class LevelTestResultLayoutEn100Test : LevelTestResultLayoutChecks(EN)

@Config(sdk = [36], qualifiers = "en-w320dp-h568dp-xhdpi", fontScale = 1.3f)
class LevelTestResultLayoutEn130Test : LevelTestResultLayoutChecks(EN)

@Config(sdk = [36], qualifiers = "en-w320dp-h568dp-xhdpi", fontScale = 2.0f)
class LevelTestResultLayoutEn200Test : LevelTestResultLayoutChecks(EN)
