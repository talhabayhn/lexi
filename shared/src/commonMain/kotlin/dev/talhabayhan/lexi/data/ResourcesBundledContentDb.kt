package dev.talhabayhan.lexi.data

import dev.talhabayhan.lexi.core.data.db.BundledContentDb
import dev.talhabayhan.lexi.shared.generated.resources.Res

/**
 * The content.db that the build generates into shared's Compose resources (`generateContentDb`, CONTENT.md §1),
 * with the compiler's version file next to it.
 */
internal class ResourcesBundledContentDb : BundledContentDb {
    override suspend fun contentVersion(): Long = Res.readBytes(VERSION_FILE).decodeToString().trim().toLong()

    override suspend fun readBytes(): ByteArray = Res.readBytes(CONTENT_DB_FILE)

    private companion object {
        const val CONTENT_DB_FILE = "files/content.db"
        const val VERSION_FILE = "files/content-version.txt"
    }
}
