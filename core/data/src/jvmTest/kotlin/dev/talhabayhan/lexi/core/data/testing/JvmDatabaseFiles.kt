package dev.talhabayhan.lexi.core.data.testing

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import dev.talhabayhan.lexi.core.data.db.AtomicFiles
import dev.talhabayhan.lexi.core.data.db.DatabaseFiles
import dev.talhabayhan.lexi.core.data.db.DatabaseFiles.Companion.CONTENT_DB
import dev.talhabayhan.lexi.core.data.db.DatabaseFiles.Companion.SETTINGS_FILE
import dev.talhabayhan.lexi.core.data.db.DatabaseFiles.Companion.USER_DB
import dev.talhabayhan.lexi.core.data.user.db.UserDatabase
import java.io.File
import java.util.Properties

/** [DatabaseFiles] on a JVM directory: SQLite JDBC driver, content opened with the read-only open flag. */
class JvmDatabaseFiles(private val dir: File) : DatabaseFiles {
    val contentFile: File get() = File(dir, CONTENT_DB)
    var installs: Int = 0
        private set

    override fun contentDbExists(): Boolean = contentFile.isFile

    override fun installContentDb(bytes: ByteArray) {
        installs++
        AtomicFiles.replaceDatabaseFile(contentFile, bytes)
    }

    override fun openContentDb(): SqlDriver =
        JdbcSqliteDriver("jdbc:sqlite:${contentFile.path}", Properties().apply { setProperty("open_mode", "1") })

    override fun openUserDb(): SqlDriver =
        JdbcSqliteDriver("jdbc:sqlite:${File(dir, USER_DB).path}", Properties(), UserDatabase.Schema)

    override fun settingsFilePath(): String = File(dir, "datastore/$SETTINGS_FILE").path
}
