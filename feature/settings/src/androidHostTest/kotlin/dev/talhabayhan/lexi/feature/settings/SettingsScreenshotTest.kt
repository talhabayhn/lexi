package dev.talhabayhan.lexi.feature.settings

import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.LexiViewport
import dev.talhabayhan.lexi.core.domain.settings.AppLanguage
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
            LexiTheme(reducedMotion = false) {
                SettingsScreen(state = SettingsUiState(dailyReminder = true), onIntent = {})
            }
        }
    }

    @Test
    fun languageTurkish() {
        captureRoboImage("src/androidHostTest/screenshots/SettingsLanguageTr.png") {
            LexiTheme(reducedMotion = false) {
                SettingsScreen(state = SettingsUiState(appLanguage = AppLanguage.TR), onIntent = {})
            }
        }
    }

    @Test
    fun languageEnglish() {
        captureRoboImage("src/androidHostTest/screenshots/SettingsLanguageEn.png") {
            LexiTheme(reducedMotion = false) {
                SettingsScreen(state = SettingsUiState(appLanguage = AppLanguage.EN), onIntent = {})
            }
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
            LexiTheme(reducedMotion = false) {
                SettingsScreen(state = SettingsUiState(dailyReminder = true), onIntent = {})
            }
        }
    }

    @Test
    @Config(qualifiers = "en-w390dp-h844dp-xhdpi")
    fun englishLanguageTurkish() {
        captureRoboImage("src/androidHostTest/screenshots/SettingsEnLanguageTr.png") {
            LexiTheme(reducedMotion = false) {
                SettingsScreen(state = SettingsUiState(appLanguage = AppLanguage.TR), onIntent = {})
            }
        }
    }

    @Test
    @Config(qualifiers = "en-w390dp-h844dp-xhdpi")
    fun englishLanguageEnglish() {
        captureRoboImage("src/androidHostTest/screenshots/SettingsEnLanguageEn.png") {
            LexiTheme(reducedMotion = false) {
                SettingsScreen(state = SettingsUiState(appLanguage = AppLanguage.EN), onIntent = {})
            }
        }
    }

    @Test
    @Config(fontScale = 1.3f)
    fun largeFont() {
        captureRoboImage("src/androidHostTest/screenshots/SettingsLargeFont.png") {
            LexiTheme(reducedMotion = false) { SettingsScreen(state = SettingsUiState(), onIntent = {}) }
        }
    }

    @Test
    @Config(qualifiers = "tr-w600dp-h1100dp-mdpi")
    fun expandedW600h1100() {
        captureRoboImage("src/androidHostTest/screenshots/Settings_w600h1100.png") {
            LexiTheme(reducedMotion = false) {
                LexiViewport { SettingsScreen(state = SettingsUiState(), onIntent = {}) }
            }
        }
    }

    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi")
    fun w320h568() = capture("Settings_w320h568")

    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi", fontScale = 1.3f)
    fun w320h568FontScale130() = capture("Settings_w320h568_fontScale130")

    @Test
    @Config(qualifiers = "tr-w360dp-h640dp-xhdpi")
    fun w360h640() = capture("Settings_w360h640")

    @Test
    @Config(qualifiers = "tr-w360dp-h640dp-xhdpi", fontScale = 1.3f)
    fun w360h640FontScale130() = capture("Settings_w360h640_fontScale130")

    @Test
    @Config(qualifiers = "tr-w600dp-h960dp-mdpi")
    fun w600h960() = capture("Settings_w600h960")

    @Test
    @Config(qualifiers = "tr-w600dp-h960dp-mdpi", fontScale = 1.3f)
    fun w600h960FontScale130() = capture("Settings_w600h960_fontScale130")

    @Test
    @Config(qualifiers = "tr-w390dp-h844dp-xhdpi", fontScale = 2.0f)
    fun fontScale200() = capture("Settings_fontScale200")

    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi", fontScale = 2.0f)
    fun w320h568FontScale200() = capture("Settings_w320h568_fontScale200")

    private fun capture(name: String) {
        captureRoboImage("src/androidHostTest/screenshots/$name.png") {
            LexiTheme(reducedMotion = false) {
                LexiViewport {
                    SettingsScreen(state = SettingsUiState(), onIntent = {})
                }
            }
        }
    }
}
