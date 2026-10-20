package dev.talhabayhan.lexi.core.domain.settings

import dev.talhabayhan.lexi.core.domain.game.GameConfig
import dev.talhabayhan.lexi.core.domain.reminder.ReminderTimes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SettingsTest {
    @Test
    fun defaults_matchTheSettingsDesign() {
        val settings = Settings()

        assertTrue(settings.soundEffects && settings.haptics && settings.speakOnCorrect && settings.reviewReminders)
        assertEquals(GameConfig().classicTimeLimitSeconds, settings.classicTimeLimitSeconds)
        assertEquals(20 * 60, settings.reminderMinuteOfDay)
        assertEquals(1, settings.dailyGoalWheels)
        assertEquals(ClueLanguage.TR, settings.clueLanguage)
        assertEquals(Accent.US, settings.accent)
    }

    @Test
    fun reminder_isOffAndPrePromptNotShownByDefault() {
        val settings = Settings()

        assertFalse(settings.dailyReminder)
        assertFalse(settings.reminderPrePromptShown)
        assertTrue(settings.reminderMinuteOfDay in ReminderTimes.optionsMinuteOfDay)
    }

    @Test
    fun reminderTimeAndDailyGoal_mustBeInRange() {
        assertFailsWith<IllegalArgumentException> { Settings(reminderMinuteOfDay = 24 * 60) }
        assertFailsWith<IllegalArgumentException> { Settings(reminderMinuteOfDay = -1) }
        assertFailsWith<IllegalArgumentException> { Settings(dailyGoalWheels = 0) }
    }
}
