package dev.talhabayhan.lexi.feature.onboarding

import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runComposeUiTest
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.LexiViewport
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertTrue

/**
 * Regression: on a phone where the page is taller than the screen (large font / display size) the primary button
 * ("Devam", "Başlayalım", the level test CTA) was the last item of the scrolling content and was cut off at the
 * viewport edge. The actions are pinned now: the button's full layout bounds are inside the screen without scrolling,
 * on a 320 × 568 phone at 100 %, 130 % and 200 % font scale, in Turkish and English. The analytics opt-in of step 3
 * stays in the scrolling body: it is either fully on screen or the page shows the scroll cue.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
abstract class OnboardingLayoutChecks(private val labels: Labels) {
    class Labels(
        val next: String,
        val start: String,
        val levelTest: String,
        val intro: String,
        val consent: String,
        val privacy: String,
    )

    @Test
    fun `step 1 primary button is fully on screen without scrolling`() =
        assertPrimaryButtonFullyVisible(OnboardingStep.Step1, labels.next)

    @Test
    fun `step 3 primary button is fully on screen without scrolling`() =
        assertPrimaryButtonFullyVisible(OnboardingStep.Step3, labels.start)

    @Test
    fun `level choice primary button is fully on screen without scrolling`() =
        assertPrimaryButtonFullyVisible(OnboardingStep.LevelChoice, labels.levelTest)

    @Test
    fun `level test intro primary button is fully on screen without scrolling`() =
        assertPrimaryButtonFullyVisible(OnboardingStep.LevelTestIntro, labels.intro)

    @Test
    fun `step 3 consent and privacy link are on screen or the page shows the scroll cue`() = runComposeUiTest {
        setStep(OnboardingStep.Step3)

        val rootHeight = onRoot().fetchSemanticsNode().size.height
        val onScreen = listOf(hasContentDescription(labels.consent), hasText(labels.privacy)).all { matcher ->
            val node = onAllNodes(matcher).fetchSemanticsNodes().single()
            node.isWithin(rootHeight)
        }
        val cue = onAllNodes(hasTestTag(OnboardingTestTags.SCROLL_CUE)).fetchSemanticsNodes().isNotEmpty()
        assertTrue(onScreen || cue, "the opt-in is below the fold and nothing says the page scrolls")
    }

    private fun assertPrimaryButtonFullyVisible(step: OnboardingStep, label: String) = runComposeUiTest {
        setStep(step)

        val rootHeight = onRoot().fetchSemanticsNode().size.height
        val node = onAllNodes(hasText(label)).fetchSemanticsNodes().maxBy { it.positionInRoot.y }
        assertTrue(
            node.isWithin(rootHeight),
            "$label is cut off: ${node.positionInRoot.y} + ${node.size.height} > $rootHeight",
        )
    }

    private fun androidx.compose.ui.test.ComposeUiTest.setStep(step: OnboardingStep) {
        setContent {
            LexiTheme(reducedMotion = true) {
                LexiViewport { OnboardingScreen(OnboardingUiState(currentStep = step), onIntent = {}) }
            }
        }
    }

    /** The unclipped layout bounds: content scrolled out of its viewport still lies below the screen. */
    private fun SemanticsNode.isWithin(rootHeight: Int): Boolean =
        positionInRoot.y >= 0f && positionInRoot.y + size.height <= rootHeight
}

private val TR = OnboardingLayoutChecks.Labels(
    next = "Devam",
    start = "Başlayalım",
    levelTest = "SEVİYE TESTİNE BAŞLA",
    intro = "Başla",
    consent = "Anonim kullanım istatistiklerini paylaş",
    privacy = "Gizlilik politikası",
)
private val EN = OnboardingLayoutChecks.Labels(
    next = "Next",
    start = "Let's start",
    levelTest = "START THE LEVEL TEST",
    intro = "Start",
    consent = "Share anonymous usage statistics",
    privacy = "Privacy policy",
)

@Config(sdk = [36], qualifiers = "tr-w320dp-h568dp-xhdpi")
class OnboardingLayoutTr100Test : OnboardingLayoutChecks(TR)

@Config(sdk = [36], qualifiers = "tr-w320dp-h568dp-xhdpi", fontScale = 1.3f)
class OnboardingLayoutTr130Test : OnboardingLayoutChecks(TR)

@Config(sdk = [36], qualifiers = "tr-w320dp-h568dp-xhdpi", fontScale = 2.0f)
class OnboardingLayoutTr200Test : OnboardingLayoutChecks(TR)

@Config(sdk = [36], qualifiers = "en-w320dp-h568dp-xhdpi")
class OnboardingLayoutEn100Test : OnboardingLayoutChecks(EN)

@Config(sdk = [36], qualifiers = "en-w320dp-h568dp-xhdpi", fontScale = 1.3f)
class OnboardingLayoutEn130Test : OnboardingLayoutChecks(EN)

@Config(sdk = [36], qualifiers = "en-w320dp-h568dp-xhdpi", fontScale = 2.0f)
class OnboardingLayoutEn200Test : OnboardingLayoutChecks(EN)

/** On the design size nothing is below the fold: the opt-in is on screen and there is no scroll cue. */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class OnboardingScrollCueDesignSizeTest {
    @Test
    fun `step 3 on the design size has no scroll cue`() = runComposeUiTest {
        setContent {
            LexiTheme(reducedMotion = true) {
                OnboardingScreen(OnboardingUiState(currentStep = OnboardingStep.Step3), onIntent = {})
            }
        }

        assertTrue(onAllNodes(hasTestTag(OnboardingTestTags.SCROLL_CUE)).fetchSemanticsNodes().isEmpty())
    }
}

/** The cue is really there where the page overflows: step 3 on a 320 × 568 phone at 130 % font scale. */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w320dp-h568dp-xhdpi", fontScale = 1.3f)
class OnboardingScrollCueSmallPhoneTest {
    @Test
    fun `step 3 on a small phone with a large font shows the scroll cue`() = runComposeUiTest {
        setContent {
            LexiTheme(reducedMotion = true) {
                OnboardingScreen(OnboardingUiState(currentStep = OnboardingStep.Step3), onIntent = {})
            }
        }

        assertTrue(onAllNodes(hasTestTag(OnboardingTestTags.SCROLL_CUE)).fetchSemanticsNodes().isNotEmpty())
    }
}
