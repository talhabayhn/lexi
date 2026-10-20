package dev.talhabayhan.lexi.feature.game

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import dev.talhabayhan.lexi.core.designsystem.feedback.LocalHaptics
import dev.talhabayhan.lexi.core.designsystem.feedback.LocalSoundPlayer
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.feedback.Haptics
import dev.talhabayhan.lexi.core.domain.feedback.SoundPlayer
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class GameWarmUpTest {

    @Test
    fun `the warm-up has no semantics and ignores every tap`() = runComposeUiTest {
        val feedback = mutableListOf<Any>()
        setFeedbackContent(feedback) { GameWarmUp(Modifier.fillMaxSize()) }

        onAllNodes(hasClickAction()).assertCountEquals(0)
        onAllNodes(hasText("PAS")).assertCountEquals(0)
        tapEverywhere()

        assertEquals(emptyList(), feedback)
    }

    /** Control: the same taps on the real screen do reach its keys. */
    @Test
    fun `the same taps on the game screen give feedback`() = runComposeUiTest {
        val feedback = mutableListOf<Any>()
        setFeedbackContent(feedback) {
            GameScreen(GameSamples.design, clock = { GameSamples.designClock }, onIntent = {})
        }

        tapEverywhere()

        assertTrue(feedback.isNotEmpty())
    }

    private fun ComposeUiTest.setFeedbackContent(feedback: MutableList<Any>, content: @Composable () -> Unit) {
        setContent {
            CompositionLocalProvider(
                LocalHaptics provides Haptics { feedback += it },
                LocalSoundPlayer provides SoundPlayer { feedback += it },
            ) {
                LexiTheme(reducedMotion = true, content = content)
            }
        }
    }

    private fun ComposeUiTest.tapEverywhere() {
        onRoot().performTouchInput {
            for (x in 1..GRID) {
                for (y in 1..GRID) click(Offset(width * x / (GRID + 1f), height * y / (GRID + 1f)))
            }
        }
        waitForIdle()
    }

    private companion object {
        const val GRID = 12
    }
}
