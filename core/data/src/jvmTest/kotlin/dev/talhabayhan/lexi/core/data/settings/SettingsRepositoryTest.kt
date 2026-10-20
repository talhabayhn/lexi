package dev.talhabayhan.lexi.core.data.settings

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import app.cash.turbine.test
import dev.talhabayhan.lexi.core.domain.game.GameConfig
import dev.talhabayhan.lexi.core.domain.settings.Accent
import dev.talhabayhan.lexi.core.domain.settings.Settings
import dev.talhabayhan.lexi.core.domain.settings.UiLanguage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsRepositoryTest {
    private val dir: File = createTempDirectory("lexi-settings-").toFile()
    private val path = File(dir, "datastore/settings.preferences_pb").path

    @AfterTest
    fun cleanUp() {
        dir.deleteRecursively()
    }

    @Test
    fun `defaults come from the design until something is stored`() = runTest {
        val repository =
            SettingsRepositoryImpl(createSettingsDataStore(path, backgroundScope), GameConfig(), backgroundScope)

        assertEquals(Settings(), repository.settings.first())
        assertEquals(180, Settings().classicTimeLimitSeconds)
        assertEquals(20 * 60, Settings().reminderMinuteOfDay)
    }

    @Test
    fun `loaded holds the stored value once the store has delivered`() = runTest {
        val repository =
            SettingsRepositoryImpl(createSettingsDataStore(path, backgroundScope), GameConfig(), backgroundScope)
        repository.update { it.copy(dailyGoalWheels = 5) }

        assertEquals(Settings(dailyGoalWheels = 5), repository.loaded.first { it != null })
    }

    @Test
    fun `updates are stored and emitted`() = runTest {
        val repository =
            SettingsRepositoryImpl(createSettingsDataStore(path, backgroundScope), GameConfig(), backgroundScope)
        val changed = Settings(
            soundEffects = false,
            haptics = false,
            speakOnCorrect = false,
            classicTimeLimitSeconds = 240,
            dailyReminder = true,
            reminderMinuteOfDay = 21 * 60 + 30,
            reminderPrePromptShown = true,
            dailyGoalWheels = 3,
            reviewReminders = false,
            appLanguage = UiLanguage.EN,
            accent = Accent.UK,
        )

        repository.settings.test {
            assertEquals(Settings(), awaitItem())
            repository.update { changed }
            assertEquals(changed, awaitItem())
            repository.update { it.copy(classicTimeLimitSeconds = 120) }
            assertEquals(changed.copy(classicTimeLimitSeconds = 120), awaitItem())
        }
    }

    @Test
    fun `invalid stored values fall back to their defaults`() = runTest {
        val dataStore = createSettingsDataStore(path, backgroundScope)
        dataStore.edit {
            it[intPreferencesKey("classic_time_limit_seconds")] = 999
            it[intPreferencesKey("reminder_minute_of_day")] = 7 * 60 + 15
            it[intPreferencesKey("daily_goal_wheels")] = 0
            it[stringPreferencesKey("ui_language")] = "KLINGON"
            it[stringPreferencesKey("accent")] = "UK"
        }

        val settings = SettingsRepositoryImpl(dataStore, GameConfig(), backgroundScope).settings.first()

        assertEquals(Settings(accent = Accent.UK), settings)
    }

    @Test
    fun `reminder time outside the 08_00-21_30 options falls back to the default`() = runTest {
        val dataStore = createSettingsDataStore(path, backgroundScope)
        dataStore.edit { it[intPreferencesKey("reminder_minute_of_day")] = 22 * 60 }

        val settings = SettingsRepositoryImpl(dataStore, GameConfig(), backgroundScope).settings.first()

        assertEquals(Settings.DEFAULT_REMINDER_MINUTE_OF_DAY, settings.reminderMinuteOfDay)
    }

    @Test
    fun `reminder pre-prompt flag is stored under reminder_preprompt_shown`() = runTest {
        val dataStore = createSettingsDataStore(path, backgroundScope)
        val repository = SettingsRepositoryImpl(dataStore, GameConfig(), backgroundScope)

        repository.setReminderPrePromptShown()

        assertEquals(true, dataStore.data.first()[booleanPreferencesKey("reminder_preprompt_shown")])
        assertEquals(true, repository.settings.first().reminderPrePromptShown)
    }
}
