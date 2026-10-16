package dev.talhabayhan.lexi.core.data.content

import dev.talhabayhan.lexi.content.db.ContentReadsQueries
import dev.talhabayhan.lexi.core.data.db.ContentDatabaseInstaller
import dev.talhabayhan.lexi.core.data.db.Databases
import dev.talhabayhan.lexi.core.domain.model.Card
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.Category
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.ExamPack
import dev.talhabayhan.lexi.core.domain.model.ExamPackCard
import dev.talhabayhan.lexi.core.domain.model.ExamPackId
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.repository.CardRepository
import dev.talhabayhan.lexi.core.domain.repository.CategoryRepository
import dev.talhabayhan.lexi.core.domain.repository.ContentVersionRepository
import dev.talhabayhan.lexi.core.domain.repository.ExamPackRepository
import dev.talhabayhan.lexi.core.domain.settings.ClueLanguage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/** Shared plumbing: content queries, and the mapping of their rows, on the IO dispatcher (never on the main thread). */
internal abstract class ContentReader(private val databases: Databases, private val io: CoroutineDispatcher) {
    protected suspend fun <T> read(block: ContentReadsQueries.() -> T): T =
        withContext(io) { databases.content.get().contentReadsQueries.block() }
}

/**
 * Level/category queries filter `pack_only = 0` (`CONTENT.md` §1). Clues are read in [clueLanguage] (Turkish in
 * v1). Broken rows are skipped and passed to [onBrokenCard] (crash reporting is wired in M7).
 */
internal class CardRepositoryImpl(
    databases: Databases,
    io: CoroutineDispatcher,
    private val clueLanguage: ClueLanguage = ClueLanguage.TR,
    private val onBrokenCard: (Throwable) -> Unit = {},
) : ContentReader(databases, io),
    CardRepository {
    private val lang get() = clueLanguage.code

    override suspend fun wheelPool(level: Level, category: CategoryId): List<Card> = read {
        if (category == CategoryId.MIXED) {
            levelCards(lang, level.name, ::cardRow).executeAsList()
        } else {
            levelCategoryCards(lang, level.name, category.value, ::cardRow).executeAsList()
        }.valid()
    }

    override suspend fun levelCards(): List<Card> = read { allLevelCards(lang, ::cardRow).executeAsList().valid() }

    override suspend fun cards(ids: Collection<CardId>): List<Card> = read {
        ids.map { it.value }.distinct().chunked(MAX_SQL_VARIABLES).flatMap { chunk ->
            cardsById(lang, chunk, ::cardRow).executeAsList()
        }.valid().sortedBy { it.id.value }
    }

    override suspend fun levelTotals(): Map<Level, Int> = read {
        levelCardCounts().executeAsList().mapNotNull { row ->
            Level.entries.firstOrNull { it.name == row.level }?.let { it to row.total.toInt() }
        }.toMap()
    }

    override suspend fun categoryOfCards(level: Level): Map<CardId, CategoryId> = read {
        levelCardCategories(level.name).executeAsList().associate { CardId(it.id) to CategoryId(it.category_id) }
    }

    private fun List<Result<Card>>.valid(): List<Card> = mapNotNull { result ->
        result.onFailure(onBrokenCard).getOrNull()
    }
}

internal class CategoryRepositoryImpl(databases: Databases, io: CoroutineDispatcher) :
    ContentReader(databases, io),
    CategoryRepository {
    override suspend fun categories(level: Level?): List<Category> = read {
        categories { id, nameTr, nameEn, levels, sort, isPro ->
            Category(CategoryId(id), nameTr, nameEn, levelsOf(levels), sort.toInt(), isPro != 0L)
        }.executeAsList().filter { level == null || level in it.levels }
    }
}

internal class ExamPackRepositoryImpl(
    databases: Databases,
    io: CoroutineDispatcher,
    private val clueLanguage: ClueLanguage = ClueLanguage.TR,
    private val onBrokenCard: (Throwable) -> Unit = {},
) : ContentReader(databases, io),
    ExamPackRepository {
    override suspend fun packs(): List<ExamPack> = read {
        examPacks { id, name, description, isPro, sort ->
            ExamPack(ExamPackId(id), name, description, isPro != 0L, sort.toInt())
        }.executeAsList()
    }

    override suspend fun cards(pack: ExamPackId): List<ExamPackCard> = read {
        examPackCards(clueLanguage.code, pack.value).executeAsList().mapNotNull { row ->
            cardOf(
                id = row.id,
                level = row.level,
                categoryId = row.category_id,
                answer = row.answer,
                letter = row.letter,
                rule = row.rule,
                pos = row.pos,
                exampleEn = row.example_en,
                difficulty = row.difficulty,
                tags = row.tags,
                clue = row.clue,
                exampleTranslation = row.example_translation,
            ).map { ExamPackCard(it, row.unit) }.onFailure(onBrokenCard).getOrNull()
        }
    }
}

internal class ContentVersionRepositoryImpl(databases: Databases, io: CoroutineDispatcher) :
    ContentReader(databases, io),
    ContentVersionRepository {
    override suspend fun contentVersion(): Long = read {
        metaValue(ContentDatabaseInstaller.CONTENT_VERSION_KEY).executeAsOneOrNull()?.toLongOrNull() ?: 0L
    }
}
