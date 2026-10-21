package dev.talhabayhan.lexi.feature.settings.reminder

import app.cash.turbine.test
import dev.talhabayhan.lexi.core.domain.reminder.AnswerReminderPrePrompt
import dev.talhabayhan.lexi.core.domain.reminder.PermissionStatus
import dev.talhabayhan.lexi.core.domain.reminder.ReminderTimes
import dev.talhabayhan.lexi.core.domain.reminder.RescheduleReminders
import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsEvent
import dev.talhabayhan.lexi.core.testing.FakeAnalyticsTracker
import dev.talhabayhan.lexi.core.testing.FakeAppClock
import dev.talhabayhan.lexi.core.testing.FakeNotificationPermission
import dev.talhabayhan.lexi.core.testing.FakeProgressRepository
import dev.talhabayhan.lexi.core.testing.FakeReminderScheduler
import dev.talhabayhan.lexi.core.testing.FakeReminderTextProvider
import dev.talhabayhan.lexi.core.testing.FakeSettingsRepository
import dev.talhabayhan.lexi.core.testing.FakeStreakRepository
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
class ReminderPrePromptViewModelTest {
    private val analytics = FakeAnalyticsTracker()
    private val settings = FakeSettingsRepository()
    private val permission = FakeNotificationPermission(PermissionStatus.CAN_ASK)
    private val scheduler = FakeReminderScheduler()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(): ReminderPrePromptViewModel {
        val reschedule = RescheduleReminders(
            settings = settings,
            streaks = FakeStreakRepository(),
            progress = FakeProgressRepository(),
            permission = permission,
            scheduler = scheduler,
            texts = FakeReminderTextProvider(),
            clock = FakeAppClock(),
        )
        return ReminderPrePromptViewModel(
            settings,
            AnswerReminderPrePrompt(settings, permission, reschedule),
            analytics,
        )
    }

    @Test
    fun `showing the pre-prompt counts as asked even when it is left without an answer`() = runTest {
        viewModel()

        assertTrue(settings.settings.value.reminderPrePromptShown)
        assertFalse(settings.settings.value.dailyReminder)
    }

    @Test
    fun `yes with the permission granted turns the reminder on at 20 00 and closes`() = runTest {
        settings.update { it.copy(reminderMinuteOfDay = 9 * 60) }
        val viewModel = viewModel()
        viewModel.effects.test {
            viewModel.onIntent(ReminderPrePromptIntent.Accept)

            assertEquals(ReminderPrePromptEffect.Done, awaitItem())
        }
        assertEquals(1, permission.requestCalls)
        assertTrue(settings.settings.value.dailyReminder)
        assertEquals(ReminderTimes.DEFAULT_MINUTE, settings.settings.value.reminderMinuteOfDay)
        assertEquals(listOf<AnalyticsEvent>(AnalyticsEvent.ReminderEnabled), analytics.events)
    }

    @Test
    fun `yes with the permission denied keeps the reminder off and closes`() = runTest {
        permission.answer = PermissionStatus.BLOCKED
        val viewModel = viewModel()
        viewModel.effects.test {
            viewModel.onIntent(ReminderPrePromptIntent.Accept)

            assertEquals(ReminderPrePromptEffect.Done, awaitItem())
        }
        assertEquals(1, permission.requestCalls)
        assertFalse(settings.settings.value.dailyReminder)
        assertTrue(analytics.events.isEmpty())
    }

    @Test
    fun `not now never asks the OS and keeps the reminder off`() = runTest {
        val viewModel = viewModel()
        viewModel.effects.test {
            viewModel.onIntent(ReminderPrePromptIntent.Decline)

            assertEquals(ReminderPrePromptEffect.Done, awaitItem())
        }
        assertEquals(0, permission.requestCalls)
        assertFalse(settings.settings.value.dailyReminder)
        assertTrue(settings.settings.value.reminderPrePromptShown)
    }

    @Test
    fun `a second tap while an answer is handled is ignored`() = runTest {
        val viewModel = viewModel()
        viewModel.effects.test {
            viewModel.onIntent(ReminderPrePromptIntent.Accept)
            viewModel.onIntent(ReminderPrePromptIntent.Decline)

            assertEquals(ReminderPrePromptEffect.Done, awaitItem())
            expectNoEvents()
        }
        assertEquals(1, permission.requestCalls)
        assertTrue(settings.settings.value.dailyReminder)
    }
}
