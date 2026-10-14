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
    fun `daily duel and level test ignore card history so everyone gets the same wheel`() {
        val ignoring = GameMode.entries.filterNot { it.usesCardHistory }.toSet()

        assertEquals(setOf(GameMode.DAILY, GameMode.DUEL, GameMode.LEVEL_TEST), ignoring)
    }
}
