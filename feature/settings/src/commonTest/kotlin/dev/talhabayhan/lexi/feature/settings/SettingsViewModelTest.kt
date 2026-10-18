package dev.talhabayhan.lexi.feature.settings

import app.cash.turbine.test
import dev.talhabayhan.lexi.core.domain.platform.AppLinks
import dev.talhabayhan.lexi.core.domain.settings.AppLanguage
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
    fun `the settings show the stored values`() = runTest {
        repository.update {
            it.copy(
                soundEffects = false,
                speakOnCorrect = false,
                classicTimeLimitSeconds = 240,
                dailyGoalWheels = 3,
                appLanguage = AppLanguage.TR,
                accent = dev.talhabayhan.lexi.core.domain.settings.Accent.UK,
            )
        }

        val state = viewModel().state.value

        assertFalse(state.soundEffects)
        assertTrue(state.haptics)
        assertFalse(state.speakOnCorrect)
        assertEquals(240, state.classicTimeLimitSeconds)
        assertEquals(3, state.dailyGoalWheels)
        assertEquals(AppLanguage.TR, state.appLanguage)
        assertEquals(dev.talhabayhan.lexi.core.domain.settings.Accent.UK, state.accent)
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
    fun `cycle classic time cycles through allowed options`() = runTest {
        val viewModel = viewModel()
        assertEquals(180, viewModel.state.value.classicTimeLimitSeconds)

        viewModel.onIntent(SettingsIntent.CycleClassicTime)
        assertEquals(240, viewModel.state.value.classicTimeLimitSeconds)
        assertEquals(240, repository.settings.value.classicTimeLimitSeconds)

        viewModel.onIntent(SettingsIntent.CycleClassicTime)
        assertEquals(300, viewModel.state.value.classicTimeLimitSeconds)
        assertEquals(300, repository.settings.value.classicTimeLimitSeconds)

        viewModel.onIntent(SettingsIntent.CycleClassicTime)
        assertEquals(120, viewModel.state.value.classicTimeLimitSeconds)
        assertEquals(120, repository.settings.value.classicTimeLimitSeconds)

        viewModel.onIntent(SettingsIntent.CycleClassicTime)
        assertEquals(180, viewModel.state.value.classicTimeLimitSeconds)
        assertEquals(180, repository.settings.value.classicTimeLimitSeconds)
    }

    @Test
    fun `cycle daily goal cycles through allowed options`() = runTest {
        val viewModel = viewModel()
        assertEquals(1, viewModel.state.value.dailyGoalWheels)

        viewModel.onIntent(SettingsIntent.CycleDailyGoal)
        assertEquals(2, viewModel.state.value.dailyGoalWheels)
        assertEquals(2, repository.settings.value.dailyGoalWheels)

        viewModel.onIntent(SettingsIntent.CycleDailyGoal)
        assertEquals(3, viewModel.state.value.dailyGoalWheels)
        assertEquals(3, repository.settings.value.dailyGoalWheels)

        viewModel.onIntent(SettingsIntent.CycleDailyGoal)
        assertEquals(5, viewModel.state.value.dailyGoalWheels)
        assertEquals(5, repository.settings.value.dailyGoalWheels)

        viewModel.onIntent(SettingsIntent.CycleDailyGoal)
        assertEquals(1, viewModel.state.value.dailyGoalWheels)
        assertEquals(1, repository.settings.value.dailyGoalWheels)
    }

    @Test
    fun `cycle accent toggles between US and UK`() = runTest {
        val viewModel = viewModel()
        assertEquals(dev.talhabayhan.lexi.core.domain.settings.Accent.US, viewModel.state.value.accent)

        viewModel.onIntent(SettingsIntent.CycleAccent)
        assertEquals(dev.talhabayhan.lexi.core.domain.settings.Accent.UK, viewModel.state.value.accent)
        assertEquals(dev.talhabayhan.lexi.core.domain.settings.Accent.UK, repository.settings.value.accent)

        viewModel.onIntent(SettingsIntent.CycleAccent)
        assertEquals(dev.talhabayhan.lexi.core.domain.settings.Accent.US, viewModel.state.value.accent)
        assertEquals(dev.talhabayhan.lexi.core.domain.settings.Accent.US, repository.settings.value.accent)
    }

    @Test
    fun `language defaults to SYSTEM`() = runTest {
        val viewModel = viewModel()
        assertEquals(AppLanguage.SYSTEM, viewModel.state.value.appLanguage)
        assertEquals(AppLanguage.SYSTEM, repository.settings.value.appLanguage)
    }

    @Test
    fun `cycle app language cycles through SYSTEM TR EN and back to SYSTEM`() = runTest {
        val viewModel = viewModel()
        assertEquals(AppLanguage.SYSTEM, viewModel.state.value.appLanguage)

        viewModel.onIntent(SettingsIntent.CycleAppLanguage)
        assertEquals(AppLanguage.TR, viewModel.state.value.appLanguage)
        assertEquals(AppLanguage.TR, repository.settings.value.appLanguage)

        viewModel.onIntent(SettingsIntent.CycleAppLanguage)
        assertEquals(AppLanguage.EN, viewModel.state.value.appLanguage)
        assertEquals(AppLanguage.EN, repository.settings.value.appLanguage)

        viewModel.onIntent(SettingsIntent.CycleAppLanguage)
        assertEquals(AppLanguage.SYSTEM, viewModel.state.value.appLanguage)
        assertEquals(AppLanguage.SYSTEM, repository.settings.value.appLanguage)
    }

    @Test
    fun `set app language persists and updates state`() = runTest {
        val viewModel = viewModel()

        viewModel.onIntent(SettingsIntent.SetAppLanguage(AppLanguage.TR))
        assertEquals(AppLanguage.TR, repository.settings.value.appLanguage)
        assertEquals(AppLanguage.TR, viewModel.state.value.appLanguage)

        viewModel.onIntent(SettingsIntent.SetAppLanguage(AppLanguage.EN))
        assertEquals(AppLanguage.EN, repository.settings.value.appLanguage)
        assertEquals(AppLanguage.EN, viewModel.state.value.appLanguage)
    }

    @Test
    fun `explicit setters write to settings and update state`() = runTest {
        val viewModel = viewModel()

        viewModel.onIntent(SettingsIntent.SetClassicTimeLimit(300))
        assertEquals(300, repository.settings.value.classicTimeLimitSeconds)
        assertEquals(300, viewModel.state.value.classicTimeLimitSeconds)

        viewModel.onIntent(SettingsIntent.SetDailyGoalWheels(5))
        assertEquals(5, repository.settings.value.dailyGoalWheels)
        assertEquals(5, viewModel.state.value.dailyGoalWheels)

        viewModel.onIntent(SettingsIntent.SetAppLanguage(AppLanguage.EN))
        assertEquals(AppLanguage.EN, repository.settings.value.appLanguage)
        assertEquals(AppLanguage.EN, viewModel.state.value.appLanguage)

        viewModel.onIntent(SettingsIntent.SetAccent(dev.talhabayhan.lexi.core.domain.settings.Accent.UK))
        assertEquals(dev.talhabayhan.lexi.core.domain.settings.Accent.UK, repository.settings.value.accent)
        assertEquals(dev.talhabayhan.lexi.core.domain.settings.Accent.UK, viewModel.state.value.accent)
    }

    @Test
    fun `back asks to navigate back`() = runTest {
        val viewModel = viewModel()
        viewModel.effects.test {
            viewModel.onIntent(SettingsIntent.Back)

            assertEquals(SettingsEffect.NavigateBack, awaitItem())
        }
    }

    @Test
    fun `open about asks to navigate to about`() = runTest {
        val viewModel = viewModel()
        viewModel.effects.test {
            viewModel.onIntent(SettingsIntent.OpenAbout)

            assertEquals(SettingsEffect.NavigateToAbout, awaitItem())
        }
    }

    @Test
    fun `open privacy emits open url effect with privacy link`() = runTest {
        val viewModel = viewModel()
        viewModel.effects.test {
            viewModel.onIntent(SettingsIntent.OpenPrivacy)

            assertEquals(SettingsEffect.OpenUrl(AppLinks.PRIVACY_POLICY), awaitItem())
        }
    }

    @Test
    fun `open terms emits open url effect with terms link`() = runTest {
        val viewModel = viewModel()
        viewModel.effects.test {
            viewModel.onIntent(SettingsIntent.OpenTerms)

            assertEquals(SettingsEffect.OpenUrl(AppLinks.TERMS_OF_SERVICE), awaitItem())
        }
    }
}
