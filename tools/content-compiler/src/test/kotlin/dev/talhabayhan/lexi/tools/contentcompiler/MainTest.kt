package dev.talhabayhan.lexi.tools.contentcompiler

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.io.path.deleteExisting
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readBytes
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MainTest {
    private val content: Path = copyFixture("content")
    private val output: Path = createTempDirectory("lexi-main-").resolve("content.db")
    private val out = ByteArrayOutputStream()
    private val err = ByteArrayOutputStream()

    private fun run(vararg args: String, env: Map<String, String> = emptyMap()): Int =
        runCompiler(arrayOf(*args), env, PrintStream(out, true, "UTF-8"), PrintStream(err, true, "UTF-8"))

    private val stdout get() = out.toString("UTF-8")
    private val stderr get() = err.toString("UTF-8")

    @Test
    fun `valid content exits 0 and prints the coverage report`() {
        val code = run("--content", "$content", "--out", "$output")

        assertEquals(0, code, stderr)
        assertTrue(output.exists())
        assertTrue("contentVersion 7" in stdout, stdout)
        assertTrue(Regex("""A1\s+4\s""").containsMatchIn(stdout), stdout)
        assertTrue("food" in stdout, stdout)
        assertTrue("Exam packs: none" in stdout, stdout)
    }

    @Test
    fun `validation errors exit 1 with file and line on stderr and no output`() {
        content.writeCards("cards/A1/bad.csv", cardRow(answer = "zebra"))

        val code = run("--content", "$content", "--out", "$output")

        assertEquals(1, code)
        assertTrue(Regex("""bad\.csv:2: error: answer 'zebra'""").containsMatchIn(stderr), stderr)
        assertFalse(output.exists())
    }

    @Test
    fun `packs come from the flag or the environment variable`() {
        val packs = copyFixture("packs")

        assertEquals(0, run("--content", "$content", "--out", "$output", "--packs", "$packs"), stderr)
        assertTrue("test-pack-one" in stdout, stdout)

        out.reset()
        assertEquals(0, run("--content", "$content", "--out", "$output", env = mapOf(PACKS_ENV to "$packs")), stderr)
        assertTrue("test-pack-two" in stdout, stdout)
    }

    @Test
    fun `compiling twice gives byte-identical output`() {
        val second = output.resolveSibling("second.db")

        assertEquals(0, run("--content", "$content", "--out", "$output"), stderr)
        assertEquals(0, run("--content", "$content", "--out", "$second"), stderr)

        assertContentEquals(output.readBytes(), second.readBytes())
        assertFalse("builtAt" in String(output.readBytes(), Charsets.ISO_8859_1), "no wall-clock timestamp by default")
    }

    @Test
    fun `SOURCE_DATE_EPOCH sets a reproducible builtAt`() {
        val second = output.resolveSibling("second.db")
        val env = mapOf(SOURCE_DATE_EPOCH_ENV to "1700000000")

        assertEquals(0, run("--content", "$content", "--out", "$output", env = env), stderr)
        assertEquals(0, run("--content", "$content", "--out", "$second", env = env), stderr)

        assertContentEquals(output.readBytes(), second.readBytes())
        assertTrue("2023-11-14T22:13:20Z" in String(output.readBytes(), Charsets.ISO_8859_1))
        assertEquals(2, run("--content", "$content", "--out", "$output", env = mapOf(SOURCE_DATE_EPOCH_ENV to "soon")))
    }

    @Test
    fun `the temp file is created under build and removed`() {
        val temp = createTempDirectory("lexi-tmp-")

        assertEquals(0, run("--content", "$content", "--out", "$output", "--tmp", "$temp"), stderr)

        val outputDirFiles = output.parent.listDirectoryEntries().map { it.fileName.toString() }
        assertEquals(listOf(output.fileName.toString()), outputDirFiles)
        assertTrue(temp.listDirectoryEntries().isEmpty())
    }

    @Test
    fun `invalid paths and unwritable outputs exit 2 with a short message`() {
        assertEquals(2, run("--content", "$content", "--out", "bad${Char(0)}path"))
        assertTrue("invalid path" in stderr, stderr)

        val occupied = createTempDirectory("lexi-occupied-")
        occupied.resolve("keep.txt").writeText("x")
        assertEquals(2, run("--content", "$content", "--out", "$occupied"))
        assertTrue(stderr.lines().none { it.trimStart().startsWith("at ") }, "no stack trace: $stderr")
    }

    @Test
    fun `version file holds the contentVersion and is written only with the DB`() {
        val versionFile = output.resolveSibling("content-version.txt")

        assertEquals(0, run("--content", "$content", "--out", "$output", "--version-file", "$versionFile"), stderr)
        assertEquals("7", versionFile.readText())

        versionFile.deleteExisting()
        content.writeCards("cards/A1/bad.csv", cardRow(answer = "zebra"))
        assertEquals(1, run("--content", "$content", "--out", "$output", "--version-file", "$versionFile"))
        assertFalse(versionFile.exists())
    }

    @Test
    fun `usage errors exit 2`() {
        assertEquals(2, run("--bogus"))
        assertEquals(2, run("--out"))
        assertTrue("Usage" in stderr, stderr)
    }

    @Test
    fun `help exits 0`() {
        assertEquals(0, run("--help"))
        assertTrue("--packs" in stdout)
    }
}
