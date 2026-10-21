package dev.talhabayhan.lexi.feature.settings

import app.cash.turbine.test
import dev.talhabayhan.lexi.core.domain.platform.AppLinks
import dev.talhabayhan.lexi.core.domain.reminder.PermissionStatus
import dev.talhabayhan.lexi.core.domain.reminder.ReminderTimes
import dev.talhabayhan.lexi.core.domain.settings.AppLanguage
import dev.talhabayhan.lexi.core.domain.settings.Settings
import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsEvent
import dev.talhabayhan.lexi.core.testing.FakeAnalyticsTracker
import dev.talhabayhan.lexi.core.testing.FakeCrashReporter
import dev.talhabayhan.lexi.core.testing.FakeNotificationPermission
import dev.talhabayhan.lexi.core.testing.FakeSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
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
    private val analytics = FakeAnalyticsTracker()
    private val crashReporter = FakeCrashReporter()
    private val repository = FakeSettingsRepository()
    private val permission = FakeNotificationPermission(PermissionStatus.GRANTED)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = SettingsViewModel(repository, permission, analytics, crashReporter)

    @Test
    fun `the first state is the stored settings when the store is already loaded`() = runTest {
        repository.update { it.copy(soundEffects = false, dailyGoalWheels = 5, classicTimeLimitSeconds = 240) }
        // The collector is never started: the very first state must already hold the stored values.
        Dispatchers.setMain(StandardTestDispatcher())

        viewModel().state.test {
            val first = awaitItem()
            assertFalse(first.soundEffects)
            assertEquals(5, first.dailyGoalWheels)
            assertEquals(240, first.classicTimeLimitSeconds)
            cancelAndIgnoreRemainingEvents()
        }
    }

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

    @Test
    fun `the reminder state follows the store`() = runTest {
        repository.update { it.copy(dailyReminder = true, reminderMinuteOfDay = 9 * 60 + 30, reviewReminders = false) }

        val state = viewModel().state.value

        assertTrue(state.dailyReminder)
        assertEquals(9 * 60 + 30, state.reminderMinuteOfDay)
        assertFalse(state.reviewReminders)
    }

    @Test
    fun `review reminders switch writes the setting`() = runTest {
        val viewModel = viewModel()

        viewModel.onIntent(SettingsIntent.SetReviewReminders(false))

        assertFalse(repository.settings.value.reviewReminders)
        assertFalse(viewModel.state.value.reviewReminders)
        viewModel.onIntent(SettingsIntent.SetReviewReminders(true))
        assertTrue(repository.settings.value.reviewReminders)
    }

    @Test
    fun `the reminder sheet opens and closes`() = runTest {
        val viewModel = viewModel()

        viewModel.onIntent(SettingsIntent.OpenReminderSheet)
        assertTrue(viewModel.state.value.reminderSheetVisible)

        viewModel.onIntent(SettingsIntent.CloseReminderSheet)
        assertFalse(viewModel.state.value.reminderSheetVisible)
    }

    @Test
    fun `opening the sheet reads the permission and a blocked one shows the hint state`() = runTest {
        permission.current = PermissionStatus.BLOCKED
        val viewModel = viewModel()

        viewModel.onIntent(SettingsIntent.OpenReminderSheet)

        assertTrue(viewModel.state.value.notificationsBlocked)
    }

    @Test
    fun `refreshing the permission follows the system settings`() = runTest {
        permission.current = PermissionStatus.BLOCKED
        val viewModel = viewModel()
        viewModel.onIntent(SettingsIntent.RefreshNotificationPermission)
        assertTrue(viewModel.state.value.notificationsBlocked)

        permission.current = PermissionStatus.GRANTED
        viewModel.onIntent(SettingsIntent.RefreshNotificationPermission)

        assertFalse(viewModel.state.value.notificationsBlocked)
    }

    @Test
    fun `turning the reminder on with the permission granted enables it`() = runTest {
        val viewModel = viewModel()

        viewModel.onIntent(SettingsIntent.SetReminderEnabled(true))

        assertTrue(repository.settings.value.dailyReminder)
        assertEquals(ReminderTimes.DEFAULT_MINUTE, repository.settings.value.reminderMinuteOfDay)
        assertEquals(0, permission.requestCalls)
        assertEquals(listOf<AnalyticsEvent>(AnalyticsEvent.ReminderEnabled), analytics.events)
    }

    @Test
    fun `turning the reminder on while the OS can ask shows the pre-prompt first and stays off`() = runTest {
        permission.current = PermissionStatus.CAN_ASK
        val viewModel = viewModel()
        viewModel.effects.test {
            viewModel.onIntent(SettingsIntent.SetReminderEnabled(true))

            assertEquals(SettingsEffect.NavigateToReminderPrePrompt, awaitItem())
        }
        assertFalse(repository.settings.value.dailyReminder)
        assertEquals(0, permission.requestCalls)
        assertTrue(analytics.events.isEmpty())
    }

    @Test
    fun `turning the reminder on while blocked stays off and shows the hint`() = runTest {
        permission.current = PermissionStatus.BLOCKED
        val viewModel = viewModel()
        viewModel.effects.test {
            viewModel.onIntent(SettingsIntent.SetReminderEnabled(true))

            expectNoEvents()
        }
        assertFalse(repository.settings.value.dailyReminder)
        assertTrue(viewModel.state.value.notificationsBlocked)
        assertTrue(analytics.events.isEmpty())
    }

    @Test
    fun `turning the reminder off writes the setting without asking the OS`() = runTest {
        repository.update { it.copy(dailyReminder = true) }
        permission.current = PermissionStatus.BLOCKED
        val viewModel = viewModel()

        viewModel.onIntent(SettingsIntent.SetReminderEnabled(false))

        assertFalse(repository.settings.value.dailyReminder)
    }

    @Test
    fun `picking a time writes it and a time outside the list is ignored`() = runTest {
        val viewModel = viewModel()

        viewModel.onIntent(SettingsIntent.SetReminderTime(9 * 60))
        assertEquals(9 * 60, repository.settings.value.reminderMinuteOfDay)
        assertEquals(9 * 60, viewModel.state.value.reminderMinuteOfDay)

        viewModel.onIntent(SettingsIntent.SetReminderTime(22 * 60))
        viewModel.onIntent(SettingsIntent.SetReminderTime(9 * 60 + 10))
        assertEquals(9 * 60, repository.settings.value.reminderMinuteOfDay)
    }

    @Test
    fun `open notification settings goes to the system settings`() = runTest {
        val viewModel = viewModel()

        viewModel.onIntent(SettingsIntent.OpenNotificationSettings)

        assertEquals(1, permission.settingsOpened)
    }

    @Test
    fun `a stored change keeps the sheet open`() = runTest {
        val viewModel = viewModel()
        viewModel.onIntent(SettingsIntent.OpenReminderSheet)

        repository.update { it.copy(soundEffects = false) }

        assertTrue(viewModel.state.value.reminderSheetVisible)
        assertFalse(viewModel.state.value.soundEffects)
    }
}
