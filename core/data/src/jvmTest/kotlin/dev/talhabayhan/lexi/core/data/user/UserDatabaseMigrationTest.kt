package dev.talhabayhan.lexi.core.data.user

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import dev.talhabayhan.lexi.core.data.user.db.UserDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Every released user.db version (frozen snapshot in `src/jvmTest/resources/user-db/<version>.sql`) must migrate to
 * exactly the schema a fresh install gets, and keep its rows. Changing a `.sq` table without a `.sqm` migration
 * fails here.
 */
class UserDatabaseMigrationTest {
    private val current = UserDatabase.Schema.version

    @Test
    fun `there is a frozen snapshot for every schema version`() {
        (1..current).forEach { version -> assertNotNull(snapshot(version), "missing user-db/$version.sql") }
    }

    @Test
    fun `every released version migrates to the current schema`() {
        val fresh = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also { UserDatabase.Schema.create(it) }
        val expected = schemaOf(fresh)

        (1..current).forEach { version ->
            val driver = driverAt(version)
            UserDatabase.Schema.migrate(driver, version, current)
            assertEquals(expected, schemaOf(driver), "schema after migrating from v$version")
            driver.close()
        }
        fresh.close()
    }

    @Test
    fun `progress rows survive every migration`() {
        (1..current).forEach { version ->
            val driver = driverAt(version)
            driver.execute(
                null,
                "INSERT INTO progress(card_id, box, due_epoch_day, seen, correct, wrong, last_seen_ms, mastered) " +
                    "VALUES ('a1-apple', 2, 20000, 3, 2, 1, 1700000000000, 0)",
                0,
            )

            UserDatabase.Schema.migrate(driver, version, current)

            val row = UserDatabase(driver).progressQueries.selectAll().executeAsOne()
            assertEquals("a1-apple", row.card_id)
            assertEquals(2L, row.box)
            assertEquals(1_700_000_000_000L, row.last_seen_ms)
            driver.close()
        }
    }

    @Test
    fun `daily_result rows survive migration to current schema`() {
        (1..current).forEach { version ->
            val driver = driverAt(version)
            driver.execute(
                null,
                "INSERT INTO daily_result(epoch_day, level, score, time_ms, share_code) " +
                    "VALUES (20000, 'B1', 310, 95000, 'share-123')",
                0,
            )

            UserDatabase.Schema.migrate(driver, version, current)

            val row = UserDatabase(driver).dailyResultQueries.byDayAndLevel(20000, "B1").executeAsOne()
            assertEquals(20000L, row.epoch_day)
            assertEquals("B1", row.level)
            assertEquals(310L, row.score)
            assertEquals(95000L, row.time_ms)
            assertEquals("share-123", row.share_code)
            driver.close()
        }
    }

    private fun snapshot(version: Long): String? = javaClass.getResource("/user-db/$version.sql")?.readText()

    private fun driverAt(version: Long): SqlDriver {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        val sql = checkNotNull(snapshot(version))
            .lines().filterNot { it.trimStart().startsWith("--") }.joinToString("\n")
        sql.split(';').map { it.trim() }.filter { it.isNotEmpty() }.forEach { driver.execute(null, it, 0) }
        driver.execute(null, "PRAGMA user_version = $version", 0)
        return driver
    }

    /** Tables with their columns (type, not-null, default, pk) and indexes with their columns, sorted. */
    private fun schemaOf(driver: SqlDriver): List<String> {
        val objects = driver.strings(
            "SELECT type || ' ' || name FROM sqlite_master " +
                "WHERE name NOT LIKE 'sqlite_%' AND type IN ('table', 'index') ORDER BY name",
        )
        return objects.flatMap { entry ->
            val (type, name) = entry.split(' ')
            val details = if (type == "table") {
                driver.strings(
                    "SELECT name || ':' || type || ':' || \"notnull\" || ':' || ifnull(dflt_value, '-') || ':' || pk " +
                        "FROM pragma_table_info('$name') ORDER BY cid",
                )
            } else {
                driver.strings("SELECT name FROM pragma_index_info('$name') ORDER BY seqno")
            }
            listOf(entry, "  sql: ${normalizedSql(driver, name)}") + details.map { "  $it" }
        }
    }

    /**
     * `sqlite_master.sql` without comments and with normalized whitespace, so CHECK, AUTOINCREMENT, DEFAULT and
     * other clauses that `pragma_table_info` doesn't show are compared too.
     */
    private fun normalizedSql(driver: SqlDriver, name: String): String =
        driver.strings("SELECT sql FROM sqlite_master WHERE name = '$name'").single()
            .lines().joinToString(" ") { it.substringBefore("--") }
            .replace(Regex("""\s+"""), " ")
            .replace(Regex("""\s*([(),])\s*"""), "$1")
            .replace("\"", "")
            .replace("`", "")
            .trim()

    private fun SqlDriver.strings(sql: String): List<String> = executeQuery(
        identifier = null,
        sql = sql,
        mapper = { cursor ->
            val result = mutableListOf<String>()
            while (cursor.next().value) result += cursor.getString(0).orEmpty()
            QueryResult.Value(result)
        },
        parameters = 0,
    ).value
}
