package dev.talhabayhan.lexi.feature.onboarding.result

import androidx.lifecycle.SavedStateHandle
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LevelTestResultViewModelTest {
    private val onboarding = FakeOnboardingRepository()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(level: String = "B1") = LevelTestResultViewModel(LevelTestResultRoute(level), onboarding)

    @Test
    fun `shows the placement of the route on the result page`() {
        val state = viewModel("C1").state.value

        assertEquals(Level.C1, state.level)
        assertEquals(LevelTestResultPage.Result, state.page)
    }

    @Test
    fun `the picker page survives process death through the saved state`() {
        val handle = SavedStateHandle()
        val first = LevelTestResultViewModel(LevelTestResultRoute("B1"), onboarding, handle)
        first.onIntent(LevelTestResultIntent.ChangeLevel)

        val restored = LevelTestResultViewModel(LevelTestResultRoute("B1"), onboarding, handle)

        assertEquals(LevelTestResultPage.Picker, restored.state.value.page)
    }

    @Test
    fun `going back from the picker is saved too`() {
        val handle = SavedStateHandle()
        val first = LevelTestResultViewModel(LevelTestResultRoute("B1"), onboarding, handle)
        first.onIntent(LevelTestResultIntent.ChangeLevel)
        first.onIntent(LevelTestResultIntent.Back)

        val restored = LevelTestResultViewModel(LevelTestResultRoute("B1"), onboarding, handle)

        assertEquals(LevelTestResultPage.Result, restored.state.value.page)
    }

    @Test
    fun `an unknown level name falls back to A1`() {
        assertEquals(Level.A1, viewModel("XX").state.value.level)
    }

    @Test
    fun `start saves the placement and completes the onboarding and goes home`() = runTest {
        val vm = viewModel("B1")

        vm.effects.test {
            vm.onIntent(LevelTestResultIntent.Start)

            assertEquals(LevelTestResultEffect.NavigateToHome, awaitItem())
            assertEquals(Level.B1, onboarding.selectedLevel.first())
            assertTrue(onboarding.onboardingCompleted.first())
        }
    }

    @Test
    fun `a second start is ignored`() = runTest {
        val vm = viewModel("B1")

        vm.effects.test {
            vm.onIntent(LevelTestResultIntent.Start)
            vm.onIntent(LevelTestResultIntent.Start)
            vm.onIntent(LevelTestResultIntent.Back)

            assertEquals(LevelTestResultEffect.NavigateToHome, awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `change level opens the picker and back returns to the result without saving`() = runTest {
        val vm = viewModel("B1")

        vm.effects.test {
            vm.onIntent(LevelTestResultIntent.ChangeLevel)
            assertEquals(LevelTestResultPage.Picker, vm.state.value.page)

            vm.onIntent(LevelTestResultIntent.Back)
            assertEquals(LevelTestResultPage.Result, vm.state.value.page)
            expectNoEvents()
        }
        assertFalse(onboarding.onboardingCompleted.first())
    }

    @Test
    fun `a picked level is saved instead of the placement`() = runTest {
        val vm = viewModel("B1")
        vm.onIntent(LevelTestResultIntent.ChangeLevel)

        vm.effects.test {
            vm.onIntent(LevelTestResultIntent.PickLevel(Level.A2))

            assertEquals(LevelTestResultEffect.NavigateToHome, awaitItem())
            assertEquals(Level.A2, onboarding.selectedLevel.first())
            assertTrue(onboarding.onboardingCompleted.first())
        }
    }

    @Test
    fun `pick and start only work on their own page`() = runTest {
        val vm = viewModel("B1")

        vm.effects.test {
            vm.onIntent(LevelTestResultIntent.PickLevel(Level.A2))
            expectNoEvents()

            vm.onIntent(LevelTestResultIntent.ChangeLevel)
            vm.onIntent(LevelTestResultIntent.Start)
            expectNoEvents()
        }
        assertFalse(onboarding.onboardingCompleted.first())
    }

    @Test
    fun `back on the result returns to the level choice without saving`() = runTest {
        val vm = viewModel("B1")

        vm.effects.test {
            vm.onIntent(LevelTestResultIntent.Back)

            assertEquals(LevelTestResultEffect.NavigateBack, awaitItem())
        }
        assertFalse(onboarding.onboardingCompleted.first())
    }

    @Test
    fun `flavour line only for A1 and for C1 and NAT`() {
        val flavours = Level.entries.associateWith { LevelTestResultUiState(level = it).flavour }

        assertEquals(LevelTestFlavour.Basics, flavours[Level.A1])
        assertNull(flavours[Level.A2])
        assertNull(flavours[Level.B1])
        assertNull(flavours[Level.B2])
        assertEquals(LevelTestFlavour.High, flavours[Level.C1])
        assertEquals(LevelTestFlavour.High, flavours[Level.NAT])
    }
}
