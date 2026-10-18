package dev.talhabayhan.lexi.core.data.settings

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
        val repository = SettingsRepositoryImpl(createSettingsDataStore(path, backgroundScope), GameConfig())

        assertEquals(Settings(), repository.settings.first())
        assertEquals(180, Settings().classicTimeLimitSeconds)
        assertEquals(20 * 60, Settings().reminderMinuteOfDay)
    }

    @Test
    fun `updates are stored and emitted`() = runTest {
        val repository = SettingsRepositoryImpl(createSettingsDataStore(path, backgroundScope), GameConfig())
        val changed = Settings(
            soundEffects = false,
            haptics = false,
            speakOnCorrect = false,
            classicTimeLimitSeconds = 240,
            dailyReminder = true,
            reminderMinuteOfDay = 7 * 60 + 30,
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
            it[intPreferencesKey("reminder_minute_of_day")] = -5
            it[intPreferencesKey("daily_goal_wheels")] = 0
            it[stringPreferencesKey("ui_language")] = "KLINGON"
            it[stringPreferencesKey("accent")] = "UK"
        }

        val settings = SettingsRepositoryImpl(dataStore, GameConfig()).settings.first()

        assertEquals(Settings(accent = Accent.UK), settings)
    }
}
