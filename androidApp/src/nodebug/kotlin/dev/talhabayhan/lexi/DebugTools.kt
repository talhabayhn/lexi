package dev.talhabayhan.lexi

import androidx.compose.runtime.Composable

/** Release-like builds (release, benchmarkRelease, nonMinifiedRelease): no developer tools at all. */
internal object DebugTools {
    val overlay: (@Composable () -> Unit)? = null
}
