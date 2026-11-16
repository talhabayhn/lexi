package dev.talhabayhan.lexi.core.designsystem.motion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * #16 letter morph at four moments, side by side on 195 × 422 dp phones (half size): mid-expand, letter in on the
 * full screen, mid-shrink, landed on the marker (last frame before the overlay goes).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w820dp-h440dp-mdpi")
class LetterMorphLayerScreenshotTest {

    @Test
    fun letterMorphFrames() {
        captureRoboImage("src/androidHostTest/screenshots/LetterMorphLayer.png") {
            LexiTheme(reducedMotion = false) {
                val motion = LexiTheme.motion
                val flow = motion.flow
                val colors = LexiTheme.colors
                val timeline = LetterMorphTimeline(flow, motion.mechanical, motion.pop, colors.red, colors.ink)
                val shrinkStart = flow.inkShrink.delayMillis.toLong()
                val moments = listOf(
                    flow.inkExpand.delayMillis + flow.inkExpand.durationMillis / 2L to null,
                    shrinkStart to null,
                    shrinkStart + flow.inkShrink.durationMillis / 2L to shrinkStart,
                    shrinkStart + flow.inkShrink.durationMillis - 1L to shrinkStart,
                )
                Row(
                    Modifier.fillMaxSize().background(colors.white),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    moments.forEach { (elapsed, shrink) ->
                        Box(Modifier.size(195.dp, 422.dp).background(colors.cream)) {
                            LetterMorphLayer("A") { screen ->
                                val source = Rect(10f, screen.bottom - 70f, screen.right - 10f, screen.bottom - 30f)
                                val target = Rect(88f, 60f, 106f, 78f)
                                timeline.frameAt(elapsed, source, screen, target, shrink)
                            }
                        }
                    }
                }
            }
        }
    }
}
