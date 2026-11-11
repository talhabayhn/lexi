package dev.talhabayhan.lexi.core.data.db

import dev.talhabayhan.lexi.core.data.db.ContentDatabaseInstaller.Companion.OLDEST_SCHEMA_COMPATIBLE_CONTENT_VERSION
import dev.talhabayhan.lexi.core.data.testing.ContentFixture
import dev.talhabayhan.lexi.core.data.testing.ContentFixture.FixtureCard
import dev.talhabayhan.lexi.core.data.testing.FakeBundledContentDb
import dev.talhabayhan.lexi.core.data.testing.JvmDatabaseFiles
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import java.io.File
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * A failed install (write or rename) keeps the old copy and leaves no temp file. A readable old copy with the
 * current content schema is used until the next start retries; an older or unreadable one fails the open.
 */
class ContentDatabaseInstallFailureTest {
    private val io = UnconfinedTestDispatcher()
    private val dir: File = createTempDirectory("lexi-install-fail-").toFile()
    private val oldVersion = OLDEST_SCHEMA_COMPATIBLE_CONTENT_VERSION.toInt()
    private val old = ContentFixture.bytes(oldVersion, listOf(FixtureCard("a1-apple", "APPLE")))
    private val new = ContentFixture.bytes(
        oldVersion + 1,
        listOf(FixtureCard("a1-apple", "APPLE"), FixtureCard("a1-bread", "BREAD")),
    )

    @AfterTest
    fun cleanUp() {
        dir.deleteRecursively()
    }

    private enum class Failure { WRITE, RENAME }

    /** Production [AtomicFiles] with the write or the rename failing once. */
    private inner class FailingFiles(private var failure: Failure?) : DatabaseFiles by JvmDatabaseFiles(dir) {
        private val target = File(dir, DatabaseFiles.CONTENT_DB)

        override fun installContentDb(bytes: ByteArray) {
            val fail = failure.also { failure = null }
            AtomicFiles.replaceDatabaseFile(
                target = target,
                bytes = bytes,
                write = { file, data ->
                    if (fail == Failure.WRITE) throw IOException("disk full")
                    AtomicFiles.writeSynced(file, data)
                },
                move = { from: Path, to: Path ->
                    if (fail == Failure.RENAME) throw IOException("rename failed")
                    AtomicFiles.moveAtomically(from, to)
                },
            )
        }
    }

    private suspend fun Databases.installedVersion(): String =
        content.get().contentReadsQueries.metaValue("contentVersion").executeAsOne()

    private fun keepsOldCopy(failure: Failure) = runTest(io) {
        JvmDatabaseFiles(dir).installContentDb(old)
        val files = FailingFiles(failure)

        val firstStart = Databases(FakeBundledContentDb(oldVersion + 1L, new), files, io)
        assertEquals("$oldVersion", firstStart.installedVersion(), "the old copy is used for this run")
        assertEquals(listOf("content.db"), dir.list()!!.sorted(), "no temp file left")
        assertEquals(old.toList(), File(dir, "content.db").readBytes().toList(), "old copy kept")

        val nextStart = Databases(FakeBundledContentDb(oldVersion + 1L, new), files, io)
        assertEquals("${oldVersion + 1}", nextStart.installedVersion(), "the next start retries the install")
    }

    @Test
    fun `failed write keeps using the old copy and the next start retries`() = keepsOldCopy(Failure.WRITE)

    @Test
    fun `failed rename keeps using the old copy and the next start retries`() = keepsOldCopy(Failure.RENAME)

    @Test
    fun `a failed install over a copy with an older content schema fails and the next open retries`() = runTest(io) {
        val older = oldVersion - 1
        JvmDatabaseFiles(dir).installContentDb(ContentFixture.bytes(older, listOf(FixtureCard("a1-apple", "APPLE"))))
        val databases = Databases(FakeBundledContentDb(oldVersion + 1L, new), FailingFiles(Failure.WRITE), io)

        assertFailsWith<IOException> { databases.content.get() }
        assertEquals(listOf("content.db"), dir.list()!!.sorted(), "no temp file left")

        assertEquals("${oldVersion + 1}", databases.installedVersion(), "the next get() retries the install")
    }

    @Test
    fun `a failed first install fails and the next open retries`() = runTest(io) {
        val databases = Databases(FakeBundledContentDb(oldVersion + 1L, new), FailingFiles(Failure.RENAME), io)

        assertFailsWith<IOException> { databases.content.get() }

        assertEquals("${oldVersion + 1}", databases.installedVersion())
    }
}
