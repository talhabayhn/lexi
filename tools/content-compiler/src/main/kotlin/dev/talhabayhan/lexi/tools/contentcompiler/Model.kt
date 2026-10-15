package dev.talhabayhan.lexi.tools.contentcompiler

import dev.talhabayhan.lexi.core.domain.model.Level
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolute

enum class Severity { ERROR, WARNING }

/** A validation finding; printed as `file:line: error: message` (IDE-clickable). */
data class Diagnostic(val severity: Severity, val file: Path?, val line: Int?, val message: String) {
    override fun toString(): String {
        val location = file?.let { displayPath(it) + (line?.let { l -> ":$l" } ?: "") + ": " } ?: ""
        return "$location${severity.name.lowercase()}: $message"
    }

    private fun displayPath(path: Path): String {
        val cwd = Path("").absolute()
        val absolute = path.absolute().normalize()
        return (if (absolute.startsWith(cwd)) cwd.relativize(absolute) else absolute).toString().replace('\\', '/')
    }
}

/** Collects diagnostics while loading and validating content. */
class Diagnostics {
    private val items = mutableListOf<Diagnostic>()

    val all: List<Diagnostic> get() = items.toList()

    fun error(file: Path?, line: Int?, message: String) {
        items += Diagnostic(Severity.ERROR, file, line, message)
    }

    fun warning(file: Path?, line: Int?, message: String) {
        items += Diagnostic(Severity.WARNING, file, line, message)
    }
}

data class Source(val file: Path, val line: Int)

data class CategoryEntry(
    val id: String,
    val nameTr: String,
    val nameEn: String,
    val levels: List<Level>,
    val sort: Int,
    val isPro: Boolean,
)

data class CardEntry(
    val id: String,
    val level: Level,
    val categoryId: String,
    val answer: String,
    val letter: Char,
    val rule: String,
    val pos: String,
    val clueTr: String,
    val exampleEn: String,
    val exampleTr: String,
    val difficulty: Int,
    val tags: List<String>,
    /** Extra card from an external exam pack directory; excluded from level coverage. */
    val fromPack: Boolean,
    val source: Source,
)

data class PackEntry(
    val id: String,
    val name: String,
    val description: String,
    val isPro: Boolean,
    val sort: Int,
    val source: Source,
)

data class PackCardEntry(val packId: String, val cardId: String, val unit: String?)

data class CompiledContent(
    val contentVersion: Int,
    val shippedLevels: Set<Level>,
    val categories: List<CategoryEntry>,
    val cards: List<CardEntry>,
    val packs: List<PackEntry>,
    val packCards: List<PackCardEntry>,
)

data class ValidationResult(val content: CompiledContent, val diagnostics: List<Diagnostic>) {
    val errors: List<Diagnostic> get() = diagnostics.filter { it.severity == Severity.ERROR }
    val warnings: List<Diagnostic> get() = diagnostics.filter { it.severity == Severity.WARNING }

    override fun toString(): String = diagnostics.joinToString("\n")
}

data class CompileResult(val validation: ValidationResult, val written: Boolean)
