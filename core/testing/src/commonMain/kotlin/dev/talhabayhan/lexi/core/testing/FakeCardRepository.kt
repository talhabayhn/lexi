package dev.talhabayhan.lexi.core.testing

import dev.talhabayhan.lexi.core.domain.model.Card
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.repository.CardRepository

/**
 * In-memory [CardRepository] over level [cards] (no exam-pack-only cards); results are sorted by id. While [failing]
 * is true every read throws [IllegalStateException] (an unreadable content database).
 */
public class FakeCardRepository(cards: Collection<Card> = emptyList()) : CardRepository {
    /** Failure injection: every read throws while true. */
    public var failing: Boolean = false

    /** Number of reads, failed ones included. */
    public var reads: Int = 0
        private set

    /** Replaceable, e.g. to simulate a content update that removed a card. */
    public var cards: List<Card> = cards.sortedBy { it.id.value }
        set(value) {
            field = value.sortedBy { it.id.value }
        }

    private fun read() {
        reads++
        check(!failing) { "injected content read failure" }
    }

    override suspend fun wheelPool(level: Level, category: CategoryId): List<Card> =
        read().let { cards }.filter { it.level == level && (category == CategoryId.MIXED || it.category == category) }

    override suspend fun levelCards(): List<Card> = read().let { cards }

    override suspend fun cards(ids: Collection<CardId>): List<Card> = read().let { cards }.filter { it.id in ids }

    override suspend fun levelTotals(): Map<Level, Int> = read().let { cards }.groupingBy { it.level }.eachCount()

    override suspend fun categoryOfCards(level: Level): Map<CardId, CategoryId> =
        read().let { cards }.filter { it.level == level }.associate { it.id to it.category }
}
