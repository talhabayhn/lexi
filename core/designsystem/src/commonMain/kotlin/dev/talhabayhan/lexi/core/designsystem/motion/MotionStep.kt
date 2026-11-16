package dev.talhabayhan.lexi.core.designsystem.motion

import androidx.compose.runtime.Immutable
import kotlin.math.roundToInt

/**
 * One timed part of a choreography: it starts [delayMillis] after the sequence starts and runs for [durationMillis].
 * Pure value so whole sequences (dialog flip, letter morph) can be unit-tested as a function of elapsed time.
 */
@Immutable
public data class MotionStep(val delayMillis: Int, val durationMillis: Int) {
    init {
        require(delayMillis >= 0 && durationMillis >= 0) { "negative step: $delayMillis + $durationMillis" }
    }

    /** Sequence time at which this step is complete. */
    val endMillis: Int get() = delayMillis + durationMillis

    /** Linear 0..1 progress at [elapsedMillis] since the sequence started (apply the easing on top). */
    public fun progressAt(elapsedMillis: Long): Float = when {
        elapsedMillis < delayMillis -> 0f
        durationMillis == 0 || elapsedMillis >= endMillis -> 1f
        else -> (elapsedMillis - delayMillis).toFloat() / durationMillis
    }

    /** The same step started [index] × [stepMillis] later (list items entering one after another). */
    public fun staggered(index: Int, stepMillis: Int): MotionStep = copy(delayMillis = delayMillis + index * stepMillis)

    /** The same step started [millis] later. */
    public fun shifted(millis: Int): MotionStep = copy(delayMillis = delayMillis + millis)

    public companion object {
        /**
         * The video-flow prototype (`docs/design/drafts/video-flow/flow.html`, `SPEED = 0.86`) played every timing at
         * 0.86× speed, so the motion the owner approved lasts `prototype value / 0.86` (docs/design/video-flow-notes.md).
         */
        public const val PROTOTYPE_TIME_SCALE: Double = 0.86

        /** Real duration of a prototype value in ms, rounded to the nearest ms. */
        public fun prototypeMillis(prototypeMillis: Int): Int = (prototypeMillis / PROTOTYPE_TIME_SCALE).roundToInt()

        /** A step written in prototype ms (as in `flow.html`), converted to real ms. */
        public fun fromPrototype(delayMillis: Int, durationMillis: Int): MotionStep =
            MotionStep(prototypeMillis(delayMillis), prototypeMillis(durationMillis))
    }
}
