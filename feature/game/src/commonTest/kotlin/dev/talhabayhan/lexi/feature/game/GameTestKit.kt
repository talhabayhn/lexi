package dev.talhabayhan.lexi.feature.game

import dev.talhabayhan.lexi.core.designsystem.component.AnswerSlotState
import dev.talhabayhan.lexi.core.designsystem.component.LetterKeyState
import dev.talhabayhan.lexi.core.domain.game.GameEvent
import dev.talhabayhan.lexi.core.domain.game.GameState
import dev.talhabayhan.lexi.core.domain.game.KeyState

/**
 * Key-tap events that type [word] on the current slot of [state] (first available key per letter). A whole word on a
 * slot that opens with its given first letter (STARTS, GAME_RULES.md §3) skips that letter.
 */
internal fun keyTaps(state: GameState, word: String): List<GameEvent> {
    val input = checkNotNull(state.currentInput)
    val keys = input.keys
    val used = mutableSetOf<Int>()
    return withoutGivenLetter(
        word,
        input.cells.firstOrNull()?.takeIf {
            it.given
        }?.letter,
        input.placementOrder.isEmpty(),
    )
        .map { letter ->
            val index = keys.indices.first {
                it !in used && keys[it].letter == letter &&
                    keys[it].state == KeyState.AVAILABLE
            }
            used += index
            GameEvent.KeyTapped(index)
        }
}

private fun withoutGivenLetter(word: String, given: Char?, nothingTyped: Boolean): String =
    if (given != null && nothingTyped && word.firstOrNull() == given) word.drop(1) else word

/** Types [word] through the ViewModel, using the key indices of the UI state. */
internal fun GameViewModel.type(word: String) {
    val cells = checkNotNull(state.value.turn).words.flatten()
    val given = cells.firstOrNull()?.takeIf { it.state == AnswerSlotState.Given }?.letter
    val nothingTyped = cells.none { it.state == AnswerSlotState.Filled || it.state == AnswerSlotState.JokerFilled }
    withoutGivenLetter(word, given, nothingTyped).forEach { letter ->
        val keys = checkNotNull(state.value.turn).keys
        val index = keys.indices.first { keys[it].letter == letter && keys[it].state == LetterKeyState.Normal }
        onIntent(GameIntent.KeyTapped(index))
    }
}
