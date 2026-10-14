package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.animation.core.SnapSpec
import androidx.compose.animation.core.TweenSpec
import dev.talhabayhan.lexi.core.designsystem.theme.LexiMotion
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

class PressAnimationSpecTest {

    @Test
    fun `press animates for 80 ms with the css ease-out curve`() {
        val motion = LexiMotion(reducedMotion = false)

        val spec = assertIs<TweenSpec<Float>>(pressAnimationSpec(motion))

        assertEquals(80, spec.durationMillis)
        assertEquals(0, spec.delay)
        assertSame(motion.press, spec.easing)
    }

    @Test
    fun `press snaps without animation under reduced motion`() {
        assertIs<SnapSpec<Float>>(pressAnimationSpec(LexiMotion(reducedMotion = true)))
    }

    @Test
    fun `press easing is css ease-out`() {
        val easeOut = LexiMotion(reducedMotion = false).press

        // CSS ease-out = cubic-bezier(0, 0, 0.58, 1): fast start, ~69 % done at half time.
        assertEquals(0f, easeOut.transform(0f))
        assertEquals(1f, easeOut.transform(1f))
        assertEquals(0.69f, easeOut.transform(0.5f), absoluteTolerance = 0.01f)
    }
}
