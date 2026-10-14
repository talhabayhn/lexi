package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.feedback.LocalHaptics
import dev.talhabayhan.lexi.core.designsystem.feedback.LocalSoundPlayer
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.feedback.HapticKind
import dev.talhabayhan.lexi.core.domain.feedback.Haptics
import dev.talhabayhan.lexi.core.domain.feedback.SoundEffect
import dev.talhabayhan.lexi.core.domain.feedback.SoundPlayer
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w390dp-h844dp-xxhdpi")
class PressableSurfaceTest {

    private val haptics = RecordingHaptics()
    private val sounds = RecordingSoundPlayer()
    private var clicks = 0

    @Test
    fun `click plays one light haptic and one tap sound`() = runComposeUiTest {
        setSurface()

        onNodeWithTag(SURFACE).performClick()
        waitForIdle()

        assertEquals(1, clicks)
        assertEquals(listOf(HapticKind.Light), haptics.played)
        assertEquals(listOf(SoundEffect.Tap), sounds.played)
    }

    @Test
    fun `cancelled touch fires nothing`() = runComposeUiTest {
        setSurface()

        onNodeWithTag(SURFACE).performTouchInput {
            down(center)
            cancel()
        }
        waitForIdle()

        assertEquals(0, clicks)
        assertTrue(haptics.played.isEmpty())
        assertTrue(sounds.played.isEmpty())
    }

    @Test
    fun `disabled surface fires nothing and does not move`() = runComposeUiTest {
        setSurface(enabled = false)
        mainClock.autoAdvance = false

        onNodeWithTag(SURFACE).performTouchInput { down(center) }
        mainClock.advanceTimeBy(PRESS_MILLIS + FRAME_MILLIS)
        assertEquals(0f, contentShiftDp())
        onNodeWithTag(SURFACE).performTouchInput { up() }
        mainClock.advanceTimeBy(PRESS_MILLIS + FRAME_MILLIS)

        assertEquals(0, clicks)
        assertTrue(haptics.played.isEmpty())
        assertTrue(sounds.played.isEmpty())
    }

    @Test
    fun `quick tap presses fully at 80 ms and is back at 160 ms`() = runComposeUiTest {
        setSurface()
        mainClock.autoAdvance = false

        onNodeWithTag(SURFACE).performTouchInput {
            down(center)
            up()
        }
        // One frame for the animation to pick up its start time, as on device.
        mainClock.advanceTimeBy(FRAME_MILLIS + PRESS_MILLIS / 2)
        val halfway = contentShiftDp()
        mainClock.advanceTimeBy(PRESS_MILLIS / 2)
        val pressed = contentShiftDp()
        mainClock.advanceTimeBy(PRESS_MILLIS + FRAME_MILLIS)
        val released = contentShiftDp()

        assertTrue(halfway > 0f && halfway < SHADOW_DP, "halfway shift $halfway dp")
        assertEquals(SHADOW_DP, pressed, absoluteTolerance = 0.05f)
        assertEquals(0f, released, absoluteTolerance = 0.05f)
    }

    private fun ComposeUiTest.setSurface(enabled: Boolean = true) {
        setContent {
            CompositionLocalProvider(LocalHaptics provides haptics, LocalSoundPlayer provides sounds) {
                LexiTheme(reducedMotion = false) {
                    PressableSurface(
                        onClick = { clicks++ },
                        enabled = enabled,
                        shadow = LexiTheme.shadows.s5,
                        modifier = Modifier.testTag(SURFACE).size(120.dp, 52.dp),
                    ) {
                        Box(Modifier.testTag(CONTENT).size(10.dp))
                    }
                }
            }
        }
    }

    /** Horizontal translation of the content relative to the (unmoved) clickable surface, in dp. */
    private fun ComposeUiTest.contentShiftDp(): Float {
        val surface = onNodeWithTag(SURFACE).fetchSemanticsNode()
        val content = onNodeWithTag(CONTENT, useUnmergedTree = true).fetchSemanticsNode()
        val idleLeft = surface.positionInRoot.x + (surface.size.width - content.size.width) / 2f
        return (content.positionInRoot.x - idleLeft) / density.density
    }

    private class RecordingHaptics : Haptics {
        val played = mutableListOf<HapticKind>()
        override fun perform(kind: HapticKind) {
            played += kind
        }
    }

    private class RecordingSoundPlayer : SoundPlayer {
        val played = mutableListOf<SoundEffect>()
        override fun play(effect: SoundEffect) {
            played += effect
        }
    }

    private companion object {
        const val SURFACE = "surface"
        const val CONTENT = "content"
        const val PRESS_MILLIS = 80L
        const val FRAME_MILLIS = 16L
        const val SHADOW_DP = 5f
    }
}
