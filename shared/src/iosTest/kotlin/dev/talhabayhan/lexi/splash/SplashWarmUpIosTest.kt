package dev.talhabayhan.lexi.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Regression (CI, iOS simulator, first launch): the warm-up laid the game board out in the splash's half-height cover,
 * the letter wheel got a size <= 0 and Skia's paragraph builder aborted the app on a negative line height (Android has
 * no such check, so the host tests passed). The warm-up layer must compose, lay out and draw at phone sizes on iOS.
 */
@OptIn(ExperimentalTestApi::class)
class SplashWarmUpIosTest {
    @Test
    fun theWarmUpLayerDrawsOnAPhoneWithoutCrashing() = runComposeUiTest {
        setContent {
            LexiTheme(reducedMotion = false) {
                Box(Modifier.size(PHONE_WIDTH, PHONE_HEIGHT)) { SplashWarmUpLayer() }
            }
        }
        waitForIdle()
        val image = onRoot().captureToImage()
        assertTrue(image.width > 0 && image.height > 0)
    }

    @Test
    fun theWarmUpLayerDrawsOnASmallPhoneWithoutCrashing() = runComposeUiTest {
        setContent {
            LexiTheme(reducedMotion = false) {
                Box(Modifier.size(SMALL_WIDTH, SMALL_HEIGHT)) { SplashWarmUpLayer() }
            }
        }
        waitForIdle()
        assertTrue(onRoot().captureToImage().width > 0)
    }

    private companion object {
        val PHONE_WIDTH = 390.dp
        val PHONE_HEIGHT = 844.dp

        // iPhone SE (2nd / 3rd gen).
        val SMALL_WIDTH = 375.dp
        val SMALL_HEIGHT = 667.dp
    }
}
