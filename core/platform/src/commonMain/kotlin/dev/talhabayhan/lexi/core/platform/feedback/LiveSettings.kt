package dev.talhabayhan.lexi.core.platform.feedback

import dev.talhabayhan.lexi.core.domain.repository.SettingsRepository
import dev.talhabayhan.lexi.core.domain.settings.Settings

/**
 * Synchronous view of the stored [Settings] for the feedback services, which are called from click handlers and
 * cannot suspend. Reads the repository's app-scoped state; the defaults (everything on) until DataStore delivers the
 * first value.
 */
public class LiveSettings(private val repository: SettingsRepository) {
    public val current: Settings get() = repository.loaded.value ?: Settings()
}
