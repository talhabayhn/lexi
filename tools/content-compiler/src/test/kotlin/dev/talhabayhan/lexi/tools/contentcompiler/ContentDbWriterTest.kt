package dev.talhabayhan.lexi.tools.contentcompiler

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import dev.talhabayhan.lexi.tools.contentcompiler.db.ContentDatabase
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ContentDbWriterTest {
    private val content: Path = copyFixture("content")
    private val outDir: Path = createTempDirectory("lexi-out-")
    private val output: Path = outDir.resolve("files/content.db")

    private fun <T> withDb(block: (SqlDriver) -> T): T {
        val driver = JdbcSqliteDriver("jdbc:sqlite:$output")
        return try {
            block(driver)
        } finally {
            driver.close()
        }
    }

    private fun SqlDriver.rows(sql: String): List<List<String?>> = executeQuery(
        identifier = null,
        sql = sql,
        mapper = { cursor ->
            val rows = mutableListOf<List<String?>>()
            while (cursor.next().value) {
                rows += (0 until COLUMNS).map { index -> runCatching { cursor.getString(index) }.getOrNull() }
            }
            QueryResult.Value(rows)
        },
        parameters = 0,
    ).value

    @Test
    fun `compile writes every table with Turkish text intact`() {
        val result = ContentCompiler.compile(
            content,
            packsDir = null,
            output = output,
            builtAt = "2026-10-15T00:00:00Z",
        )

        result.validation.assertNoErrors()
        assertTrue(result.written)
        withDb { db ->
            assertEquals(
                listOf(listOf("builtAt", "2026-10-15T00:00:00Z"), listOf("contentVersion", "7")),
                db.rows("SELECT key, value FROM meta ORDER BY key").map { it.take(2) },
            )
            assertEquals(
                listOf("mixed", "food", "things"),
                db.rows("SELECT id FROM category ORDER BY sort").map { it[0] },
            )
            assertEquals("A1|B1", db.rows("SELECT levels FROM category WHERE id = 'food'").single()[0])
            assertEquals(6, db.rows("SELECT id FROM card").size)
            assertEquals(
                listOf("a1-tea", "A1", "food", "TEA", "T", "STARTS", "noun") +
                    listOf("We drink tea, not coffee.", "1", "drink|warm"),
                db.rows("SELECT * FROM card WHERE id = 'a1-tea'").single().take(10),
            )
            assertEquals(
                listOf("a1-tea", "tr", "çay", "Kahve değil, çay içeriz."),
                db.rows("SELECT * FROM clue WHERE card_id = 'a1-tea'").single().take(4),
            )
            assertEquals(0, db.rows("SELECT id FROM exam_pack").size)
            assertEquals(0, db.rows("SELECT pack_id FROM exam_pack_card").size)
            assertEquals(
                ContentDatabase.Schema.version.toString(),
                db.rows("PRAGMA user_version").single()[0],
            )
        }
        assertEquals(listOf(output), output.parent.listDirectoryEntries(), "no temp files left behind")
    }

    @Test
    fun `compile with packs writes pack tables and extra cards`() {
        val result = ContentCompiler.compile(content, copyFixture("packs"), output, builtAt = "now")

        result.validation.assertNoErrors()
        withDb { db ->
            assertEquals(
                listOf(
                    listOf(
                        "test-pack-one", "Test Pack One", "Synthetic fixture pack", "1", "0",
                        "Test Pack One", "Test Pack One", "Synthetic fixture pack", "A1-B1", "10", "ABC",
                    ),
                ),
                db.rows("SELECT * FROM exam_pack WHERE id = 'test-pack-one'").map { it.take(11) },
            )
            assertEquals(4, db.rows("SELECT card_id FROM exam_pack_card").size)
            assertEquals(1, db.rows("SELECT id FROM card WHERE id = 'b1-zest'").size)
            // Pack-only cards never reach free level wheels (those queries filter pack_only = 0).
            assertEquals(listOf("b1-zest"), db.rows("SELECT id FROM card WHERE pack_only = 1").map { it[0] })
            assertEquals(6, db.rows("SELECT id FROM card WHERE pack_only = 0").size)
        }
    }

    @Test
    fun `a failed compile leaves the previous content db untouched`() {
        output.parent.toFile().mkdirs()
        output.writeText("previous")
        content.writeCards("cards/A1/bad.csv", cardRow(answer = "zebra"))

        val result = ContentCompiler.compile(content, packsDir = null, output = output, builtAt = "now")

        assertFalse(result.written)
        assertTrue(result.validation.errors.isNotEmpty())
        assertEquals("previous", output.readText())
        assertEquals(listOf(output), output.parent.listDirectoryEntries())
    }

    @Test
    fun `the output directory is created when missing`() {
        ContentCompiler.compile(content, packsDir = null, output = output, builtAt = "now")

        assertTrue(output.exists())
    }

    private companion object {
        const val COLUMNS = 11
    }
}
