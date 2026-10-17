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
import dev.talhabayhan.lexi.core.domain.repository.ProgressRepository
import dev.talhabayhan.lexi.core.domain.srs.CardProgress
import dev.talhabayhan.lexi.core.domain.time.AppClock
import dev.talhabayhan.lexi.feature.game.GameRoute
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The result of one finished game (`06-result`, ADR 0019 addendum): counts come from the [route], the review rows
 * are listed in 4 groups (Doğru, Yanlış, Pas, Süre bitti). Wrong and Passed are automatically in the notebook;
 * Süre bitti cards can be added manually via [ResultIntent.AddToNotebook]. Navigation intents are handled by
 * the route composable.
 */
internal class ResultViewModel(
    private val route: ResultRoute,
    private val cards: CardRepository,
    private val speaker: Speaker,
    config: GameConfig,
    private val progress: ProgressRepository,
    private val clock: AppClock,
) : ViewModel() {
    private val mutableState = MutableStateFlow(initialState(route, config))
    val state: StateFlow<ResultUiState> = mutableState.asStateFlow()

    /**
     * The game behind TEKRAR OYNA: same level, mode and category. Null for a finished Daily: it allows one scored
     * attempt per day (`GAME_RULES.md` §5), so the button is hidden ([ResultUiState.canPlayAgain]).
     */
    val playAgain: GameRoute?
        get() = if (mutableState.value.canPlayAgain) {
            GameRoute(route.gameLevel, route.gameMode, route.categoryId)
        } else {
            null
        }

    private var allGroups: List<ReviewGroupUi> = emptyList()
    private var allRows: List<ReviewRowUi> = emptyList()

    /** The running [load], so a double tap on "Tekrar dene" reads once. */
    private var loading: Job? = null

    init {
        loading = viewModelScope.launch { load() }
    }

    fun onIntent(intent: ResultIntent) {
        when (intent) {
            ResultIntent.ShowAll -> publishRows()

            is ResultIntent.Speak -> speaker.speak(intent.word)

            ResultIntent.Retry -> if (mutableState.value.reviewError && loading?.isActive != true) {
                mutableState.update { it.copy(isLoading = true, reviewError = false) }
                loading = viewModelScope.launch { load() }
            }

            is ResultIntent.AddToNotebook -> {
                viewModelScope.launch {
                    val cardId = CardId(intent.cardId)
                    val today = clock.epochDay(clock.nowMs())
                    val existing = try {
                        progress.get(listOf(cardId))[cardId]
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        null
                    }
                    val updated = (existing ?: CardProgress(cardId)).copy(
                        box = 0,
                        dueEpochDay = today,
                        mastered = false,
                    )
                    try {
                        progress.save(listOf(updated))
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        log.e(e) { "could not save progress for ${intent.cardId}" }
                        return@launch
                    }
                    allGroups = allGroups.map { group ->
                        if (group.outcome == ReviewOutcome.TimeUp) {
                            group.copy(
                                rows = group.rows.map { row ->
                                    if (row.cardId == intent.cardId) {
                                        row.copy(isAddedToNotebook = true)
                                    } else {
                                        row
                                    }
                                }.toImmutableList(),
                            )
                        } else {
                            group
                        }
                    }
                    allRows = allGroups.flatMap { it.rows }
                    mutableState.update { current ->
                        current.copy(
                            groups = allGroups.toImmutableList(),
                            rows = allRows.toImmutableList(),
                            addedToNotebook = current.addedToNotebook + 1,
                        )
                    }
                }
            }

            // Navigation (route composable) and the share card (M6).
            ResultIntent.PlayAgain, ResultIntent.Share, ResultIntent.Home -> Unit
        }
    }

    override fun onCleared() {
        speaker.stop()
    }

    private suspend fun load() {
        val passedSet = route.passedCards.toSet()
        val correctList = route.correctCards
        val wrongList = if (route.wrongCards.isNotEmpty()) {
            route.wrongCards
        } else {
            route.missedCards.filter { it !in passedSet }
        }
        val passedList = route.passedCards
        val timeUpList = route.timeUpCards

        val allIds = (correctList + wrongList + passedList + timeUpList).ifEmpty {
            route.missedCards
        }.map(::CardId)

        if (allIds.isEmpty()) {
            mutableState.update { it.copy(isLoading = false) }
            return
        }

        val byId = try {
            cards.cards(allIds).associateBy { it.id }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // TODO(M7): report through the crash reporter. The counts are still shown; the list offers a retry.
            log.e(e) { "review cards not loaded" }
            mutableState.update { it.copy(isLoading = false, reviewError = true) }
            return
        }

        val timeUpProgress = if (timeUpList.isNotEmpty()) {
            try {
                progress.get(timeUpList.map(::CardId))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                log.w(e) { "could not read progress for time-up cards" }
                emptyMap()
            }
        } else {
            emptyMap()
        }

        val correctRows = correctList.mapNotNull { id ->
            byId[CardId(id)]?.toRow(ReviewOutcome.Correct)
        }
        val wrongRows = wrongList.mapNotNull { id ->
            byId[CardId(id)]?.toRow(ReviewOutcome.Wrong)
        }
        val passedRows = passedList.mapNotNull { id ->
            byId[CardId(id)]?.toRow(ReviewOutcome.Passed)
        }
        val timeUpRows = timeUpList.mapNotNull { id ->
            val cardId = CardId(id)
            val inNotebook = timeUpProgress[cardId]?.inNotebook == true
            byId[cardId]?.toRow(ReviewOutcome.TimeUp, isAddedToNotebook = inNotebook)
        }

        val groupList = buildList {
            if (correctRows.isNotEmpty()) add(ReviewGroupUi(ReviewOutcome.Correct, correctRows.toImmutableList()))
            if (wrongRows.isNotEmpty()) add(ReviewGroupUi(ReviewOutcome.Wrong, wrongRows.toImmutableList()))
            if (passedRows.isNotEmpty()) add(ReviewGroupUi(ReviewOutcome.Passed, passedRows.toImmutableList()))
            if (timeUpRows.isNotEmpty()) add(ReviewGroupUi(ReviewOutcome.TimeUp, timeUpRows.toImmutableList()))
        }

        allGroups = groupList
        allRows = groupList.flatMap { it.rows }
        publishRows()
    }

    private fun publishRows() {
        mutableState.update {
            it.copy(
                groups = allGroups.toImmutableList(),
                rows = allRows.toImmutableList(),
                hiddenRows = 0,
                isLoading = false,
            )
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

        fun Card.toRow(outcome: ReviewOutcome, isAddedToNotebook: Boolean = false) = ReviewRowUi(
            letter = letter,
            word = answer.lowercase(),
            clue = clue,
            example = example,
            outcome = outcome,
            level = level,
            cardId = id.value,
            isAddedToNotebook = isAddedToNotebook,
        )
    }
}
