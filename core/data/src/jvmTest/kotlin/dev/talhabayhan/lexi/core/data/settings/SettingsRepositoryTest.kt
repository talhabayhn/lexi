package dev.talhabayhan.lexi.core.data.settings

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import app.cash.turbine.test
import dev.talhabayhan.lexi.core.domain.game.GameConfig
import dev.talhabayhan.lexi.core.domain.model.Level
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
        assertEquals(GameConfig().duelClockSeconds, Settings().duelClockSeconds)
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
            reminderPrePromptAsks = 2,
            reminderPrePromptLastAskMs = 1_700_000_000_000L,
            dailyGoalWheels = 3,
            reviewReminders = false,
            reminderShowWord = false,
            appLanguage = UiLanguage.EN,
            accent = Accent.UK,
            duelPlayerNames = listOf("Ayşe", ""),
            duelLevel = Level.B2,
            duelClockSeconds = 240,
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
            it[intPreferencesKey("duel_clock_seconds")] = 100
            it[stringPreferencesKey("duel_level")] = "Z9"
        }

        val settings = SettingsRepositoryImpl(dataStore, GameConfig(), backgroundScope).settings.first()

        assertEquals(Settings(accent = Accent.UK), settings)
    }

    @Test
    fun `duel names are stored per player and cut to the maximum length`() = runTest {
        val dataStore = createSettingsDataStore(path, backgroundScope)
        dataStore.edit {
            it[stringPreferencesKey("duel_player_1_name")] = "  Bir çok uzun isim burada  "
            it[stringPreferencesKey("duel_player_2_name")] = "Can"
        }

        val settings = SettingsRepositoryImpl(dataStore, GameConfig(), backgroundScope).settings.first()

        assertEquals(listOf("Bir çok uzun", "Can"), settings.duelPlayerNames)
    }

    @Test
    fun `reminder time outside the 08_00-21_30 options falls back to the default`() = runTest {
        val dataStore = createSettingsDataStore(path, backgroundScope)
        dataStore.edit { it[intPreferencesKey("reminder_minute_of_day")] = 22 * 60 }

        val settings = SettingsRepositoryImpl(dataStore, GameConfig(), backgroundScope).settings.first()

        assertEquals(Settings.DEFAULT_REMINDER_MINUTE_OF_DAY, settings.reminderMinuteOfDay)
    }

    @Test
    fun `reminder pre-prompt asks are counted and stored`() = runTest {
        val dataStore = createSettingsDataStore(path, backgroundScope)
        val repository = SettingsRepositoryImpl(dataStore, GameConfig(), backgroundScope)

        repository.recordReminderPrePromptAsked(5_000L)
        repository.recordReminderPrePromptAsked(6_000L)

        assertEquals(1, repository.settings.first().reminderPrePromptAsks)
        assertEquals(true, repository.settings.first().reminderPrePromptShown)
        assertEquals(true, dataStore.data.first()[booleanPreferencesKey("reminder_preprompt_shown")])
    }

    @Test
    fun `a v1_0 shown flag counts as one earlier ask`() = runTest {
        val dataStore = createSettingsDataStore(path, backgroundScope)
        dataStore.edit { it[booleanPreferencesKey("reminder_preprompt_shown")] = true }
        val repository = SettingsRepositoryImpl(dataStore, GameConfig(), backgroundScope)

        val settings = repository.settings.first { it.reminderPrePromptAsks > 0 }

        assertEquals(1, settings.reminderPrePromptAsks)
        assertEquals(0L, settings.reminderPrePromptLastAskMs)
    }
}
