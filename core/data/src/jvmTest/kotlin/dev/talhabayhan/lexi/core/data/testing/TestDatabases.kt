package dev.talhabayhan.lexi.core.data.testing

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import dev.talhabayhan.lexi.core.data.content.db.ContentDatabase
import dev.talhabayhan.lexi.core.data.db.Databases
import dev.talhabayhan.lexi.core.data.db.SuspendLazy
import dev.talhabayhan.lexi.core.data.testing.ContentFixture.FixtureCard
import dev.talhabayhan.lexi.core.data.user.db.UserDatabase
import kotlinx.coroutines.CoroutineDispatcher

/** In-memory content.db (from [ContentFixture]) and user.db, as the repositories see them. */
class TestDatabases(
    io: CoroutineDispatcher,
    cards: List<FixtureCard> = emptyList(),
    packs: List<ContentFixture.FixturePack> = emptyList(),
    contentVersion: Int = 1,
) {
    val contentDriver: SqlDriver = ContentFixture.inMemory(contentVersion, cards, packs)
    val userDriver: SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also { UserDatabase.Schema.create(it) }
    val databases = Databases(
        content = SuspendLazy(io) { ContentDatabase(contentDriver) },
        user = SuspendLazy(io) { UserDatabase(userDriver) },
    )

    fun close() {
        contentDriver.close()
        userDriver.close()
    }
}
