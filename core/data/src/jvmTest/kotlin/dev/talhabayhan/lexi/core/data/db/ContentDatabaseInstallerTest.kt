package dev.talhabayhan.lexi.core.data.db

import app.cash.sqldelight.db.SqlDriver
import dev.talhabayhan.lexi.core.data.testing.ContentFixture
import dev.talhabayhan.lexi.core.data.testing.ContentFixture.FixtureCard
import dev.talhabayhan.lexi.core.data.testing.FakeBundledContentDb
import dev.talhabayhan.lexi.core.data.testing.JvmDatabaseFiles
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ContentDatabaseInstallerTest {
    private val dir: File = createTempDirectory("lexi-installer-").toFile()
    private val files = JvmDatabaseFiles(dir)
    private val v1 = ContentFixture.bytes(1, listOf(FixtureCard("a1-apple", "APPLE")))
    private val v2 = ContentFixture.bytes(2, listOf(FixtureCard("a1-apple", "APPLE"), FixtureCard("a1-bread", "BREAD")))
    private val opened = mutableListOf<SqlDriver>()

    @AfterTest
    fun cleanUp() {
        opened.forEach { it.close() }
        dir.deleteRecursively()
    }

    private suspend fun open(bundled: FakeBundledContentDb): SqlDriver =
        ContentDatabaseInstaller(bundled, files).open().also { opened += it }

    private fun SqlDriver.version() = ContentDatabaseInstaller.contentVersion(this)

    @Test
    fun `first run copies the bundled DB`() = runTest {
        val driver = open(FakeBundledContentDb(1, v1))

        assertEquals(1, files.installs)
        assertEquals(1L, driver.version())
    }

    @Test
    fun `same contentVersion keeps the installed copy without reading the bundle`() = runTest {
        open(FakeBundledContentDb(1, v1)).close()
        val bundled = FakeBundledContentDb(1, v1)

        val driver = open(bundled)

        assertEquals(1, files.installs)
        assertEquals(0, bundled.reads)
        assertEquals(1L, driver.version())
    }

    @Test
    fun `newer bundled contentVersion replaces the installed copy`() = runTest {
        open(FakeBundledContentDb(1, v1)).close()

        val driver = open(FakeBundledContentDb(2, v2))

        assertEquals(2, files.installs)
        assertEquals(2L, driver.version())
    }

    @Test
    fun `an installed copy that is not a content DB is replaced`() = runTest {
        files.contentFile.writeText("not a database")

        val driver = open(FakeBundledContentDb(1, v1))

        assertEquals(1, files.installs)
        assertEquals(1L, driver.version())
    }

    @Test
    fun `stale side files are removed and no temp file is left`() = runTest {
        File(dir, "content.db-wal").writeText("stale")
        File(dir, "content.db-journal").writeText("stale")

        open(FakeBundledContentDb(1, v1))

        assertEquals(listOf("content.db"), dir.list()!!.sorted())
    }
}
