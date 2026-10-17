package dev.talhabayhan.lexi.tools.contentcompiler

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import dev.talhabayhan.lexi.tools.contentcompiler.db.ContentDatabase
import java.nio.file.Path
import kotlin.io.path.deleteExisting
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExamPackTest {
    private val content: Path = copyFixture("content")
    private val packs: Path = copyFixture("packs")

    private fun validate() = ContentCompiler.validate(content, packs)

    @Test
    fun `without a packs directory there are no packs`() {
        val result = ContentCompiler.validate(content, packsDir = null)

        result.assertNoErrors()
        assertTrue(result.content.packs.isEmpty())
        assertTrue(result.content.packCards.isEmpty())
    }

    @Test
    fun `fixture packs load with their units, metadata and extra cards`() {
        val result = validate()

        result.assertNoErrors()
        assertEquals(listOf("test-pack-one", "test-pack-two"), result.content.packs.map { it.id })
        val packOne = result.content.packs.single { it.id == "test-pack-one" }
        assertEquals("Test Pack One", packOne.name)
        assertEquals("Test Pack One", packOne.titleTr)
        assertEquals("Test Pack One", packOne.titleEn)
        assertEquals("Synthetic fixture pack", packOne.descriptionTr)
        assertEquals("A1-B1", packOne.targetLevelRange)
        assertEquals(10, packOne.cardTarget)
        assertEquals("ABC", packOne.freePreviewLetters)
        assertTrue(packOne.isPro)

        assertEquals(4, result.content.packCards.size)
        assertEquals("Ünite 1", result.content.packCards.single { it.packId == "test-pack-two" }.unit)
        assertEquals(null, result.content.packCards.single { it.cardId == "b1-zest" }.unit)
        val zest = result.content.cards.single { it.id == "b1-zest" }
        assertTrue(zest.fromPack, "extra pack cards are marked so coverage ignores them")
    }

    @Test
    fun `unit rows must reference known cards once per pack`() {
        packs.resolve("units/test-pack-one.csv").writeText("card_id,unit\na1-apple,1\nb1-nothing,1\na1-apple,2\n")
        val result = validate()

        result.assertError("test-pack-one.csv", 3, "unknown card 'b1-nothing'")
        result.assertError("test-pack-one.csv", 4, "duplicate card 'a1-apple' in pack 'test-pack-one'")
    }

    @Test
    fun `unit files need a declared pack and packs without units warn`() {
        packs.resolve("units/test-pack-three.csv").writeText("card_id,unit\na1-apple,1\n")
        packs.resolve("units/test-pack-two.csv").deleteExisting()
        val result = validate()

        result.assertError("test-pack-three.csv", null, "pack 'test-pack-three' is not declared in packs.csv")
        result.assertWarning("packs.csv", 3, "pack 'test-pack-two' has no units file")
    }

    @Test
    fun `packs csv is validated`() {
        packs.resolve("packs.csv").writeText(
            "id,name,description,is_pro,sort\ntest-pack-one,,x,1,0\ntest-pack-two,Two,,yes,z\ntest-pack-two,Two,,1,1\n",
        )
        val result = validate()

        result.assertError("packs.csv", 2, "name must not be empty")
        result.assertError("packs.csv", 3, "is_pro 'yes'")
        result.assertError("packs.csv", 3, "sort 'z'")
        result.assertError("packs.csv", 4, "duplicate pack id 'test-pack-two'")
    }

    @Test
    fun `missing packs-meta csv is an error`() {
        packs.resolve("packs-meta.csv").deleteExisting()
        val result = validate()

        result.assertError("packs-meta.csv", null, "missing in the exam pack directory")
    }

    @Test
    fun `packs-meta csv ids must match packs csv exactly`() {
        packs.resolve("packs-meta.csv").writeText(
            "id,title_tr,title_en,description_tr,target_level_range,card_target,free_preview_letters\n" +
                "test-pack-one,One,One,Desc,A1-B1,10,ABC\n" +
                "test-pack-three,Three,Three,Desc,A1-B1,10,\n",
        )
        val result = validate()

        result.assertError("packs-meta.csv", null, "pack 'test-pack-two' is missing from packs-meta.csv")
        result.assertError("packs-meta.csv", 3, "pack 'test-pack-three' in packs-meta.csv is not declared in packs.csv")
    }

    @Test
    fun `packs-meta csv fields are validated`() {
        packs.resolve("packs-meta.csv").writeText(
            "id,title_tr,title_en,description_tr,target_level_range,card_target,free_preview_letters\n" +
                "test-pack-one,,English,,A1-B1,10,ABC\n" +
                "test-pack-two,Türkçe,,,B1-A1,0,abc\n",
        )
        val result = validate()

        result.assertError("packs-meta.csv", 2, "title_tr must not be empty")
        result.assertError("packs-meta.csv", 3, "title_en must not be empty")
        result.assertError("packs-meta.csv", 3, "target_level_range 'B1-A1' must be <LEVEL>-<LEVEL>")
        result.assertError("packs-meta.csv", 3, "card_target '0' must be a positive integer")
        result.assertError("packs-meta.csv", 3, "free_preview_letters 'abc' must be uppercase A-Z letters")
    }

    @Test
    fun `free_preview_letters cannot contain duplicate letters`() {
        packs.resolve("packs-meta.csv").writeText(
            "id,title_tr,title_en,description_tr,target_level_range,card_target,free_preview_letters\n" +
                "test-pack-one,One,One,,A1-B1,10,ABA\n" +
                "test-pack-two,Two,Two,,A1-B1,10,\n",
        )
        val result = validate()

        result.assertError("packs-meta.csv", 2, "free_preview_letters 'ABA' must not contain duplicate letters")
    }

    private fun aCard(id: String, answer: String): String =
        cardRow(id = id, answer = answer, letter = "A", exampleEn = "Have an $answer.")

    @Test
    fun `duplicate answers within a pack are rejected`() {
        content.writeCards("cards/A1/dup.csv", aCard("a1-apple-clone", "APPLE"))
        packs.resolve("units/test-pack-one.csv").writeText(
            "card_id,unit\na1-apple,1\na1-apple-clone,1\nb1-zest,\n",
        )
        val result = validate()

        result.assertError("dup.csv", 2, "duplicate answer 'APPLE' in pack 'test-pack-one'")
    }

    @Test
    fun `per-pack letter coverage below 3 is an error for shipped packs`() {
        content.resolve("meta.json").writeText(
            """{ "contentVersion": 7, "shippedLevels": [], "shippedPacks": ["test-pack-one"] }""",
        )
        val result = validate()

        result.assertError("test-pack-one.csv", null, "pack 'test-pack-one' letter A has 1 card(s), minimum 3")
        result.assertError("test-pack-one.csv", null, "pack 'test-pack-one' letter C has 0 card(s), minimum 3")
    }

    @Test
    fun `per-pack letter coverage warns below 5 for shipped packs`() {
        content.resolve("meta.json").writeText(
            """{ "contentVersion": 7, "shippedLevels": [], "shippedPacks": ["test-pack-one"] }""",
        )
        content.writeCards(
            "cards/A1/a_cards.csv",
            aCard("a1-avocado", "AVOCADO"),
            aCard("a1-almond", "ALMOND"),
            aCard("a1-apricot", "APRICOT"),
        )
        packs.resolve("units/test-pack-one.csv").writeText(
            "card_id,unit\na1-apple,1\na1-avocado,1\na1-almond,1\na1-apricot,1\n",
        )
        val result = validate()

        result.assertWarning("test-pack-one.csv", null, "pack 'test-pack-one' letter A has 4 card(s), recommended 5")
    }

    @Test
    fun `unshipped packs skip coverage error and emit thin coverage warning`() {
        val result = validate()

        result.assertNoErrors()
        result.assertWarning("test-pack-one.csv", null, "pack 'test-pack-one' (not shipped yet):")
    }

    @Test
    fun `pack loading into in-memory sqlite database`() {
        val result = validate()
        result.assertNoErrors()

        JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).use { driver ->
            ContentDatabase.Schema.create(driver)
            val queries = ContentDatabase(driver).contentWritesQueries
            result.content.packs.forEach {
                queries.insertExamPack(
                    id = it.id,
                    name = it.name,
                    description = it.description,
                    is_pro = if (it.isPro) 1L else 0L,
                    sort = it.sort.toLong(),
                    title_tr = it.titleTr,
                    title_en = it.titleEn,
                    description_tr = it.descriptionTr,
                    level_range = it.targetLevelRange,
                    card_target = it.cardTarget.toLong(),
                    free_preview_letters = it.freePreviewLetters,
                )
            }
            result.content.packCards.forEach { queries.insertExamPackCard(it.packId, it.cardId, it.unit) }

            val rows = driver.executeQuery(
                identifier = null,
                sql = "SELECT id, title_tr, title_en, level_range, card_target, free_preview_letters " +
                    "FROM exam_pack ORDER BY sort",
                mapper = { cursor ->
                    val list = mutableListOf<List<String?>>()
                    while (cursor.next().value) {
                        list += (0 until 6).map { cursor.getString(it) }
                    }
                    QueryResult.Value(list)
                },
                parameters = 0,
            ).value

            assertEquals(2, rows.size)
            assertEquals(listOf("test-pack-one", "Test Pack One", "Test Pack One", "A1-B1", "10", "ABC"), rows[0])
            assertEquals(listOf("test-pack-two", "Test Pack Two", "Test Pack Two", "A1-B1", "10", ""), rows[1])

            val cardRows = driver.executeQuery(
                identifier = null,
                sql = "SELECT pack_id, card_id, unit FROM exam_pack_card ORDER BY pack_id, card_id",
                mapper = { cursor ->
                    val list = mutableListOf<List<String?>>()
                    while (cursor.next().value) {
                        list += (0 until 3).map { cursor.getString(it) }
                    }
                    QueryResult.Value(list)
                },
                parameters = 0,
            ).value

            assertEquals(4, cardRows.size)
            assertEquals(listOf("test-pack-one", "a1-apple", "Unit 1"), cardRows[0])
        }
    }

    @Test
    fun `extra pack cards follow the card rules and must be used by a pack`() {
        packs.writeCards("cards/B1/more.csv", cardRow(id = "b1-yogurt", answer = "ZEST", exampleEn = "Zest."))
        packs.writeCards("cards/B1/unused.csv", cardRow(id = "b1-zebra"))
        val result = validate()

        result.assertError("more.csv", 2, "duplicate id 'b1-yogurt'")
        result.assertError("more.csv", 2, "duplicate answer 'ZEST' in B1")
        result.assertWarning("unused.csv", 2, "pack card 'b1-zebra' is not used by any pack")
    }

    @Test
    fun `a missing packs directory or packs csv is an error`() {
        ContentCompiler.validate(content, packs.resolve("nope")).assertError("nope", null, "does not exist")

        packs.resolve("packs.csv").deleteExisting()
        validate().assertError("packs.csv", null, "missing")
    }

    @Test
    fun `pack card example leaking an answer of its level is rejected`() {
        packs.writeCards(
            "cards/B1/leaking.csv",
            cardRow(
                id = "b1-snack",
                answer = "SNACK",
                letter = "S",
                exampleEn = "Have some ZEST with this snack.",
            ),
        )
        val result = validate()

        result.assertError("leaking.csv", 2, "example_en of 'b1-snack' contains the answer 'ZEST' of 'b1-zest'")
    }

    @Test
    fun `base card example leaking a pack card answer is rejected`() {
        content.writeCards(
            "cards/B1/base_leaker.csv",
            cardRow(
                id = "b1-snack",
                answer = "SNACK",
                letter = "S",
                exampleEn = "Add some zest to the snack.",
            ),
        )
        val result = validate()

        result.assertError("base_leaker.csv", 2, "example_en of 'b1-snack' contains the answer 'ZEST' of 'b1-zest'")
    }

    @Test
    fun `pack card clue leaking its own answer is rejected`() {
        packs.writeCards(
            "cards/B1/clue_leak.csv",
            cardRow(
                id = "b1-leaker",
                answer = "LEAKER",
                letter = "L",
                clueTr = "leaker kelimesi",
            ),
        )
        val result = validate()

        result.assertError("clue_leak.csv", 2, "clue_tr contains the answer 'LEAKER'")
    }
}
