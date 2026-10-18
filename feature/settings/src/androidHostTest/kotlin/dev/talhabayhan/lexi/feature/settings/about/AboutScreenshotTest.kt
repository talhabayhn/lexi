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

    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi")
    fun w320h568() = capture("About_w320h568")

    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi", fontScale = 1.3f)
    fun w320h568FontScale130() = capture("About_w320h568_fontScale130")

    @Test
    @Config(qualifiers = "tr-w360dp-h640dp-xhdpi")
    fun w360h640() = capture("About_w360h640")

    @Test
    @Config(qualifiers = "tr-w360dp-h640dp-xhdpi", fontScale = 1.3f)
    fun w360h640FontScale130() = capture("About_w360h640_fontScale130")

    @Test
    @Config(qualifiers = "tr-w600dp-h960dp-mdpi")
    fun w600h960() = capture("About_w600h960")

    @Test
    @Config(qualifiers = "tr-w600dp-h960dp-mdpi", fontScale = 1.3f)
    fun w600h960FontScale130() = capture("About_w600h960_fontScale130")

    private fun capture(name: String) {
        captureRoboImage("src/androidHostTest/screenshots/$name.png") {
            LexiTheme(reducedMotion = false) {
                LexiViewport {
                    AboutScreen(state = AboutUiState(), onIntent = {})
                }
            }
        }
    }
}
