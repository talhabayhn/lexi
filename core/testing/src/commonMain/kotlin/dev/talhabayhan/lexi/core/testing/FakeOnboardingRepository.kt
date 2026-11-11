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

    /** While true, every write throws [IllegalStateException] without storing (a full disk; failure injection). */
    public var failingWrites: Boolean = false

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        check(!failingWrites) { "injected onboarding write failure" }
        _onboardingCompleted.value = completed
    }

    override suspend fun saveSelectedLevel(level: Level) {
        check(!failingWrites) { "injected onboarding write failure" }
        _selectedLevel.value = level
    }
}
