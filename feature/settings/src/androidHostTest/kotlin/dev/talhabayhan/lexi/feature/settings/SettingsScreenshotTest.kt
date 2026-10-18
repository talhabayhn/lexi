package dev.talhabayhan.lexi.feature.settings

import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.LexiViewport
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The stateless settings screen at the size and scale of `docs/design/screens/11-settings.png` (390 x 844dp,
 * xhdpi = 2x), in Turkish like the design.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class SettingsScreenshotTest {

    @Test
    fun designSample() {
        captureRoboImage("src/androidHostTest/screenshots/Settings.png") {
            LexiTheme(reducedMotion = false) { SettingsScreen(state = SettingsUiState(), onIntent = {}) }
        }
    }

    @Test
    fun switchesOff() {
        captureRoboImage("src/androidHostTest/screenshots/SettingsOff.png") {
            LexiTheme(reducedMotion = false) {
                SettingsScreen(
                    state = SettingsUiState(soundEffects = false, haptics = false, speakOnCorrect = false),
                    onIntent = {},
                )
            }
        }
    }

    @Test
    @Config(qualifiers = "en-w390dp-h844dp-xhdpi")
    fun english() {
        captureRoboImage("src/androidHostTest/screenshots/SettingsEn.png") {
            LexiTheme(reducedMotion = false) { SettingsScreen(state = SettingsUiState(), onIntent = {}) }
        }
    }

    @Test
    @Config(fontScale = 1.3f)
    fun largeFont() {
        captureRoboImage("src/androidHostTest/screenshots/SettingsLargeFont.png") {
            LexiTheme(reducedMotion = false) { SettingsScreen(state = SettingsUiState(), onIntent = {}) }
        }
    }

    /** Expanded tier (`LexiViewport`): the design scaled, the column centred and at most 430dp (× scale) wide. */
    @Test
    @Config(qualifiers = "tr-w600dp-h1100dp-mdpi")
    fun expandedW600h1100() {
        captureRoboImage("src/androidHostTest/screenshots/Settings_w600h1100.png") {
            LexiTheme(reducedMotion = false) {
                LexiViewport { SettingsScreen(state = SettingsUiState(), onIntent = {}) }
            }
        }
    }
}
