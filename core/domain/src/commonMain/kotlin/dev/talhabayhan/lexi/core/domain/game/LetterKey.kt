package dev.talhabayhan.lexi.core.domain.game

import kotlinx.serialization.Serializable

/** State of a letter key during a slot (`GAME_RULES.md` §4, §7). */
@Serializable
public enum class KeyState {
    AVAILABLE,

    /** Its letter is placed in an answer slot; Backspace makes it AVAILABLE again. */
    USED,

    /** Disabled by the Remove distractors joker. */
    DISABLED,
}

/** One key of the slot's letter keyboard. Keys keep their order for the whole slot. */
@Serializable
public data class LetterKey(
    val letter: Char,
    /** Not part of the answer's letter multiset. */
    val isDistractor: Boolean,
    val state: KeyState = KeyState.AVAILABLE,
)
