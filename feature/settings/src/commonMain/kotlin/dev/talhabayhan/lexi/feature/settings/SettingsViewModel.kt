package dev.talhabayhan.lexi.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.talhabayhan.lexi.core.domain.game.GameConfig
import dev.talhabayhan.lexi.core.domain.platform.AppLinks
import dev.talhabayhan.lexi.core.domain.repository.SettingsRepository
import dev.talhabayhan.lexi.core.domain.settings.Accent
import dev.talhabayhan.lexi.core.domain.settings.AppLanguage
import dev.talhabayhan.lexi.core.domain.settings.Settings
import dev.talhabayhan.lexi.core.platform.language.syncPlatformAppLanguage
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
 * Settings (`11-settings`): settings read from and written to [SettingsRepository]. The state is always the stored
 * value, so controls only move once the write has landed; game, feedback and TTS services read the same store.
 */
internal class SettingsViewModel(
    private val settings: SettingsRepository,
    private val config: GameConfig = GameConfig(),
) : ViewModel() {
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
                        classicTimeLimitSeconds = it.classicTimeLimitSeconds,
                        dailyGoalWheels = it.dailyGoalWheels,
                        appLanguage = it.appLanguage,
                        accent = it.accent,
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

            SettingsIntent.CycleClassicTime -> write { current ->
                val options = config.classicTimeLimitOptionsSeconds
                val idx = options.indexOf(current.classicTimeLimitSeconds)
                val next = if (idx >= 0) options[(idx + 1) % options.size] else options.first()
                current.copy(classicTimeLimitSeconds = next)
            }

            SettingsIntent.CycleDailyGoal -> write { current ->
                val idx = DAILY_GOAL_OPTIONS.indexOf(current.dailyGoalWheels)
                val next = if (idx >= 0) {
                    DAILY_GOAL_OPTIONS[(idx + 1) % DAILY_GOAL_OPTIONS.size]
                } else {
                    DAILY_GOAL_OPTIONS.first()
                }
                current.copy(dailyGoalWheels = next)
            }

            SettingsIntent.CycleAppLanguage -> write { current ->
                val options = AppLanguage.entries
                val idx = options.indexOf(current.appLanguage)
                val next = if (idx >= 0) options[(idx + 1) % options.size] else options.first()
                syncPlatformAppLanguage(next)
                current.copy(appLanguage = next)
            }

            SettingsIntent.CycleAccent -> write { current ->
                val next = if (current.accent == Accent.US) Accent.UK else Accent.US
                current.copy(accent = next)
            }

            is SettingsIntent.SetClassicTimeLimit -> write { it.copy(classicTimeLimitSeconds = intent.seconds) }

            is SettingsIntent.SetDailyGoalWheels -> write { it.copy(dailyGoalWheels = intent.wheels) }

            is SettingsIntent.SetAppLanguage -> write { current ->
                syncPlatformAppLanguage(intent.language)
                current.copy(appLanguage = intent.language)
            }

            is SettingsIntent.SetAccent -> write { it.copy(accent = intent.accent) }

            SettingsIntent.OpenAbout -> effectChannel.trySend(SettingsEffect.NavigateToAbout)

            SettingsIntent.OpenPrivacy -> effectChannel.trySend(SettingsEffect.OpenUrl(AppLinks.PRIVACY_POLICY))

            SettingsIntent.OpenTerms -> effectChannel.trySend(SettingsEffect.OpenUrl(AppLinks.TERMS_OF_SERVICE))
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

    internal companion object {
        val DAILY_GOAL_OPTIONS: List<Int> = listOf(1, 2, 3, 5)
    }
}
