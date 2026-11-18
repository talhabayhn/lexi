package dev.talhabayhan.lexi.feature.levels.picker

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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

/** The Home warm-up of the card flip and dialog (`FlowWarmUp`) is invisible to a screen reader and to taps. */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class CategoryFlipWarmUpTest {

    @Test
    fun `the flip warm-up has no semantics and ignores every tap`() = runComposeUiTest {
        val feedback = mutableListOf<Any>()
        setContent {
            CompositionLocalProvider(
                LocalHaptics provides Haptics { feedback += it },
                LocalSoundPlayer provides SoundPlayer { feedback += it },
            ) {
                LexiTheme(reducedMotion = false) { CategoryFlipWarmUp(Modifier.fillMaxSize()) }
            }
        }

        onAllNodes(hasClickAction()).assertCountEquals(0)
        onAllNodes(hasText("OYNA", substring = true, ignoreCase = true)).assertCountEquals(0)
        onRoot().performTouchInput {
            for (x in 1..GRID) {
                for (y in 1..GRID) click(Offset(width * x / (GRID + 1f), height * y / (GRID + 1f)))
            }
        }
        waitForIdle()

        assertEquals(emptyList(), feedback)
    }

    private companion object {
        const val GRID = 8
    }
}
