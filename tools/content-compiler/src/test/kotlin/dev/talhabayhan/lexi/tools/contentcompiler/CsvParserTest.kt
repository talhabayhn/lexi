package dev.talhabayhan.lexi.tools.contentcompiler

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CsvParserTest {
    private fun parse(text: String) = CsvParser.parse(text)

    @Test
    fun `plain records keep their 1-based line numbers`() {
        val result = parse("a,b,c\n1,2,3\n")

        assertEquals(emptyList(), result.errors)
        assertEquals(listOf(listOf("a", "b", "c"), listOf("1", "2", "3")), result.records.map { it.fields })
        assertEquals(listOf(1, 2), result.records.map { it.line })
    }

    @Test
    fun `quoted fields may contain commas, escaped quotes and line breaks`() {
        val result = parse("id,text\n1,\"a, \"\"b\"\"\nc\"\n2,d\n")

        assertEquals(emptyList(), result.errors)
        assertEquals(listOf("1", "a, \"b\"\nc"), result.records[1].fields)
        assertEquals(2, result.records[1].line)
        assertEquals(4, result.records[2].line)
    }

    @Test
    fun `CRLF line endings, missing final newline and an empty last field are handled`() {
        val result = parse("a,b\r\n1,\r\n2,x")

        assertEquals(listOf(listOf("a", "b"), listOf("1", ""), listOf("2", "x")), result.records.map { it.fields })
    }

    @Test
    fun `a UTF-8 byte order mark is ignored and Turkish characters survive`() {
        val result = parse("${Char(0xFEFF)}clue\nçğıİöşü ÇĞÖŞÜ\n")

        assertEquals(listOf("clue"), result.records[0].fields)
        assertEquals(listOf("çğıİöşü ÇĞÖŞÜ"), result.records[1].fields)
    }

    @Test
    fun `blank lines are skipped`() {
        val result = parse("a\n\n1\n\n")

        assertEquals(listOf(1, 3), result.records.map { it.line })
    }

    @Test
    fun `an unterminated quote is an error at the line where the field started`() {
        val result = parse("a,b\n1,\"open\n2,3\n")

        assertEquals(1, result.errors.size)
        assertEquals(2, result.errors.single().line)
        assertTrue("unterminated" in result.errors.single().message)
    }

    @Test
    fun `text after a closing quote and a stray quote in an unquoted field are errors`() {
        assertEquals(2, parse("a\n\"x\"y\n").errors.single().line)
        assertEquals(3, parse("a\nb\nx\"y\n").errors.single().line)
    }
}
