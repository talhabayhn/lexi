package dev.talhabayhan.lexi.core.data.db

import app.cash.sqldelight.db.SqlDriver
import dev.talhabayhan.lexi.core.data.content.db.ContentDatabase

/**
 * Makes sure app storage holds the bundled `content.db` and opens it read-only (`ARCHITECTURE.md` §6). The bundled
 * DB is copied on first run and whenever its `meta.contentVersion` differs from the installed copy's (an app update
 * brings newer content; a downgrade during development brings the content that matches the code). An installed copy
 * that can't be opened or read is replaced as well. Blocking: call it on the IO dispatcher.
 */
internal class ContentDatabaseInstaller(private val bundled: BundledContentDb, private val files: DatabaseFiles) {
    suspend fun open(): SqlDriver {
        val bundledVersion = bundled.contentVersion()
        if (files.contentDbExists()) {
            val driver = runCatching { files.openContentDb() }.getOrNull()
            val installedVersion = driver?.let { runCatching { contentVersion(it) }.getOrNull() }
            if (driver != null && installedVersion == bundledVersion) return driver
            driver?.close()
        }
        files.installContentDb(bundled.readBytes())
        return files.openContentDb()
    }

    companion object {
        const val CONTENT_VERSION_KEY = "contentVersion"

        /** `meta.contentVersion` of an open content DB, or null when missing or not a number. */
        fun contentVersion(driver: SqlDriver): Long? =
            ContentDatabase(driver).contentReadsQueries.metaValue(CONTENT_VERSION_KEY).executeAsOneOrNull()
                ?.toLongOrNull()
    }
}
