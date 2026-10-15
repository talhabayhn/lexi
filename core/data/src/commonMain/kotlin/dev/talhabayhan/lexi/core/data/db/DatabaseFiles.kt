package dev.talhabayhan.lexi.core.data.db

import app.cash.sqldelight.db.SqlDriver

/**
 * Platform storage and SQLite drivers for both databases (`ARCHITECTURE.md` §6–§7): Android uses the context's
 * database directory, iOS `Application Support/databases`. Blocking; callers run it on the IO dispatcher.
 */
public interface DatabaseFiles {
    /** Whether an installed `content.db` copy exists in app storage. */
    public fun contentDbExists(): Boolean

    /**
     * Replaces the installed `content.db` with [bytes] atomically: writes a temp file in the same directory, then
     * renames it over the old copy (no driver may have the old copy open). Stale `-wal`/`-shm`/`-journal` files of
     * the old copy are removed first, so they can never be applied to the new file.
     */
    public fun installContentDb(bytes: ByteArray)

    /** Opens the installed `content.db` read-only (`PRAGMA query_only`); its schema is never created or migrated. */
    public fun openContentDb(): SqlDriver

    /** Opens (creating or migrating as needed) `user.db`. */
    public fun openUserDb(): SqlDriver

    /** Absolute path of the DataStore Preferences file for settings. */
    public fun settingsFilePath(): String

    public companion object {
        public const val CONTENT_DB: String = "content.db"
        public const val USER_DB: String = "user.db"
        public const val SETTINGS_FILE: String = "settings.preferences_pb"

        /** SQLite side files that belong to a database file. */
        public val SIDE_FILE_SUFFIXES: List<String> = listOf("-journal", "-wal", "-shm")
    }
}

/** `content.db` as bundled with the app (Compose resources, read in `shared`). */
public interface BundledContentDb {
    /** `meta.contentVersion` of the bundled DB, from the version file written next to it by the compiler. */
    public suspend fun contentVersion(): Long

    public suspend fun readBytes(): ByteArray
}
