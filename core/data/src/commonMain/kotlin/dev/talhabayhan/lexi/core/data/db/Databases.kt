package dev.talhabayhan.lexi.core.data.db

import dev.talhabayhan.lexi.core.data.content.db.ContentDatabase
import dev.talhabayhan.lexi.core.data.user.db.UserDatabase
import kotlinx.coroutines.CoroutineDispatcher

/** Both SQLite databases, opened lazily on [io] (content: installed from the bundle first). One per app. */
public class Databases(bundled: BundledContentDb, files: DatabaseFiles, io: CoroutineDispatcher) {
    private val installer = ContentDatabaseInstaller(bundled, files)

    internal val content: LazyDatabase<ContentDatabase> =
        LazyDatabase(io) { ContentDatabase(installer.open()) }

    internal val user: LazyDatabase<UserDatabase> = LazyDatabase(io) { UserDatabase(files.openUserDb()) }
}
