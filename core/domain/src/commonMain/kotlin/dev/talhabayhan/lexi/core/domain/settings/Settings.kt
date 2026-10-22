package dev.talhabayhan.lexi.core.domain.settings

import dev.talhabayhan.lexi.core.domain.reminder.ReminderTimes

/**
 * Player settings (`11-settings` design, `PRODUCT.md`). Defaults are the design's values; stored in DataStore by
 * `SettingsRepository`.
 */
public data class Settings(
    val soundEffects: Boolean = true,
    val haptics: Boolean = true,
    /** "Doğru cevapta telaffuzu çal": speak the answer after a correct answer. */
    val speakOnCorrect: Boolean = true,
    /** Classic total time; one of `GameConfig.classicTimeLimitOptionsSeconds` (120/180/240/300). */
    val classicTimeLimitSeconds: Int = DEFAULT_CLASSIC_TIME_LIMIT_SECONDS,
    /** Daily reminder notification; off until the player turns it on or accepts the pre-prompt (`ReminderPlanner`). */
    val dailyReminder: Boolean = false,
    /** Reminder time as minutes after local midnight (design: 20:00); one of `ReminderTimes.optionsMinuteOfDay`. */
    val reminderMinuteOfDay: Int = DEFAULT_REMINDER_MINUTE_OF_DAY,
    /** The one-time reminder pre-prompt was shown (any answer); never shown again (`ShouldShowReminderPrePrompt`). */
    val reminderPrePromptShown: Boolean = false,
    /** "Günlük hedef": wheels per day, at least 1. */
    val dailyGoalWheels: Int = 1,
    /** "Kelime defteri tekrarları": remind about due notebook cards. */
    val reviewReminders: Boolean = true,
    val appLanguage: AppLanguage = AppLanguage.SYSTEM,
    val clueLanguage: ClueLanguage = ClueLanguage.TR,
    val accent: Accent = Accent.US,
    /** Opt-in to anonymous usage statistics (Firebase Analytics, KVKK); off until the player agrees. */
    val analyticsConsent: Boolean = false,
) {
    public val uiLanguage: AppLanguage get() = appLanguage

    init {
        require(reminderMinuteOfDay in 0 until MINUTES_PER_DAY) { "reminderMinuteOfDay $reminderMinuteOfDay" }
        require(dailyGoalWheels >= 1) { "dailyGoalWheels $dailyGoalWheels must be at least 1" }
    }

    public companion object {
        public const val DEFAULT_CLASSIC_TIME_LIMIT_SECONDS: Int = 180
        public const val DEFAULT_REMINDER_MINUTE_OF_DAY: Int = ReminderTimes.DEFAULT_MINUTE
        public const val MINUTES_PER_DAY: Int = 24 * 60
    }
}

/** App UI language; [SYSTEM] follows the device (Turkish or English, `CLAUDE.md`). */
public enum class AppLanguage(public val localeTag: String?) {
    SYSTEM(null),
    TR("tr"),
    EN("en"),
}

public typealias UiLanguage = AppLanguage

/** Language of clues and example translations; Turkish only in v1 (`PRODUCT.md`: Spanish later). */
public enum class ClueLanguage(public val code: String) { TR("tr"), }

/** English pronunciation used by the Listen joker and TTS. */
public enum class Accent { US, UK }
