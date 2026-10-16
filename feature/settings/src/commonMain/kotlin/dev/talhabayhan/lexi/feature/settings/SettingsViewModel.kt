package dev.talhabayhan.lexi.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.talhabayhan.lexi.core.domain.repository.SettingsRepository
import dev.talhabayhan.lexi.core.domain.settings.Settings
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Settings (`11-settings`): the three working switches read from and written to the [SettingsRepository]. The state
 * is always the stored value, so a switch only moves once the write has landed; `SoundPlayer`, `Haptics` and the
 * game's speech read the same store.
 */
internal class SettingsViewModel(private val settings: SettingsRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = mutableState.asStateFlow()

    private val effectChannel = Channel<SettingsEffect>(Channel.BUFFERED)
    val effects: Flow<SettingsEffect> = effectChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            settings.settings
                // A broken read keeps the defaults shown; the switches still try to write. TODO(M7): report it.
                .catch { }
                .collect {
                    mutableState.value = SettingsUiState(
                        soundEffects = it.soundEffects,
                        haptics = it.haptics,
                        speakOnCorrect = it.speakOnCorrect,
                    )
                }
        }
    }

    fun onIntent(intent: SettingsIntent) {
        when (intent) {
            SettingsIntent.Back -> effectChannel.trySend(SettingsEffect.NavigateBack)
            is SettingsIntent.SetSoundEffects -> write { it.copy(soundEffects = intent.enabled) }
            is SettingsIntent.SetHaptics -> write { it.copy(haptics = intent.enabled) }
            is SettingsIntent.SetSpeakOnCorrect -> write { it.copy(speakOnCorrect = intent.enabled) }
        }
    }

    private fun write(transform: (Settings) -> Settings) {
        viewModelScope.launch {
            try {
                settings.update(transform)
            } catch (e: CancellationException) {
                throw e
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                // The switch stays where it was (the state follows the store). TODO(M7): report it.
            }
        }
    }
}
