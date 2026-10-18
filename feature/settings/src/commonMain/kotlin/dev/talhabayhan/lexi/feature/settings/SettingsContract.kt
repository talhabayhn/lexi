package dev.talhabayhan.lexi.feature.settings

import androidx.compose.runtime.Immutable
import dev.talhabayhan.lexi.core.domain.settings.Accent
import dev.talhabayhan.lexi.core.domain.settings.Settings

/** What `11-settings` shows: the live settings and the placeholders for rows arriving later in M7. */
@Immutable
internal data class SettingsUiState(
    val soundEffects: Boolean = true,
    val haptics: Boolean = true,
    val speakOnCorrect: Boolean = true,
    val classicTimeLimitSeconds: Int = Settings.DEFAULT_CLASSIC_TIME_LIMIT_SECONDS,
    val dailyGoalWheels: Int = 1,
    val accent: Accent = Accent.US,
)

internal sealed interface SettingsIntent {
    data object Back : SettingsIntent

    data class SetSoundEffects(val enabled: Boolean) : SettingsIntent

    data class SetHaptics(val enabled: Boolean) : SettingsIntent

    data class SetSpeakOnCorrect(val enabled: Boolean) : SettingsIntent

    data object CycleClassicTime : SettingsIntent

    data object CycleDailyGoal : SettingsIntent

    data object CycleAccent : SettingsIntent

    data class SetClassicTimeLimit(val seconds: Int) : SettingsIntent

    data class SetDailyGoalWheels(val wheels: Int) : SettingsIntent

    data class SetAccent(val accent: Accent) : SettingsIntent

    data object OpenAbout : SettingsIntent

    data object OpenPrivacy : SettingsIntent

    data object OpenTerms : SettingsIntent
}

internal sealed interface SettingsEffect {
    data object NavigateBack : SettingsEffect

    data object NavigateToAbout : SettingsEffect

    data class OpenUrl(val url: String) : SettingsEffect
}
