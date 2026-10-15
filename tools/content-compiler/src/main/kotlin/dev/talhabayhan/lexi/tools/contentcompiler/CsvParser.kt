package dev.talhabayhan.lexi.tools.contentcompiler

/** One CSV record; [line] is the 1-based line where the record starts (quoted fields may span lines). */
data class CsvRecord(val line: Int, val fields: List<String>)

data class CsvError(val line: Int, val message: String)

data class CsvResult(val records: List<CsvRecord>, val errors: List<CsvError>)

/**
 * Strict RFC 4180 parser (comma separator, `"` quoting with `""` escapes, CRLF or LF). A leading UTF-8 BOM is
 * dropped and blank lines are skipped. Malformed records are reported and skipped; parsing continues.
 */
object CsvParser {
    private val BOM = Char(0xFEFF)

    fun parse(text: String): CsvResult {
        val records = mutableListOf<CsvRecord>()
        val errors = mutableListOf<CsvError>()
        val input = text.removePrefix(BOM.toString())
        var i = 0
        var line = 1

        while (i < input.length) {
            val recordLine = line
            val fields = mutableListOf<String>()
            val field = StringBuilder()
            var error: String? = null
            var endOfRecord = false

            while (!endOfRecord && error == null) {
                if (i < input.length && input[i] == '"') {
                    // Quoted field.
                    i++
                    var closed = false
                    while (i < input.length) {
                        val c = input[i]
                        if (c == '"') {
                            if (i + 1 < input.length && input[i + 1] == '"') {
                                field.append('"')
                                i += 2
                                continue
                            }
                            i++
                            closed = true
                            break
                        }
                        if (c == '\n') line++
                        field.append(c)
                        i++
                    }
                    if (!closed) {
                        error = "unterminated quoted field"
                        break
                    }
                    if (i < input.length && input[i] != ',' && !input.isLineBreakAt(i)) {
                        error = "unexpected text after a closing quote"
                        break
                    }
                } else {
                    while (i < input.length && input[i] != ',' && !input.isLineBreakAt(i)) {
                        if (input[i] == '"') {
                            error = "quote inside an unquoted field (quote the whole field and double the quote)"
                            break
                        }
                        field.append(input[i])
                        i++
                    }
                    if (error != null) break
                }
                fields += field.toString()
                field.clear()
                when {
                    i >= input.length -> endOfRecord = true

                    input[i] == ',' -> i++

                    else -> {
                        i = input.skipLineBreak(i)
                        line++
                        endOfRecord = true
                    }
                }
            }

            if (error != null) {
                errors += CsvError(recordLine, error)
                // Resynchronise at the next line.
                while (i < input.length && !input.isLineBreakAt(i)) i++
                if (i < input.length) {
                    i = input.skipLineBreak(i)
                    line++
                }
            } else if (!(fields.size == 1 && fields[0].isEmpty())) {
                records += CsvRecord(recordLine, fields)
            }
        }
        return CsvResult(records, errors)
    }

    private fun String.isLineBreakAt(index: Int): Boolean = this[index] == '\n' || this[index] == '\r'

    private fun String.skipLineBreak(index: Int): Int =
        if (this[index] == '\r' && index + 1 < length && this[index + 1] == '\n') index + 2 else index + 1
}
