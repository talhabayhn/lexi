package dev.talhabayhan.lexi.tools.contentcompiler

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
    fun `fixture packs load with their units and extra cards`() {
        val result = validate()

        result.assertNoErrors()
        assertEquals(listOf("test-pack-one", "test-pack-two"), result.content.packs.map { it.id })
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
}
