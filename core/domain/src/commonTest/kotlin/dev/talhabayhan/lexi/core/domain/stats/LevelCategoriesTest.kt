package dev.talhabayhan.lexi.core.domain.stats

import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.Category
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.srs.CardProgress
import kotlin.test.Test
import kotlin.test.assertEquals

class LevelCategoriesTest {
    private fun category(id: String, sort: Int) = Category(CategoryId(id), id, id, setOf(Level.A1), sort, isPro = false)

    private val food = category("food-drink", 1)
    private val home = category("home", 2)
    private val travel = category("travel", 3)

    private val content = LevelCategoryContent(
        categories = listOf(food, home, travel),
        categoryOf = mapOf(
            CardId("apple") to food.id,
            CardId("bread") to food.id,
            CardId("chair") to home.id,
        ),
    )

    @Test
    fun `categories keep their order with card totals and learned words and empty ones are left out`() {
        val progress = listOf(
            CardProgress(CardId("apple"), seen = 1, correct = 1),
            CardProgress(CardId("chair"), seen = 2, wrong = 2),
            // Another level's card: ignored.
            CardProgress(CardId("eagle"), seen = 1, correct = 1),
        )

        assertEquals(
            listOf(CategoryWords(food, learned = 1, total = 2), CategoryWords(home, learned = 0, total = 1)),
            content.words(progress),
        )
    }

    @Test
    fun `a level without categories has no words`() {
        assertEquals(emptyList(), LevelCategoryContent(emptyList(), emptyMap()).words(emptyList()))
    }
}
