package dev.talhabayhan.lexi.feature.settings

import androidx.compose.runtime.Immutable

/** What `11-settings` shows: the switches that work today; every other row is a "soon" placeholder (M7). */
@Immutable
internal data class SettingsUiState(
    val soundEffects: Boolean = true,
    val haptics: Boolean = true,
    val speakOnCorrect: Boolean = true,
)

internal sealed interface SettingsIntent {
    data object Back : SettingsIntent

    data class SetSoundEffects(val enabled: Boolean) : SettingsIntent

    data class SetHaptics(val enabled: Boolean) : SettingsIntent

    data class SetSpeakOnCorrect(val enabled: Boolean) : SettingsIntent
}

internal sealed interface SettingsEffect {
    data object NavigateBack : SettingsEffect
}
