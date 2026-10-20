package dev.talhabayhan.lexi.core.domain.repository

import dev.talhabayhan.lexi.core.domain.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/** Player settings (DataStore Preferences, `ARCHITECTURE.md` §6). */
public interface SettingsRepository {
    /** Current settings; defaults until changed. Invalid stored values fall back to the default. */
    public val settings: Flow<Settings>

    /**
     * The same settings as a state held for the whole app lifetime: null until the store has delivered its first
     * value, then always the stored value. Lets a screen start from the real values instead of defaults.
     */
    public val loaded: StateFlow<Settings?>

    /** Atomically applies [transform] to the stored settings. */
    public suspend fun update(transform: (Settings) -> Settings)
}
