package dev.talhabayhan.lexi.feature.game.duel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import dev.talhabayhan.lexi.core.designsystem.component.ReviewOutcome
import dev.talhabayhan.lexi.core.domain.feedback.Speaker
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.repository.CardRepository
import dev.talhabayhan.lexi.core.domain.telemetry.CrashReporter
import dev.talhabayhan.lexi.feature.game.result.ReviewGroupUi
import dev.talhabayhan.lexi.feature.game.result.toReviewRow
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** One player's answer review after a duel ([DuelReviewRoute]); the list is the one of `06-result`. */
@Immutable
internal data class DuelReviewUiState(
    val level: Level,
    val end: DuelEnd,
    /** 0-based player. */
    val player: Int,
    /** As typed; "" = the default name. */
    val name: String,
    /** Doğru, Yanlış, Pas: only the non-empty groups, rows in wheel order. */
    val groups: ImmutableList<ReviewGroupUi> = persistentListOf(),
    val expandedGroups: ImmutableSet<ReviewOutcome> = persistentSetOf(),
    /** True until the cards are read. */
    val isLoading: Boolean = true,
    /** The cards could not be read; the list shows an empty state with "Tekrar dene". */
    val reviewError: Boolean = false,
)

internal sealed interface DuelReviewIntent {
    /** The back button: back to the duel result (handled by the route composable). */
    data object Back : DuelReviewIntent

    /** "Daha fazla göster" / "Daha az göster" under a group. */
    data class ToggleGroup(val outcome: ReviewOutcome) : DuelReviewIntent

    /** Speaker button of a row. */
    data class Speak(val word: String) : DuelReviewIntent

    /** "Tekrar dene" after the cards could not be read. */
    data object Retry : DuelReviewIntent
}

/**
 * Reads the cards of [route] by id and groups them like the solo result (Doğru, Yanlış, Pas). Nothing is stored: a
 * duel never touches the notebook (`GameConfig.srsExcludedModes`), so there is no "Deftere ekle".
 */
internal class DuelReviewViewModel(
    private val route: DuelReviewRoute,
    private val cards: CardRepository,
    private val speaker: Speaker,
    private val crashReporter: CrashReporter,
) : ViewModel() {
    private val mutableState = MutableStateFlow(
        DuelReviewUiState(level = route.duelLevel, end = route.duelEnd, player = route.player, name = route.name),
    )
    val state: StateFlow<DuelReviewUiState> = mutableState.asStateFlow()

    /** The running [load], so a double tap on "Tekrar dene" reads once. */
    private var loading: Job? = null

    init {
        loading = viewModelScope.launch { load() }
    }

    fun onIntent(intent: DuelReviewIntent) {
        when (intent) {
            is DuelReviewIntent.ToggleGroup -> mutableState.update {
                val expanded = it.expandedGroups
                val toggled = if (intent.outcome in expanded) expanded - intent.outcome else expanded + intent.outcome
                it.copy(expandedGroups = toggled.toImmutableSet())
            }

            is DuelReviewIntent.Speak -> speaker.speak(intent.word)

            DuelReviewIntent.Retry -> if (mutableState.value.reviewError && loading?.isActive != true) {
                mutableState.update { it.copy(isLoading = true, reviewError = false) }
                loading = viewModelScope.launch { load() }
            }

            // Navigation: handled by the route composable.
            DuelReviewIntent.Back -> Unit
        }
    }

    override fun onCleared() {
        speaker.stop()
    }

    private suspend fun load() {
        val lists = listOf(
            ReviewOutcome.Correct to route.correctCards,
            ReviewOutcome.Wrong to route.wrongCards,
            ReviewOutcome.Passed to route.passedCards,
        )
        val ids = lists.flatMap { it.second }.map(::CardId)
        if (ids.isEmpty()) {
            mutableState.update { it.copy(isLoading = false) }
            return
        }
        val byId = try {
            cards.cards(ids).associateBy { it.id }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            log.e(e) { "duel review cards not loaded" }
            crashReporter.recordException(e, "duel review cards not loaded")
            mutableState.update { it.copy(isLoading = false, reviewError = true) }
            return
        }
        val groups = lists.mapNotNull { (outcome, list) ->
            val rows = list.mapNotNull { id -> byId[CardId(id)]?.toReviewRow(outcome) }
            if (rows.isEmpty()) null else ReviewGroupUi(outcome, rows.toImmutableList())
        }
        mutableState.update { it.copy(groups = groups.toImmutableList(), isLoading = false) }
    }

    private companion object {
        val log = Logger.withTag("DuelReviewViewModel")
    }
}
