package dev.talhabayhan.lexi.core.domain.game

import dev.talhabayhan.lexi.core.domain.card
import dev.talhabayhan.lexi.core.domain.model.LetterRule
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class WheelTest {
    @Test
    fun `slot rule comes from its card`() {
        val slot = Slot(letter = 'X', card = card("BOX", letter = 'X'), state = SlotState.TODO)

        assertEquals(LetterRule.CONTAINS, slot.rule)
    }

    @Test
    fun `skipped slot has no card and no rule`() {
        val slot = Slot(letter = 'Q', card = null, state = SlotState.SKIPPED)

        assertNull(slot.rule)
        assertEquals(false, slot.isPlayable)
    }

    @Test
    fun `slot without card must be skipped and a skipped slot has no card`() {
        assertFailsWith<IllegalArgumentException> { Slot('Q', card = null, state = SlotState.TODO) }
        assertFailsWith<IllegalArgumentException> { Slot('A', card = card("APPLE"), state = SlotState.SKIPPED) }
    }

    @Test
    fun `slot card must serve the slot letter`() {
        assertFailsWith<IllegalArgumentException> { Slot('B', card = card("APPLE"), state = SlotState.TODO) }
    }

    @Test
    fun `wheel letters must be in A-Z order without duplicates`() {
        val a = Slot('A', card("APPLE"), SlotState.TODO)
        val b = Slot('B', card("BREAD"), SlotState.TODO)

        assertFailsWith<IllegalArgumentException> { Wheel(listOf(b, a)) }
        assertFailsWith<IllegalArgumentException> { Wheel(listOf(a, a)) }
    }

    @Test
    fun `wheel exposes playable slots and finds slots by letter`() {
        val wheel = Wheel(
            listOf(
                Slot('A', card("APPLE"), SlotState.TODO),
                Slot('B', null, SlotState.SKIPPED),
                Slot('C', card("CHAIR"), SlotState.TODO),
            ),
        )

        assertEquals(listOf('A', 'C'), wheel.playableSlots.map { it.letter })
        assertEquals(2, wheel.indexOf('C'))
        assertEquals(-1, wheel.indexOf('Z'))
    }

    @Test
    fun `wheel survives a JSON round trip`() {
        val wheel = Wheel(
            listOf(
                Slot('A', card("APPLE"), SlotState.CORRECT),
                Slot('B', null, SlotState.SKIPPED),
                Slot('X', card("BOX", letter = 'X'), SlotState.PASSED),
            ),
        )

        val json = Json.encodeToString(Wheel.serializer(), wheel)

        assertEquals(wheel, Json.decodeFromString(Wheel.serializer(), json))
    }

    @Test
    fun `letter key starts available`() {
        assertEquals(KeyState.AVAILABLE, LetterKey('K', isDistractor = true).state)
    }
}
