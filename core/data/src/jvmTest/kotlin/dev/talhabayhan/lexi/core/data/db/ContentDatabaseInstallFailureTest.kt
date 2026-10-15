package dev.talhabayhan.lexi.core.data.db

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

/** A failed install (write or rename) keeps the old copy, leaves no temp file, and the next open retries. */
class ContentDatabaseInstallFailureTest {
    private val io = UnconfinedTestDispatcher()
    private val dir: File = createTempDirectory("lexi-install-fail-").toFile()
    private val v1 = ContentFixture.bytes(1, listOf(FixtureCard("a1-apple", "APPLE")))
    private val v2 = ContentFixture.bytes(2, listOf(FixtureCard("a1-apple", "APPLE"), FixtureCard("a1-bread", "BREAD")))

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

    private fun check(failure: Failure) = runTest(io) {
        JvmDatabaseFiles(dir).installContentDb(v1)
        val files = FailingFiles(failure)
        val databases = Databases(FakeBundledContentDb(2, v2), files, io)

        assertFailsWith<IOException> { databases.content.get() }
        assertEquals(listOf("content.db"), dir.list()!!.sorted(), "no temp file left")
        assertEquals(v1.toList(), File(dir, "content.db").readBytes().toList(), "old copy kept")

        val version = databases.content.get().contentReadsQueries.metaValue("contentVersion").executeAsOne()
        assertEquals("2", version, "the next get() retries the install")
    }

    @Test
    fun `failed write keeps the old copy and the next open retries`() = check(Failure.WRITE)

    @Test
    fun `failed rename keeps the old copy and the next open retries`() = check(Failure.RENAME)
}
