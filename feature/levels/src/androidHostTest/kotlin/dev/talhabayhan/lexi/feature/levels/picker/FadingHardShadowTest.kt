package dev.talhabayhan.lexi.feature.levels.picker

import android.graphics.Bitmap
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.motion.FlowMotion
import dev.talhabayhan.lexi.core.designsystem.motion.LocalMotionPolicy
import dev.talhabayhan.lexi.core.designsystem.motion.MotionPolicy
import dev.talhabayhan.lexi.core.designsystem.motion.MotionTier
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.math.abs
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Regression (iPhone, 1.1.1): hard shadows popped in about 250 ms after their element looked opaque. A `graphicsLayer`
 * with alpha < 1 is rendered offscreen, clipped to its own bounds, and a hard shadow draws right of and below them, so
 * it was cut off until the fade reached exactly 1. Stepping the test clock frame by frame through each fade, every frame
 * in which the element is visible must also show its shadow outside the element's bounds.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class FadingHardShadowTest {

    // One card, no packs: the area around the dialog's bottom edge is the plain screen under the scrim.
    private val oneCard = PickerSampleState.copy(categories = persistentListOf(PickerSampleState.categories.first()))

    private var state by mutableStateOf(oneCard)

    private lateinit var flow: FlowMotion

    private fun picker(tier: MotionTier, entrance: Boolean = false, block: ComposeUiTest.() -> Unit) =
        runComposeUiTest {
            mainClock.autoAdvance = false
            val policy = MotionPolicy().apply { forcedTier = tier }
            setContent {
                CompositionLocalProvider(LocalMotionPolicy provides policy) {
                    LexiTheme(reducedMotion = false) {
                        flow = LexiTheme.motion.flow
                        CategoryPickerScreen(state, onIntent = {}, entrance = entrance)
                    }
                }
            }
            block()
        }

    private fun ComposeUiTest.openDialog() {
        mainClock.advanceTimeByFrame()
        state = state.copy(dialog = state.categories.first(), selectedCategory = CategoryId.MIXED)
        Snapshot.sendApplyNotifications()
    }

    /**
     * Steps [frames] frames; in each, [element]'s bounds give three samples: [body] (inside the element, its border),
     * [shadow] (outside it, inside the hard shadow) and [background] (outside both, same backdrop). Whenever the body
     * differs from the backdrop the shadow must too. Fails if fewer than 2 frames caught the element mid-fade.
     */
    private fun ComposeUiTest.assertShadowFollowsBody(
        frames: Int,
        firstFrame: Int = 0,
        element: () -> SemanticsNodeInteraction,
        body: (Rect) -> Pair<Dp, Dp>,
        shadow: (Rect) -> Pair<Dp, Dp>,
        background: (Rect) -> Pair<Dp, Dp>,
    ) {
        var midFade = 0
        var lastBody = 0
        repeat(frames) { frame ->
            mainClock.advanceTimeByFrame()
            if (frame < firstFrame) return@repeat
            val node = runCatching { element().fetchSemanticsNode() }.getOrNull() ?: return@repeat
            if (node.size.width == 0) return@repeat
            val bounds = element().getBoundsInRoot().let {
                Rect(it.left.value, it.top.value, it.right.value, it.bottom.value)
            }
            val image = onRoot().captureToImage().asAndroidBitmap()
            // Early in the Full flip the content is laid out at the final size but drawn from the card's rectangle.
            val b = image.at(body(bounds)) ?: return@repeat
            val s = image.at(shadow(bounds)) ?: return@repeat
            val bg = image.at(background(bounds)) ?: return@repeat
            if (differs(b, bg)) {
                if (!differs(s, bg)) {
                    val seen = "element visible (${hex(b)} over ${hex(bg)})"
                    fail("frame $frame: $seen but its hard shadow is missing (${hex(s)})")
                }
                if (lastBody != 0 && b != lastBody) midFade++
            }
            lastBody = b
        }
        assertTrue(midFade >= 2, "the fade was not caught mid-way ($midFade frames)")
    }

    private fun ComposeUiTest.playButton() = onNode(hasText("OYNA") and hasClickAction(), useUnmergedTree = false)

    private fun ComposeUiTest.panel() = onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.PaneTitle))

    private fun Bitmap.at(point: Pair<Dp, Dp>): Int? {
        val x = (point.first.value * PX_PER_DP).toInt()
        val y = (point.second.value * PX_PER_DP).toInt()
        return if (x in 0 until width && y in 0 until height) getPixel(x, y) else null
    }

    @Test
    fun `the PLAY button keeps its hard shadow while its row fades in`() = picker(MotionTier.Full) {
        openDialog()
        assertShadowFollowsBody(
            frames = OPEN_FRAMES,
            // The rows fade in once the panel has grown to its own rectangle (before, the content is drawn from the
            // card's rectangle, away from its semantics bounds).
            firstFrame = (flow.dialogGrow.endMillis / FRAME_MILLIS).toInt() + 1,
            element = { playButton() },
            // The bottom border, 3dp into the s6 shadow below it, the panel's padding left of the button.
            body = { it.centerX() to (it.bottom - 1f).dp },
            shadow = { it.centerX() to (it.bottom + 3f).dp },
            background = { (it.left - 6f).dp to (it.bottom + 3f).dp },
        )
    }

    @Test
    fun `the Lite dialog panel keeps its hard shadow while it fades in`() = picker(MotionTier.Lite) {
        openDialog()
        assertShadowFollowsBody(
            frames = OPEN_FRAMES,
            element = { panel() },
            body = { it.centerX() to (it.bottom - 1f).dp },
            shadow = { it.centerX() to (it.bottom + 4f).dp },
            background = { SCREEN_EDGE to (it.bottom + 4f).dp },
        )
    }

    @Test
    fun `the cards keep their hard shadows while the grid enters`() = picker(MotionTier.Full, entrance = true) {
        assertShadowFollowsBody(
            frames = ENTRANCE_FRAMES,
            element = { onNodeWithContentDescription("Karışık, 412 / 850 kelime öğrenildi") },
            body = { it.centerX() to (it.bottom - 1f).dp },
            shadow = { it.centerX() to (it.bottom + 2f).dp },
            background = { SCREEN_EDGE to (it.bottom + 2f).dp },
        )
    }

    private fun Rect.centerX(): Dp = ((left + right) / 2f).dp

    private fun differs(a: Int, b: Int): Boolean = channels(a).zip(channels(b)).any { (x, y) -> abs(x - y) >= MIN_DIFF }

    private fun channels(c: Int) = listOf((c shr 16) and 0xFF, (c shr 8) and 0xFF, c and 0xFF)

    private fun hex(c: Int) = "#%06X".format(c and 0xFFFFFF)

    private companion object {
        const val OPEN_FRAMES = 50
        const val ENTRANCE_FRAMES = 50
        const val MIN_DIFF = 8
        const val FRAME_MILLIS = 16L
        const val PX_PER_DP = 2f // xhdpi
        val SCREEN_EDGE = 4.dp
    }
}
