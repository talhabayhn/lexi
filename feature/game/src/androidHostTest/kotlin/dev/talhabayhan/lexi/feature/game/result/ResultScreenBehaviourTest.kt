package dev.talhabayhan.lexi.feature.game.result

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.game.GameMode
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class ResultScreenBehaviourTest {

    @Test
    fun `tekrar oyna is shown after a normal game`() = runComposeUiTest {
        setContent { LexiTheme(reducedMotion = true) { ResultScreen(ResultSamples.design, onIntent = {}) } }

        onNodeWithText("TEKRAR OYNA").assertIsDisplayed()
    }

    @Test
    fun `tekrar oyna is hidden after a daily and share stays`() = runComposeUiTest {
        setContent {
            LexiTheme(reducedMotion = true) {
                ResultScreen(ResultSamples.design.copy(mode = GameMode.DAILY), onIntent = {})
            }
        }

        onNodeWithText("TEKRAR OYNA").assertDoesNotExist()
        onNodeWithText("Paylaş").assertIsDisplayed()
    }
}
