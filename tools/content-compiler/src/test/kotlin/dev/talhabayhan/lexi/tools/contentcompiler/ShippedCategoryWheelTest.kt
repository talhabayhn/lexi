package dev.talhabayhan.lexi.tools.contentcompiler

import dev.talhabayhan.lexi.core.domain.game.GameConfig
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.game.Wheel
import dev.talhabayhan.lexi.core.domain.game.WheelGenerator
import dev.talhabayhan.lexi.core.domain.model.Card
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.LetterRule
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.model.PartOfSpeech
import dev.talhabayhan.lexi.core.domain.random.SeededRandom
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Category wheels built from the shipped `content/` (`GAME_RULES.md` §1, thin-category fill). Regression for the
 * one-letter game: a category with fewer than [GameConfig.minCategoryLetters] letters (the removed False friends B1 / B2
 * had one card, Music B1 nine) used to give a wheel that one answer completed, paying the Classic time bonus.
 */
class ShippedCategoryWheelTest {
    private val config = GameConfig()

    private val contentDir = requireNotNull(System.getProperty("lexi.contentDir")) {
        "lexi.contentDir is set by Gradle: run :tools:content-compiler:test"
    }

    private val content = ContentCompiler.validate(Path(contentDir), packsDir = null)
        .also { it.assertNoErrors() }
        .content

    private val pool: List<Card> = content.cards.filterNot { it.fromPack }.map { it.toCard() }

    private fun wheel(level: Level, category: String, seed: Long): Wheel = WheelGenerator.generate(
        pool = pool,
        level = level,
        category = CategoryId(category),
        mode = GameMode.CLASSIC,
        random = SeededRandom(seed).derive(WheelGenerator.SEED_PURPOSE),
        config = config,
    )

    private fun ownLetters(level: Level, category: String): Set<Char> =
        pool.filter { it.level == level && it.category == CategoryId(category) }.mapTo(mutableSetOf()) { it.letter }

    @Test
    fun `every shipped level has all 26 letters`() {
        for (level in content.shippedLevels) {
            assertEquals(26, pool.filter { it.level == level }.map { it.letter }.toSet().size, "$level")
        }
    }

    @Test
    fun `no shipped category is listed below the minimum listing size`() {
        for (category in content.categories.filter { it.id != CategoryId.MIXED.value }) {
            for (level in category.levels.filter { it in content.shippedLevels }) {
                val own = pool.count { it.level == level && it.category == CategoryId(category.id) }
                assertTrue(own >= ContentCompiler.MIN_CATEGORY_CARDS_LISTED, "${category.id} $level has $own cards")
                assertTrue(
                    ownLetters(level, category.id).size >= config.minCategoryLetters,
                    "${category.id} $level has too few letters",
                )
            }
        }
    }

    @Test
    fun `every shipped category with cards plays at least the minimum number of letters`() {
        for (category in content.categories.filter { it.id != CategoryId.MIXED.value }) {
            for (level in category.levels.filter { it in content.shippedLevels }) {
                val own = ownLetters(level, category.id)
                for (seed in 1L..3L) {
                    val wheel = wheel(level, category.id, seed)
                    val playable = wheel.playableSlots
                    val picked = playable.map { it.card!! }
                    when {
                        // A category without cards stays "no words" (Opened.NoCards), as before.
                        own.isEmpty() -> assertTrue(playable.isEmpty(), "${category.id} $level")

                        own.size < config.minCategoryLetters -> assertEquals(26, playable.size, "${category.id} $level")

                        else -> {
                            assertEquals(own.size, playable.size, "${category.id} $level")
                            assertTrue(picked.all { it.category == CategoryId(category.id) }, "${category.id} $level")
                        }
                    }
                    assertTrue(picked.all { it.level == level }, "${category.id} $level")
                    assertEquals(picked.size, picked.distinctBy { it.answer }.size, "${category.id} $level seed $seed")
                }
            }
        }
    }

    private fun CardEntry.toCard() = Card(
        id = CardId(id),
        level = level,
        category = CategoryId(categoryId),
        answer = answer,
        letter = letter,
        rule = LetterRule.valueOf(rule),
        partOfSpeech = PartOfSpeech.valueOf(pos.uppercase()),
        clue = clueTr,
        example = exampleEn,
        exampleTranslation = exampleTr,
        difficulty = difficulty,
        tags = tags.toSet(),
    )
}
