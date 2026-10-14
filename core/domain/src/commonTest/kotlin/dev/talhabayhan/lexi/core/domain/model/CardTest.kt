package dev.talhabayhan.lexi.core.domain.model

import dev.talhabayhan.lexi.core.domain.card
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CardTest {
    @Test
    fun `STARTS rule is satisfied when the answer starts with the letter`() {
        assertTrue(LetterRule.STARTS.isSatisfiedBy(answer = "KITCHEN", letter = 'K'))
        assertFalse(LetterRule.STARTS.isSatisfiedBy(answer = "BAKER", letter = 'K'))
    }

    @Test
    fun `CONTAINS rule needs the letter inside but not as first letter`() {
        assertTrue(LetterRule.CONTAINS.isSatisfiedBy(answer = "BOX", letter = 'X'))
        assertFalse(LetterRule.CONTAINS.isSatisfiedBy(answer = "XRAY", letter = 'X'))
        assertFalse(LetterRule.CONTAINS.isSatisfiedBy(answer = "BOOK", letter = 'X'))
    }

    @Test
    fun `answer letters exclude the fixed spaces of multi-word answers`() {
        assertEquals(6, card("GIVE UP").answerLetters)
        assertEquals(5, card("APPLE").answerLetters)
    }

    @Test
    fun `card rejects answers outside uppercase A-Z and single spaces`() {
        assertFailsWith<IllegalArgumentException> { card("apple", letter = 'A', rule = LetterRule.STARTS) }
        assertFailsWith<IllegalArgumentException> { card("GIVE  UP") }
        assertFailsWith<IllegalArgumentException> { card(" GIVE") }
        assertFailsWith<IllegalArgumentException> { card("ÇAY", letter = 'C', rule = LetterRule.STARTS) }
    }

    @Test
    fun `card answers have 2 to 14 letters`() {
        assertEquals(2, card("GO").answerLetters)
        assertEquals(14, card("ABCDEFG HIJKLMN").answerLetters)
        assertFailsWith<IllegalArgumentException> { card("A") }
        assertFailsWith<IllegalArgumentException> { card("ABCDEFGHIJKLMNO") }
    }

    @Test
    fun `card rejects a letter outside A-Z`() {
        assertFailsWith<IllegalArgumentException> { card("APPLE", letter = 'a', rule = LetterRule.CONTAINS) }
    }

    @Test
    fun `card rejects a rule that does not match its letter`() {
        assertFailsWith<IllegalArgumentException> { card("BOX", letter = 'X', rule = LetterRule.STARTS) }
        assertFailsWith<IllegalArgumentException> { card("BOX", letter = 'B', rule = LetterRule.CONTAINS) }
    }

    @Test
    fun `card rejects difficulty outside 1 to 5`() {
        val valid = card("APPLE")
        assertFailsWith<IllegalArgumentException> { valid.copy(difficulty = 0) }
        assertFailsWith<IllegalArgumentException> { valid.copy(difficulty = 6) }
    }

    @Test
    fun `mixed category id is the virtual all-categories id`() {
        assertEquals("mixed", CategoryId.MIXED.value)
    }

    @Test
    fun `levels are ordered A1 to NAT`() {
        assertEquals(listOf("A1", "A2", "B1", "B2", "C1", "NAT"), Level.entries.map { it.name })
    }
}
