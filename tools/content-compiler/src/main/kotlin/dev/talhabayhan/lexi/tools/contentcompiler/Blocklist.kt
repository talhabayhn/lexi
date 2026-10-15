package dev.talhabayhan.lexi.tools.contentcompiler

import java.nio.file.Path

/**
 * Profanity/blocklist from `content/blocklist.txt`: `[en]` terms are checked in `answer` and `example_en`, `[tr]`
 * terms in `clue_tr` and `example_tr`; whole-word. English: case- and diacritic-insensitive. Turkish: Turkish
 * case folding with diacritics kept (`sik` must not flag `sık` or `şık`). `#` starts a comment.
 */
class Blocklist(private val english: List<String>, private val turkish: List<String>) {

    /** Reports every hit in [row]; returns false when there was one. */
    fun check(row: CsvRow, diagnostics: Diagnostics): Boolean {
        var clean = true
        val checks = listOf(
            Triple(english, EN_COLUMNS, TextMatch::containsWords),
            Triple(turkish, TR_COLUMNS, TextMatch::containsTurkishWords),
        )
        for ((terms, columns, matches) in checks) {
            for (column in columns) {
                terms.filter { matches(row[column], it) }.forEach { term ->
                    clean = false
                    diagnostics.error(row.file, row.line, "blocklisted term '$term' in $column")
                }
            }
        }
        return clean
    }

    companion object {
        private val EN_COLUMNS = listOf("answer", "example_en")
        private val TR_COLUMNS = listOf("clue_tr", "example_tr")

        val EMPTY = Blocklist(emptyList(), emptyList())

        fun load(file: Path, diagnostics: Diagnostics): Blocklist {
            val text = CsvFiles.readUtf8(file, diagnostics) ?: return EMPTY
            val english = mutableListOf<String>()
            val turkish = mutableListOf<String>()
            var section: MutableList<String>? = null
            text.lines().forEachIndexed { index, raw ->
                val line = raw.substringBefore('#').trim()
                when {
                    line.isEmpty() -> Unit
                    line == "[en]" -> section = english
                    line == "[tr]" -> section = turkish
                    section == null -> diagnostics.error(file, index + 1, "term before a [en] or [tr] section")
                    else -> section?.add(line)
                }
            }
            return Blocklist(english, turkish)
        }
    }
}
