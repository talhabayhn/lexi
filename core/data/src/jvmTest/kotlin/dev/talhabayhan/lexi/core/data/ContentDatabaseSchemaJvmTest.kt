package dev.talhabayhan.lexi.core.data

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import dev.talhabayhan.lexi.core.data.content.db.ContentDatabase
import kotlin.test.Test
import kotlin.test.assertEquals

class ContentDatabaseSchemaJvmTest {
    @Test
    fun `schema creates every content table and index from CONTENT md section 7`() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        ContentDatabase.Schema.create(driver)

        val names = driver.executeQuery(
            identifier = null,
            sql = "SELECT name FROM sqlite_master WHERE type IN ('table', 'index') " +
                "AND name NOT LIKE 'sqlite_%' ORDER BY name",
            mapper = { cursor ->
                val result = mutableListOf<String>()
                while (cursor.next().value) result += cursor.getString(0)!!
                QueryResult.Value(result)
            },
            parameters = 0,
        ).value

        assertEquals(
            listOf(
                "card",
                "card_level_category",
                "card_level_letter",
                "category",
                "clue",
                "exam_pack",
                "exam_pack_card",
                "meta",
            ),
            names,
        )
        driver.close()
    }
}
