package dev.talhabayhan.lexi.feature.settings

import androidx.compose.runtime.Immutable
import dev.talhabayhan.lexi.core.domain.reminder.ReminderTimes
import dev.talhabayhan.lexi.core.domain.settings.Accent
import dev.talhabayhan.lexi.core.domain.settings.AppLanguage
import dev.talhabayhan.lexi.core.domain.settings.Settings

/** What `11-settings` shows: the live settings, plus the state of the reminder sheet (DESIGN-DEBT, no design). */
@Immutable
internal data class SettingsUiState(
    val soundEffects: Boolean = true,
    val haptics: Boolean = true,
    val speakOnCorrect: Boolean = true,
    val classicTimeLimitSeconds: Int = Settings.DEFAULT_CLASSIC_TIME_LIMIT_SECONDS,
    val dailyGoalWheels: Int = 1,
    /** "Günlük hatırlatma": the stored switch; its row shows [reminderMinuteOfDay] when on. */
    val dailyReminder: Boolean = false,
    val reminderMinuteOfDay: Int = ReminderTimes.DEFAULT_MINUTE,
    /** "Kelime defteri tekrarları". */
    val reviewReminders: Boolean = true,
    val reminderSheetVisible: Boolean = false,
    /** The OS refuses notifications for good: the sheet shows the hint and "Ayarları aç". */
    val notificationsBlocked: Boolean = false,
    val appLanguage: AppLanguage = AppLanguage.SYSTEM,
    val accent: Accent = Accent.US,
)

internal sealed interface SettingsIntent {
    data object Back : SettingsIntent

    data class SetSoundEffects(val enabled: Boolean) : SettingsIntent

    data class SetHaptics(val enabled: Boolean) : SettingsIntent

    data class SetSpeakOnCorrect(val enabled: Boolean) : SettingsIntent

    data object CycleClassicTime : SettingsIntent

    data object CycleDailyGoal : SettingsIntent

    data object CycleAppLanguage : SettingsIntent

    data object CycleAccent : SettingsIntent

    data class SetClassicTimeLimit(val seconds: Int) : SettingsIntent

    data class SetDailyGoalWheels(val wheels: Int) : SettingsIntent

    data class SetAppLanguage(val language: AppLanguage) : SettingsIntent

    data class SetAccent(val accent: Accent) : SettingsIntent

    data class SetReviewReminders(val enabled: Boolean) : SettingsIntent

    /** The reminder row: opens the sheet (and re-reads the OS permission). */
    data object OpenReminderSheet : SettingsIntent

    data object CloseReminderSheet : SettingsIntent

    /** The sheet's switch. On asks for the permission first when needed (pre-prompt, then the OS dialog). */
    data class SetReminderEnabled(val enabled: Boolean) : SettingsIntent

    data class SetReminderTime(val minuteOfDay: Int) : SettingsIntent

    /** Back from the system settings (ON_RESUME): the permission may have changed. */
    data object RefreshNotificationPermission : SettingsIntent

    data object OpenNotificationSettings : SettingsIntent

    data object OpenAbout : SettingsIntent

    data object OpenPrivacy : SettingsIntent

    data object OpenTerms : SettingsIntent
}

internal sealed interface SettingsEffect {
    data object NavigateBack : SettingsEffect

    data object NavigateToAbout : SettingsEffect

    /** Turning the reminder on while the OS can still ask: our pre-prompt comes first. */
    data object NavigateToReminderPrePrompt : SettingsEffect

    data class OpenUrl(val url: String) : SettingsEffect
}
