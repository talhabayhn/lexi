package dev.talhabayhan.lexi.feature.onboarding

import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Level test intro (DESIGN-DEBT, no design: built from `16-components` on the onboarding layout). */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class LevelTestIntroScreenshotTest {
    @Test
    fun levelTestIntro() {
        captureRoboImage("src/androidHostTest/screenshots/OnboardingLevelTestIntro.png") {
            LexiTheme(reducedMotion = false) {
                OnboardingScreen(state = OnboardingUiState(currentStep = OnboardingStep.LevelTestIntro), onIntent = {})
            }
        }
    }
}
