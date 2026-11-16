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
    fun `the newest attached owner wins even when the older one lays out later`() {
        val anchors = MorphAnchors()
        val leaving = Any()
        val entering = Any()
        anchors.attach("marker", leaving)
        anchors.attach("marker", entering)
        anchors.update("marker", entering, b)

        anchors.update("marker", leaving, a)

        assertEquals(b, anchors["marker"])
        anchors.release("marker", leaving)
        assertEquals(b, anchors["marker"])
    }

    @Test
    fun `an attached owner without a layout yet does not hide the older rectangle`() {
        val anchors = MorphAnchors()
        val older = Any()
        anchors.update("marker", older, a)

        anchors.attach("marker", Any())

        assertEquals(a, anchors["marker"])
    }

    @Test
    fun `releasing the newest owner hands the key back to the older one`() {
        val anchors = MorphAnchors()
        val older = Any()
        val newer = Any()
        anchors.update("marker", older, a)
        anchors.update("marker", newer, b)

        anchors.release("marker", newer)

        assertEquals(a, anchors["marker"])
    }
}
