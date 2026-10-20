package dev.talhabayhan.lexi.core.domain.reminder

import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.repository.SettingsRepository
import dev.talhabayhan.lexi.core.domain.settings.Settings
import dev.talhabayhan.lexi.core.domain.srs.CardProgress
import dev.talhabayhan.lexi.core.testing.FakeAppClock
import dev.talhabayhan.lexi.core.testing.FakeNotificationPermission
import dev.talhabayhan.lexi.core.testing.FakeProgressRepository
import dev.talhabayhan.lexi.core.testing.FakeReminderScheduler
import dev.talhabayhan.lexi.core.testing.FakeReminderTextProvider
import dev.talhabayhan.lexi.core.testing.FakeSettingsRepository
import dev.talhabayhan.lexi.core.testing.FakeStreakRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RescheduleRemindersTest {
    private val clock = FakeAppClock() // 2026-10-15 12:00 UTC
    private val today = clock.epochDay(clock.nowMs())
    private val settings = FakeSettingsRepository(Settings(dailyReminder = true))
    private val streaks = FakeStreakRepository(listOf(today - 1))
    private val progress = FakeProgressRepository()
    private val permission = FakeNotificationPermission(current = PermissionStatus.GRANTED)
    private val scheduler = FakeReminderScheduler()
    private val reschedule = RescheduleReminders(
        settings = settings,
        streaks = streaks,
        progress = progress,
        permission = permission,
        scheduler = scheduler,
        texts = FakeReminderTextProvider(),
        clock = clock,
    )

    @Test
    fun `enabled with permission schedules the plan with resolved texts and plan-index ids`() = runTest {
        reschedule()

        val first = scheduler.scheduled.first()
        assertEquals(ScheduledReminder(7_100, today, 20 * 60, "notif_daily_3_title", "notif_daily_3_body"), first)
        assertEquals(
            List(scheduler.scheduled.size) { ScheduledReminder.FIRST_ID + it },
            scheduler.scheduled.map { it.id },
        )
        assertEquals(listOf(0L, 1L, 2L, 6L, 13L), scheduler.scheduled.map { it.epochDay - today })
    }

    @Test
    fun `disabled reminder cancels everything`() = runTest {
        reschedule()
        settings.update { it.copy(dailyReminder = false) }

        reschedule()

        assertEquals(emptyList(), scheduler.scheduled)
        assertEquals(1, scheduler.cancelCalls)
    }

    @Test
    fun `enabled without granted permission schedules nothing`() = runTest {
        for (status in listOf(PermissionStatus.CAN_ASK, PermissionStatus.BLOCKED)) {
            permission.current = status

            reschedule()

            assertEquals(emptyList(), scheduler.scheduled)
            assertEquals(0, scheduler.replaceCalls)
        }
        assertEquals(0, permission.requestCalls)
    }

    @Test
    fun `revoking the permission after scheduling cancels the scheduled reminders`() = runTest {
        reschedule()
        assertTrue(scheduler.scheduled.isNotEmpty())

        permission.current = PermissionStatus.BLOCKED
        reschedule()

        assertEquals(emptyList(), scheduler.scheduled)
        assertEquals(1, scheduler.cancelCalls)
    }

    @Test
    fun `uses the local minute of now to skip a passed reminder`() = runTest {
        clock.currentMs = today * FakeAppClock.MS_PER_DAY + (20 * 60 + 5) * FakeAppClock.MS_PER_MINUTE

        reschedule()

        assertEquals(today + 1, scheduler.scheduled.first().epochDay)
    }

    @Test
    fun `due notebook cards drive the review variant`() = runTest {
        progress.save(
            List(5) { CardProgress(CardId("c$it"), box = 1, dueEpochDay = today - 1) } +
                CardProgress(CardId("mastered"), box = 4, dueEpochDay = today - 1, mastered = true) +
                CardProgress(CardId("removed"), box = null, dueEpochDay = null),
        )

        reschedule()

        assertEquals("notif_review_1_body:5", scheduler.scheduled.first().body)
    }

    @Test
    fun `review variants are left out when the review setting is off`() = runTest {
        progress.save(List(12) { CardProgress(CardId("c$it"), box = 1, dueEpochDay = today) })
        settings.update { it.copy(reviewReminders = false) }

        reschedule()

        assertTrue(scheduler.scheduled.none { it.title.startsWith("notif_review") })
    }

    @Test
    fun `run reschedules once after changes settle`() = runTest {
        startRun()
        advanceTimeBy(RescheduleReminders.DEFAULT_DEBOUNCE_MS + 1)
        assertEquals(1, scheduler.replaceCalls)

        settings.update { it.copy(reminderMinuteOfDay = 21 * 60) }
        advanceTimeBy(100)
        settings.update { it.copy(reminderMinuteOfDay = 21 * 60 + 30) }
        advanceTimeBy(RescheduleReminders.DEFAULT_DEBOUNCE_MS + 1)

        assertEquals(2, scheduler.replaceCalls)
        assertTrue(scheduler.scheduled.all { it.minuteOfDay == 21 * 60 + 30 })
    }

    @Test
    fun `run follows a play day and ignores unrelated settings`() = runTest {
        startRun()
        advanceTimeBy(RescheduleReminders.DEFAULT_DEBOUNCE_MS + 1)

        settings.update { it.copy(soundEffects = false) }
        advanceTimeBy(RescheduleReminders.DEFAULT_DEBOUNCE_MS + 1)
        assertEquals(1, scheduler.replaceCalls)

        streaks.days = listOf(today - 1, today)
        advanceTimeBy(RescheduleReminders.DEFAULT_DEBOUNCE_MS + 1)
        assertEquals(2, scheduler.replaceCalls)
        assertEquals(today + 1, scheduler.scheduled.first().epochDay)
    }

    @Test
    fun `run cancels when the reminder is turned off`() = runTest {
        startRun()
        advanceTimeBy(RescheduleReminders.DEFAULT_DEBOUNCE_MS + 1)

        settings.update { it.copy(dailyReminder = false) }
        advanceTimeBy(RescheduleReminders.DEFAULT_DEBOUNCE_MS + 1)

        assertEquals(emptyList(), scheduler.scheduled)
        assertEquals(1, scheduler.cancelCalls)
    }

    @Test
    fun `a failing scheduler does not throw and the next call schedules again`() = runTest {
        scheduler.failure = IllegalStateException("exact alarms revoked")

        reschedule()

        assertEquals(emptyList(), scheduler.scheduled)
        scheduler.failure = null
        reschedule()
        assertTrue(scheduler.scheduled.isNotEmpty())
    }

    @Test
    fun `run survives a failing scheduler and retries the same plan on the next change`() = runTest {
        scheduler.failure = IllegalStateException("binder died")
        startRun()
        advanceTimeBy(RescheduleReminders.DEFAULT_DEBOUNCE_MS + 1)
        assertEquals(1, scheduler.replaceCalls)
        assertEquals(emptyList(), scheduler.scheduled)

        scheduler.failure = null
        // Same plan (an unrelated play day of the past): still scheduled, the failed attempt didn't count.
        streaks.days = listOf(today - 30, today - 1)
        advanceTimeBy(RescheduleReminders.DEFAULT_DEBOUNCE_MS + 1)

        assertEquals(2, scheduler.replaceCalls)
        assertTrue(scheduler.scheduled.isNotEmpty())
    }

    @Test
    fun `a failing settings flow does not throw from invoke`() = runTest {
        val failing = rescheduleWith(ThrowingSettingsRepository(settings, failures = Int.MAX_VALUE))

        failing()

        assertEquals(0, scheduler.replaceCalls)
    }

    @Test
    fun `run re-subscribes after the settings flow fails`() = runTest {
        val failing = rescheduleWith(ThrowingSettingsRepository(settings, failures = 2))
        backgroundScope.launch { failing.run(retryDelayMs = 1_000) }
        runCurrent()
        advanceTimeBy(RescheduleReminders.DEFAULT_DEBOUNCE_MS + 1)
        assertEquals(0, scheduler.replaceCalls)

        // Retries after 1 s and 2 s; the third subscription delivers.
        advanceTimeBy(3_000 + RescheduleReminders.DEFAULT_DEBOUNCE_MS + 1)
        assertEquals(1, scheduler.replaceCalls)

        settings.update { it.copy(reminderMinuteOfDay = 21 * 60) }
        advanceTimeBy(RescheduleReminders.DEFAULT_DEBOUNCE_MS + 1)
        assertEquals(2, scheduler.replaceCalls)
        assertTrue(scheduler.scheduled.all { it.minuteOfDay == 21 * 60 })
    }

    private fun rescheduleWith(settingsRepository: SettingsRepository) = RescheduleReminders(
        settings = settingsRepository,
        streaks = streaks,
        progress = progress,
        permission = permission,
        scheduler = scheduler,
        texts = FakeReminderTextProvider(),
        clock = clock,
    )

    private fun TestScope.startRun() {
        backgroundScope.launch { reschedule.run() }
        runCurrent()
    }

    /** The first [failures] subscriptions of [settings] fail like a DataStore read error, later ones delegate. */
    private class ThrowingSettingsRepository(private val delegate: FakeSettingsRepository, private var failures: Int) :
        SettingsRepository by delegate {
        override val settings: Flow<Settings> = flow {
            if (failures > 0) {
                failures--
                throw IllegalStateException("DataStore read failed")
            }
            emitAll(delegate.settings)
        }
    }
}
