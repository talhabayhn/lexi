package dev.talhabayhan.lexi.core.domain.game

import kotlin.test.Test
import kotlin.test.assertEquals

class DuelNamesTest {
    @Test
    fun `names are trimmed and cut to the maximum length`() {
        assertEquals("Ayşe", DuelNames.sanitize("  Ayşe  ", maxLength = 12))
        assertEquals("Abcdefghijkl", DuelNames.sanitize("Abcdefghijklmnop", maxLength = 12))
        assertEquals("", DuelNames.sanitize("   ", maxLength = 12))
    }

    @Test
    fun `line breaks become spaces and a cut never leaves a trailing space`() {
        assertEquals("Ali Veli", DuelNames.sanitize("Ali\nVeli", maxLength = 12))
        assertEquals("Ali", DuelNames.sanitize("Ali  Veli", maxLength = 4))
    }

    @Test
    fun `typing keeps inner and trailing spaces but never more than the maximum`() {
        assertEquals("Ali ", DuelNames.cut("Ali ", maxLength = 12))
        assertEquals("Abc", DuelNames.cut("Abcdef", maxLength = 3))
    }

    @Test
    fun `an emoji counts as one character and is never split`() {
        val smile = "😀"
        assertEquals("ab$smile", DuelNames.sanitize("ab${smile}cd", maxLength = 3))
        assertEquals("a", DuelNames.sanitize("a$smile", maxLength = 1))
    }
}
