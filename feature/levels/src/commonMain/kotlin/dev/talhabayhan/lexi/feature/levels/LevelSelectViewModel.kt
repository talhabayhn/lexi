package dev.talhabayhan.lexi.feature.levels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.repository.CardRepository
import dev.talhabayhan.lexi.core.domain.repository.ProgressRepository
import dev.talhabayhan.lexi.core.domain.repository.SettingsRepository
import dev.talhabayhan.lexi.core.domain.stats.Stats
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Level and mode select (`03-level-select`): card totals and the player's progress per level, the Classic time from
 * settings, the selection, and the start of the game as an effect.
 */
internal class LevelSelectViewModel(
    private val cards: CardRepository,
    progress: ProgressRepository,
    settings: SettingsRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(LevelSelectUiState())
    val state: StateFlow<LevelSelectUiState> = mutableState.asStateFlow()

    private val effectChannel = Channel<LevelSelectEffect>(Channel.BUFFERED)
    val effects: Flow<LevelSelectEffect> = effectChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            val (totals, levelOf) = loadContent()
            combine(progress.observeAll(), settings.settings) { answered, current ->
                val byLevel = Stats.levelProgress(answered, levelOf, totals)
                Pair(
                    Level.entries.map { level ->
                        val entry = byLevel[level]
                        LevelProgressUi(level, entry?.learned ?: 0, entry?.total ?: 0)
                    }.toImmutableList(),
                    current.classicTimeLimitSeconds,
                )
            }
                // A broken read keeps the last numbers (zeros at first); the screen itself still works.
                .catch { }
                .collect { (levels, classicSeconds) ->
                    mutableState.update { it.copy(levels = levels, classicTimeLimitSeconds = classicSeconds) }
                }
        }
    }

    /**
     * Card totals per level and the level of every card. Content never changes while the app runs, so both are read
     * once; empty when the content cannot be read.
     */
    private suspend fun loadContent(): Pair<Map<Level, Int>, Map<CardId, Level>> = try {
        val totals = cards.levelTotals()
        totals to cards.levelCards().associate { it.id to it.level }
    } catch (e: CancellationException) {
        throw e
    } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
        emptyMap<Level, Int>() to emptyMap()
    }

    fun onIntent(intent: LevelSelectIntent) {
        when (intent) {
            is LevelSelectIntent.SelectLevel -> mutableState.update { it.copy(selectedLevel = intent.level) }

            is LevelSelectIntent.SelectMode -> mutableState.update { it.copy(selectedMode = intent.mode) }

            LevelSelectIntent.Start -> {
                val current = mutableState.value
                effectChannel.trySend(LevelSelectEffect.StartGame(current.selectedLevel, current.selectedMode))
            }

            LevelSelectIntent.Back -> effectChannel.trySend(LevelSelectEffect.NavigateBack)
        }
    }
}
