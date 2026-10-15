package dev.talhabayhan.lexi.core.data.db

import dev.talhabayhan.lexi.core.data.content.db.ContentDatabase
import dev.talhabayhan.lexi.core.data.user.db.UserDatabase
import kotlinx.coroutines.CoroutineDispatcher

/** Both SQLite databases, opened lazily on the IO dispatcher (content: installed from the bundle first). One per app. */
public class Databases internal constructor(
    internal val content: SuspendLazy<ContentDatabase>,
    internal val user: SuspendLazy<UserDatabase>,
) {
    public constructor(bundled: BundledContentDb, files: DatabaseFiles, io: CoroutineDispatcher) : this(
        content = SuspendLazy(io) { ContentDatabase(ContentDatabaseInstaller(bundled, files).open()) },
        user = SuspendLazy(io) { UserDatabase(files.openUserDb()) },
    )
}
