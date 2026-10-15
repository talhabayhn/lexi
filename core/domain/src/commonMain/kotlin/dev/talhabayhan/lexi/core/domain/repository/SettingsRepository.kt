package dev.talhabayhan.lexi.core.domain.repository

import dev.talhabayhan.lexi.core.domain.settings.Settings
import kotlinx.coroutines.flow.Flow

/** Player settings (DataStore Preferences, `ARCHITECTURE.md` §6). */
public interface SettingsRepository {
    /** Current settings; defaults until changed. Invalid stored values fall back to the default. */
    public val settings: Flow<Settings>

    /** Atomically applies [transform] to the stored settings. */
    public suspend fun update(transform: (Settings) -> Settings)
}
