package dev.talhabayhan.lexi.feature.levels.picker

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runComposeUiTest
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import dev.talhabayhan.lexi.core.designsystem.motion.LocalMotionPolicy
import dev.talhabayhan.lexi.core.designsystem.motion.MotionPolicy
import dev.talhabayhan.lexi.core.designsystem.motion.MotionTier
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Fail-safe (`motion.md` § Fail-safes): the grid entrance (#18) interrupted by the app going to the background ends with
 * every card fully drawn, on the next frame, instead of resuming mid-fade (or, as in 1.1.2 on iOS, never showing).
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class GridEntranceFailSafeTest {

    private class Owner : LifecycleOwner {
        val registry = LifecycleRegistry.createUnsafe(this).apply { currentState = Lifecycle.State.RESUMED }
        override val lifecycle: Lifecycle get() = registry
    }

    private fun ComposeUiTest.picker(entrance: Boolean, owner: Owner) {
        mainClock.autoAdvance = false
        val policy = MotionPolicy().apply { forcedTier = MotionTier.Full }
        setContent {
            CompositionLocalProvider(LocalMotionPolicy provides policy, LocalLifecycleOwner provides owner) {
                LexiTheme(reducedMotion = false) {
                    CategoryPickerScreen(PickerSampleState, onIntent = {}, entrance = entrance)
                }
            }
        }
    }

    private fun ComposeUiTest.grid(): ImageBitmap = onRoot().captureToImage()

    @Test
    fun `the background mid-entrance lands every card`() {
        lateinit var atRest: IntArray
        runComposeUiTest {
            picker(entrance = false, owner = Owner())
            repeat(3) { mainClock.advanceTimeByFrame() }
            atRest = grid().pixels()
        }
        runComposeUiTest {
            val owner = Owner()
            picker(entrance = true, owner = owner)
            repeat(3) { mainClock.advanceTimeByFrame() }
            assertTrue(!grid().pixels().contentEquals(atRest), "the entrance is running")
            owner.registry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            repeat(2) { mainClock.advanceTimeByFrame() }
            val after = grid().pixels()
            assertEquals(0, after.indices.count { after[it] != atRest[it] }, "every card at rest")
        }
    }

    private fun ImageBitmap.pixels(): IntArray {
        val map = toPixelMap()
        return IntArray(width * height) { i -> map[i % width, i / width].hashCode() }
    }
}
