package dev.talhabayhan.lexi.core.designsystem.theme

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertIs

/** Robolectric: `Outline.Rounded` builds an Android `Path` for non-uniform corners. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HalfPillShapeTest {

    private fun outline(width: Float, height: Float): Outline.Rounded =
        assertIs<Outline.Rounded>(HalfPillShape.createOutline(Size(width, height), LayoutDirection.Ltr, Density(1f)))

    @Test
    fun `80 x 160 has a full half-disc on the right`() {
        val rect = outline(80f, 160f).roundRect

        assertEquals(CornerRadius(80f), rect.topRightCornerRadius)
        assertEquals(CornerRadius(80f), rect.bottomRightCornerRadius)
        assertEquals(CornerRadius.Zero, rect.topLeftCornerRadius)
        assertEquals(CornerRadius.Zero, rect.bottomLeftCornerRadius)
        assertEquals(160f, rect.height)
    }

    @Test
    fun `radius is capped at half the height on wide shapes`() {
        val rect = outline(100f, 40f).roundRect

        assertEquals(CornerRadius(20f), rect.topRightCornerRadius)
        assertEquals(CornerRadius(20f), rect.bottomRightCornerRadius)
    }
}
