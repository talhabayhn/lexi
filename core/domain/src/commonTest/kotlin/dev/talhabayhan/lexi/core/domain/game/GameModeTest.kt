package dev.talhabayhan.lexi.core.domain.game

import kotlin.test.Test
import kotlin.test.assertEquals

class GameModeTest {
    @Test
    fun `classic hardcore daily and duel are scored`() {
        val scored = GameMode.entries.filter { it.isScored }.toSet()

        assertEquals(setOf(GameMode.CLASSIC, GameMode.HARDCORE, GameMode.DAILY, GameMode.DUEL), scored)
    }

    @Test
    fun `only hardcore ends on the first wrong answer`() {
        assertEquals(listOf(GameMode.HARDCORE), GameMode.entries.filter { it.endsOnFirstWrong })
    }

    @Test
    fun `only classic practice and hardcore wheels use card history`() {
        val using = GameMode.entries.filter { it.usesCardHistory }.toSet()

        assertEquals(setOf(GameMode.CLASSIC, GameMode.PRACTICE, GameMode.HARDCORE), using)
    }
}
