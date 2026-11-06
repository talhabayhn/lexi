package dev.talhabayhan.lexi.core.domain.settings

import dev.talhabayhan.lexi.core.domain.model.Level
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
    /** How often the proactive reminder pre-prompt was shown (max two, `ReminderPrePromptPolicy`). */
    val reminderPrePromptAsks: Int = 0,
    /** Epoch ms of the latest pre-prompt (0 = never); the second one waits 7 days after it. */
    val reminderPrePromptLastAskMs: Long = 0L,
    /** "Günlük hedef": wheels per day, at least 1. */
    val dailyGoalWheels: Int = 1,
    /** "Kelime defteri tekrarları": remind about due notebook cards. */
    val reviewReminders: Boolean = true,
    val appLanguage: AppLanguage = AppLanguage.SYSTEM,
    val clueLanguage: ClueLanguage = ClueLanguage.TR,
    val accent: Accent = Accent.US,
    /** Opt-in to anonymous usage statistics (Firebase Analytics, KVKK); off until the player agrees. */
    val analyticsConsent: Boolean = false,
    /**
     * The duel players' names from the last match (`GAME_RULES.md` §8), two entries; "" = the default name
     * ("1. Oyuncu"). Never sent anywhere (no analytics, no crash keys).
     */
    val duelPlayerNames: List<String> = listOf("", ""),
    /** The level of the last duel setup. */
    val duelLevel: Level = Level.A1,
    /** The clock per player of the last duel setup; one of `GameConfig.duelClockOptionsSeconds`. */
    val duelClockSeconds: Int = DEFAULT_DUEL_CLOCK_SECONDS,
) {
    public val uiLanguage: AppLanguage get() = appLanguage

    /** The pre-prompt was shown at least once. */
    public val reminderPrePromptShown: Boolean get() = reminderPrePromptAsks > 0

    init {
        require(reminderMinuteOfDay in 0 until MINUTES_PER_DAY) { "reminderMinuteOfDay $reminderMinuteOfDay" }
        require(dailyGoalWheels >= 1) { "dailyGoalWheels $dailyGoalWheels must be at least 1" }
        require(duelPlayerNames.size == 2) { "duelPlayerNames needs 2 entries, was ${duelPlayerNames.size}" }
    }

    public companion object {
        public const val DEFAULT_CLASSIC_TIME_LIMIT_SECONDS: Int = 180
        public const val DEFAULT_DUEL_CLOCK_SECONDS: Int = 150
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
