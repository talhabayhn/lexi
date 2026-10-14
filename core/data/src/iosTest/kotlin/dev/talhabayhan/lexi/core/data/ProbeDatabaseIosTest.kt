package dev.talhabayhan.lexi.core.data

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import co.touchlab.sqliter.DatabaseConfiguration
import dev.talhabayhan.lexi.core.data.db.ProbeDatabase
import kotlin.test.Test
import kotlin.test.assertEquals

class ProbeDatabaseIosTest {
    @Test
    fun native_driver_links_and_round_trips_a_row() {
        val driver = NativeSqliteDriver(
            schema = ProbeDatabase.Schema,
            name = "probe.db",
            onConfiguration = { config: DatabaseConfiguration ->
                config.copy(inMemory = true)
            },
        )
        val queries = ProbeDatabase(driver).toolchainProbeQueries

        queries.insert(id = 1, label = "a")

        assertEquals(listOf("a"), queries.selectAll().executeAsList().map { it.label })
        driver.close()
    }
}
