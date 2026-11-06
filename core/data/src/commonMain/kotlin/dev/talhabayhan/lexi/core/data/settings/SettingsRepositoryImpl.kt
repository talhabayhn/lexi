package dev.talhabayhan.lexi.core.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.talhabayhan.lexi.core.domain.game.DuelNames
import dev.talhabayhan.lexi.core.domain.game.GameConfig
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.reminder.ReminderTimes
import dev.talhabayhan.lexi.core.domain.repository.SettingsRepository
import dev.talhabayhan.lexi.core.domain.settings.Accent
import dev.talhabayhan.lexi.core.domain.settings.ClueLanguage
import dev.talhabayhan.lexi.core.domain.settings.Settings
import dev.talhabayhan.lexi.core.domain.settings.UiLanguage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import okio.Path.Companion.toPath

/** DataStore Preferences file at [path] (absolute; see `DatabaseFiles.settingsFilePath`). One per process. */
public fun createSettingsDataStore(path: String, scope: CoroutineScope): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(scope = scope, produceFile = { path.toPath() })

/**
 * [Settings] in DataStore Preferences. Keys are stable (renaming one loses the stored value); invalid stored values
 * (e.g. a removed time option or enum entry) read as the default.
 */
internal class SettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
    private val config: GameConfig,
    scope: CoroutineScope,
) : SettingsRepository {
    override val settings: Flow<Settings> = dataStore.data.map { it.toSettings() }.distinctUntilChanged()

    // Collected eagerly in the app-scoped [scope] so the stored values are ready before the first screen needs them.
    override val loaded: StateFlow<Settings?> = settings.catch {
    }.stateIn<Settings?>(scope, SharingStarted.Eagerly, null)

    override suspend fun update(transform: (Settings) -> Settings) {
        dataStore.edit { prefs -> prefs.write(transform(prefs.toSettings())) }
    }

    private fun Preferences.toSettings(): Settings {
        val defaults = Settings()
        return Settings(
            soundEffects = this[SOUND_EFFECTS] ?: defaults.soundEffects,
            haptics = this[HAPTICS] ?: defaults.haptics,
            speakOnCorrect = this[SPEAK_ON_CORRECT] ?: defaults.speakOnCorrect,
            classicTimeLimitSeconds = this[CLASSIC_TIME_LIMIT]
                ?.takeIf { it in config.classicTimeLimitOptionsSeconds }
                ?: defaults.classicTimeLimitSeconds,
            dailyReminder = this[DAILY_REMINDER] ?: defaults.dailyReminder,
            reminderMinuteOfDay = this[REMINDER_MINUTE]
                ?.takeIf { it in ReminderTimes.optionsMinuteOfDay }
                ?: defaults.reminderMinuteOfDay,
            // v1.0 stored a boolean: shown once, at an unknown time (0 = long ago, so the second ask may follow).
            reminderPrePromptAsks = this[REMINDER_PREPROMPT_ASKS]
                ?: if (this[REMINDER_PREPROMPT_SHOWN] == true) 1 else defaults.reminderPrePromptAsks,
            reminderPrePromptLastAskMs = this[REMINDER_PREPROMPT_LAST_ASK] ?: defaults.reminderPrePromptLastAskMs,
            dailyGoalWheels = this[DAILY_GOAL]?.takeIf { it >= 1 } ?: defaults.dailyGoalWheels,
            reviewReminders = this[REVIEW_REMINDERS] ?: defaults.reviewReminders,
            appLanguage = enumOrNull<UiLanguage>(this[UI_LANGUAGE]) ?: defaults.appLanguage,
            clueLanguage = enumOrNull<ClueLanguage>(this[CLUE_LANGUAGE]) ?: defaults.clueLanguage,
            accent = enumOrNull<Accent>(this[ACCENT]) ?: defaults.accent,
            analyticsConsent = this[ANALYTICS_CONSENT] ?: defaults.analyticsConsent,
            duelPlayerNames = DUEL_NAMES.map { key ->
                DuelNames.sanitize(this[key].orEmpty(), config.duelNameMaxLength)
            },
            duelLevel = enumOrNull<Level>(this[DUEL_LEVEL]) ?: defaults.duelLevel,
            duelClockSeconds = this[DUEL_CLOCK]
                ?.takeIf { it in config.duelClockOptionsSeconds }
                ?: defaults.duelClockSeconds,
        )
    }

    private fun MutablePreferences.write(settings: Settings) {
        this[SOUND_EFFECTS] = settings.soundEffects
        this[HAPTICS] = settings.haptics
        this[SPEAK_ON_CORRECT] = settings.speakOnCorrect
        this[CLASSIC_TIME_LIMIT] = settings.classicTimeLimitSeconds
        this[DAILY_REMINDER] = settings.dailyReminder
        this[REMINDER_MINUTE] = settings.reminderMinuteOfDay
        this[REMINDER_PREPROMPT_SHOWN] = settings.reminderPrePromptShown
        this[REMINDER_PREPROMPT_ASKS] = settings.reminderPrePromptAsks
        this[REMINDER_PREPROMPT_LAST_ASK] = settings.reminderPrePromptLastAskMs
        this[DAILY_GOAL] = settings.dailyGoalWheels
        this[REVIEW_REMINDERS] = settings.reviewReminders
        this[UI_LANGUAGE] = settings.uiLanguage.name
        this[CLUE_LANGUAGE] = settings.clueLanguage.name
        this[ACCENT] = settings.accent.name
        this[ANALYTICS_CONSENT] = settings.analyticsConsent
        DUEL_NAMES.forEachIndexed { index, key -> this[key] = settings.duelPlayerNames[index] }
        this[DUEL_LEVEL] = settings.duelLevel.name
        this[DUEL_CLOCK] = settings.duelClockSeconds
    }

    private inline fun <reified E : Enum<E>> enumOrNull(name: String?): E? =
        enumValues<E>().firstOrNull { it.name == name }

    private companion object {
        val SOUND_EFFECTS = booleanPreferencesKey("sound_effects")
        val HAPTICS = booleanPreferencesKey("haptics")
        val SPEAK_ON_CORRECT = booleanPreferencesKey("speak_on_correct")
        val CLASSIC_TIME_LIMIT = intPreferencesKey("classic_time_limit_seconds")
        val DAILY_REMINDER = booleanPreferencesKey("daily_reminder")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute_of_day")
        val REMINDER_PREPROMPT_SHOWN = booleanPreferencesKey("reminder_preprompt_shown")
        val REMINDER_PREPROMPT_ASKS = intPreferencesKey("reminder_preprompt_asks")
        val REMINDER_PREPROMPT_LAST_ASK = longPreferencesKey("reminder_preprompt_last_ask_ms")
        val DAILY_GOAL = intPreferencesKey("daily_goal_wheels")
        val REVIEW_REMINDERS = booleanPreferencesKey("review_reminders")
        val UI_LANGUAGE = stringPreferencesKey("ui_language")
        val CLUE_LANGUAGE = stringPreferencesKey("clue_language")
        val ACCENT = stringPreferencesKey("accent")
        val ANALYTICS_CONSENT = booleanPreferencesKey("analytics_consent")
        val DUEL_NAMES = listOf(stringPreferencesKey("duel_player_1_name"), stringPreferencesKey("duel_player_2_name"))
        val DUEL_LEVEL = stringPreferencesKey("duel_level")
        val DUEL_CLOCK = intPreferencesKey("duel_clock_seconds")
    }
}
