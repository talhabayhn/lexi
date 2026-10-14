package dev.talhabayhan.lexi.core.domain.game

import dev.talhabayhan.lexi.core.domain.model.Card
import dev.talhabayhan.lexi.core.domain.model.LetterRule
import kotlinx.serialization.Serializable

/** Slot states (`GAME_RULES.md` §2). `TODO → CURRENT → (CORRECT | WRONG | PASSED)`; `PASSED → CURRENT` next lap. */
@Serializable
public enum class SlotState {
    TODO,
    CURRENT,
    CORRECT,
    WRONG,
    PASSED,

    /** No card exists for the letter: not playable, counts as neither right nor wrong. */
    SKIPPED,
}

/** One wheel position: `(letter, rule, card)` plus its state. Only [SlotState.SKIPPED] slots have no card. */
@Serializable
public data class Slot(val letter: Char, val card: Card?, val state: SlotState) {
    init {
        require(letter in 'A'..'Z') { "letter '$letter' must be A-Z" }
        require((card == null) == (state == SlotState.SKIPPED)) { "only SKIPPED slots have no card ($letter, $state)" }
        require(card == null || card.letter == letter) { "card ${card?.id} serves '${card?.letter}', not '$letter'" }
    }

    public val rule: LetterRule? get() = card?.rule

    public val isPlayable: Boolean get() = state != SlotState.SKIPPED
}

/**
 * The letter wheel: slots in A–Z order, each letter at most once. A regular wheel has 26 slots (missing letters
 * are SKIPPED); a review wheel only has the letters with due cards (`GAME_RULES.md` §1).
 */
@Serializable
public data class Wheel(val slots: List<Slot>) {
    init {
        require(slots.zipWithNext().all { (a, b) -> a.letter < b.letter }) {
            "slots must be in strictly ascending A-Z order: ${slots.map { it.letter }}"
        }
    }

    public val playableSlots: List<Slot> get() = slots.filter { it.isPlayable }

    /** Index of the slot with [letter], or -1. */
    public fun indexOf(letter: Char): Int = slots.indexOfFirst { it.letter == letter }
}
