package dev.talhabayhan.lexi.feature.home

import androidx.compose.runtime.Immutable

@Immutable
internal data class HomeUiState(val isReady: Boolean = true)

internal sealed interface HomeIntent {
    /** Temporary placeholder entry: start an A1 classic game (until 02-home / 03-level-select). */
    data object StartGame : HomeIntent
}

internal sealed interface HomeEffect {
    data object NavigateToGame : HomeEffect
}
