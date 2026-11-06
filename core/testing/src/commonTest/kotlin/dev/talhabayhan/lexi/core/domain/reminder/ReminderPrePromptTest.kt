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

    private val two = listOf(real, real)
    private val can = PermissionStatus.CAN_ASK
    private val weekMs = ReminderPrePromptPolicy.SECOND_ASK_DELAY_MS

    private fun show(s: Settings, sessions: List<SessionRecord>, p: PermissionStatus = can, now: Long = 0L) =
        ReminderPrePromptPolicy.shouldShow(s, sessions, p, now)

    @Test
    fun `pre-prompt needs two completed real games and not one`() {
        val s = Settings()

        assertFalse(show(s, emptyList()))
        assertFalse(show(s, listOf(real)))
        assertTrue(show(s, two))
        assertFalse(show(s, listOf(real, real.copy(completed = false))))
        assertFalse(show(s, listOf(real, real.copy(mode = GameMode.LEVEL_TEST))))
        assertFalse(show(s, listOf(real, real.copy(mode = GameMode.DUEL))))
        assertFalse(show(s, listOf(real, real.copy(mode = null))))
        for (mode in listOf(GameMode.PRACTICE, GameMode.HARDCORE, GameMode.DAILY, GameMode.REVIEW)) {
            assertTrue(show(s, listOf(real, real.copy(mode = mode))), "$mode")
        }
    }

    @Test
    fun `second ask waits seven days after the first and needs the OS dialog to be possible`() {
        val asked = Settings(reminderPrePromptAsks = 1, reminderPrePromptLastAskMs = 1_000L)

        assertFalse(show(asked, two, now = 1_000L))
        assertFalse(show(asked, two, now = 1_000L + weekMs - 1))
        assertTrue(show(asked, two, now = 1_000L + weekMs))
        assertFalse(show(asked, two, PermissionStatus.BLOCKED, now = 1_000L + weekMs))
    }

    @Test
    fun `at most two asks in total`() {
        val twice = Settings(reminderPrePromptAsks = 2, reminderPrePromptLastAskMs = 0L)

        assertFalse(show(twice, two, now = 10 * weekMs))
    }

    @Test
    fun `pre-prompt is not shown when the reminder is on or permission is granted`() {
        assertFalse(show(Settings(dailyReminder = true), two))
        assertFalse(show(Settings(), two, PermissionStatus.GRANTED))
        assertTrue(show(Settings(), two, PermissionStatus.BLOCKED))
    }

    @Test
    fun `should show reads settings sessions permission and the clock`() = runTest {
        val f = Fixture()
        val show = ShouldShowReminderPrePrompt(f.settings, f.sessions, f.permission, f.clock)

        assertFalse(show())
        f.sessions.record(completedSession(GameMode.LEVEL_TEST))
        f.sessions.record(completedSession(GameMode.CLASSIC))
        assertFalse(show())
        f.sessions.record(completedSession(GameMode.CLASSIC))
        assertTrue(show())

        f.answer(accepted = false)
        assertFalse(show())
        f.clock.advanceBy(weekMs - 1)
        assertFalse(show())
        f.clock.advanceBy(1)
        assertTrue(show())

        f.answer(accepted = false)
        f.clock.advanceBy(10 * weekMs)
        assertFalse(show())
    }

    @Test
    fun `an ask counted on screen entry and again by the answer stays one ask`() = runTest {
        val f = Fixture()

        f.settings.recordReminderPrePromptAsked(f.clock.nowMs())
        f.answer(accepted = false)

        assertEquals(1, f.settings.settings.first().reminderPrePromptAsks)
    }

    @Test
    fun `real game count leaves out the level test`() = runTest {
        val f = Fixture()
        val show = ShouldShowReminderPrePrompt(f.settings, f.sessions, f.permission, f.clock)

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
        val answer = AnswerReminderPrePrompt(settings, permission, reschedule, clock)
    }
}
