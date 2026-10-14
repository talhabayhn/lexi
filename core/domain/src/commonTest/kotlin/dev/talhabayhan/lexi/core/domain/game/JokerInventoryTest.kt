package dev.talhabayhan.lexi.core.domain.game

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class JokerInventoryTest {
    @Test
    fun `missing joker types count as zero`() {
        assertEquals(0, JokerInventory().count(JokerType.LISTEN))
    }

    @Test
    fun `adding jokers increases the count of that type only`() {
        val inventory = JokerInventory().plus(JokerType.EXAMPLE, 2).plus(JokerType.EXAMPLE, 1)

        assertEquals(3, inventory.count(JokerType.EXAMPLE))
        assertEquals(0, inventory.count(JokerType.REVEAL_LETTER))
    }

    @Test
    fun `consuming a joker decreases its count by one`() {
        val inventory = JokerInventory(mapOf(JokerType.REVEAL_LETTER to 2))

        assertEquals(1, inventory.consume(JokerType.REVEAL_LETTER)?.count(JokerType.REVEAL_LETTER))
    }

    @Test
    fun `consuming an empty joker type is not possible`() {
        assertNull(JokerInventory().consume(JokerType.REMOVE_DISTRACTORS))
    }

    @Test
    fun `inventory rejects negative counts and amounts`() {
        assertFailsWith<IllegalArgumentException> { JokerInventory(mapOf(JokerType.LISTEN to -1)) }
        assertFailsWith<IllegalArgumentException> { JokerInventory().plus(JokerType.LISTEN, -1) }
    }
}
