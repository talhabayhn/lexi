package dev.talhabayhan.lexi.core.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.talhabayhan.lexi.core.domain.game.GameConfig
import dev.talhabayhan.lexi.core.domain.repository.SettingsRepository
import dev.talhabayhan.lexi.core.domain.settings.Accent
import dev.talhabayhan.lexi.core.domain.settings.ClueLanguage
import dev.talhabayhan.lexi.core.domain.settings.Settings
import dev.talhabayhan.lexi.core.domain.settings.UiLanguage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath

/** DataStore Preferences file at [path] (absolute; see `DatabaseFiles.settingsFilePath`). One per process. */
public fun createSettingsDataStore(path: String, scope: CoroutineScope): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(scope = scope, produceFile = { path.toPath() })

/**
 * [Settings] in DataStore Preferences. Keys are stable (renaming one loses the stored value); invalid stored values
 * (e.g. a removed time option or enum entry) read as the default.
 */
internal class SettingsRepositoryImpl(private val dataStore: DataStore<Preferences>, private val config: GameConfig) :
    SettingsRepository {
    override val settings: Flow<Settings> = dataStore.data.map { it.toSettings() }.distinctUntilChanged()

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
                ?.takeIf { it in 0 until Settings.MINUTES_PER_DAY }
                ?: defaults.reminderMinuteOfDay,
            dailyGoalWheels = this[DAILY_GOAL]?.takeIf { it >= 1 } ?: defaults.dailyGoalWheels,
            reviewReminders = this[REVIEW_REMINDERS] ?: defaults.reviewReminders,
            uiLanguage = enumOrNull<UiLanguage>(this[UI_LANGUAGE]) ?: defaults.uiLanguage,
            clueLanguage = enumOrNull<ClueLanguage>(this[CLUE_LANGUAGE]) ?: defaults.clueLanguage,
            accent = enumOrNull<Accent>(this[ACCENT]) ?: defaults.accent,
        )
    }

    private fun MutablePreferences.write(settings: Settings) {
        this[SOUND_EFFECTS] = settings.soundEffects
        this[HAPTICS] = settings.haptics
        this[SPEAK_ON_CORRECT] = settings.speakOnCorrect
        this[CLASSIC_TIME_LIMIT] = settings.classicTimeLimitSeconds
        this[DAILY_REMINDER] = settings.dailyReminder
        this[REMINDER_MINUTE] = settings.reminderMinuteOfDay
        this[DAILY_GOAL] = settings.dailyGoalWheels
        this[REVIEW_REMINDERS] = settings.reviewReminders
        this[UI_LANGUAGE] = settings.uiLanguage.name
        this[CLUE_LANGUAGE] = settings.clueLanguage.name
        this[ACCENT] = settings.accent.name
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
        val DAILY_GOAL = intPreferencesKey("daily_goal_wheels")
        val REVIEW_REMINDERS = booleanPreferencesKey("review_reminders")
        val UI_LANGUAGE = stringPreferencesKey("ui_language")
        val CLUE_LANGUAGE = stringPreferencesKey("clue_language")
        val ACCENT = stringPreferencesKey("accent")
    }
}
