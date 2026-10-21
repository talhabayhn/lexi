package dev.talhabayhan.lexi.feature.game

import dev.talhabayhan.lexi.core.designsystem.component.AnswerSlotState
import dev.talhabayhan.lexi.core.designsystem.component.LetterKeyState
import dev.talhabayhan.lexi.core.domain.game.GameEvent
import dev.talhabayhan.lexi.core.domain.game.GameState
import dev.talhabayhan.lexi.core.domain.game.KeyState

/**
 * Key-tap events that type [word] on the current slot of [state] (first available key per letter). A whole word on a
 * slot that opens with its given letter (GAME_RULES.md §3) skips that letter's cell.
 */
internal fun keyTaps(state: GameState, word: String): List<GameEvent> {
    val input = checkNotNull(state.currentInput)
    val keys = input.keys
    val used = mutableSetOf<Int>()
    return withoutGivenLetter(
        word,
        input.cells.indexOfFirst { it?.given == true },
        input.cells.firstOrNull { it?.given == true }?.letter,
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

/** [word] without the [given] letter at its cell [givenCell] (-1 = none), when [word] has it there. */
private fun withoutGivenLetter(word: String, givenCell: Int, given: Char?, nothingTyped: Boolean): String {
    val letters = word.replace(" ", "")
    val skip = given != null && nothingTyped && letters.getOrNull(givenCell) == given
    return if (skip) letters.removeRange(givenCell, givenCell + 1) else letters
}

/** Types [word] through the ViewModel, using the key indices of the UI state. */
internal fun GameViewModel.type(word: String) {
    val cells = checkNotNull(state.value.turn).words.flatten()
    val givenCell = cells.indexOfFirst { it.state == AnswerSlotState.Given }
    val nothingTyped = cells.none { it.state == AnswerSlotState.Filled || it.state == AnswerSlotState.JokerFilled }
    withoutGivenLetter(word, givenCell, cells.getOrNull(givenCell)?.letter, nothingTyped).forEach { letter ->
        val keys = checkNotNull(state.value.turn).keys
        val index = keys.indices.first { keys[it].letter == letter && keys[it].state == LetterKeyState.Normal }
        onIntent(GameIntent.KeyTapped(index))
    }
}
