package dev.talhabayhan.lexi.tools.contentcompiler

import dev.talhabayhan.lexi.core.domain.AppInfo
import java.io.IOException
import java.io.PrintStream
import java.io.UncheckedIOException
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.sql.SQLException
import java.time.Instant
import kotlin.io.path.Path
import kotlin.io.path.deleteIfExists
import kotlin.system.exitProcess

/** Environment variable for the external (private) exam pack directory; `--packs` wins over it. */
const val PACKS_ENV = "LEXI_PACKS_DIR"

/** Reproducible-builds convention: seconds since the epoch, written as `meta.builtAt` when set. */
const val SOURCE_DATE_EPOCH_ENV = "SOURCE_DATE_EPOCH"

const val EXIT_OK = 0
const val EXIT_INVALID = 1
const val EXIT_USAGE = 2

private const val DEFAULT_CONTENT = "content"

// The app build generates its own copy (`generateContentDb`, docs/CONTENT.md §1); a manual run writes here.
private const val DEFAULT_OUTPUT = "build/content/content.db"
private val OPTIONS = setOf("--content", "--out", "--packs", "--tmp", "--version-file")

private val USAGE = """
    Usage: content-compiler [--content <dir>] [--out <file>] [--packs <dir>] [--tmp <dir>] [--version-file <file>]
      --content <dir>  content source directory (default: $DEFAULT_CONTENT)
      --out <file>     content.db to write (default: $DEFAULT_OUTPUT)
      --packs <dir>    external exam pack directory (default: ${'$'}$PACKS_ENV, else none).
                       Exam pack content never lives in this repository (docs/CONTENT.md §1).
      --tmp <dir>      where the DB is built before the atomic move (default: ${ContentCompiler.DEFAULT_TEMP_DIR})
      --version-file <file>  also write meta.contentVersion as plain text (read by the app's copy check)
    ${'$'}$SOURCE_DATE_EPOCH_ENV (seconds) sets meta.builtAt; without it builtAt is omitted (byte-identical output).
    Exit codes: 0 ok, 1 validation errors (nothing written), 2 usage or I/O error.
""".trimIndent()

fun main(args: Array<String>) {
    exitProcess(runCompiler(args, System.getenv(), System.out, System.err))
}

/** CLI entry point without process exit, for tests. */
fun runCompiler(args: Array<String>, env: Map<String, String>, out: PrintStream, err: PrintStream): Int {
    val options = mutableMapOf<String, String>()
    var i = 0
    while (i < args.size) {
        val arg = args[i]
        when {
            arg == "--help" || arg == "-h" -> {
                out.println(USAGE)
                return EXIT_OK
            }

            arg in OPTIONS && i + 1 < args.size -> {
                options[arg] = args[i + 1]
                i += 2
            }

            else -> {
                err.println("${AppInfo.NAME} content-compiler: unexpected argument '$arg'")
                err.println(USAGE)
                return EXIT_USAGE
            }
        }
    }

    val builtAt = env[SOURCE_DATE_EPOCH_ENV]?.takeIf { it.isNotBlank() }?.let { epoch ->
        val seconds = epoch.trim().toLongOrNull()
        if (seconds == null) {
            err.println("${AppInfo.NAME} content-compiler: $SOURCE_DATE_EPOCH_ENV must be seconds since the epoch")
            return EXIT_USAGE
        }
        Instant.ofEpochSecond(seconds).toString()
    }

    fun fail(message: String): Int {
        err.println("${AppInfo.NAME} content-compiler: $message")
        return EXIT_USAGE
    }

    val packsArg = options["--packs"] ?: env[PACKS_ENV]?.takeIf { it.isNotBlank() }
    val output: Path
    val result = try {
        val contentDir = Path(options["--content"] ?: DEFAULT_CONTENT)
        output = Path(options["--out"] ?: DEFAULT_OUTPUT)
        val tempDir = options["--tmp"]?.let { Path(it) } ?: ContentCompiler.DEFAULT_TEMP_DIR
        val packsDir = packsArg?.let { Path(it) }
        val versionFile = options["--version-file"]?.let { Path(it) }
        ContentCompiler.compile(contentDir, packsDir, output, builtAt, tempDir).also { result ->
            if (versionFile == null) return@also
            if (result.written) {
                ContentDbWriter.writeVersionFile(result.validation.content.contentVersion, versionFile, tempDir)
            } else {
                versionFile.deleteIfExists()
            }
        }
    } catch (e: InvalidPathException) {
        return fail("invalid path: ${e.message}")
    } catch (e: IOException) {
        return fail("I/O error: ${e.javaClass.simpleName}: ${e.message}")
    } catch (e: UncheckedIOException) {
        return fail("I/O error: ${e.cause?.javaClass?.simpleName}: ${e.message}")
    } catch (e: SQLException) {
        return fail("SQLite error: ${e.message}")
    }

    out.println("${AppInfo.NAME} content-compiler")
    out.println(Report.render(result.validation, packsArg))
    result.validation.warnings.forEach { out.println(it) }
    result.validation.errors.forEach { err.println(it) }
    return if (result.written) {
        out.println("Wrote $output")
        EXIT_OK
    } else {
        err.println("content.db NOT written: ${result.validation.errors.size} error(s)")
        EXIT_INVALID
    }
}
