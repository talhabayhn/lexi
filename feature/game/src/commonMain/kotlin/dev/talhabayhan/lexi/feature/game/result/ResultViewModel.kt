package dev.talhabayhan.lexi.feature.game.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import dev.talhabayhan.lexi.core.designsystem.component.ResultDefaults
import dev.talhabayhan.lexi.core.designsystem.component.ReviewOutcome
import dev.talhabayhan.lexi.core.domain.feedback.Speaker
import dev.talhabayhan.lexi.core.domain.game.GameConfig
import dev.talhabayhan.lexi.core.domain.model.Card
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.repository.CardRepository
import dev.talhabayhan.lexi.feature.game.GameRoute
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The result of one finished game (`06-result`, ADR 0019 addendum): counts come from the [route], the review rows
 * ("Tekrar edilecekler") are the route's missed cards read from the content. They are exactly the cards `FinishGame`
 * put into box 0 (`GAME_RULES.md` §10): wrong and passed slots, nothing unanswered. Navigation intents are handled by
 * the route composable.
 */
internal class ResultViewModel(
    private val route: ResultRoute,
    private val cards: CardRepository,
    private val speaker: Speaker,
    config: GameConfig,
) : ViewModel() {
    private val mutableState = MutableStateFlow(initialState(route, config))
    val state: StateFlow<ResultUiState> = mutableState.asStateFlow()

    /** The game behind TEKRAR OYNA: same level, mode and category. */
    // TODO(M6): the Daily wheel allows one scored attempt per day (GAME_RULES.md §5), so a finished Daily must not be
    //  replayable from here (hide or redirect TEKRAR OYNA).
    val playAgain: GameRoute get() = GameRoute(route.gameLevel, route.gameMode, route.categoryId)

    private var allRows: List<ReviewRowUi> = emptyList()
    private var expanded = false

    init {
        viewModelScope.launch { load() }
    }

    fun onIntent(intent: ResultIntent) {
        when (intent) {
            ResultIntent.ShowAll -> {
                expanded = true
                publishRows()
            }

            is ResultIntent.Speak -> speaker.speak(intent.word)

            // Navigation (route composable) and the share card (M6).
            ResultIntent.PlayAgain, ResultIntent.Share, ResultIntent.Home -> Unit
        }
    }

    override fun onCleared() {
        speaker.stop()
    }

    private suspend fun load() {
        val ids = route.missedCards.map(::CardId)
        val byId = try {
            cards.cards(ids).associateBy { it.id }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // TODO(M7): report through the crash reporter. The counts are still shown, the list stays empty.
            log.e(e) { "review cards not loaded" }
            emptyMap()
        }
        val passed = route.passedCards.toSet()
        // A card that left the content since the game is skipped (its progress is kept, CONTENT.md §8).
        allRows = ids.mapNotNull { id -> byId[id]?.toRow(passed = id.value in passed) }
        publishRows()
    }

    private fun publishRows() {
        val visible = if (expanded || allRows.size <= ResultDefaults.COLLAPSED_REVIEW_ROWS + 1) {
            allRows
        } else {
            allRows.take(ResultDefaults.COLLAPSED_REVIEW_ROWS)
        }
        mutableState.update {
            it.copy(rows = visible.toImmutableList(), hiddenRows = allRows.size - visible.size, isLoading = false)
        }
    }

    private companion object {
        const val MS_PER_SECOND = 1_000L
        val log = Logger.withTag("ResultViewModel")

        fun initialState(route: ResultRoute, config: GameConfig): ResultUiState {
            val mode = route.gameMode
            val stored = route.recorded && mode !in config.srsExcludedModes
            return ResultUiState(
                level = route.gameLevel,
                mode = mode,
                reason = route.gameOverReason,
                score = route.score,
                longestStreak = route.longestStreak,
                remainingSeconds = route.remainingMs?.let { ((it + MS_PER_SECOND - 1) / MS_PER_SECOND).toInt() },
                correct = route.correct,
                wrong = route.wrong,
                passed = route.passed,
                addedToNotebook = if (stored) route.missedCards.size else 0,
            )
        }

        fun Card.toRow(passed: Boolean) = ReviewRowUi(
            letter = letter,
            word = answer.lowercase(),
            clue = clue,
            example = example,
            outcome = if (passed) ReviewOutcome.Passed else ReviewOutcome.Wrong,
        )
    }
}
