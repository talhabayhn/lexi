package dev.talhabayhan.lexi.core.platform.feedback

import dev.talhabayhan.lexi.core.domain.repository.SettingsRepository
import dev.talhabayhan.lexi.core.domain.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * Synchronous view of the stored [Settings] for the feedback services, which are called from click handlers and
 * cannot suspend. Holds the defaults (everything on) until DataStore delivers the first value.
 */
public class LiveSettings(repository: SettingsRepository, private val scope: CoroutineScope) : AutoCloseable {
    private val state: StateFlow<Settings> = repository.settings.stateIn(scope, SharingStarted.Eagerly, Settings())

    public val current: Settings get() = state.value

    override fun close() {
        scope.cancel()
    }
}
