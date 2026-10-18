package dev.talhabayhan.lexi.feature.settings.about

import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.LexiViewport
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Screenshot tests for the About and licenses screen (M8.5, DESIGN-DEBT).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class AboutScreenshotTest {

    @Test
    fun designSample() {
        captureRoboImage("src/androidHostTest/screenshots/About.png") {
            LexiTheme(reducedMotion = false) {
                AboutScreen(state = AboutUiState(), onIntent = {})
            }
        }
    }

    @Test
    @Config(qualifiers = "en-w390dp-h844dp-xhdpi")
    fun english() {
        captureRoboImage("src/androidHostTest/screenshots/AboutEn.png") {
            LexiTheme(reducedMotion = false) {
                AboutScreen(state = AboutUiState(), onIntent = {})
            }
        }
    }

    @Test
    @Config(fontScale = 1.3f)
    fun largeFont() {
        captureRoboImage("src/androidHostTest/screenshots/AboutLargeFont.png") {
            LexiTheme(reducedMotion = false) {
                AboutScreen(state = AboutUiState(), onIntent = {})
            }
        }
    }

    @Test
    @Config(qualifiers = "tr-w600dp-h1100dp-mdpi")
    fun expandedW600h1100() {
        captureRoboImage("src/androidHostTest/screenshots/About_w600h1100.png") {
            LexiTheme(reducedMotion = false) {
                LexiViewport {
                    AboutScreen(state = AboutUiState(), onIntent = {})
                }
            }
        }
    }
}
