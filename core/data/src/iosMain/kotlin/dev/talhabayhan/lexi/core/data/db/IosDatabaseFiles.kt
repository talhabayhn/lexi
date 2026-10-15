package dev.talhabayhan.lexi.core.data.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import co.touchlab.sqliter.DatabaseFileContext
import co.touchlab.sqliter.JournalMode
import dev.talhabayhan.lexi.core.data.content.db.ContentDatabase
import dev.talhabayhan.lexi.core.data.db.DatabaseFiles.Companion.CONTENT_DB
import dev.talhabayhan.lexi.core.data.db.DatabaseFiles.Companion.SETTINGS_FILE
import dev.talhabayhan.lexi.core.data.db.DatabaseFiles.Companion.SIDE_FILE_SUFFIXES
import dev.talhabayhan.lexi.core.data.db.DatabaseFiles.Companion.USER_DB
import dev.talhabayhan.lexi.core.data.user.db.UserDatabase
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.writeToFile
import platform.posix.O_RDONLY
import platform.posix.close
import platform.posix.fsync
import platform.posix.open
import platform.posix.rename

/**
 * Databases in `Application Support/databases` (SQLiter's default directory), settings in
 * `Application Support/datastore`.
 */
public class IosDatabaseFiles : DatabaseFiles {
    private val fileManager: NSFileManager get() = NSFileManager.defaultManager

    /** Full path of the installed copy; also creates the databases directory. */
    private fun contentPath(): String = DatabaseFileContext.databasePath(CONTENT_DB, null)

    override fun contentDbExists(): Boolean = fileManager.fileExistsAtPath(contentPath())

    @OptIn(ExperimentalForeignApi::class)
    override fun installContentDb(bytes: ByteArray) {
        val target = contentPath()
        val temp = "$target.tmp"
        try {
            check(bytes.toNSData().writeToFile(temp, atomically = true)) { "could not write $temp" }
            syncToDisk(temp)
            SIDE_FILE_SUFFIXES.forEach { fileManager.removeItemAtPath(target + it, null) }
            check(rename(temp, target) == 0) { "could not rename $temp to $target" }
        } finally {
            fileManager.removeItemAtPath(temp, null)
        }
    }

    /**
     * Read-only: `query_only` on every connection, rollback journal (no WAL files). The compiler writes
     * `user_version` = schema version, so SQLiter never creates or migrates it; on a wrong file that attempt fails
     * and the installer replaces the copy.
     */
    override fun openContentDb(): SqlDriver = NativeSqliteDriver(
        schema = ContentDatabase.Schema,
        name = CONTENT_DB,
        onConfiguration = { config ->
            config.copy(
                journalMode = JournalMode.DELETE,
                lifecycleConfig = config.lifecycleConfig.copy(
                    onCreateConnection = { connection -> connection.rawExecSql("PRAGMA query_only = 1") },
                ),
            )
        },
    )

    override fun openUserDb(): SqlDriver = NativeSqliteDriver(UserDatabase.Schema, USER_DB)

    override fun settingsFilePath(): String {
        val appSupport = NSSearchPathForDirectoriesInDomains(NSApplicationSupportDirectory, NSUserDomainMask, true)
            .first() as String
        return "$appSupport/datastore/$SETTINGS_FILE"
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun ByteArray.toNSData(): NSData = usePinned { pinned ->
    NSData.create(bytes = if (isEmpty()) null else pinned.addressOf(0), length = size.convert())
}

/** fsync, so the temp file's bytes are on disk before it is renamed over the old copy. */
@OptIn(ExperimentalForeignApi::class)
private fun syncToDisk(path: String) {
    val fd = open(path, O_RDONLY)
    check(fd >= 0) { "could not open $path for fsync" }
    try {
        check(fsync(fd) == 0) { "fsync failed for $path" }
    } finally {
        close(fd)
    }
}
