package dev.talhabayhan.lexi.core.testing

import dev.talhabayhan.lexi.core.domain.model.Card
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.ExamPack
import dev.talhabayhan.lexi.core.domain.model.ExamPackCard
import dev.talhabayhan.lexi.core.domain.model.ExamPackId
import dev.talhabayhan.lexi.core.domain.repository.ExamPackRepository

/**
 * In-memory [ExamPackRepository]: [packs] in display order and [cards] per pack id (unit order as given). While
 * [failing] is true every read throws [IllegalStateException].
 */
public class FakeExamPackRepository(
    public var packs: List<ExamPack> = emptyList(),
    public var cards: Map<ExamPackId, List<ExamPackCard>> = emptyMap(),
) : ExamPackRepository {
    /** Failure injection: every read throws while true. */
    public var failing: Boolean = false

    override suspend fun packs(): List<ExamPack> {
        check(!failing) { "injected exam pack read failure" }
        return packs
    }

    override suspend fun cards(pack: ExamPackId): List<ExamPackCard> {
        check(!failing) { "injected exam pack read failure" }
        return cards[pack].orEmpty()
    }

    override suspend fun cardIds(pack: ExamPackId): List<CardId> = cards(pack).map { it.card.id }.sortedBy { it.value }
}

/** Test pack with defaults (free unless [isPro]). */
public fun testExamPack(
    id: String,
    isPro: Boolean = false,
    sort: Int = 0,
    titleTr: String = id,
    titleEn: String = id,
    descriptionTr: String = "",
    freePreviewLetters: String = "",
): ExamPack = ExamPack(
    id = ExamPackId(id),
    name = titleTr,
    description = descriptionTr,
    isPro = isPro,
    sort = sort,
    titleTr = titleTr,
    titleEn = titleEn,
    descriptionTr = descriptionTr,
    levelRange = "A1-NAT",
    cardTarget = 1,
    freePreviewLetters = freePreviewLetters,
)

/** [cards] as members of one pack, all in [unit]. */
public fun packCards(cards: List<Card>, unit: String? = null): List<ExamPackCard> = cards.map { ExamPackCard(it, unit) }
