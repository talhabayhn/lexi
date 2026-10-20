package dev.talhabayhan.lexi.core.domain.repository

import dev.talhabayhan.lexi.core.domain.model.Card
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.Category
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.ContentIndex
import dev.talhabayhan.lexi.core.domain.model.ExamPack
import dev.talhabayhan.lexi.core.domain.model.ExamPackCard
import dev.talhabayhan.lexi.core.domain.model.ExamPackId
import dev.talhabayhan.lexi.core.domain.model.Level

/**
 * Cards of the bundled content (`ARCHITECTURE.md` §6). Content never changes while the app runs, so reads are
 * one-shot. Broken rows are skipped, never thrown (`ARCHITECTURE.md` §10).
 */
public interface CardRepository {
    /**
     * Wheel pool for `WheelGenerator.generate`: cards of [level] in [category] ([CategoryId.MIXED] = all
     * categories), sorted by id. Exam-pack-only cards are never included.
     */
    public suspend fun wheelPool(level: Level, category: CategoryId = CategoryId.MIXED): List<Card>

    /** Every level card (no exam-pack-only cards), e.g. the level-test pool. */
    public suspend fun levelCards(): List<Card>

    /** Cards by id of any kind (notebook, review wheel, exam packs), sorted by id; unknown ids are skipped. */
    public suspend fun cards(ids: Collection<CardId>): List<Card>

    /**
     * Level and letter of every level card plus the card count per level, read with one slim query (no clues) and
     * kept in memory: the content is read-only at runtime. Prefer this over [levelCards] when only ids are needed.
     */
    public suspend fun contentIndex(): ContentIndex

    /** Level card count per level (no exam-pack-only cards), for `Stats.levelProgress`. */
    public suspend fun levelTotals(): Map<Level, Int>

    /**
     * Category of every card of [level] (no exam-pack-only cards), by card id: the category cards' totals (group and
     * count) and, with the player's progress, their learned counts (`Stats.categoryProgress`). Light: no clues.
     */
    public suspend fun categoryOfCards(level: Level): Map<CardId, CategoryId>
}

public interface CategoryRepository {
    /** Categories in display order, incl. the virtual [CategoryId.MIXED]; only those of [level] when given. */
    public suspend fun categories(level: Level? = null): List<Category>
}

public interface ExamPackRepository {
    /** Packs in display order (empty in builds without the private pack content). */
    public suspend fun packs(): List<ExamPack>

    /** Cards of [pack] by unit, then id; includes the pack's exam-pack-only cards. */
    public suspend fun cards(pack: ExamPackId): List<ExamPackCard>
}

public interface ContentVersionRepository {
    /** `meta.contentVersion` of the installed content.db. */
    public suspend fun contentVersion(): Long
}
