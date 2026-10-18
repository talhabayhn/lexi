package dev.talhabayhan.lexi.core.data.settings

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import app.cash.turbine.test
import dev.talhabayhan.lexi.core.domain.model.Level
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OnboardingRepositoryTest {
    private val dir: File = createTempDirectory("lexi-onboarding-").toFile()
    private val path = File(dir, "datastore/settings.preferences_pb").path

    @AfterTest
    fun cleanUp() {
        dir.deleteRecursively()
    }

    @Test
    fun `defaults are false and A1 until stored`() = runTest {
        val repository = OnboardingRepositoryImpl(createSettingsDataStore(path, backgroundScope))

        assertFalse(repository.onboardingCompleted.first())
        assertEquals(Level.A1, repository.selectedLevel.first())
    }

    @Test
    fun `completion flag is stored and emitted`() = runTest {
        val repository = OnboardingRepositoryImpl(createSettingsDataStore(path, backgroundScope))

        repository.onboardingCompleted.test {
            assertFalse(awaitItem())
            repository.setOnboardingCompleted(true)
            assertTrue(awaitItem())
        }
    }

    @Test
    fun `selected level is stored and emitted`() = runTest {
        val repository = OnboardingRepositoryImpl(createSettingsDataStore(path, backgroundScope))

        repository.selectedLevel.test {
            assertEquals(Level.A1, awaitItem())
            repository.saveSelectedLevel(Level.B2)
            assertEquals(Level.B2, awaitItem())
        }
    }

    @Test
    fun `invalid stored level falls back to A1`() = runTest {
        val dataStore = createSettingsDataStore(path, backgroundScope)
        dataStore.edit {
            it[stringPreferencesKey("selected_level")] = "INVALID_LEVEL"
        }

        val repository = OnboardingRepositoryImpl(dataStore)
        assertEquals(Level.A1, repository.selectedLevel.first())
    }
}
