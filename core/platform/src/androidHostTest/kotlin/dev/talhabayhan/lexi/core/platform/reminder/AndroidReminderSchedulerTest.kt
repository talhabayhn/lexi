package dev.talhabayhan.lexi.core.platform.reminder

import android.Manifest
import android.app.AlarmManager
import android.app.Application
import android.app.NotificationManager
import android.content.Intent
import dev.talhabayhan.lexi.core.domain.reminder.ScheduledReminder
import dev.talhabayhan.lexi.core.domain.time.AppClock
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class AndroidReminderSchedulerTest {
    private val app: Application = RuntimeEnvironment.getApplication()
    private val zone: ZoneId = ZoneId.systemDefault()
    private val today: Long = LocalDate.of(2030, 5, 10).toEpochDay()
    private val nowMs: Long = LocalDate.ofEpochDay(today).atTime(12, 0).atZone(zone).toInstant().toEpochMilli()
    private val clock = object : AppClock {
        override fun nowMs() = this@AndroidReminderSchedulerTest.nowMs
        override fun epochDay(atMs: Long) = today
        override fun msUntilNextDay(atMs: Long) = 0L
    }
    private val scheduler = AndroidReminderScheduler(app, clock) { "Hatirlatmalar" }

    private fun reminder(index: Int, dayOffset: Long, minute: Int = 20 * 60) = ScheduledReminder(
        id = ScheduledReminder.FIRST_ID + index,
        epochDay = today + dayOffset,
        minuteOfDay = minute,
        title = "Title $index",
        body = "Body $index",
    )

    private fun alarms() = shadowOf(app.getSystemService(AlarmManager::class.java)).scheduledAlarms

    @Before
    fun setUp() {
        shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
    }

    @Test
    fun replaceAllArmsFutureEntriesOnlyAndPersistsThePlan() = runBlocking {
        scheduler.replaceAll(listOf(reminder(0, 0, minute = 8 * 60), reminder(1, 0), reminder(2, 1)))

        // 08:00 today is already past at 12:00.
        assertEquals(2, alarms().size)
        assertEquals(
            listOf(
                LocalDate.ofEpochDay(today).atTime(20, 0).atZone(zone).toInstant().toEpochMilli(),
                LocalDate.ofEpochDay(today + 1).atTime(20, 0).atZone(zone).toInstant().toEpochMilli(),
            ),
            alarms().map { it.triggerAtMs }.sorted(),
        )
        assertEquals(3, ReminderPlanStore(app).load().size)
        assertEquals("Hatirlatmalar", ReminderPlanStore(app).channelName)
    }

    @Test
    fun replaceAllCancelsThePreviousPlan() = runBlocking {
        scheduler.replaceAll(listOf(reminder(0, 0), reminder(1, 1), reminder(2, 2)))
        scheduler.replaceAll(listOf(reminder(0, 3)))

        assertEquals(1, alarms().size)
        assertEquals(1, ReminderPlanStore(app).load().size)
    }

    @Test
    fun cancelAllClearsAlarmsAndPlan() = runBlocking {
        scheduler.replaceAll(listOf(reminder(0, 0), reminder(1, 1)))
        scheduler.cancelAll()

        assertTrue(alarms().isEmpty())
        assertTrue(ReminderPlanStore(app).load().isEmpty())
    }

    @Test
    fun rearmRestoresStoredPlanAndSkipsPastEntries() = runBlocking {
        scheduler.replaceAll(listOf(reminder(0, 0), reminder(1, 1), reminder(2, 2)))
        shadowOf(app.getSystemService(AlarmManager::class.java)).scheduledAlarms.clear()
        ReminderAlarms.cancelAll(app)
        assertTrue(alarms().isEmpty())

        // The device was off until the evening of day +1: only day +2 is still ahead.
        val later = LocalDate.ofEpochDay(today + 1).atTime(21, 0).atZone(zone).toInstant().toEpochMilli()
        AndroidReminderScheduler.rearmStoredPlan(app, later)

        assertEquals(1, alarms().size)
    }

    @Test
    fun rescheduleReceiverRearmsOnBoot() = runBlocking {
        scheduler.replaceAll(listOf(reminder(0, 400)))
        ReminderAlarms.cancelAll(app)
        assertTrue(alarms().isEmpty())

        ReminderRescheduleReceiver().onReceive(app, Intent(Intent.ACTION_BOOT_COMPLETED))

        assertEquals(1, alarms().size)
    }

    @Test
    fun alarmReceiverPostsOnTheRemindersChannel() = runBlocking {
        scheduler.replaceAll(listOf(reminder(0, 1)))
        val intent = Intent(app, ReminderAlarmReceiver::class.java)
            .putExtra(ReminderAlarms.EXTRA_ID, 7100)
            .putExtra(ReminderAlarms.EXTRA_TITLE, "Title")
            .putExtra(ReminderAlarms.EXTRA_BODY, "Body")

        ReminderAlarmReceiver().onReceive(app, intent)

        val manager = shadowOf(app.getSystemService(NotificationManager::class.java))
        val posted = manager.allNotifications.single()
        assertEquals("reminders", posted.channelId)
        assertEquals("Title", posted.extras.getString("android.title"))
        val channel = app.getSystemService(NotificationManager::class.java).getNotificationChannel("reminders")
        assertEquals("Hatirlatmalar", channel.name.toString())
    }

    @Test
    fun alarmReceiverDoesNotPostWhenNotificationsAreOff() {
        shadowOf(app.getSystemService(NotificationManager::class.java)).setNotificationsEnabled(false)
        val intent = Intent(app, ReminderAlarmReceiver::class.java)
            .putExtra(ReminderAlarms.EXTRA_ID, 7100)
            .putExtra(ReminderAlarms.EXTRA_TITLE, "Title")
            .putExtra(ReminderAlarms.EXTRA_BODY, "Body")

        ReminderAlarmReceiver().onReceive(app, intent)

        assertFalse(shadowOf(app.getSystemService(NotificationManager::class.java)).allNotifications.isNotEmpty())
    }
}
