package dev.talhabayhan.lexi.core.domain.stats

import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.Category
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.repository.CardRepository
import dev.talhabayhan.lexi.core.domain.repository.CategoryRepository
import dev.talhabayhan.lexi.core.domain.srs.CardProgress

/** One selectable category of a level with its card count ([total]) and the player's [learned] words. */
public data class CategoryWords(val category: Category, val learned: Int, val total: Int)

/**
 * The selectable categories of one level (display order, without the virtual [CategoryId.MIXED] and the neutral
 * [CategoryId.GENERAL]) and the category of each of the level's cards. Content never changes while the app runs, so
 * it is read once per level; the learned counts follow the progress ([words]).
 */
public data class LevelCategoryContent(val categories: List<Category>, val categoryOf: Map<CardId, CategoryId>) {
    /** The categories that have at least one card of the level, in display order, with the learned counts. */
    public fun words(progress: Collection<CardProgress>): List<CategoryWords> {
        val byCategory = Stats.categoryProgress(progress, categoryOf)
        return categories.mapNotNull { category ->
            val entry = byCategory[category.id] ?: return@mapNotNull null
            CategoryWords(category, entry.learned, entry.total)
        }
    }
}

/**
 * Reads a level's [LevelCategoryContent] (Home's category sheet, `02c-home-v2`; the data of `07-categories`). NAT has
 * none: its cards are all in [CategoryId.GENERAL] (`CONTENT.md` §4). Throws when the content cannot be read.
 */
public class GetLevelCategories(private val cards: CardRepository, private val categories: CategoryRepository) {
    public suspend operator fun invoke(level: Level): LevelCategoryContent = LevelCategoryContent(
        categories = categories.categories(level).filter { it.id != CategoryId.MIXED && it.id != CategoryId.GENERAL },
        categoryOf = cards.categoryOfCards(level),
    )
}
