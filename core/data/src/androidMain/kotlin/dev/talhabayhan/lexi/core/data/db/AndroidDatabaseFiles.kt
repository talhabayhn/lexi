package dev.talhabayhan.lexi.core.data.db

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import dev.talhabayhan.lexi.core.data.content.db.ContentDatabase
import dev.talhabayhan.lexi.core.data.db.DatabaseFiles.Companion.CONTENT_DB
import dev.talhabayhan.lexi.core.data.db.DatabaseFiles.Companion.SETTINGS_FILE
import dev.talhabayhan.lexi.core.data.db.DatabaseFiles.Companion.USER_DB
import dev.talhabayhan.lexi.core.data.user.db.UserDatabase
import java.io.File

/** Databases in the app's database directory (`Context.getDatabasePath`), settings in `filesDir/datastore`. */
public class AndroidDatabaseFiles(context: Context) : DatabaseFiles {
    private val context = context.applicationContext

    override fun contentDbExists(): Boolean = context.getDatabasePath(CONTENT_DB).isFile

    override fun installContentDb(bytes: ByteArray) {
        AtomicFiles.replaceDatabaseFile(context.getDatabasePath(CONTENT_DB), bytes)
    }

    override fun openContentDb(): SqlDriver = AndroidSqliteDriver(
        schema = ContentDatabase.Schema,
        context = context,
        name = CONTENT_DB,
        callback = ReadOnlyCallback(),
    )

    override fun openUserDb(): SqlDriver = AndroidSqliteDriver(UserDatabase.Schema, context, USER_DB)

    override fun settingsFilePath(): String = File(context.filesDir, "datastore/$SETTINGS_FILE").absolutePath

    /**
     * content.db is never created or migrated: its `user_version` equals the schema version (written by the
     * compiler), and `query_only` turns any write, including a create or migrate attempt on a wrong file, into an
     * error. The installer then replaces the copy.
     */
    private class ReadOnlyCallback : AndroidSqliteDriver.Callback(ContentDatabase.Schema) {
        override fun onConfigure(db: SupportSQLiteDatabase) {
            db.query("PRAGMA query_only = 1").close()
        }
    }
}
