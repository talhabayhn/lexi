package dev.talhabayhan.lexi.feature.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

/** Placeholder home (M0); the designed home screen (`02-home`) replaces it in M4. */
internal class HomeViewModel : ViewModel() {
    private val mutableState = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = mutableState.asStateFlow()

    private val effectChannel = Channel<HomeEffect>(Channel.BUFFERED)
    val effects: Flow<HomeEffect> = effectChannel.receiveAsFlow()

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.StartGame -> effectChannel.trySend(HomeEffect.NavigateToGame)
        }
    }
}
