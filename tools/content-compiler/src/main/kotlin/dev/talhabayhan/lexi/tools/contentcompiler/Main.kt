package dev.talhabayhan.lexi.tools.contentcompiler

import dev.talhabayhan.lexi.core.domain.AppInfo
import java.io.PrintStream
import java.nio.file.Path
import java.time.Instant
import kotlin.io.path.Path
import kotlin.system.exitProcess

/** Environment variable for the external (private) exam pack directory; `--packs` wins over it. */
const val PACKS_ENV = "LEXI_PACKS_DIR"

const val EXIT_OK = 0
const val EXIT_INVALID = 1
const val EXIT_USAGE = 2

private const val DEFAULT_CONTENT = "content"
private const val DEFAULT_OUTPUT = "shared/src/commonMain/composeResources/files/content.db"

private val USAGE = """
    Usage: content-compiler [--content <dir>] [--out <file>] [--packs <dir>]
      --content <dir>  content source directory (default: $DEFAULT_CONTENT)
      --out <file>     content.db to write (default: $DEFAULT_OUTPUT)
      --packs <dir>    external exam pack directory (default: ${'$'}$PACKS_ENV, else none).
                       Exam pack content never lives in this repository (docs/CONTENT.md §1).
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

            arg in setOf("--content", "--out", "--packs") && i + 1 < args.size -> {
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
    val contentDir = Path(options["--content"] ?: DEFAULT_CONTENT)
    val output = Path(options["--out"] ?: DEFAULT_OUTPUT)
    val packsDir: Path? = (options["--packs"] ?: env[PACKS_ENV]?.takeIf { it.isNotBlank() })?.let { Path(it) }

    val result = try {
        ContentCompiler.compile(contentDir, packsDir, output, builtAt = Instant.now().toString())
    } catch (e: java.io.IOException) {
        err.println("${AppInfo.NAME} content-compiler: I/O error: ${e.message}")
        return EXIT_USAGE
    }

    out.println("${AppInfo.NAME} content-compiler")
    out.println(Report.render(result.validation, packsDir?.toString()))
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
