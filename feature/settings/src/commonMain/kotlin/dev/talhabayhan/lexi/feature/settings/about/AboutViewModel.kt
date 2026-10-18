package dev.talhabayhan.lexi.feature.settings.about

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

internal class AboutViewModel : ViewModel() {
    private val mutableState = MutableStateFlow(AboutUiState())
    val state: StateFlow<AboutUiState> = mutableState.asStateFlow()

    private val effectChannel = Channel<AboutEffect>(Channel.BUFFERED)
    val effects: Flow<AboutEffect> = effectChannel.receiveAsFlow()

    fun onIntent(intent: AboutIntent) {
        when (intent) {
            AboutIntent.Back -> effectChannel.trySend(AboutEffect.NavigateBack)
            is AboutIntent.OpenUrl -> effectChannel.trySend(AboutEffect.OpenUrl(intent.url))
        }
    }
}
