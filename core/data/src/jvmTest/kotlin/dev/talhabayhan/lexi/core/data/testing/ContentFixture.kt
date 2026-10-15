package dev.talhabayhan.lexi.core.data.testing

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import dev.talhabayhan.lexi.core.data.content.db.ContentDatabase
import java.nio.file.Files

/**
 * Builds content.db files the way `tools:content-compiler` does (shared schema, `user_version` = schema version),
 * from SQL fixtures, so core:data tests don't depend on the compiler (module rules).
 */
object ContentFixture {
    data class FixtureCard(
        val id: String,
        val answer: String,
        val letter: Char = answer.first(),
        val level: String = "A1",
        val category: String = "food",
        val rule: String = "STARTS",
        val pos: String = "noun",
        val clue: String = "ipucu ${answer.lowercase()}",
        val example: String = "An example with $answer.",
        val exampleTr: String? = "Örnek.",
        val difficulty: Int = 1,
        val tags: String = "",
        val packOnly: Boolean = false,
    )

    data class FixturePack(val id: String, val name: String, val sort: Int, val cards: Map<String, String?>)

    val CATEGORIES: List<List<Any>> = listOf(
        listOf("mixed", "Karışık", "Mixed", "A1|A2|B1|B2|C1|NAT", 0, 0),
        listOf("food", "Yiyecek", "Food", "A1|A2", 1, 0),
        listOf("things", "Eşyalar", "Things", "A1", 2, 0),
        listOf("travel", "Seyahat", "Travel", "B1", 3, 1),
    )

    /** Writes a complete content.db into [driver]. */
    fun write(
        driver: SqlDriver,
        contentVersion: Int,
        cards: List<FixtureCard>,
        packs: List<FixturePack> = emptyList(),
    ) {
        ContentDatabase.Schema.create(driver)
        driver.execute(null, "PRAGMA user_version = ${ContentDatabase.Schema.version}", 0)
        driver.insert("INSERT INTO meta(key, value) VALUES (?, ?)", "contentVersion", contentVersion.toString())
        CATEGORIES.forEach { driver.insert("INSERT INTO category VALUES (?, ?, ?, ?, ?, ?)", *it.toTypedArray()) }
        cards.forEach {
            driver.insert(
                "INSERT INTO card VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                it.id, it.level, it.category, it.answer, it.letter.toString(), it.rule, it.pos, it.example,
                it.difficulty, it.tags, if (it.packOnly) 1 else 0,
            )
            driver.insert("INSERT INTO clue VALUES (?, ?, ?, ?)", it.id, "tr", it.clue, it.exampleTr)
        }
        packs.forEach { pack ->
            driver.insert("INSERT INTO exam_pack VALUES (?, ?, ?, ?, ?)", pack.id, pack.name, "", 1, pack.sort)
            pack.cards.forEach { (cardId, unit) ->
                driver.insert("INSERT INTO exam_pack_card VALUES (?, ?, ?)", pack.id, cardId, unit)
            }
        }
    }

    /** content.db file bytes. */
    fun bytes(contentVersion: Int, cards: List<FixtureCard>, packs: List<FixturePack> = emptyList()): ByteArray {
        val file = Files.createTempFile("lexi-content-", ".db")
        try {
            Files.delete(file)
            JdbcSqliteDriver("jdbc:sqlite:$file").use { write(it, contentVersion, cards, packs) }
            return Files.readAllBytes(file)
        } finally {
            Files.deleteIfExists(file)
        }
    }

    /** In-memory content DB. */
    fun inMemory(contentVersion: Int, cards: List<FixtureCard>, packs: List<FixturePack> = emptyList()): SqlDriver =
        JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also { write(it, contentVersion, cards, packs) }

    private fun SqlDriver.insert(sql: String, vararg values: Any?) {
        execute(null, sql, values.size) {
            values.forEachIndexed { index, value ->
                when (value) {
                    null -> bindString(index, null)
                    is String -> bindString(index, value)
                    is Int -> bindLong(index, value.toLong())
                    is Long -> bindLong(index, value)
                    else -> error("unsupported fixture value $value")
                }
            }
        }
    }
}
