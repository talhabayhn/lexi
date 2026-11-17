package dev.talhabayhan.lexi

import androidx.compose.runtime.Composable
import dev.talhabayhan.lexi.debug.MotionDebugOverlay

/**
 * Debug builds only (this source set is not part of release, benchmarkRelease or nonMinifiedRelease, which compile the
 * no-op twin in `src/nodebug`): the motion tier switch and frame-time graph ([MotionDebugOverlay], `:shared`).
 */
internal object DebugTools {
    val overlay: (@Composable () -> Unit)? = { MotionDebugOverlay() }
}
