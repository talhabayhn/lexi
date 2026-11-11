package dev.talhabayhan.lexi.core.domain.repository

import dev.talhabayhan.lexi.core.domain.model.Level
import kotlinx.coroutines.flow.Flow

/**
 * First-run onboarding state (`ARCHITECTURE.md` §2, `BACKLOG.md` row B4).
 * Stored in DataStore Preferences alongside settings. The flows never throw: a store that can't be read emits the
 * defaults (not completed, A1) and completes.
 */
public interface OnboardingRepository {
    /** True once the player has completed or skipped the first-run onboarding. */
    public val onboardingCompleted: Flow<Boolean>

    /** The chosen or default starting level. */
    public val selectedLevel: Flow<Level>

    /** Marks onboarding as completed. */
    public suspend fun setOnboardingCompleted(completed: Boolean = true)

    /** Saves the selected starting level. */
    public suspend fun saveSelectedLevel(level: Level)
}
