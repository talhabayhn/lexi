package dev.talhabayhan.lexi.tools.contentcompiler

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import dev.talhabayhan.lexi.tools.contentcompiler.db.ContentDatabase
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.absolute
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists

/**
 * Writes [CompiledContent] to a fresh SQLite file using the schema shared with `core:data` (`CONTENT.md` §7).
 * The file is built next to [output] and moved into place only when complete, so a failure never leaves a
 * half-written `content.db`. `PRAGMA user_version` = schema version, so the app's driver never re-creates it.
 */
object ContentDbWriter {
    const val CLUE_LANG_TR = "tr"

    fun write(content: CompiledContent, output: Path, builtAt: String) {
        val target = output.absolute()
        target.parent.createDirectories()
        val temp = Files.createTempFile(target.parent, "${target.fileName}.", ".tmp")
        try {
            temp.deleteIfExists()
            JdbcSqliteDriver("jdbc:sqlite:$temp").use { driver ->
                driver.execute(null, "PRAGMA foreign_keys = ON", 0)
                ContentDatabase.Schema.create(driver)
                driver.execute(null, "PRAGMA user_version = ${ContentDatabase.Schema.version}", 0)
                val queries = ContentDatabase(driver).contentWritesQueries
                queries.transaction {
                    queries.insertMeta("contentVersion", content.contentVersion.toString())
                    queries.insertMeta("builtAt", builtAt)
                    content.categories.forEach {
                        queries.insertCategory(
                            id = it.id,
                            name_tr = it.nameTr,
                            name_en = it.nameEn,
                            levels = it.levels.joinToString("|") { level -> level.name },
                            sort = it.sort.toLong(),
                            is_pro = if (it.isPro) 1 else 0,
                        )
                    }
                    content.cards.forEach {
                        queries.insertCard(
                            id = it.id,
                            level = it.level.name,
                            category_id = it.categoryId,
                            answer = it.answer,
                            letter = it.letter.toString(),
                            rule = it.rule,
                            pos = it.pos,
                            example_en = it.exampleEn,
                            difficulty = it.difficulty.toLong(),
                            tags = it.tags.joinToString("|"),
                            pack_only = if (it.fromPack) 1 else 0,
                        )
                        queries.insertClue(it.id, CLUE_LANG_TR, it.clueTr, it.exampleTr)
                    }
                    content.packs.forEach {
                        queries.insertExamPack(it.id, it.name, it.description, if (it.isPro) 1 else 0, it.sort.toLong())
                    }
                    content.packCards.forEach { queries.insertExamPackCard(it.packId, it.cardId, it.unit) }
                }
            }
            try {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
            } catch (_: AtomicMoveNotSupportedException) {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING)
            }
        } finally {
            temp.deleteIfExists()
        }
    }
}
