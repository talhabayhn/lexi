package dev.talhabayhan.lexi.feature.settings.about

import androidx.lifecycle.ViewModel
import dev.talhabayhan.lexi.core.domain.platform.AppLinks
import dev.talhabayhan.lexi.core.domain.rating.ReviewRequester
import dev.talhabayhan.lexi.core.domain.repository.SettingsRepository
import dev.talhabayhan.lexi.core.domain.settings.AppLanguage
import dev.talhabayhan.lexi.core.platform.language.isTurkishUi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

internal class AboutViewModel(reviewRequester: ReviewRequester, private val settings: SettingsRepository) :
    ViewModel() {
    private val mutableState = MutableStateFlow(AboutUiState(storePageUrl = reviewRequester.storePageUrl))
    val state: StateFlow<AboutUiState> = mutableState.asStateFlow()

    private val effectChannel = Channel<AboutEffect>(Channel.BUFFERED)
    val effects: Flow<AboutEffect> = effectChannel.receiveAsFlow()

    fun onIntent(intent: AboutIntent) {
        when (intent) {
            AboutIntent.Back -> effectChannel.trySend(AboutEffect.NavigateBack)
            AboutIntent.Rate -> openUrl(state.value.storePageUrl)
            AboutIntent.OpenPrivacy -> openUrl(AppLinks.privacyPolicy(uiIsTurkish()))
            AboutIntent.OpenTerms -> openUrl(AppLinks.termsOfService(uiIsTurkish()))
            AboutIntent.OpenSupport -> openUrl(AppLinks.support(uiIsTurkish()))
        }
    }

    private fun uiIsTurkish(): Boolean = (settings.loaded.value?.appLanguage ?: AppLanguage.SYSTEM).isTurkishUi()

    /** A blank URL (no store listing) opens nothing. */
    private fun openUrl(url: String) {
        if (url.isNotBlank()) effectChannel.trySend(AboutEffect.OpenUrl(url))
    }
}
