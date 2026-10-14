package dev.talhabayhan.lexi.core.domain.model

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/** Stable card id, `<level>-<answer>[-n]` (`CONTENT.md` §2). Never reused or renamed. */
@Serializable
@JvmInline
public value class CardId(public val value: String)

@Serializable
public enum class PartOfSpeech { NOUN, VERB, ADJ, ADV, PHRASE, IDIOM, PHRASAL }

/**
 * One vocabulary card (`CONTENT.md` §2). [clue] and [exampleTranslation] are in the player's clue language
 * (Turkish in v1). Invariants that the engine relies on are checked here; full content validation is done by
 * `tools:content-compiler`.
 */
@Serializable
public data class Card(
    val id: CardId,
    val level: Level,
    val category: CategoryId,
    /** English answer, see [AnswerRules]: uppercase A–Z words, 2–14 letters (spaces are fixed gaps). */
    val answer: String,
    /** Wheel letter this card serves. */
    val letter: Char,
    val rule: LetterRule,
    val partOfSpeech: PartOfSpeech,
    val clue: String,
    /** English example sentence containing the answer (blanked by the Example joker). */
    val example: String,
    val exampleTranslation: String,
    /** 1–5 within the level. */
    val difficulty: Int,
    val tags: Set<String> = emptySet(),
) {
    init {
        require(AnswerRules.isValid(answer)) { "answer '$answer' must be A-Z words with 2-14 letters" }
        require(letter in 'A'..'Z') { "letter '$letter' must be A-Z" }
        require(rule.isSatisfiedBy(answer, letter)) { "answer '$answer' does not satisfy $rule '$letter'" }
        require(difficulty in 1..5) { "difficulty $difficulty must be 1-5" }
    }

    /** Number of letters the player has to type (spaces excluded). */
    public val answerLetters: Int get() = AnswerRules.letterCount(answer)
}
