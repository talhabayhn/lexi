package dev.talhabayhan.lexi.feature.onboarding

import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.LexiViewport
import dev.talhabayhan.lexi.core.domain.model.Level
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Onboarding screenshots: 3 steps, level choice, level picker, step 2 in English, and large font.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class OnboardingScreenshotTest {

    @Test
    fun onboardingStep1() {
        captureRoboImage("src/androidHostTest/screenshots/OnboardingStep1.png") {
            LexiTheme(reducedMotion = false) {
                OnboardingScreen(
                    state = OnboardingUiState(currentStep = OnboardingStep.Step1),
                    onIntent = {},
                )
            }
        }
    }

    @Test
    fun onboardingStep2() {
        captureRoboImage("src/androidHostTest/screenshots/OnboardingStep2.png") {
            LexiTheme(reducedMotion = false) {
                OnboardingScreen(
                    state = OnboardingUiState(currentStep = OnboardingStep.Step2),
                    onIntent = {},
                )
            }
        }
    }

    @Test
    fun onboardingStep3() {
        captureRoboImage("src/androidHostTest/screenshots/OnboardingStep3.png") {
            LexiTheme(reducedMotion = false) {
                OnboardingScreen(
                    state = OnboardingUiState(currentStep = OnboardingStep.Step3),
                    onIntent = {},
                )
            }
        }
    }

    @Test
    fun onboardingLevelChoice() {
        captureRoboImage("src/androidHostTest/screenshots/OnboardingLevelChoice.png") {
            LexiTheme(reducedMotion = false) {
                OnboardingScreen(
                    state = OnboardingUiState(currentStep = OnboardingStep.LevelChoice),
                    onIntent = {},
                )
            }
        }
    }

    @Test
    fun onboardingLevelPicker() {
        captureRoboImage("src/androidHostTest/screenshots/OnboardingLevelPicker.png") {
            LexiTheme(reducedMotion = false) {
                OnboardingScreen(
                    state = OnboardingUiState(
                        currentStep = OnboardingStep.LevelPicker,
                        selectedLevel = Level.A1,
                    ),
                    onIntent = {},
                )
            }
        }
    }

    @Test
    @Config(qualifiers = "en-w390dp-h844dp-xhdpi")
    fun onboardingStep2En() {
        captureRoboImage("src/androidHostTest/screenshots/OnboardingStep2En.png") {
            LexiTheme(reducedMotion = false) {
                OnboardingScreen(
                    state = OnboardingUiState(currentStep = OnboardingStep.Step2),
                    onIntent = {},
                )
            }
        }
    }

    @Test
    @Config(fontScale = 1.3f)
    fun onboardingStep2LargeFont() {
        captureRoboImage("src/androidHostTest/screenshots/OnboardingStep2LargeFont.png") {
            LexiTheme(reducedMotion = false) {
                OnboardingScreen(
                    state = OnboardingUiState(currentStep = OnboardingStep.Step2),
                    onIntent = {},
                )
            }
        }
    }

    // Layout tests: Step 1
    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi")
    fun step1_w320h568() = capture("OnboardingStep1_w320h568", step1)

    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi", fontScale = 1.3f)
    fun step1_w320h568FontScale130() = capture("OnboardingStep1_w320h568_fontScale130", step1)

    @Test
    @Config(qualifiers = "tr-w360dp-h640dp-xhdpi")
    fun step1_w360h640() = capture("OnboardingStep1_w360h640", step1)

    @Test
    @Config(qualifiers = "tr-w360dp-h640dp-xhdpi", fontScale = 1.3f)
    fun step1_w360h640FontScale130() = capture("OnboardingStep1_w360h640_fontScale130", step1)

    @Test
    @Config(qualifiers = "tr-w600dp-h960dp-mdpi")
    fun step1_w600h960() = capture("OnboardingStep1_w600h960", step1)

    @Test
    @Config(qualifiers = "tr-w600dp-h960dp-mdpi", fontScale = 1.3f)
    fun step1_w600h960FontScale130() = capture("OnboardingStep1_w600h960_fontScale130", step1)

    // Layout tests: Step 2
    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi")
    fun step2_w320h568() = capture("OnboardingStep2_w320h568", step2)

    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi", fontScale = 1.3f)
    fun step2_w320h568FontScale130() = capture("OnboardingStep2_w320h568_fontScale130", step2)

    @Test
    @Config(qualifiers = "tr-w360dp-h640dp-xhdpi")
    fun step2_w360h640() = capture("OnboardingStep2_w360h640", step2)

    @Test
    @Config(qualifiers = "tr-w360dp-h640dp-xhdpi", fontScale = 1.3f)
    fun step2_w360h640FontScale130() = capture("OnboardingStep2_w360h640_fontScale130", step2)

    @Test
    @Config(qualifiers = "tr-w600dp-h960dp-mdpi")
    fun step2_w600h960() = capture("OnboardingStep2_w600h960", step2)

    @Test
    @Config(qualifiers = "tr-w600dp-h960dp-mdpi", fontScale = 1.3f)
    fun step2_w600h960FontScale130() = capture("OnboardingStep2_w600h960_fontScale130", step2)

    // Layout tests: Step 3
    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi")
    fun step3_w320h568() = capture("OnboardingStep3_w320h568", step3)

    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi", fontScale = 1.3f)
    fun step3_w320h568FontScale130() = capture("OnboardingStep3_w320h568_fontScale130", step3)

    @Test
    @Config(qualifiers = "tr-w360dp-h640dp-xhdpi")
    fun step3_w360h640() = capture("OnboardingStep3_w360h640", step3)

    @Test
    @Config(qualifiers = "tr-w360dp-h640dp-xhdpi", fontScale = 1.3f)
    fun step3_w360h640FontScale130() = capture("OnboardingStep3_w360h640_fontScale130", step3)

    @Test
    @Config(qualifiers = "tr-w600dp-h960dp-mdpi")
    fun step3_w600h960() = capture("OnboardingStep3_w600h960", step3)

    @Test
    @Config(qualifiers = "tr-w600dp-h960dp-mdpi", fontScale = 1.3f)
    fun step3_w600h960FontScale130() = capture("OnboardingStep3_w600h960_fontScale130", step3)

    // Layout tests: Level Choice
    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi")
    fun levelChoice_w320h568() = capture("OnboardingLevelChoice_w320h568", levelChoice)

    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi", fontScale = 1.3f)
    fun levelChoice_w320h568FontScale130() = capture("OnboardingLevelChoice_w320h568_fontScale130", levelChoice)

    @Test
    @Config(qualifiers = "tr-w360dp-h640dp-xhdpi")
    fun levelChoice_w360h640() = capture("OnboardingLevelChoice_w360h640", levelChoice)

    @Test
    @Config(qualifiers = "tr-w360dp-h640dp-xhdpi", fontScale = 1.3f)
    fun levelChoice_w360h640FontScale130() = capture("OnboardingLevelChoice_w360h640_fontScale130", levelChoice)

    @Test
    @Config(qualifiers = "tr-w600dp-h960dp-mdpi")
    fun levelChoice_w600h960() = capture("OnboardingLevelChoice_w600h960", levelChoice)

    @Test
    @Config(qualifiers = "tr-w600dp-h960dp-mdpi", fontScale = 1.3f)
    fun levelChoice_w600h960FontScale130() = capture("OnboardingLevelChoice_w600h960_fontScale130", levelChoice)

    // Layout tests: Level Picker
    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi")
    fun levelPicker_w320h568() = capture("OnboardingLevelPicker_w320h568", levelPicker)

    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi", fontScale = 1.3f)
    fun levelPicker_w320h568FontScale130() = capture("OnboardingLevelPicker_w320h568_fontScale130", levelPicker)

    @Test
    @Config(qualifiers = "tr-w360dp-h640dp-xhdpi")
    fun levelPicker_w360h640() = capture("OnboardingLevelPicker_w360h640", levelPicker)

    @Test
    @Config(qualifiers = "tr-w360dp-h640dp-xhdpi", fontScale = 1.3f)
    fun levelPicker_w360h640FontScale130() = capture("OnboardingLevelPicker_w360h640_fontScale130", levelPicker)

    @Test
    @Config(qualifiers = "tr-w600dp-h960dp-mdpi")
    fun levelPicker_w600h960() = capture("OnboardingLevelPicker_w600h960", levelPicker)

    @Test
    @Config(qualifiers = "tr-w600dp-h960dp-mdpi", fontScale = 1.3f)
    fun levelPicker_w600h960FontScale130() = capture("OnboardingLevelPicker_w600h960_fontScale130", levelPicker)

    private fun capture(name: String, state: OnboardingUiState) {
        captureRoboImage("src/androidHostTest/screenshots/$name.png") {
            LexiTheme(reducedMotion = false) {
                LexiViewport {
                    OnboardingScreen(state = state, onIntent = {})
                }
            }
        }
    }

    private companion object {
        val step1 = OnboardingUiState(currentStep = OnboardingStep.Step1)
        val step2 = OnboardingUiState(currentStep = OnboardingStep.Step2)
        val step3 = OnboardingUiState(currentStep = OnboardingStep.Step3)
        val levelChoice = OnboardingUiState(currentStep = OnboardingStep.LevelChoice)
        val levelPicker = OnboardingUiState(currentStep = OnboardingStep.LevelPicker, selectedLevel = Level.A1)
    }
}
