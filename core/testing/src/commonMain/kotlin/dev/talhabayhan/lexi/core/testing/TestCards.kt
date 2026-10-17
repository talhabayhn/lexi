package dev.talhabayhan.lexi.core.testing

import dev.talhabayhan.lexi.core.domain.game.Slot
import dev.talhabayhan.lexi.core.domain.game.SlotState
import dev.talhabayhan.lexi.core.domain.game.Wheel
import dev.talhabayhan.lexi.core.domain.model.Card
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.LetterRule
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.model.PartOfSpeech

/** Test card with sensible defaults; [letter] defaults to the first letter of [answer] (rule STARTS). */
public fun testCard(
    answer: String,
    letter: Char = answer.first(),
    rule: LetterRule = if (answer.first() == letter) LetterRule.STARTS else LetterRule.CONTAINS,
    level: Level = Level.A1,
    category: String = "food-drink",
    id: String = "${level.name.lowercase()}-${answer.lowercase().replace(' ', '-')}",
): Card = Card(
    id = CardId(id),
    level = level,
    category = CategoryId(category),
    answer = answer,
    letter = letter,
    rule = rule,
    partOfSpeech = PartOfSpeech.NOUN,
    clue = "ipucu",
    example = "An example with $answer.",
    exampleTranslation = "Bir örnek.",
    difficulty = 1,
)

/** Wheel with one TODO slot per card, in A–Z order; the cards must serve distinct letters. */
public fun testWheel(cards: Collection<Card>): Wheel =
    Wheel(cards.sortedBy { it.letter }.map { Slot(it.letter, it, SlotState.TODO) })

/** [testWheel] of one STARTS card per answer (answers must start with distinct letters). */
public fun testWheel(vararg answers: String, level: Level = Level.A1): Wheel =
    testWheel(answers.map { testCard(it, level = level) })

/** One STARTS card per letter A–Z with answer `<letter><suffix>`. */
public fun testAlphabetPool(
    level: Level = Level.A1,
    category: String = "food-drink",
    suffix: String = "XY",
): List<Card> = ('A'..'Z').map { letter -> testCard("$letter$suffix", level = level, category = category) }
