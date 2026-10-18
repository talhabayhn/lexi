package dev.talhabayhan.lexi.core.testing

import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** In-memory [OnboardingRepository] for unit tests. */
public class FakeOnboardingRepository(initialCompleted: Boolean = false, initialLevel: Level = Level.A1) :
    OnboardingRepository {
    private val _onboardingCompleted: MutableStateFlow<Boolean> = MutableStateFlow(initialCompleted)
    override val onboardingCompleted: Flow<Boolean> = _onboardingCompleted.asStateFlow()

    private val _selectedLevel: MutableStateFlow<Level> = MutableStateFlow(initialLevel)
    override val selectedLevel: Flow<Level> = _selectedLevel.asStateFlow()

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        _onboardingCompleted.value = completed
    }

    override suspend fun saveSelectedLevel(level: Level) {
        _selectedLevel.value = level
    }
}
