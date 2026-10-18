package dev.talhabayhan.lexi.feature.onboarding

import app.cash.turbine.test
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.testing.FakeOnboardingRepository
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
    private val onboarding = FakeOnboardingRepository()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = OnboardingViewModel(onboarding)

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

        vm.onIntent(OnboardingIntent.OpenLevelPicker)
        assertEquals(OnboardingStep.LevelPicker, vm.state.value.currentStep)

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
    fun `skip from level picker finishes with A1 and sets completed flag`() = runTest {
        val vm = viewModel()
        vm.onIntent(OnboardingIntent.Next)
        vm.onIntent(OnboardingIntent.Next)
        vm.onIntent(OnboardingIntent.Next)
        vm.onIntent(OnboardingIntent.OpenLevelPicker)
        assertEquals(OnboardingStep.LevelPicker, vm.state.value.currentStep)

        vm.effects.test {
            vm.onIntent(OnboardingIntent.Skip)

            assertEquals(OnboardingEffect.NavigateToHome, awaitItem())
            assertTrue(onboarding.onboardingCompleted.first())
            assertEquals(Level.A1, onboarding.selectedLevel.first())
        }
    }

    @Test
    fun `manual pick saves the level and sets completed flag and navigates home`() = runTest {
        val vm = viewModel()
        vm.onIntent(OnboardingIntent.Next)
        vm.onIntent(OnboardingIntent.Next)
        vm.onIntent(OnboardingIntent.Next)
        vm.onIntent(OnboardingIntent.OpenLevelPicker)

        vm.effects.test {
            vm.onIntent(OnboardingIntent.PickLevel(Level.B2))

            assertEquals(OnboardingEffect.NavigateToHome, awaitItem())
            assertTrue(onboarding.onboardingCompleted.first())
            assertEquals(Level.B2, onboarding.selectedLevel.first())
            assertEquals(Level.B2, vm.state.value.selectedLevel)
        }
    }

    @Test
    fun `first-run routing navigates to home when already completed`() = runTest {
        val completedRepo = FakeOnboardingRepository(initialCompleted = true)
        val vm = OnboardingViewModel(completedRepo)

        vm.effects.test {
            assertEquals(OnboardingEffect.NavigateToHome, awaitItem())
        }
    }

    @Test
    fun `first-run routing does not navigate away when not completed`() = runTest {
        val freshRepo = FakeOnboardingRepository(initialCompleted = false)
        val vm = OnboardingViewModel(freshRepo)

        assertFalse(freshRepo.onboardingCompleted.first())
        assertEquals(OnboardingStep.Step1, vm.state.value.currentStep)
    }
}
