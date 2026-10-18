package dev.talhabayhan.lexi.feature.onboarding

import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
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
}
