package dev.talhabayhan.lexi.core.designsystem.motion

import androidx.compose.ui.geometry.Rect
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MorphAnchorsTest {
    private val a = Rect(0f, 0f, 10f, 10f)
    private val b = Rect(5f, 5f, 15f, 15f)

    @Test
    fun `an anchor is found by key until its owner releases it`() {
        val anchors = MorphAnchors()
        val owner = Any()

        anchors.update("play", owner, a)
        assertEquals(a, anchors["play"])

        anchors.release("play", owner)
        assertNull(anchors["play"])
    }

    @Test
    fun `the newest owner wins and an old owner's release keeps it`() {
        val anchors = MorphAnchors()
        val leaving = Any()
        val entering = Any()
        anchors.update("marker", leaving, a)
        anchors.update("marker", entering, b)

        anchors.release("marker", leaving)

        assertEquals(b, anchors["marker"])
    }
}
