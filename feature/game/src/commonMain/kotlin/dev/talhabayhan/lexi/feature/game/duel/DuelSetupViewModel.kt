package dev.talhabayhan.lexi.feature.game.duel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import dev.talhabayhan.lexi.core.domain.game.DuelNames
import dev.talhabayhan.lexi.core.domain.game.GameConfig
import dev.talhabayhan.lexi.core.domain.repository.SettingsRepository
import dev.talhabayhan.lexi.core.domain.telemetry.CrashReporter
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The duel setup (`17-duel-setup`, `GAME_RULES.md` §8): starts from the last match's names, level and clock (settings,
 * DataStore); BAŞLA stores them for the next match and starts the duel. Names are trimmed and cut to
 * `GameConfig.duelNameMaxLength`; an empty name is the default ("1. Oyuncu").
 */
internal class DuelSetupViewModel(
    private val settings: SettingsRepository,
    private val config: GameConfig,
    private val crashReporter: CrashReporter,
) : ViewModel() {
    private val mutableState = MutableStateFlow(
        DuelSetupUiState(
            clockSeconds = config.duelClockSeconds,
            clockOptionsSeconds = config.duelClockOptionsSeconds.toImmutableList(),
            nameMaxLength = config.duelNameMaxLength,
        ),
    )
    val state: StateFlow<DuelSetupUiState> = mutableState.asStateFlow()

    private val effectChannel = Channel<DuelSetupEffect>(Channel.BUFFERED)
    val effects: Flow<DuelSetupEffect> = effectChannel.receiveAsFlow()

    private var starting = false

    init {
        viewModelScope.launch {
            val stored = try {
                settings.settings.first()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                log.e(e) { "duel setup not read" }
                null
            }
            mutableState.update { current ->
                if (stored == null) {
                    current.copy(isLoaded = true)
                } else {
                    current.copy(
                        isLoaded = true,
                        names = stored.duelPlayerNames.map(::clean).toImmutableList(),
                        level = stored.duelLevel,
                        clockSeconds = stored.duelClockSeconds.takeIf { it in config.duelClockOptionsSeconds }
                            ?: config.duelClockSeconds,
                    )
                }
            }
        }
    }

    fun onIntent(intent: DuelSetupIntent) {
        // Until the last match's setup is read, choices would be overwritten by it: only back is taken.
        if (!state.value.isLoaded && intent != DuelSetupIntent.Back) return
        when (intent) {
            is DuelSetupIntent.EditName -> mutableState.update {
                if (intent.player !in it.names.indices) {
                    it
                } else {
                    it.copy(editingPlayer = intent.player, nameDraft = it.names[intent.player])
                }
            }

            is DuelSetupIntent.NameChanged -> mutableState.update {
                if (it.editingPlayer == null) it else it.copy(nameDraft = cut(intent.text))
            }

            DuelSetupIntent.SaveName -> mutableState.update {
                val player = it.editingPlayer ?: return@update it
                val names = it.names.toMutableList().also { list -> list[player] = clean(it.nameDraft) }
                it.copy(names = names.toImmutableList(), editingPlayer = null, nameDraft = "")
            }

            DuelSetupIntent.CancelName -> mutableState.update { it.copy(editingPlayer = null, nameDraft = "") }

            is DuelSetupIntent.SelectLevel -> mutableState.update { it.copy(level = intent.level) }

            is DuelSetupIntent.SelectClock -> mutableState.update {
                if (intent.seconds in config.duelClockOptionsSeconds) it.copy(clockSeconds = intent.seconds) else it
            }

            DuelSetupIntent.Start -> start()

            DuelSetupIntent.Back -> if (state.value.editingPlayer != null) {
                onIntent(DuelSetupIntent.CancelName)
            } else {
                effectChannel.trySend(DuelSetupEffect.NavigateBack)
            }
        }
    }

    /** Stores the setup (a storage failure is reported, the duel starts anyway) and starts once. */
    private fun start() {
        val current = state.value
        if (!current.isLoaded || current.editingPlayer != null || starting) return
        starting = true
        viewModelScope.launch {
            withContext(NonCancellable) {
                try {
                    settings.update {
                        it.copy(
                            duelPlayerNames = current.names,
                            duelLevel = current.level,
                            duelClockSeconds = current.clockSeconds,
                        )
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    log.e(e) { "duel setup not saved" }
                    crashReporter.recordException(e, "duel setup not saved")
                }
            }
            effectChannel.send(DuelSetupEffect.StartDuel(DuelRoute(current.level, current.clockSeconds, current.names)))
            starting = false
        }
    }

    /** While typing: cut to the maximum length, otherwise as typed (trimmed on save). */
    private fun cut(text: String): String = DuelNames.cut(text, config.duelNameMaxLength)

    private fun clean(name: String): String = DuelNames.sanitize(name, config.duelNameMaxLength)

    private companion object {
        val log = Logger.withTag("DuelSetupViewModel")
    }
}
