package dev.talhabayhan.lexi.feature.game

import dev.talhabayhan.lexi.core.designsystem.component.AnswerSlotState
import dev.talhabayhan.lexi.core.designsystem.component.LetterKeyState
import dev.talhabayhan.lexi.core.designsystem.component.WheelLetterState
import dev.talhabayhan.lexi.core.designsystem.component.WheelLetterUi
import dev.talhabayhan.lexi.core.domain.game.GameState
import dev.talhabayhan.lexi.core.domain.game.GameStatus
import dev.talhabayhan.lexi.core.domain.game.KeyState
import dev.talhabayhan.lexi.core.domain.game.SlotState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/** Pure mapping of engine state to the screen's resting look (ADR 0019 §2). */
internal object GameUiMapper {
    private const val MS_PER_SECOND = 1_000L

    /**
     * [previous] keeps the last turn visible after game over (the engine clears the current slot when it finishes)
     * until the screen navigates away.
     */
    fun map(state: GameState, previous: GameUiState? = null): GameUiState {
        val wheel = state.wheel.slots
            .filter { it.isPlayable }
            .map { WheelLetterUi(it.letter, it.state.toWheelState()) }
            .toImmutableList()
        if (state.status == GameStatus.NOT_STARTED) return GameUiState(isLoading = true, wheel = wheel)
        val turn = turnOf(state) ?: previous?.turn
        return GameUiState(
            isLoading = false,
            wheel = wheel,
            score = state.score,
            turn = turn,
            isPaused = state.isPaused,
            inputEnabled = state.status == GameStatus.PLAYING &&
                !state.isPaused &&
                !state.isRevealing &&
                state.currentInput != null,
        )
    }

    /** Clock for the HUD; before `Start` the full [timeLimitMs] is shown. Null limit = untimed mode. */
    fun clock(state: GameState, timeLimitMs: Long?, warningSeconds: Int): GameClockUi {
        if (timeLimitMs == null || timeLimitMs <= 0) return GameClockUi.Untimed
        val remaining = (state.remainingMs ?: timeLimitMs).coerceIn(0L, timeLimitMs)
        val seconds = ((remaining + MS_PER_SECOND - 1) / MS_PER_SECOND).toInt()
        return GameClockUi(
            isTimed = true,
            remainingMs = remaining,
            fraction = remaining.toFloat() / timeLimitMs,
            wholeSeconds = seconds,
            isWarning = seconds <= warningSeconds,
        )
    }

    private fun turnOf(state: GameState): GameTurnUi? {
        val slot = state.currentSlot ?: return null
        val card = slot.card ?: return null
        val input = state.currentInput ?: return null
        val answer = card.answer.replace(" ", "")
        val firstEmpty = input.cells.indexOf(null)
        val cells = input.cells.mapIndexed { index, placed ->
            when {
                state.isRevealing -> AnswerCellUi(answer[index], AnswerSlotState.Wrong)
                placed == null && index == firstEmpty -> AnswerCellUi(null, AnswerSlotState.Active)
                placed == null -> AnswerCellUi(null, AnswerSlotState.Empty)
                placed.revealed -> AnswerCellUi(placed.letter, AnswerSlotState.JokerFilled)
                else -> AnswerCellUi(placed.letter, AnswerSlotState.Filled)
            }
        }
        return GameTurnUi(
            letter = slot.letter,
            rule = card.rule,
            clue = card.clue,
            partOfSpeech = card.partOfSpeech,
            level = card.level,
            letterCount = card.answerLetters,
            words = cells.splitIntoWords(card.answer),
            keys = input.keys.map { KeyUi(it.letter, it.state.toKeyState()) }.toImmutableList(),
            isRevealing = state.isRevealing,
        )
    }

    private fun List<AnswerCellUi>.splitIntoWords(answer: String): ImmutableList<ImmutableList<AnswerCellUi>> {
        var start = 0
        return answer.split(' ').map { word ->
            subList(start, start + word.length).toImmutableList().also { start += word.length }
        }.toImmutableList()
    }

    private fun SlotState.toWheelState(): WheelLetterState = when (this) {
        SlotState.TODO, SlotState.SKIPPED -> WheelLetterState.Todo
        SlotState.CURRENT -> WheelLetterState.Current
        SlotState.CORRECT -> WheelLetterState.Correct
        SlotState.WRONG -> WheelLetterState.Wrong
        SlotState.PASSED -> WheelLetterState.Passed
    }

    private fun KeyState.toKeyState(): LetterKeyState = when (this) {
        KeyState.AVAILABLE -> LetterKeyState.Normal
        KeyState.USED -> LetterKeyState.Used
        KeyState.DISABLED -> LetterKeyState.RemovedByJoker
    }
}
