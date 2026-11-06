package dev.talhabayhan.lexi.core.domain.reminder

import dev.talhabayhan.lexi.core.domain.model.CardId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReminderWordPickerTest {
    private fun ids(prefix: String, n: Int) = List(n) { CardId("$prefix$it") }

    @Test
    fun `due words come before other answered words`() {
        val due = ids("due", 3)
        val answered = ids("a", 10)

        val picked = ReminderWordPicker.pick(due, answered, count = 7, seed = 1L)

        assertEquals(7, picked.size)
        assertEquals(due.toSet(), picked.take(3).toSet())
        assertTrue(picked.drop(3).all { it in answered })
    }

    @Test
    fun `a word is never picked twice even when it is both due and answered`() {
        val due = ids("c", 4)
        val answered = ids("c", 4) + ids("x", 6)

        val picked = ReminderWordPicker.pick(due, answered, count = 7, seed = 9L)

        assertEquals(picked.distinct(), picked)
        assertEquals(7, picked.size)
    }

    @Test
    fun `same seed and input give the same words regardless of input order`() {
        val due = ids("due", 5)
        val answered = ids("a", 20)

        val first = ReminderWordPicker.pick(due, answered, 7, seed = 42L)
        val again = ReminderWordPicker.pick(due.reversed(), answered.reversed(), 7, seed = 42L)

        assertEquals(first, again)
        assertTrue(first != ReminderWordPicker.pick(due, answered, 7, seed = 43L))
    }

    @Test
    fun `fewer words than slots returns only what exists`() {
        assertEquals(2, ReminderWordPicker.pick(emptyList(), ids("a", 2), 7, seed = 1L).size)
        assertEquals(emptyList(), ReminderWordPicker.pick(emptyList(), emptyList(), 7, seed = 1L))
        assertEquals(emptyList(), ReminderWordPicker.pick(ids("d", 3), ids("a", 3), 0, seed = 1L))
    }

    @Test
    fun `display text lowercases the answer and truncates a long meaning`() {
        assertEquals("kitchen", ReminderWordPicker.displayWord("KITCHEN"))
        val long = "a".repeat(100)
        val shown = ReminderWordPicker.displayMeaning(long)
        assertEquals(ReminderWordPicker.MAX_MEANING_CHARS, shown.length)
        assertTrue(shown.endsWith("…"))
        assertEquals("mutfak", ReminderWordPicker.displayMeaning("  mutfak "))
    }
}
