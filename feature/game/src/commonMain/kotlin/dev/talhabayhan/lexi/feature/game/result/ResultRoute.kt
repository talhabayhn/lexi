package dev.talhabayhan.lexi.feature.game.result

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
) {
    internal val gameLevel: Level get() = Level.valueOf(level)

    internal val gameMode: GameMode get() = GameMode.valueOf(mode)

    internal val gameOverReason: GameOverReason get() = GameOverReason.valueOf(reason)

    internal val categoryId: CategoryId? get() = category?.let(::CategoryId)

    internal companion object {
        /** The result of the finished [state]; [category] is the route's category of the game. */
        fun of(state: GameState, category: CategoryId?, recorded: Boolean): ResultRoute {
            val summary = checkNotNull(state.summary) { "the game is not finished" }
            val passed = state.wheel.slots
                .filter { it.state == SlotState.PASSED }
                .mapNotNullTo(mutableSetOf()) { it.card?.id }
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
                passedCards = summary.missedCards.filter { it in passed }.map(CardId::value),
                recorded = recorded,
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
    ResultScreen(
        state = state,
        onIntent = { intent ->
            when (intent) {
                ResultIntent.PlayAgain -> viewModel.playAgain?.let(onPlayAgain)

                ResultIntent.Home -> onHome()

                // TODO(M6): share card (`12-share-card`) rendered to an image + system share sheet.
                ResultIntent.Share -> Unit

                else -> viewModel.onIntent(intent)
            }
        },
    )
}
