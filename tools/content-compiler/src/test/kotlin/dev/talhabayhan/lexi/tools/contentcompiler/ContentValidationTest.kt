package dev.talhabayhan.lexi.tools.contentcompiler

import java.nio.file.Path
import kotlin.io.path.writeBytes
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ContentValidationTest {
    private val content: Path = copyFixture("content")

    private fun validate() = ContentCompiler.validate(content, packsDir = null)

    private fun withExtraA1(vararg rows: String): ValidationResult {
        content.writeCards("cards/A1/extra.csv", *rows)
        return validate()
    }

    private fun ValidationResult.assertNoErrorAt(line: Int) =
        assertTrue(errors.none { it.line == line }, "unexpected error at line $line:\n" + errors.joinToString("\n"))

    @Test
    fun `the synthetic fixture is valid and keeps Turkish characters`() {
        val result = validate()

        result.assertNoErrors()
        assertEquals(6, result.content.cards.size)
        val tea = result.content.cards.single { it.id == "a1-tea" }
        assertEquals("çay", tea.clueTr)
        assertEquals("Kahve değil, çay içeriz.", tea.exampleTr)
        assertEquals(listOf("drink", "warm"), tea.tags)
        assertEquals(7, result.content.contentVersion)
    }

    @Test
    fun `ids are unique across files and levels`() {
        withExtraA1(cardRow(id = "a1-apple", answer = "AXE", letter = "A", exampleEn = "An axe."))
            .assertError("extra.csv", 2, "duplicate id 'a1-apple'")
    }

    @Test
    fun `ids are lowercase slugs that start with the level`() {
        val result = withExtraA1(
            cardRow(id = "A1-Zebra"),
            cardRow(id = "b1-zebras", answer = "ZEBRAS", exampleEn = "Two zebras."),
            cardRow(id = "a1-horse", answer = "ZOO", exampleEn = "We go to the zoo."),
        )

        result.assertError("extra.csv", 2, "id 'A1-Zebra' must be a lowercase slug")
        result.assertError("extra.csv", 3, "must start with 'a1-'")
        result.assertWarning("extra.csv", 4, "id 'a1-horse' does not follow <level>-<answer>")
    }

    @Test
    fun `answers use the shared AnswerRules charset and length`() {
        val bad = listOf("Zebra", "Z3BRA", "ZEBRA  CROSSING", "Z", "ZABCDEFGHIJKLMN", "ZEBRA-X")
        val rows = bad.mapIndexed { i, answer -> cardRow(id = "a1-z$i", answer = answer, exampleEn = "x $answer x") }
        val result = withExtraA1(*rows.toTypedArray())

        bad.indices.forEach { i -> result.assertError("extra.csv", i + 2, "answer '${bad[i]}'") }
    }

    @Test
    fun `rule must be consistent with the letter`() {
        val result = withExtraA1(
            cardRow(id = "a1-zebra", letter = "B", rule = "STARTS"),
            cardRow(id = "a1-zoo", answer = "ZOO", letter = "Z", rule = "CONTAINS", exampleEn = "The zoo."),
            cardRow(id = "a1-zip", answer = "ZIP", letter = "Q", rule = "CONTAINS", exampleEn = "A zip."),
            cardRow(id = "a1-zen", answer = "ZEN", letter = "ZZ", exampleEn = "Zen."),
            cardRow(id = "a1-zap", answer = "ZAP", rule = "BEGINS", exampleEn = "Zap."),
        )

        result.assertError("extra.csv", 2, "does not start with 'B'")
        result.assertError("extra.csv", 3, "starts with 'Z' (CONTAINS")
        result.assertError("extra.csv", 4, "does not contain 'Q'")
        result.assertError("extra.csv", 5, "letter 'ZZ'")
        result.assertError("extra.csv", 6, "rule 'BEGINS'")
    }

    @Test
    fun `clue must not contain the answer, case and diacritic insensitive`() {
        val result = withExtraA1(
            cardRow(id = "a1-simit", answer = "SIMIT", letter = "S", clueTr = "susamlı SİMİT", exampleEn = "A simit."),
            cardRow(id = "a1-zebra", clueTr = "Zebralar"),
            cardRow(id = "a1-go", answer = "GO", letter = "G", clueTr = "gol atmak", exampleEn = "Go home."),
            cardRow(id = "a1-ox", answer = "OX", letter = "O", clueTr = "öküz, ox", exampleEn = "An ox."),
        )

        result.assertError("extra.csv", 2, "clue_tr contains the answer")
        result.assertError("extra.csv", 3, "clue_tr contains the answer")
        result.assertNoErrorAt(4)
        result.assertError("extra.csv", 5, "clue_tr contains the answer")
    }

    @Test
    fun `example must contain the answer as whole words`() {
        val result = withExtraA1(
            cardRow(id = "a1-zebra", exampleEn = "Zebras run."),
            cardRow(id = "a1-zoo-keeper", answer = "ZOO KEEPER", exampleEn = "The ZOO keeper is here."),
        )

        result.assertError("extra.csv", 2, "example_en must contain the answer")
        result.assertNoErrorAt(3)
    }

    @Test
    fun `an answer may appear once per level`() {
        val result = withExtraA1(
            cardRow(id = "a1-apple-2", answer = "APPLE", letter = "P", rule = "CONTAINS", exampleEn = "An apple."),
        )

        result.assertError("extra.csv", 2, "duplicate answer 'APPLE' in A1 (also at food.csv:2)")
        result.assertError("food.csv", 2, "duplicate answer 'APPLE' in A1 (also at extra.csv:2)")
        assertEquals(2, result.errors.size, "B1 also has APPLE, which is fine: ${result.errors}")
    }

    @Test
    fun `header must have exactly the schema columns and rows the same width`() {
        content.resolve("cards/A1/extra.csv").writeText("id,answer,letter\n")
        validate().assertError("extra.csv", 1, "missing column(s): rule")

        content.resolve("cards/A1/extra.csv").writeText("$CARD_HEADER,notes\n")
        validate().assertError("extra.csv", 1, "unknown column(s): notes")

        content.resolve("cards/A1/extra.csv").writeText("$CARD_HEADER\na1-x,X\n")
        validate().assertError("extra.csv", 2, "expected 11 fields, found 2")
    }

    @Test
    fun `fields must not have leading or trailing spaces`() {
        withExtraA1(cardRow(clueTr = "çizgili hayvan "))
            .assertError("extra.csv", 2, "clue_tr has leading or trailing whitespace")
    }

    @Test
    fun `files must be valid UTF-8 without double-encoded Turkish characters`() {
        val bytes = "$CARD_HEADER\n${cardRow()}\n".toByteArray() + byteArrayOf(0xC3.toByte(), 0x28, 0x0A)
        content.resolve("cards/A1/extra.csv").writeBytes(bytes)
        validate().assertError("extra.csv", 3, "not valid UTF-8")

        withExtraA1(cardRow(clueTr = "çizgili hayvanÄ±")).assertError("extra.csv", 2, "double-encoded")
    }

    @Test
    fun `column values are checked`() {
        val result = withExtraA1(
            cardRow(id = "a1-zebra", clueTr = "x".repeat(61)),
            cardRow(id = "a1-zoo", answer = "ZOO", exampleEn = "A zoo.", exampleTr = ""),
            cardRow(id = "a1-zip", answer = "ZIP", exampleEn = "A zip.", pos = "Noun"),
            cardRow(id = "a1-zen", answer = "ZEN", exampleEn = "Zen.", difficulty = "6"),
            cardRow(id = "a1-zap", answer = "ZAP", exampleEn = "Zap.", difficulty = "x"),
            cardRow(id = "a1-zinc", answer = "ZINC", exampleEn = "Zinc.", tags = "Bad Tag|ok"),
            cardRow(id = "a1-zero", answer = "ZERO", exampleEn = "Zero.", category = "nope"),
            cardRow(id = "a1-zone", answer = "ZONE", exampleEn = "A zone.", category = "mixed"),
            cardRow(id = "a1-zest", answer = "ZEST", exampleEn = "Zest.", clueTr = ""),
        )

        result.assertError("extra.csv", 2, "clue_tr is longer than 60")
        result.assertError("extra.csv", 3, "example_tr must not be empty")
        result.assertError("extra.csv", 4, "pos 'Noun'")
        result.assertError("extra.csv", 5, "difficulty '6'")
        result.assertError("extra.csv", 6, "difficulty 'x'")
        result.assertError("extra.csv", 7, "tag 'Bad Tag'")
        result.assertError("extra.csv", 8, "unknown category 'nope'")
        result.assertError("extra.csv", 9, "virtual category 'mixed'")
        result.assertError("extra.csv", 10, "clue_tr must not be empty")
    }

    @Test
    fun `category not offered for the level is a warning`() {
        content.writeCards("cards/B1/extra.csv", cardRow(id = "b1-zebra", category = "things"))

        validate().assertWarning("extra.csv", 2, "category 'things' is not listed for B1")
    }

    @Test
    fun `blocklisted terms are rejected per language`() {
        val result = withExtraA1(
            cardRow(id = "a1-zebra", exampleEn = "The zebra said badword."),
            cardRow(id = "a1-zoo", answer = "ZOO", exampleEn = "A zoo.", clueTr = "Kötükelime yeri"),
            cardRow(id = "a1-zip", answer = "ZIP", exampleEn = "A zip.", exampleTr = "badword"),
        )

        result.assertError("extra.csv", 2, "blocklisted term 'badword' in example_en")
        result.assertError("extra.csv", 3, "blocklisted term 'kötükelime' in clue_tr")
        result.assertNoErrorAt(4)
    }

    @Test
    fun `unknown level folders are rejected`() {
        content.writeCards("cards/D9/x.csv", cardRow())

        validate().assertError("x.csv", null, "unknown level folder 'D9'")
    }

    @Test
    fun `categories csv is validated`() {
        content.resolve("categories.csv").writeText(
            "id,name_tr,name_en,levels,sort,is_pro\nmixed,Karışık,Mixed,A1|B1,0,0\nfood,Yemek,Food,A1|X2,a,2\n" +
                "things,,Things,A1,2,0\nfood,Yemek,Food,A1,3,0\n",
        )
        val result = validate()

        result.assertError("categories.csv", 3, "level 'X2'")
        result.assertError("categories.csv", 3, "sort 'a'")
        result.assertError("categories.csv", 3, "is_pro '2'")
        result.assertError("categories.csv", 4, "name_tr must not be empty")
        result.assertError("categories.csv", 5, "duplicate category id 'food'")
    }

    @Test
    fun `meta json needs a positive contentVersion and known shipped levels`() {
        content.resolve("meta.json").writeText("""{ "contentVersion": 0, "shippedLevels": ["Z9"] }""")
        val result = validate()

        result.assertError("meta.json", null, "contentVersion")
        result.assertError("meta.json", null, "shippedLevels")
    }

    @Test
    fun `unshipped levels only warn about thin letter coverage`() {
        val result = validate()

        result.assertNoErrors()
        result.assertWarning("meta.json", null, "A1 (not shipped yet)")
    }

    @Test
    fun `shipped levels need at least 3 cards per letter and warn below 5`() {
        content.resolve("meta.json").writeText("""{ "contentVersion": 7, "shippedLevels": ["B1"] }""")
        val rows = ('A'..'Z').flatMap { letter ->
            val count = when (letter) {
                'Q' -> 2
                'K' -> 4
                else -> 5
            }
            (1..count).map { n ->
                val answer = "$letter${"QWXYZ"[n - 1]}A"
                cardRow(
                    id = "b1-${answer.lowercase()}",
                    answer = answer,
                    letter = "$letter",
                    clueTr = "ipucu",
                    exampleEn = "We say $answer here.",
                )
            }
        }
        content.resolve("cards/B1/food.csv").writeText("$CARD_HEADER\n")
        content.writeCards("cards/B1/coverage.csv", *rows.toTypedArray())
        val result = validate()

        result.assertError("meta.json", null, "B1 letter Q has 2 card(s), minimum 3")
        result.assertWarning("meta.json", null, "B1 letter K has 4 card(s), recommended 5")
        assertEquals(1, result.errors.size, result.errors.joinToString("\n"))
    }

    @Test
    fun `the repository content compiles`() {
        val result = ContentCompiler.validate(Path.of("../../content"), packsDir = null)

        result.assertNoErrors()
        assertTrue(result.content.cards.size >= 26)
    }
}
