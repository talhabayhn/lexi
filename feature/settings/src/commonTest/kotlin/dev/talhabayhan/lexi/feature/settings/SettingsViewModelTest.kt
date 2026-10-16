package dev.talhabayhan.lexi.feature.settings

import app.cash.turbine.test
import dev.talhabayhan.lexi.core.domain.settings.Settings
import dev.talhabayhan.lexi.core.testing.FakeSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class SettingsViewModelTest {
    private val repository = FakeSettingsRepository()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = SettingsViewModel(repository)

    @Test
    fun `the switches show the stored values`() = runTest {
        repository.update { it.copy(soundEffects = false, speakOnCorrect = false) }

        val state = viewModel().state.value

        assertFalse(state.soundEffects)
        assertTrue(state.haptics)
        assertFalse(state.speakOnCorrect)
    }

    @Test
    fun `each switch is written to the settings and only that one`() = runTest {
        val viewModel = viewModel()

        viewModel.onIntent(SettingsIntent.SetSoundEffects(false))
        assertEquals(Settings(soundEffects = false), repository.settings.value)
        assertFalse(viewModel.state.value.soundEffects)

        viewModel.onIntent(SettingsIntent.SetHaptics(false))
        assertEquals(Settings(soundEffects = false, haptics = false), repository.settings.value)
        assertFalse(viewModel.state.value.haptics)

        viewModel.onIntent(SettingsIntent.SetSpeakOnCorrect(false))
        assertEquals(Settings(soundEffects = false, haptics = false, speakOnCorrect = false), repository.settings.value)
        assertFalse(viewModel.state.value.speakOnCorrect)

        viewModel.onIntent(SettingsIntent.SetHaptics(true))
        assertTrue(repository.settings.value.haptics)
        assertTrue(viewModel.state.value.haptics)
    }

    @Test
    fun `back asks to navigate back`() = runTest {
        val viewModel = viewModel()
        viewModel.effects.test {
            viewModel.onIntent(SettingsIntent.Back)

            assertEquals(SettingsEffect.NavigateBack, awaitItem())
        }
    }
}
