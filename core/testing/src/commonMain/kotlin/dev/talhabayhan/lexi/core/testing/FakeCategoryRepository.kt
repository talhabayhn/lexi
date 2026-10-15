package dev.talhabayhan.lexi.core.testing

import dev.talhabayhan.lexi.core.domain.model.Category
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.repository.CategoryRepository

/** In-memory [CategoryRepository]: [categories] in display order, filtered by level like the real one. */
public class FakeCategoryRepository(public var categories: List<Category> = emptyList()) : CategoryRepository {
    override suspend fun categories(level: Level?): List<Category> =
        categories.filter { level == null || level in it.levels }
}

/** Test category with defaults; [levels] defaults to every level. */
public fun testCategory(
    id: String,
    nameTr: String = id,
    nameEn: String = id,
    levels: Set<Level> = Level.entries.toSet(),
    sort: Int = 1,
    isPro: Boolean = false,
): Category = Category(CategoryId(id), nameTr, nameEn, levels, sort, isPro)
