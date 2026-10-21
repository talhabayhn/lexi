package dev.talhabayhan.lexi.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.talhabayhan.lexi.core.domain.game.GameConfig
import dev.talhabayhan.lexi.core.domain.platform.AppLinks
import dev.talhabayhan.lexi.core.domain.reminder.NotificationPermission
import dev.talhabayhan.lexi.core.domain.reminder.PermissionStatus
import dev.talhabayhan.lexi.core.domain.reminder.ReminderTimes
import dev.talhabayhan.lexi.core.domain.repository.SettingsRepository
import dev.talhabayhan.lexi.core.domain.settings.Accent
import dev.talhabayhan.lexi.core.domain.settings.AppLanguage
import dev.talhabayhan.lexi.core.domain.settings.Settings
import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsEvent
import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsTracker
import dev.talhabayhan.lexi.core.domain.telemetry.CrashReporter
import dev.talhabayhan.lexi.core.platform.language.syncPlatformAppLanguage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Settings (`11-settings`): settings read from and written to [SettingsRepository]. The state is always the stored
 * value, so controls only move once the write has landed; game, feedback and TTS services read the same store.
 */
internal class SettingsViewModel(
    private val settings: SettingsRepository,
    private val permission: NotificationPermission,
    private val analytics: AnalyticsTracker,
    private val crashReporter: CrashReporter,
    private val config: GameConfig = GameConfig(),
) : ViewModel() {
    // Starts from the stored values when they are already loaded, so the screen composes once with the real ones.
    private val mutableState = MutableStateFlow(settings.loaded.value?.let { withStored(it) } ?: SettingsUiState())
    val state: StateFlow<SettingsUiState> = mutableState.asStateFlow()

    private val effectChannel = Channel<SettingsEffect>(Channel.BUFFERED)
    val effects: Flow<SettingsEffect> = effectChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            settings.settings
                // A broken read keeps the defaults shown; the switches still try to write.
                .catch { e -> crashReporter.recordException(e, "settings not read") }
                .collect { value -> mutableState.update { current -> withStored(value, current) } }
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

            is SettingsIntent.SetReviewReminders -> write { it.copy(reviewReminders = intent.enabled) }

            SettingsIntent.OpenReminderSheet -> {
                mutableState.update { it.copy(reminderSheetVisible = true) }
                refreshPermission()
            }

            SettingsIntent.CloseReminderSheet -> mutableState.update { it.copy(reminderSheetVisible = false) }

            is SettingsIntent.SetReminderEnabled ->
                if (intent.enabled) turnReminderOn() else write { it.copy(dailyReminder = false) }

            is SettingsIntent.SetReminderTime -> if (intent.minuteOfDay in ReminderTimes.optionsMinuteOfDay) {
                write { it.copy(reminderMinuteOfDay = intent.minuteOfDay) }
            }

            SettingsIntent.RefreshNotificationPermission -> refreshPermission()

            SettingsIntent.OpenNotificationSettings -> permission.openSystemSettings()

            SettingsIntent.OpenAbout -> effectChannel.trySend(SettingsEffect.NavigateToAbout)

            SettingsIntent.OpenPrivacy -> effectChannel.trySend(SettingsEffect.OpenUrl(AppLinks.PRIVACY_POLICY))

            SettingsIntent.OpenTerms -> effectChannel.trySend(SettingsEffect.OpenUrl(AppLinks.TERMS_OF_SERVICE))
        }
    }

    /** Granted: on. Can ask: our pre-prompt (it asks the OS and turns the reminder on). Blocked: stays off, hint. */
    private fun turnReminderOn() {
        viewModelScope.launch {
            when (permission.status()) {
                PermissionStatus.GRANTED -> write(onWritten = { analytics.track(AnalyticsEvent.ReminderEnabled) }) {
                    it.copy(dailyReminder = true)
                }

                PermissionStatus.CAN_ASK -> effectChannel.trySend(SettingsEffect.NavigateToReminderPrePrompt)

                PermissionStatus.BLOCKED -> mutableState.update { it.copy(notificationsBlocked = true) }
            }
        }
    }

    private fun refreshPermission() {
        viewModelScope.launch {
            val blocked = permission.status() == PermissionStatus.BLOCKED
            mutableState.update { it.copy(notificationsBlocked = blocked) }
        }
    }

    private fun write(onWritten: () -> Unit = {}, transform: (Settings) -> Settings) {
        viewModelScope.launch {
            try {
                settings.update(transform)
            } catch (e: CancellationException) {
                throw e
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                // The switch stays where it was (the state follows the store).
                crashReporter.recordException(e, "settings not written")
                return@launch
            }
            onWritten()
        }
    }

    internal companion object {
        val DAILY_GOAL_OPTIONS: List<Int> = listOf(1, 2, 3, 5)
    }
}

/** The stored values on top of the sheet state, which isn't stored. */
private fun withStored(settings: Settings, current: SettingsUiState = SettingsUiState()) = current.copy(
    soundEffects = settings.soundEffects,
    haptics = settings.haptics,
    speakOnCorrect = settings.speakOnCorrect,
    classicTimeLimitSeconds = settings.classicTimeLimitSeconds,
    dailyGoalWheels = settings.dailyGoalWheels,
    dailyReminder = settings.dailyReminder,
    reminderMinuteOfDay = settings.reminderMinuteOfDay,
    reviewReminders = settings.reviewReminders,
    appLanguage = settings.appLanguage,
    accent = settings.accent,
)
