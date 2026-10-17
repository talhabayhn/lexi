package dev.talhabayhan.lexi.core.data.content

import dev.talhabayhan.lexi.core.data.testing.ContentFixture.FixtureCard
import dev.talhabayhan.lexi.core.data.testing.ContentFixture.FixturePack
import dev.talhabayhan.lexi.core.data.testing.TestDatabases
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.ExamPackId
import dev.talhabayhan.lexi.core.domain.model.LetterRule
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.model.PartOfSpeech
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ContentRepositoriesTest {
    private val io = StandardTestDispatcher()
    private val db = TestDatabases(
        io = io,
        contentVersion = 4,
        cards = listOf(
            FixtureCard("a1-apple", "APPLE", category = "food", tags = "fruit|red"),
            FixtureCard("a1-bread", "BREAD", category = "food", exampleTr = null),
            FixtureCard("a1-box", "BOX", category = "things", rule = "STARTS", pos = "noun"),
            FixtureCard("a1-six", "SIX", letter = 'X', rule = "CONTAINS", category = "things", pos = "adj"),
            FixtureCard("b1-bill", "BILL", level = "B1", category = "travel"),
            FixtureCard("b1-exam", "EXAM", level = "B1", category = "travel", packOnly = true),
            // Broken row (difficulty out of range): skipped, never thrown.
            FixtureCard("a1-cup", "CUP", category = "things", difficulty = 9),
        ),
        packs = listOf(
            FixturePack("lgs", "LGS", sort = 2, cards = mapOf("b1-exam" to "Ünite 2", "a1-apple" to "Ünite 1")),
            FixturePack("yds", "YDS", sort = 1, cards = emptyMap()),
        ),
    )
    private val cards = CardRepositoryImpl(db.databases, io)

    @AfterTest
    fun close() = db.close()

    @Test
    fun `wheel pool of a level and category is sorted by id and maps every column`() = runTest(io) {
        val pool = cards.wheelPool(Level.A1, CategoryId("things"))

        assertEquals(listOf("a1-box", "a1-six"), pool.map { it.id.value })
        val six = pool.last()
        assertEquals('X', six.letter)
        assertEquals(LetterRule.CONTAINS, six.rule)
        assertEquals(PartOfSpeech.ADJ, six.partOfSpeech)
        assertEquals("ipucu six", six.clue)
        assertEquals("An example with SIX.", six.example)
        assertEquals("Örnek.", six.exampleTranslation)
    }

    @Test
    fun `mixed wheel pool has every category of the level`() = runTest(io) {
        val pool = cards.wheelPool(Level.A1, CategoryId.MIXED)

        assertEquals(listOf("a1-apple", "a1-box", "a1-bread", "a1-six"), pool.map { it.id.value })
        assertEquals(setOf("fruit", "red"), pool.first().tags)
        assertEquals("", pool.single { it.id.value == "a1-bread" }.exampleTranslation)
    }

    @Test
    fun `exam-pack-only cards never reach level wheels or totals`() = runTest(io) {
        assertEquals(listOf("b1-bill"), cards.wheelPool(Level.B1).map { it.id.value })
        assertEquals(listOf("b1-bill"), cards.wheelPool(Level.B1, CategoryId("travel")).map { it.id.value })
        assertEquals(
            listOf("a1-apple", "a1-box", "a1-bread", "a1-six", "b1-bill"),
            cards.levelCards().map {
                it.id.value
            },
        )
        assertEquals(mapOf(Level.A1 to 5, Level.B1 to 1), cards.levelTotals())
    }

    @Test
    fun `category of cards covers one level without exam-pack-only cards`() = runTest(io) {
        val a1 = cards.categoryOfCards(Level.A1)

        assertEquals(
            mapOf(
                CardId("a1-apple") to CategoryId("food"),
                CardId("a1-box") to CategoryId("things"),
                CardId("a1-bread") to CategoryId("food"),
                CardId("a1-cup") to CategoryId("things"),
                CardId("a1-six") to CategoryId("things"),
            ),
            a1,
        )
        // The index reads ids only: a card with a broken column elsewhere still counts toward its category total.
        assertEquals(mapOf(CardId("b1-bill") to CategoryId("travel")), cards.categoryOfCards(Level.B1))
        assertEquals(emptyMap(), cards.categoryOfCards(Level.NAT))
    }

    @Test
    fun `cards by id include exam-pack-only cards and skip unknown ids`() = runTest(io) {
        val found = cards.cards(listOf(CardId("b1-exam"), CardId("a1-apple"), CardId("gone")))

        assertEquals(listOf("a1-apple", "b1-exam"), found.map { it.id.value })
    }

    @Test
    fun `categories come in sort order, optionally for one level`() = runTest(io) {
        val categories = CategoryRepositoryImpl(db.databases, io)

        assertEquals(listOf("mixed", "food", "things", "travel"), categories.categories().map { it.id.value })
        assertEquals(listOf("mixed", "travel"), categories.categories(Level.B1).map { it.id.value })
        val travel = categories.categories(Level.B1).last()
        assertEquals(setOf(Level.B1), travel.levels)
        assertEquals(true, travel.isPro)
        assertEquals("Seyahat", travel.nameTr)
    }

    @Test
    fun `exam packs and their cards by unit`() = runTest(io) {
        val packs = ExamPackRepositoryImpl(db.databases, io)

        assertEquals(listOf("yds", "lgs"), packs.packs().map { it.id.value })
        val yds = packs.packs().first()
        assertEquals("yds", yds.id.value)
        assertEquals("YDS", yds.name)
        assertEquals("YDS", yds.titleTr)
        assertEquals("YDS", yds.titleEn)
        assertEquals("A1-B1", yds.levelRange)
        assertEquals(100, yds.cardTarget)
        assertEquals(true, yds.isPro)
        val lgs = packs.cards(ExamPackId("lgs"))
        assertEquals(listOf("a1-apple" to "Ünite 1", "b1-exam" to "Ünite 2"), lgs.map { it.card.id.value to it.unit })
    }

    @Test
    fun `content version comes from meta`() = runTest(io) {
        assertEquals(4L, ContentVersionRepositoryImpl(db.databases, io).contentVersion())
    }
}
