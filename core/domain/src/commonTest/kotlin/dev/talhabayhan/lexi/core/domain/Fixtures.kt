package dev.talhabayhan.lexi.core.domain

import dev.talhabayhan.lexi.core.domain.model.Card
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.LetterRule
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.model.PartOfSpeech

/** Test card with sensible defaults; `letter` defaults to the first letter of [answer] (rule STARTS). */
fun card(
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

/** One STARTS card per letter A–Z with answer `<letter><suffix>`. */
fun fullAlphabetPool(level: Level = Level.A1, category: String = "food-drink", suffix: String = "XY"): List<Card> =
    ('A'..'Z').map { letter -> card("$letter$suffix", level = level, category = category) }
