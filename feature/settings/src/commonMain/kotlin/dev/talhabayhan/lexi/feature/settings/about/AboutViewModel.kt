package dev.talhabayhan.lexi.feature.settings.about

import androidx.lifecycle.ViewModel
import dev.talhabayhan.lexi.core.domain.platform.AppLinks
import dev.talhabayhan.lexi.core.domain.rating.ReviewRequester
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

internal class AboutViewModel(reviewRequester: ReviewRequester) : ViewModel() {
    private val mutableState = MutableStateFlow(AboutUiState(storePageUrl = reviewRequester.storePageUrl))
    val state: StateFlow<AboutUiState> = mutableState.asStateFlow()

    private val effectChannel = Channel<AboutEffect>(Channel.BUFFERED)
    val effects: Flow<AboutEffect> = effectChannel.receiveAsFlow()

    fun onIntent(intent: AboutIntent) {
        when (intent) {
            AboutIntent.Back -> effectChannel.trySend(AboutEffect.NavigateBack)
            AboutIntent.Rate -> openUrl(state.value.storePageUrl)
            AboutIntent.OpenPrivacy -> openUrl(AppLinks.PRIVACY_POLICY)
            AboutIntent.OpenTerms -> openUrl(AppLinks.TERMS_OF_SERVICE)
            AboutIntent.OpenSupport -> openUrl(AppLinks.SUPPORT)
        }
    }

    /** A blank URL (no store listing) opens nothing. */
    private fun openUrl(url: String) {
        if (url.isNotBlank()) effectChannel.trySend(AboutEffect.OpenUrl(url))
    }
}
