package dev.talhabayhan.lexi.core.testing

import dev.talhabayhan.lexi.core.domain.model.Card
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.repository.CardRepository

/** In-memory [CardRepository] over level [cards] (no exam-pack-only cards); results are sorted by id. */
public class FakeCardRepository(cards: Collection<Card> = emptyList()) : CardRepository {
    /** Replaceable, e.g. to simulate a content update that removed a card. */
    public var cards: List<Card> = cards.sortedBy { it.id.value }
        set(value) {
            field = value.sortedBy { it.id.value }
        }

    override suspend fun wheelPool(level: Level, category: CategoryId): List<Card> =
        cards.filter { it.level == level && (category == CategoryId.MIXED || it.category == category) }

    override suspend fun levelCards(): List<Card> = cards

    override suspend fun cards(ids: Collection<CardId>): List<Card> = cards.filter { it.id in ids }

    override suspend fun levelTotals(): Map<Level, Int> = cards.groupingBy { it.level }.eachCount()

    override suspend fun categoryOfCards(level: Level): Map<CardId, CategoryId> =
        cards.filter { it.level == level }.associate { it.id to it.category }
}
