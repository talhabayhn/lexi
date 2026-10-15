package dev.talhabayhan.lexi.core.testing

import dev.talhabayhan.lexi.core.domain.repository.SettingsRepository
import dev.talhabayhan.lexi.core.domain.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** In-memory [SettingsRepository]; [settings] always holds the stored value. */
public class FakeSettingsRepository(initial: Settings = Settings()) : SettingsRepository {
    private val state = MutableStateFlow(initial)

    override val settings: StateFlow<Settings> = state.asStateFlow()

    override suspend fun update(transform: (Settings) -> Settings) {
        state.update(transform)
    }
}
