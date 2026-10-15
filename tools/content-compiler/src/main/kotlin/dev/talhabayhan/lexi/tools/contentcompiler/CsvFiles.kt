package dev.talhabayhan.lexi.tools.contentcompiler

import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.CodingErrorAction
import java.nio.file.Files
import java.nio.file.Path

/** One CSV row addressed by column name. */
class CsvRow(val file: Path, val line: Int, private val values: Map<String, String>) {
    operator fun get(column: String): String = values.getValue(column)
}

/** Strict UTF-8 file reading and header-checked CSV tables, reporting into [Diagnostics]. */
object CsvFiles {
    /** UTF-8 bytes read as Latin-1, e.g. `ı` (C4 B1) shows up as `Ä±`. */
    private val DOUBLE_ENCODED = Regex("""[\xC2-\xC5][\x80-\xBF]""")
    private val CONTROL = Regex("""[\p{Cc}&&[^\r\n\t]]|\x{FFFD}""")

    /** Decodes [file] as strict UTF-8; reports the line of the first malformed byte and returns null. */
    fun readUtf8(file: Path, diagnostics: Diagnostics): String? {
        val bytes = Files.readAllBytes(file)
        val decoder = Charsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
        val input = ByteBuffer.wrap(bytes)
        val output = CharBuffer.allocate(bytes.size + 1)
        val result = decoder.decode(input, output, true)
        if (result.isError) {
            val line = 1 + (0 until input.position()).count { bytes[it] == '\n'.code.toByte() }
            diagnostics.error(file, line, "not valid UTF-8 (byte offset ${input.position()}); save the file as UTF-8")
            return null
        }
        decoder.flush(output)
        return output.flip().toString()
    }

    /**
     * Reads a CSV table whose header must contain exactly [columns] (any order). Returns null when the file or
     * header is unusable; malformed rows are reported and skipped. Every value is checked for stray whitespace,
     * control characters and double-encoded UTF-8.
     */
    fun readTable(file: Path, columns: List<String>, diagnostics: Diagnostics): List<CsvRow>? {
        val text = readUtf8(file, diagnostics) ?: return null
        val parsed = CsvParser.parse(text)
        parsed.errors.forEach { diagnostics.error(file, it.line, it.message) }
        val header = parsed.records.firstOrNull()
        if (header == null) {
            diagnostics.error(file, 1, "empty file; expected header: ${columns.joinToString(",")}")
            return null
        }
        val missing = columns - header.fields.toSet()
        val unknown = header.fields - columns.toSet()
        val duplicated = header.fields.groupBy { it }.filterValues { it.size > 1 }.keys
        if (missing.isNotEmpty()) diagnostics.error(file, header.line, "missing column(s): ${missing.joinToString()}")
        if (unknown.isNotEmpty()) diagnostics.error(file, header.line, "unknown column(s): ${unknown.joinToString()}")
        if (duplicated.isNotEmpty()) {
            diagnostics.error(
                file,
                header.line,
                "duplicate column(s): ${duplicated.joinToString()}",
            )
        }
        if (missing.isNotEmpty() || unknown.isNotEmpty() || duplicated.isNotEmpty()) return null

        return parsed.records.drop(1).mapNotNull { record ->
            if (record.fields.size != header.fields.size) {
                diagnostics.error(
                    file,
                    record.line,
                    "expected ${header.fields.size} fields, found ${record.fields.size} (quote values that contain commas)",
                )
                return@mapNotNull null
            }
            val values = header.fields.zip(record.fields).toMap()
            values.forEach { (column, value) -> checkValue(file, record.line, column, value, diagnostics) }
            CsvRow(file, record.line, values)
        }
    }

    private fun checkValue(file: Path, line: Int, column: String, value: String, diagnostics: Diagnostics) {
        if (value != value.trim()) diagnostics.error(file, line, "$column has leading or trailing whitespace")
        if ('\n' in value || '\r' in value) diagnostics.error(file, line, "$column must be a single line")
        if (CONTROL.containsMatchIn(value.replace("\n", "").replace("\r", ""))) {
            diagnostics.error(file, line, "$column contains control or replacement characters")
        }
        if (DOUBLE_ENCODED.containsMatchIn(value)) {
            diagnostics.error(file, line, "$column looks double-encoded (e.g. 'Ä±' for 'ı'); re-save the file as UTF-8")
        }
    }
}
