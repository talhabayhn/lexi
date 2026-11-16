package dev.talhabayhan.lexi.core.designsystem.motion

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals

class MorphMathTest {

    @Test
    fun `rect lerp interpolates every edge`() {
        val from = Rect(0f, 100f, 200f, 160f)
        val to = Rect(300f, 40f, 336f, 76f)

        assertEquals(from, lerpRect(from, to, 0f))
        assertEquals(to, lerpRect(from, to, 1f))
        assertEquals(Rect(150f, 70f, 268f, 118f), lerpRect(from, to, 0.5f))
    }

    @Test
    fun `rect lerp follows an overshooting fraction`() {
        assertEquals(110f, lerpRect(Rect(0f, 0f, 0f, 0f), Rect(100f, 0f, 100f, 0f), 1.1f).left)
    }

    @Test
    fun `colour lerp mixes srgb channels like the prototype`() {
        val red = Color(0xFFE0534C)
        val ink = Color(0xFF1F242D)

        assertEquals(red, lerpSrgb(red, ink, 0f))
        assertEquals(ink, lerpSrgb(red, ink, 1f))
        // Prototype: rgb(round(v + (c2 - v) * e)) → at 0.5: (128, 60, 61).
        val mid = lerpSrgb(red, ink, 0.5f)
        assertEquals(128, (mid.red * 255).roundToInt())
        assertEquals(60, (mid.green * 255).roundToInt())
        assertEquals(61, (mid.blue * 255).roundToInt())
    }

    @Test
    fun `colour lerp clamps the fraction`() {
        assertEquals(Color.Black, lerpSrgb(Color.White, Color.Black, 1.4f))
        assertEquals(Color.White, lerpSrgb(Color.White, Color.Black, -0.2f))
    }
}
