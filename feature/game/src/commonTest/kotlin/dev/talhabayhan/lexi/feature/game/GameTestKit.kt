package dev.talhabayhan.lexi.feature.game

import dev.talhabayhan.lexi.core.designsystem.component.LetterKeyState
import dev.talhabayhan.lexi.core.domain.game.GameEvent
import dev.talhabayhan.lexi.core.domain.game.GameState
import dev.talhabayhan.lexi.core.domain.game.KeyState

/** Key-tap events that type [word] on the current slot of [state] (first available key per letter). */
internal fun keyTaps(state: GameState, word: String): List<GameEvent> {
    val keys = checkNotNull(state.currentInput).keys
    val used = mutableSetOf<Int>()
    return word.map { letter ->
        val index = keys.indices.first {
            it !in used && keys[it].letter == letter &&
                keys[it].state == KeyState.AVAILABLE
        }
        used += index
        GameEvent.KeyTapped(index)
    }
}

/** Types [word] through the ViewModel, using the key indices of the UI state. */
internal fun GameViewModel.type(word: String) {
    word.forEach { letter ->
        val keys = checkNotNull(state.value.turn).keys
        val index = keys.indices.first { keys[it].letter == letter && keys[it].state == LetterKeyState.Normal }
        onIntent(GameIntent.KeyTapped(index))
    }
}
