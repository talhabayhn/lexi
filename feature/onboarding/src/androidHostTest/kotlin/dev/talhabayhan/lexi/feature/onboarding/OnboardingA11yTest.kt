package dev.talhabayhan.lexi.feature.onboarding

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.runComposeUiTest
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals

/** Screen-reader semantics of the onboarding pages. */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class OnboardingA11yTest {
    @Test
    fun `the step indicator is one node that says which step it is`() = runComposeUiTest {
        setContent {
            LexiTheme(reducedMotion = true) {
                OnboardingScreen(OnboardingUiState(currentStep = OnboardingStep.Step2), onIntent = {})
            }
        }

        onNodeWithContentDescription("Adım 2/3").assertExists()
    }

    @Test
    fun `every page title is a heading`() = runComposeUiTest {
        var step by mutableStateOf(OnboardingStep.Step1)
        setContent {
            LexiTheme(reducedMotion = true) { OnboardingScreen(OnboardingUiState(currentStep = step), onIntent = {}) }
        }

        OnboardingStep.entries.forEach { page ->
            step = page
            waitForIdle()
            val headings = onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
            assertEquals(1, headings.fetchSemanticsNodes().size, "headings on $page")
        }
    }
}
