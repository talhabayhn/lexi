package dev.talhabayhan.lexi.core.data

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import co.touchlab.sqliter.DatabaseConfiguration
import dev.talhabayhan.lexi.core.data.user.db.UserDatabase
import kotlin.test.Test
import kotlin.test.assertEquals

class UserDatabaseIosTest {
    @Test
    fun native_driver_creates_user_db_and_round_trips_a_row() {
        val driver = NativeSqliteDriver(
            schema = UserDatabase.Schema,
            name = "user-test.db",
            onConfiguration = { config: DatabaseConfiguration -> config.copy(inMemory = true) },
        )
        val queries = UserDatabase(driver).streakQueries

        queries.markDay(20_000)
        queries.markDay(20_000)

        assertEquals(listOf(20_000L), queries.days().executeAsList())
        driver.close()
    }
}
