package dev.talhabayhan.lexi.feature.home

import androidx.compose.runtime.Immutable

@Immutable
internal data class HomeUiState(val isReady: Boolean = true)

internal sealed interface HomeIntent

internal sealed interface HomeEffect
