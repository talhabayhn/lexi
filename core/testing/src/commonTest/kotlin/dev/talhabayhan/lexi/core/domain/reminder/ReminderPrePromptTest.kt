package dev.talhabayhan.lexi.core.domain.reminder

import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.session.SessionResult
import dev.talhabayhan.lexi.core.domain.settings.Settings
import dev.talhabayhan.lexi.core.domain.stats.SessionRecord
import dev.talhabayhan.lexi.core.testing.FakeAppClock
import dev.talhabayhan.lexi.core.testing.FakeNotificationPermission
import dev.talhabayhan.lexi.core.testing.FakeProgressRepository
import dev.talhabayhan.lexi.core.testing.FakeReminderScheduler
import dev.talhabayhan.lexi.core.testing.FakeReminderTextProvider
import dev.talhabayhan.lexi.core.testing.FakeSessionRepository
import dev.talhabayhan.lexi.core.testing.FakeSettingsRepository
import dev.talhabayhan.lexi.core.testing.FakeStreakRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ReminderPrePromptTest {
    private val real = SessionRecord(epochDay = 1, completed = true, correct = 10, mode = GameMode.CLASSIC)

    @Test
    fun `pre-prompt needs a completed real game`() {
        val s = Settings()
        val can = PermissionStatus.CAN_ASK

        assertFalse(ReminderPrePromptPolicy.shouldShow(s, emptyList(), can))
        assertTrue(ReminderPrePromptPolicy.shouldShow(s, listOf(real), can))
        assertFalse(ReminderPrePromptPolicy.shouldShow(s, listOf(real.copy(completed = false)), can))
        assertFalse(ReminderPrePromptPolicy.shouldShow(s, listOf(real.copy(mode = GameMode.LEVEL_TEST)), can))
        assertFalse(ReminderPrePromptPolicy.shouldShow(s, listOf(real.copy(mode = GameMode.DUEL)), can))
        assertFalse(ReminderPrePromptPolicy.shouldShow(s, listOf(real.copy(mode = null)), can))
        for (mode in listOf(GameMode.PRACTICE, GameMode.HARDCORE, GameMode.DAILY, GameMode.REVIEW)) {
            assertTrue(ReminderPrePromptPolicy.shouldShow(s, listOf(real.copy(mode = mode)), can), "$mode")
        }
    }

    @Test
    fun `pre-prompt is shown only once`() {
        assertFalse(
            ReminderPrePromptPolicy.shouldShow(
                Settings(reminderPrePromptShown = true),
                listOf(real),
                PermissionStatus.CAN_ASK,
            ),
        )
    }

    @Test
    fun `pre-prompt is not shown when the reminder is on or permission is granted`() {
        val on = Settings(dailyReminder = true)
        assertFalse(ReminderPrePromptPolicy.shouldShow(on, listOf(real), PermissionStatus.CAN_ASK))
        assertFalse(ReminderPrePromptPolicy.shouldShow(Settings(), listOf(real), PermissionStatus.GRANTED))
        assertTrue(ReminderPrePromptPolicy.shouldShow(Settings(), listOf(real), PermissionStatus.BLOCKED))
    }

    @Test
    fun `should show reads settings sessions and permission`() = runTest {
        val f = Fixture()
        val show = ShouldShowReminderPrePrompt(f.settings, f.sessions, f.permission)

        assertFalse(show())
        f.sessions.record(completedSession(GameMode.LEVEL_TEST))
        assertFalse(show())
        f.sessions.record(completedSession(GameMode.CLASSIC))
        assertTrue(show())
        f.settings.setReminderPrePromptShown()
        assertFalse(show())
    }

    @Test
    fun `real game count leaves out the level test`() = runTest {
        val f = Fixture()
        val show = ShouldShowReminderPrePrompt(f.settings, f.sessions, f.permission)

        f.sessions.record(completedSession(GameMode.LEVEL_TEST))
        assertEquals(0, show.realGameCount())
        f.sessions.record(completedSession(GameMode.CLASSIC))
        assertEquals(1, show.realGameCount())
    }

    @Test
    fun `yes with permission granted turns the reminder on at 20_00 and schedules it`() = runTest {
        val f = Fixture(Settings(reminderMinuteOfDay = 9 * 60))

        val status = f.answer(accepted = true)

        assertEquals(PermissionStatus.GRANTED, status)
        val s = f.settings.settings.first()
        assertTrue(s.dailyReminder && s.reminderPrePromptShown)
        assertEquals(ReminderTimes.DEFAULT_MINUTE, s.reminderMinuteOfDay)
        assertTrue(f.scheduler.scheduled.isNotEmpty())
    }

    @Test
    fun `yes with permission denied only marks the pre-prompt shown`() = runTest {
        val f = Fixture()
        f.permission.answer = PermissionStatus.BLOCKED

        assertEquals(PermissionStatus.BLOCKED, f.answer(accepted = true))

        val s = f.settings.settings.first()
        assertTrue(s.reminderPrePromptShown)
        assertFalse(s.dailyReminder)
        assertEquals(0, f.scheduler.replaceCalls)
    }

    @Test
    fun `not now marks the pre-prompt shown without asking the OS`() = runTest {
        val f = Fixture()

        assertNull(f.answer(accepted = false))

        assertTrue(f.settings.settings.first().reminderPrePromptShown)
        assertFalse(f.settings.settings.first().dailyReminder)
        assertEquals(0, f.permission.requestCalls)
    }

    private fun completedSession(mode: GameMode) = SessionResult(
        mode = mode,
        level = Level.A1,
        category = null,
        seed = 1L,
        startedAtMs = 0L,
        endedAtMs = 1L,
        epochDay = 1L,
        score = 0,
        correct = 5,
        wrong = 0,
        passed = 0,
        completed = true,
    )

    private class Fixture(initial: Settings = Settings()) {
        val clock = FakeAppClock()
        val settings = FakeSettingsRepository(initial)
        val sessions = FakeSessionRepository()
        val permission = FakeNotificationPermission()
        val scheduler = FakeReminderScheduler()
        val reschedule = RescheduleReminders(
            settings = settings,
            streaks = FakeStreakRepository(listOf(clock.epochDay(clock.nowMs()) - 1)),
            progress = FakeProgressRepository(),
            permission = permission,
            scheduler = scheduler,
            texts = FakeReminderTextProvider(),
            clock = clock,
        )
        val answer = AnswerReminderPrePrompt(settings, permission, reschedule)
    }
}
