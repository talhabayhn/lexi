package dev.talhabayhan.lexi.splash

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.Density
import dev.talhabayhan.lexi.core.designsystem.component.LexiSplash
import dev.talhabayhan.lexi.core.designsystem.component.LexiSplashLogo
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.AppInfo
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals

/**
 * The launch pose (`docs/PERF.md` § Launch logo): the held blocks sit where the iOS launch image (rendered at font
 * scale 1) has them, whatever the text size, and the background-free copy drawn above the warm-up's cover is laid out
 * exactly like the splash's own.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xxhdpi")
class LaunchLogoPoseTest {
    private fun logoBounds(fontScale: Float, backgroundFree: Boolean): Rect {
        var bounds = Rect.Zero
        runComposeUiTest {
            setContent {
                val density = LocalDensity.current
                CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                    LexiTheme(reducedMotion = false) {
                        if (backgroundFree) {
                            LexiSplashLogo(tagline = TAGLINE)
                        } else {
                            LexiSplash(progress = { 0f }, tagline = TAGLINE, logoOnly = true)
                        }
                    }
                }
            }
            bounds = onAllNodesWithContentDescription(AppInfo.NAME).fetchSemanticsNodes().first().boundsInRoot
        }
        return bounds
    }

    @Test
    fun `the held logo does not move with the font scale`() {
        assertEquals(logoBounds(1f, backgroundFree = false), logoBounds(2f, backgroundFree = false))
    }

    @Test
    fun `the logo above the warm-up cover is laid out like the splash's own`() {
        assertEquals(logoBounds(1f, backgroundFree = false), logoBounds(1f, backgroundFree = true))
        assertEquals(logoBounds(1.5f, backgroundFree = false), logoBounds(1.5f, backgroundFree = true))
    }

    private companion object {
        const val TAGLINE = "A'DAN Z'YE İNGİLİZCE"
    }
}
