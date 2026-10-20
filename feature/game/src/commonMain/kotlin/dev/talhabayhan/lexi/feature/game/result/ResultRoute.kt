package dev.talhabayhan.lexi.feature.game.result

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.game.GameOverReason
import dev.talhabayhan.lexi.core.domain.game.GameState
import dev.talhabayhan.lexi.core.domain.game.SlotState
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.share.ShareCardBuilder
import dev.talhabayhan.lexi.core.domain.share.ShareCell
import dev.talhabayhan.lexi.core.domain.share.SlotOutcome
import dev.talhabayhan.lexi.feature.game.GameRoute
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

/**
 * Type-safe destination of the result screen (`06-result`), ADR 0019 addendum: the route carries the finished game's
 * small summary instead of a session id, because the review list needs per-card outcomes that `user.db` does not
 * store (sessions keep counts only). Card ids only; clue, example and answer are read from the content by id. Enums
 * are names (no enum nav type on iOS, see `GameRoute`); lists of strings and nullable longs are built-in nav types.
 */
@Serializable
data class ResultRoute(
    val level: String,
    val mode: String,
    val category: String? = null,
    val reason: String,
    /** Final score (time bonus and Hardcore multiplier included). */
    val score: Int,
    val correct: Int,
    val wrong: Int,
    val passed: Int,
    val longestStreak: Int,
    /** Null in untimed modes. */
    val remainingMs: Long? = null,
    /** WRONG or PASSED cards in wheel order (`GameSummary.missedCards`): what `FinishGame` put into box 0. */
    val missedCards: List<String> = emptyList(),
    /** The PASSED part of [missedCards] (the rest was wrong). */
    val passedCards: List<String> = emptyList(),
    /** `FinishGame` stored the session and the notebook update (false after a storage failure). */
    val recorded: Boolean = true,
    val correctCards: List<String> = emptyList(),
    val wrongCards: List<String> = emptyList(),
    val timeUpCards: List<String> = emptyList(),
    /** The playable letters in wheel order (`ShareCardBuilder.cellsOf`), one per share-card cell. */
    val wheelLetters: String = "",
    /** The `SlotOutcome` name of each of [wheelLetters], same order. */
    val wheelOutcomes: List<String> = emptyList(),
) {
    internal val gameLevel: Level get() = Level.valueOf(level)

    internal val gameMode: GameMode get() = GameMode.valueOf(mode)

    internal val gameOverReason: GameOverReason get() = GameOverReason.valueOf(reason)

    internal val categoryId: CategoryId? get() = category?.let(::CategoryId)

    /** The share card's cells; letters without a matching or known outcome (e.g. from an older build) are left out. */
    internal val shareCells: List<ShareCell>
        get() = wheelLetters.toList().zip(wheelOutcomes).mapNotNull { (letter, outcome) ->
            SlotOutcome.entries.firstOrNull { it.name == outcome }?.let { ShareCell(letter, it) }
        }

    internal companion object {
        /** The result of the finished [state]; [category] is the route's category of the game. */
        fun of(state: GameState, category: CategoryId?, recorded: Boolean): ResultRoute {
            val summary = checkNotNull(state.summary) { "the game is not finished" }
            val passedCardsList = state.wheel.slots
                .filter { it.state == SlotState.PASSED }
                .mapNotNull { it.card?.id?.value }
            val correctCardsList = state.wheel.slots
                .filter { it.state == SlotState.CORRECT }
                .mapNotNull { it.card?.id?.value }
            val wrongCardsList = state.wheel.slots
                .filter { it.state == SlotState.WRONG }
                .mapNotNull { it.card?.id?.value }
            val timeUpCardsList = state.wheel.slots
                .filter { it.state == SlotState.TODO || it.state == SlotState.CURRENT }
                .mapNotNull { it.card?.id?.value }
            val passedSet = passedCardsList.toSet()
            val cells = ShareCardBuilder.cellsOf(state.wheel.slots)
            return ResultRoute(
                level = state.level.name,
                mode = state.mode.name,
                category = category?.value,
                reason = summary.reason.name,
                score = summary.score,
                correct = summary.correct,
                wrong = summary.wrong,
                passed = summary.passed,
                longestStreak = summary.longestStreak,
                remainingMs = summary.remainingMs,
                missedCards = summary.missedCards.map(CardId::value),
                passedCards = summary.missedCards.filter { it.value in passedSet }.map(CardId::value),
                recorded = recorded,
                correctCards = correctCardsList,
                wrongCards = wrongCardsList,
                timeUpCards = timeUpCardsList,
                wheelLetters = cells.map { it.letter }.joinToString(""),
                wheelOutcomes = cells.map { it.outcome.name },
            )
        }
    }
}

/**
 * Registers the result screen. [onPlayAgain] starts a new game with the same level, mode and category; [onHome]
 * returns to the home screen ("Ana menüye dön"; system back does the same, the graph keeps Home below the result).
 */
fun NavGraphBuilder.resultScreen(onPlayAgain: (GameRoute) -> Unit, onHome: () -> Unit) {
    composable<ResultRoute> {
        ResultRoute(onPlayAgain = onPlayAgain, onHome = onHome)
    }
}

@Composable
internal fun ResultRoute(
    onPlayAgain: (GameRoute) -> Unit,
    onHome: () -> Unit,
    viewModel: ResultViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var shareErrorVisible by remember { mutableStateOf(false) }
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ResultEffect.ShareFailed -> shareErrorVisible = true
            }
        }
    }
    ResultScreen(
        state = state,
        onIntent = { intent ->
            when (intent) {
                ResultIntent.PlayAgain -> viewModel.playAgain?.let(onPlayAgain)
                ResultIntent.Home -> onHome()
                else -> viewModel.onIntent(intent)
            }
        },
        shareErrorVisible = shareErrorVisible,
        onShareErrorDismissed = { shareErrorVisible = false },
    )
}
