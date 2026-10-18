package dev.talhabayhan.lexi.core.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal class OnboardingRepositoryImpl(private val dataStore: DataStore<Preferences>) : OnboardingRepository {

    override val onboardingCompleted: Flow<Boolean> = dataStore.data
        .map { it[ONBOARDING_COMPLETED] ?: false }
        .distinctUntilChanged()

    override val selectedLevel: Flow<Level> = dataStore.data
        .map { prefs ->
            prefs[SELECTED_LEVEL]?.let { name ->
                Level.entries.firstOrNull { it.name == name }
            } ?: Level.A1
        }
        .distinctUntilChanged()

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { prefs ->
            prefs[ONBOARDING_COMPLETED] = completed
        }
    }

    override suspend fun saveSelectedLevel(level: Level) {
        dataStore.edit { prefs ->
            prefs[SELECTED_LEVEL] = level.name
        }
    }

    private companion object {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val SELECTED_LEVEL = stringPreferencesKey("selected_level")
    }
}
