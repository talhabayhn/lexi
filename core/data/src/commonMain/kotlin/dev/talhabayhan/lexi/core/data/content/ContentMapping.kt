package dev.talhabayhan.lexi.core.data.content

import dev.talhabayhan.lexi.core.domain.model.Card
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.LetterRule
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.model.PartOfSpeech

/** SQLite's default variable limit on old Android versions is 999; `IN (...)` lists are chunked below it. */
internal const val MAX_SQL_VARIABLES = 500

/**
 * Maps a `card` row joined with its clue (columns of the `ContentReads.sq` card queries). A row that breaks a
 * domain invariant yields a failure, so callers can skip it instead of crashing (`ARCHITECTURE.md` §10).
 */
@Suppress("LongParameterList")
internal fun cardOf(
    id: String,
    level: String,
    categoryId: String,
    answer: String,
    letter: String,
    rule: String,
    pos: String,
    exampleEn: String,
    difficulty: Long,
    tags: String,
    clue: String,
    exampleTranslation: String?,
): Result<Card> = runCatching {
    Card(
        id = CardId(id),
        level = Level.valueOf(level),
        category = CategoryId(categoryId),
        answer = answer,
        letter = letter.single(),
        rule = LetterRule.valueOf(rule),
        partOfSpeech = PartOfSpeech.valueOf(pos.uppercase()),
        clue = clue,
        example = exampleEn,
        exampleTranslation = exampleTranslation.orEmpty(),
        difficulty = difficulty.toInt(),
        tags = tags.split('|').filter { it.isNotEmpty() }.toSet(),
    )
}

/** Card mapper with the column order of the generated `card.*, clue.clue, clue.example_translation` queries. */
@Suppress("LongParameterList", "UNUSED_PARAMETER")
internal fun cardRow(
    id: String,
    level: String,
    categoryId: String,
    answer: String,
    letter: String,
    rule: String,
    pos: String,
    exampleEn: String,
    difficulty: Long,
    tags: String,
    packOnly: Long,
    clue: String,
    exampleTranslation: String?,
): Result<Card> =
    cardOf(id, level, categoryId, answer, letter, rule, pos, exampleEn, difficulty, tags, clue, exampleTranslation)

internal fun levelsOf(levels: String): Set<Level> =
    levels.split('|').mapNotNull { name -> Level.entries.firstOrNull { it.name == name } }.toSet()
