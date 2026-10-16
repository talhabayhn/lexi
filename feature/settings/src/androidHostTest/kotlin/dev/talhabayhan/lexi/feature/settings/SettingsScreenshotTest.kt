package dev.talhabayhan.lexi.feature.settings

import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
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
}
