package dev.talhabayhan.lexi.core.designsystem.motion

import kotlin.test.Test
import kotlin.test.assertEquals

class MotionStepTest {

    @Test
    fun `prototype values are scaled by 1 over 0_86 and rounded`() {
        assertEquals(93, MotionStep.prototypeMillis(80))
        assertEquals(140, MotionStep.prototypeMillis(120))
        assertEquals(302, MotionStep.prototypeMillis(260))
        assertEquals(349, MotionStep.prototypeMillis(300))
        assertEquals(0, MotionStep.prototypeMillis(0))
    }

    @Test
    fun `from prototype scales delay and duration separately`() {
        assertEquals(MotionStep(delayMillis = 291, durationMillis = 233), MotionStep.fromPrototype(250, 200))
    }

    @Test
    fun `progress is 0 before the delay and linear inside and 1 after the end`() {
        val step = MotionStep(delayMillis = 100, durationMillis = 200)

        assertEquals(0f, step.progressAt(0))
        assertEquals(0f, step.progressAt(100))
        assertEquals(0.5f, step.progressAt(200))
        assertEquals(1f, step.progressAt(300))
        assertEquals(1f, step.progressAt(10_000))
        assertEquals(300, step.endMillis)
    }

    @Test
    fun `a zero duration step jumps at its delay`() {
        val step = MotionStep(delayMillis = 50, durationMillis = 0)

        assertEquals(0f, step.progressAt(49))
        assertEquals(1f, step.progressAt(50))
    }

    @Test
    fun `stagger shifts the delay per index and shifted moves the whole step`() {
        val step = MotionStep(delayMillis = 10, durationMillis = 200)

        assertEquals(MotionStep(delayMillis = 10, durationMillis = 200), step.staggered(index = 0, stepMillis = 23))
        assertEquals(MotionStep(delayMillis = 79, durationMillis = 200), step.staggered(index = 3, stepMillis = 23))
        assertEquals(MotionStep(delayMillis = 510, durationMillis = 200), step.shifted(500))
    }
}
