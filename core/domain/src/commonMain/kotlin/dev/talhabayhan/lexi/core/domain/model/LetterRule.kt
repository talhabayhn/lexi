package dev.talhabayhan.lexi.core.domain.model

import kotlinx.serialization.Serializable

/** How the wheel letter relates to the answer (`GAME_RULES.md` §1, `CONTENT.md` §2). */
@Serializable
public enum class LetterRule {
    /** "K ile başlar": the answer starts with the letter. */
    STARTS,

    /** "İçinde X geçer": the answer contains the letter, but not as its first letter. */
    CONTAINS,
    ;

    public fun isSatisfiedBy(answer: String, letter: Char): Boolean = when (this) {
        STARTS -> answer.firstOrNull() == letter
        CONTAINS -> answer.firstOrNull() != letter && letter in answer
    }
}
