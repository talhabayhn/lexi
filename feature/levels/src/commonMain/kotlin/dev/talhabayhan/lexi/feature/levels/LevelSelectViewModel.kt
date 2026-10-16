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
import kotlinx.coroutines.flow.collectLatest
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
    private val progress: ProgressRepository,
    private val settings: SettingsRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(LevelSelectUiState())
    val state: StateFlow<LevelSelectUiState> = mutableState.asStateFlow()

    private val effectChannel = Channel<LevelSelectEffect>(Channel.BUFFERED)
    val effects: Flow<LevelSelectEffect> = effectChannel.receiveAsFlow()

    /** Bumped by "Tekrar dene": reads the content again. */
    private val reloads = MutableStateFlow(0)

    init {
        viewModelScope.launch {
            reloads.collectLatest { observe(loadContent()) }
        }
    }

    /** The level progress and the Classic time; [content] null = the content could not be read (zeros). */
    private suspend fun observe(content: Pair<Map<Level, Int>, Map<CardId, Level>>?) {
        mutableState.update { it.copy(hasError = content == null) }
        val (totals, levelOf) = content ?: (emptyMap<Level, Int>() to emptyMap())
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
            // A broken progress read keeps the last numbers (zeros at first); the screen itself still works.
            .catch { }
            .collect { (levels, classicSeconds) ->
                mutableState.update { it.copy(levels = levels, classicTimeLimitSeconds = classicSeconds) }
            }
    }

    /**
     * Card totals per level and the level of every card. Content never changes while the app runs, so both are read
     * once; null when the content cannot be read.
     */
    private suspend fun loadContent(): Pair<Map<Level, Int>, Map<CardId, Level>>? = try {
        val totals = cards.levelTotals()
        totals to cards.levelCards().associate { it.id to it.level }
    } catch (e: CancellationException) {
        throw e
    } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
        // TODO(M7): report to the crash reporter once it is behind its interface.
        null
    }

    fun onIntent(intent: LevelSelectIntent) {
        when (intent) {
            is LevelSelectIntent.SelectLevel -> mutableState.update { it.copy(selectedLevel = intent.level) }

            is LevelSelectIntent.SelectMode -> mutableState.update { it.copy(selectedMode = intent.mode) }

            LevelSelectIntent.Start -> {
                val current = mutableState.value
                effectChannel.trySend(LevelSelectEffect.StartGame(current.selectedLevel, current.selectedMode))
            }

            LevelSelectIntent.OpenCategories -> {
                val current = mutableState.value
                effectChannel.trySend(LevelSelectEffect.OpenCategories(current.selectedLevel, current.selectedMode))
            }

            LevelSelectIntent.Back -> effectChannel.trySend(LevelSelectEffect.NavigateBack)

            LevelSelectIntent.Retry -> if (mutableState.value.hasError) reloads.update { it + 1 }
        }
    }
}
