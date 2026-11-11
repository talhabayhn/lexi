package dev.talhabayhan.lexi.core.platform.share

import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ShareFileTest {
    private val dir: File = Files.createTempDirectory("lexi-share-test").toFile().resolve("share")

    @AfterTest
    fun tearDown() {
        dir.parentFile.deleteRecursively()
    }

    @Test
    fun everyShareGetsItsOwnFileAndKeepsTheEarlierCard() {
        val first = writeShareFile(dir, byteArrayOf(1))
        val second = writeShareFile(dir, byteArrayOf(2))

        assertNotEquals(first, second)
        assertContentEquals(byteArrayOf(1), first.readBytes())
        assertContentEquals(byteArrayOf(2), second.readBytes())
        assertTrue(first.name.startsWith("lexi-share") && first.name.endsWith(".png"))
    }

    @Test
    fun onlyTheNewestFilesAreKept() {
        val written = List(SHARE_FILES_KEPT + 2) { i ->
            writeShareFile(dir, byteArrayOf(i.toByte())).also { it.setLastModified(1_000_000L + i * 1_000L) }
        }
        // The newest one may carry an older timestamp than the rest (coarse clock): it is kept anyway.
        val latest = writeShareFile(dir, byteArrayOf(9)).also { it.setLastModified(0L) }

        val kept = dir.listFiles()!!.toSet()
        assertEquals(SHARE_FILES_KEPT, kept.size)
        assertTrue(latest in kept)
        assertEquals(written.takeLast(SHARE_FILES_KEPT - 1).toSet() + latest, kept)
    }

    @Test
    fun theFormerSingleShareFileIsPruned() {
        dir.mkdirs()
        val legacy = File(dir, "lexi-share.png").apply {
            writeBytes(byteArrayOf(0))
            setLastModified(1L)
        }
        repeat(SHARE_FILES_KEPT) { writeShareFile(dir, byteArrayOf(it.toByte())) }

        assertTrue(!legacy.exists())
    }
}
