package dev.talhabayhan.lexi.core.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import dev.talhabayhan.lexi.core.data.db.ProbeDatabase
import kotlin.test.Test
import kotlin.test.assertEquals

class ProbeDatabaseJvmTest {
    @Test
    fun `inserted rows are read back in id order`() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        ProbeDatabase.Schema.create(driver)
        val queries = ProbeDatabase(driver).toolchainProbeQueries

        queries.insert(id = 2, label = "b")
        queries.insert(id = 1, label = "a")

        assertEquals(listOf("a", "b"), queries.selectAll().executeAsList().map { it.label })
        driver.close()
    }
}
