package dev.talhabayhan.lexi.feature.onboarding

import app.cash.turbine.test
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.platform.AppLinks
import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsEvent
import dev.talhabayhan.lexi.core.testing.FakeAnalyticsTracker
import dev.talhabayhan.lexi.core.testing.FakeCardRepository
import dev.talhabayhan.lexi.core.testing.FakeOnboardingRepository
import dev.talhabayhan.lexi.core.testing.FakeSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {
    private val analytics = FakeAnalyticsTracker()
    private val onboarding = FakeOnboardingRepository()
    private val cards = FakeCardRepository()
    private val settings = FakeSettingsRepository()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = OnboardingViewModel(onboarding, cards, settings, analytics)

    private fun levelChoice() = viewModel().apply {
        repeat(3) { onIntent(OnboardingIntent.Next) }
        assertEquals(OnboardingStep.LevelChoice, state.value.currentStep)
    }

    @Test
    fun `step navigation progresses forward and backward through steps`() = runTest {
        val vm = viewModel()
        assertEquals(OnboardingStep.Step1, vm.state.value.currentStep)

        vm.onIntent(OnboardingIntent.Next)
        assertEquals(OnboardingStep.Step2, vm.state.value.currentStep)

        vm.onIntent(OnboardingIntent.Next)
        assertEquals(OnboardingStep.Step3, vm.state.value.currentStep)

        vm.onIntent(OnboardingIntent.Next)
        assertEquals(OnboardingStep.LevelChoice, vm.state.value.currentStep)

        vm.onIntent(OnboardingIntent.Next)
        assertEquals(OnboardingStep.LevelChoice, vm.state.value.currentStep)

        vm.onIntent(OnboardingIntent.StartLevelTest)
        assertEquals(OnboardingStep.LevelTestIntro, vm.state.value.currentStep)

        vm.onIntent(OnboardingIntent.Back)
        assertEquals(OnboardingStep.LevelChoice, vm.state.value.currentStep)

        vm.onIntent(OnboardingIntent.Back)
        assertEquals(OnboardingStep.Step3, vm.state.value.currentStep)

        vm.onIntent(OnboardingIntent.Back)
        assertEquals(OnboardingStep.Step2, vm.state.value.currentStep)

        vm.onIntent(OnboardingIntent.Back)
        assertEquals(OnboardingStep.Step1, vm.state.value.currentStep)
    }

    @Test
    fun `skip from step 1 finishes with A1 and sets completed flag`() = runTest {
        val vm = viewModel()
        vm.effects.test {
            vm.onIntent(OnboardingIntent.Skip)

            assertEquals(OnboardingEffect.NavigateToHome, awaitItem())
            assertTrue(onboarding.onboardingCompleted.first())
            assertEquals(Level.A1, onboarding.selectedLevel.first())
            assertEquals(listOf<AnalyticsEvent>(AnalyticsEvent.OnboardingComplete), analytics.events)
        }
    }

    @Test
    fun `skip from step 2 finishes with A1 and sets completed flag`() = runTest {
        val vm = viewModel()
        vm.onIntent(OnboardingIntent.Next)
        assertEquals(OnboardingStep.Step2, vm.state.value.currentStep)

        vm.effects.test {
            vm.onIntent(OnboardingIntent.Skip)

            assertEquals(OnboardingEffect.NavigateToHome, awaitItem())
            assertTrue(onboarding.onboardingCompleted.first())
            assertEquals(Level.A1, onboarding.selectedLevel.first())
        }
    }

    @Test
    fun `skip from step 3 finishes with A1 and sets completed flag`() = runTest {
        val vm = viewModel()
        vm.onIntent(OnboardingIntent.Next)
        vm.onIntent(OnboardingIntent.Next)
        assertEquals(OnboardingStep.Step3, vm.state.value.currentStep)

        vm.effects.test {
            vm.onIntent(OnboardingIntent.Skip)

            assertEquals(OnboardingEffect.NavigateToHome, awaitItem())
            assertTrue(onboarding.onboardingCompleted.first())
            assertEquals(Level.A1, onboarding.selectedLevel.first())
        }
    }

    @Test
    fun `skip from level choice finishes with A1 and sets completed flag`() = runTest {
        val vm = viewModel()
        vm.onIntent(OnboardingIntent.Next)
        vm.onIntent(OnboardingIntent.Next)
        vm.onIntent(OnboardingIntent.Next)
        assertEquals(OnboardingStep.LevelChoice, vm.state.value.currentStep)

        vm.effects.test {
            vm.onIntent(OnboardingIntent.Skip)

            assertEquals(OnboardingEffect.NavigateToHome, awaitItem())
            assertTrue(onboarding.onboardingCompleted.first())
            assertEquals(Level.A1, onboarding.selectedLevel.first())
        }
    }

    @Test
    fun `skip from the level test intro finishes with A1 and sets completed flag`() = runTest {
        val vm = levelChoice()
        vm.onIntent(OnboardingIntent.StartLevelTest)

        vm.effects.test {
            vm.onIntent(OnboardingIntent.Skip)

            assertEquals(OnboardingEffect.NavigateToHome, awaitItem())
            assertTrue(onboarding.onboardingCompleted.first())
            assertEquals(Level.A1, onboarding.selectedLevel.first())
        }
    }

    @Test
    fun `a level chip saves the level and sets completed flag and navigates home`() = runTest {
        val vm = levelChoice()

        vm.effects.test {
            vm.onIntent(OnboardingIntent.PickLevel(Level.B2))

            assertEquals(OnboardingEffect.NavigateToHome, awaitItem())
            assertTrue(onboarding.onboardingCompleted.first())
            assertEquals(Level.B2, onboarding.selectedLevel.first())
            assertEquals(Level.B2, vm.state.value.selectedLevel)
            assertEquals(listOf<AnalyticsEvent>(AnalyticsEvent.OnboardingComplete), analytics.events)
        }
    }

    @Test
    fun `a second tap while finishing saves and navigates once`() = runTest {
        val vm = levelChoice()

        vm.effects.test {
            vm.onIntent(OnboardingIntent.PickLevel(Level.B2))
            vm.onIntent(OnboardingIntent.PickLevel(Level.C1))

            assertEquals(OnboardingEffect.NavigateToHome, awaitItem())
            expectNoEvents()
            assertEquals(Level.B2, onboarding.selectedLevel.first())
            assertEquals(1, analytics.events.size)
        }
    }

    @Test
    fun `the level test button opens the intro and warms up the level cards`() = runTest {
        val vm = levelChoice()

        vm.effects.test {
            vm.onIntent(OnboardingIntent.StartLevelTest)

            assertEquals(OnboardingStep.LevelTestIntro, vm.state.value.currentStep)
            assertEquals(1, cards.reads)
            expectNoEvents()
        }
        // Once per screen: back and in again reads nothing more.
        vm.onIntent(OnboardingIntent.Back)
        assertEquals(OnboardingStep.LevelChoice, vm.state.value.currentStep)
        vm.onIntent(OnboardingIntent.StartLevelTest)
        assertEquals(1, cards.reads)
        assertFalse(onboarding.onboardingCompleted.first())
    }

    @Test
    fun `a failed warm-up is ignored`() = runTest {
        cards.failing = true
        val vm = levelChoice()

        vm.onIntent(OnboardingIntent.StartLevelTest)

        assertEquals(OnboardingStep.LevelTestIntro, vm.state.value.currentStep)
        assertEquals(1, cards.reads)
    }

    @Test
    fun `begin starts the level test without saving anything`() = runTest {
        val vm = levelChoice()
        vm.onIntent(OnboardingIntent.StartLevelTest)

        vm.effects.test {
            vm.onIntent(OnboardingIntent.BeginLevelTest)

            assertEquals(OnboardingEffect.StartLevelTest, awaitItem())
            assertEquals(OnboardingStep.LevelTestIntro, vm.state.value.currentStep)
            assertFalse(onboarding.onboardingCompleted.first())
        }
    }

    @Test
    fun `begin outside the intro does nothing`() = runTest {
        val vm = levelChoice()

        vm.effects.test {
            vm.onIntent(OnboardingIntent.BeginLevelTest)

            expectNoEvents()
        }
    }

    @Test
    fun `once the level test covers the screen it returns to the level choice`() = runTest {
        val vm = levelChoice()
        vm.onIntent(OnboardingIntent.StartLevelTest)
        vm.onIntent(OnboardingIntent.BeginLevelTest)
        vm.onIntent(OnboardingIntent.LevelTestOpened)

        vm.onIntent(OnboardingIntent.ScreenStopped)

        assertEquals(OnboardingStep.LevelChoice, vm.state.value.currentStep)
    }

    @Test
    fun `a refused level test navigation keeps the intro when the screen stops later`() = runTest {
        val vm = levelChoice()
        vm.onIntent(OnboardingIntent.StartLevelTest)
        vm.onIntent(OnboardingIntent.BeginLevelTest)

        vm.onIntent(OnboardingIntent.ScreenStopped)

        assertEquals(OnboardingStep.LevelTestIntro, vm.state.value.currentStep)
    }

    @Test
    fun `going to the background on the intro keeps the intro`() = runTest {
        val vm = levelChoice()
        vm.onIntent(OnboardingIntent.StartLevelTest)

        vm.onIntent(OnboardingIntent.ScreenStopped)

        assertEquals(OnboardingStep.LevelTestIntro, vm.state.value.currentStep)
    }

    @Test
    fun `the level test button only works on the level choice`() = runTest {
        val vm = viewModel()

        vm.onIntent(OnboardingIntent.StartLevelTest)

        assertEquals(OnboardingStep.Step1, vm.state.value.currentStep)
        assertEquals(0, cards.reads)
    }

    @Test
    fun `the analytics checkbox starts unchecked and nothing is stored`() = runTest {
        val vm = viewModel()

        assertFalse(vm.state.value.analyticsConsent)
        assertFalse(settings.settings.value.analyticsConsent)
    }

    @Test
    fun `finishing without ticking the checkbox keeps analytics off`() = runTest {
        val vm = levelChoice()

        vm.onIntent(OnboardingIntent.PickLevel(Level.B1))

        assertFalse(settings.settings.value.analyticsConsent)
    }

    @Test
    fun `ticking the checkbox stores the consent and unticking withdraws it`() = runTest {
        val vm = viewModel()

        vm.onIntent(OnboardingIntent.SetAnalyticsConsent(true))
        assertTrue(vm.state.value.analyticsConsent)
        assertTrue(settings.settings.value.analyticsConsent)

        vm.onIntent(OnboardingIntent.SetAnalyticsConsent(false))
        assertFalse(vm.state.value.analyticsConsent)
        assertFalse(settings.settings.value.analyticsConsent)
    }

    @Test
    fun `the privacy link opens the policy`() = runTest {
        val vm = viewModel()

        vm.effects.test {
            vm.onIntent(OnboardingIntent.OpenPrivacy)
            assertEquals(OnboardingEffect.OpenUrl(AppLinks.PRIVACY_POLICY), awaitItem())
        }
    }

    @Test
    fun `first-run routing navigates to home when already completed`() = runTest {
        val completedRepo = FakeOnboardingRepository(initialCompleted = true)
        val vm = OnboardingViewModel(completedRepo, cards, settings, analytics)

        vm.effects.test {
            assertEquals(OnboardingEffect.NavigateToHome, awaitItem())
        }
    }

    @Test
    fun `first-run routing does not navigate away when not completed`() = runTest {
        val freshRepo = FakeOnboardingRepository(initialCompleted = false)
        val vm = OnboardingViewModel(freshRepo, cards, settings, analytics)

        assertFalse(freshRepo.onboardingCompleted.first())
        assertEquals(OnboardingStep.Step1, vm.state.value.currentStep)
    }
}
