package dev.talhabayhan.lexi.core.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dev.talhabayhan.lexi.core.domain.game.GameConfig
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import java.io.File
import java.io.IOException
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/** A settings file that can't be read never crashes a collector: reads fall back to the defaults. */
class SettingsStorageFailureTest {
    private val dir: File = createTempDirectory("lexi-settings-broken-").toFile()
    private val path = File(dir, "datastore/settings.preferences_pb").path

    @AfterTest
    fun cleanUp() {
        dir.deleteRecursively()
    }

    /** A store whose every read fails, like a file that became unreadable. */
    private object UnreadableStore : DataStore<Preferences> {
        override val data: Flow<Preferences> = flow { throw IOException("injected read failure") }

        override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences =
            throw IOException("injected write failure")
    }

    private fun writeCorruptedFile() {
        File(path).apply { parentFile.mkdirs() }.writeBytes(ByteArray(64) { 0xFF.toByte() })
    }

    @Test
    fun `a corrupted settings file is replaced by the defaults`() = runTest {
        writeCorruptedFile()
        val dataStore = createSettingsDataStore(path, backgroundScope)
        val repository = SettingsRepositoryImpl(dataStore, GameConfig(), backgroundScope)

        assertEquals(Settings(), repository.settings.first())
        repository.update { it.copy(dailyGoalWheels = 4) }
        assertEquals(4, repository.settings.first().dailyGoalWheels)
    }

    @Test
    fun `a corrupted settings file reads onboarding as not completed`() = runTest {
        writeCorruptedFile()
        val repository = OnboardingRepositoryImpl(createSettingsDataStore(path, backgroundScope))

        assertFalse(repository.onboardingCompleted.first())
        assertEquals(Level.A1, repository.selectedLevel.first())
    }

    @Test
    fun `an unreadable store reads as the defaults instead of throwing`() = runTest {
        val repository = SettingsRepositoryImpl(UnreadableStore, GameConfig(), backgroundScope)

        assertEquals(listOf(Settings()), repository.settings.toList())
        assertEquals(Settings(), repository.loaded.first { it != null })
    }

    @Test
    fun `an unreadable store reads onboarding as the defaults instead of throwing`() = runTest {
        val repository = OnboardingRepositoryImpl(UnreadableStore)

        assertEquals(listOf(false), repository.onboardingCompleted.toList())
        assertEquals(listOf(Level.A1), repository.selectedLevel.toList())
    }
}
