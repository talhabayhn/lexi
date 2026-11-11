package dev.talhabayhan.lexi.core.data.db

import app.cash.sqldelight.db.SqlDriver
import dev.talhabayhan.lexi.core.data.content.db.ContentDatabase
import kotlinx.coroutines.CancellationException

/**
 * Makes sure app storage holds the bundled `content.db` and opens it read-only (`ARCHITECTURE.md` §6). The bundled
 * DB is copied on first run and whenever its `meta.contentVersion` differs from the installed copy's (an app update
 * brings newer content; a downgrade during development brings the content that matches the code). An installed copy
 * that can't be opened or read is replaced as well. Blocking: call it on the IO dispatcher.
 *
 * When the install fails (disk full, rename failed; the atomic copy leaves the old file untouched) and the old copy
 * is readable and has the current content schema ([OLDEST_SCHEMA_COMPATIBLE_CONTENT_VERSION]), the old copy is used
 * for this run and the next start retries. Otherwise the failure is thrown (the next open retries).
 */
internal class ContentDatabaseInstaller(private val bundled: BundledContentDb, private val files: DatabaseFiles) {
    suspend fun open(): SqlDriver {
        val bundledVersion = bundled.contentVersion()
        var fallbackAllowed = false
        if (files.contentDbExists()) {
            val driver = orNull { files.openContentDb() }
            val installedVersion = driver?.let { orNull { contentVersion(it) } }
            if (driver != null && installedVersion == bundledVersion) return driver
            driver?.close()
            fallbackAllowed = installedVersion != null && installedVersion >= OLDEST_SCHEMA_COMPATIBLE_CONTENT_VERSION
        }
        try {
            files.installContentDb(bundled.readBytes())
        } catch (e: CancellationException) {
            throw e
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            if (!fallbackAllowed) throw e
            val old = orNull { files.openContentDb() } ?: throw e
            if (orNull { contentVersion(old) } == null) {
                old.close()
                throw e
            }
            return old
        }
        return files.openContentDb()
    }

    /** Null when [block] fails (unreadable copy); cancellation is never swallowed. */
    private inline fun <T> orNull(block: () -> T): T? = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
        null
    }

    companion object {
        const val CONTENT_VERSION_KEY = "contentVersion"

        /**
         * The oldest `meta.contentVersion` built with the current `ContentSchema.sq` (unchanged since content 19,
         * before v1.0.2): only such an installed copy may stand in for a failed install. Raise it to the new
         * content version whenever the content schema changes.
         */
        const val OLDEST_SCHEMA_COMPATIBLE_CONTENT_VERSION = 19L

        /** `meta.contentVersion` of an open content DB, or null when missing or not a number. */
        fun contentVersion(driver: SqlDriver): Long? =
            ContentDatabase(driver).contentReadsQueries.metaValue(CONTENT_VERSION_KEY).executeAsOneOrNull()
                ?.toLongOrNull()
    }
}
