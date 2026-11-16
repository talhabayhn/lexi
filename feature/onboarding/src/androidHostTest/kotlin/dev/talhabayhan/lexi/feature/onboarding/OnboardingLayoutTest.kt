package dev.talhabayhan.lexi.feature.onboarding

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertTrue

/**
 * Regression: on a phone where the page is taller than the screen (large font / display size) the primary button
 * ("Devam", "Başlayalım", the level test CTA) was the last item of the scrolling content and was cut off at the
 * viewport edge. The actions are pinned now: the button's full layout bounds are inside the screen without scrolling.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w320dp-h568dp-xhdpi", fontScale = 1.3f)
class OnboardingLayoutTest {
    @Test
    fun `step 1 primary button is fully on screen without scrolling`() =
        assertPrimaryButtonFullyVisible(OnboardingStep.Step1, "Devam")

    @Test
    fun `step 3 primary button is fully on screen without scrolling`() =
        assertPrimaryButtonFullyVisible(OnboardingStep.Step3, "Başlayalım")

    @Test
    fun `level choice primary button is fully on screen without scrolling`() =
        assertPrimaryButtonFullyVisible(OnboardingStep.LevelChoice, "SEVİYE TESTİNE BAŞLA")

    @Test
    fun `level test intro primary button is fully on screen without scrolling`() =
        assertPrimaryButtonFullyVisible(OnboardingStep.LevelTestIntro, "Başla")

    private fun assertPrimaryButtonFullyVisible(step: OnboardingStep, label: String) = runComposeUiTest {
        setContent {
            LexiTheme(reducedMotion = true) {
                OnboardingScreen(OnboardingUiState(currentStep = step), onIntent = {})
            }
        }

        val screen = onRoot().getUnclippedBoundsInRoot()
        val bounds = onNodeWithText(label).getUnclippedBoundsInRoot()
        assertTrue(bounds.top >= 0.dp, "$label starts above the screen: ${bounds.top}")
        assertTrue(bounds.bottom <= screen.bottom, "$label is cut off: bottom ${bounds.bottom} > ${screen.bottom}")
    }
}
