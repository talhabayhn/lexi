package dev.talhabayhan.lexi.feature.settings.about

import app.cash.turbine.test
import dev.talhabayhan.lexi.core.domain.platform.AppLinks
import dev.talhabayhan.lexi.core.domain.platform.StoreLinks
import dev.talhabayhan.lexi.core.domain.settings.AppLanguage
import dev.talhabayhan.lexi.core.domain.settings.Settings
import dev.talhabayhan.lexi.core.testing.FakeReviewRequester
import dev.talhabayhan.lexi.core.testing.FakeSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Every About row opens the right page (store-review audit: each link must reach UrlOpener with its URL). */
@OptIn(ExperimentalCoroutinesApi::class)
class AboutViewModelTest {
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private suspend fun opens(
        intent: AboutIntent,
        storeUrl: String = StoreLinks.ANDROID_STORE_URL,
        language: AppLanguage = AppLanguage.EN,
    ): AboutEffect {
        val viewModel = AboutViewModel(
            FakeReviewRequester(storePageUrl = storeUrl),
            FakeSettingsRepository(Settings(appLanguage = language)),
        )
        lateinit var effect: AboutEffect
        viewModel.effects.test {
            viewModel.onIntent(intent)
            effect = awaitItem()
            cancelAndIgnoreRemainingEvents()
        }
        return effect
    }

    @Test
    fun `privacy opens the privacy policy`() = runTest {
        assertEquals(AboutEffect.OpenUrl(AppLinks.PRIVACY_POLICY), opens(AboutIntent.OpenPrivacy))
    }

    @Test
    fun `terms open the terms of service`() = runTest {
        assertEquals(AboutEffect.OpenUrl(AppLinks.TERMS_OF_SERVICE), opens(AboutIntent.OpenTerms))
    }

    @Test
    fun `support opens the support page`() = runTest {
        assertEquals(AboutEffect.OpenUrl(AppLinks.SUPPORT), opens(AboutIntent.OpenSupport))
    }

    @Test
    fun `rate opens the platform store listing`() = runTest {
        assertEquals(AboutEffect.OpenUrl(StoreLinks.ANDROID_STORE_URL), opens(AboutIntent.Rate))
        assertEquals(
            AboutEffect.OpenUrl(StoreLinks.IOS_STORE_URL),
            opens(AboutIntent.Rate, storeUrl = StoreLinks.IOS_STORE_URL),
        )
    }

    @Test
    fun `back navigates back`() = runTest {
        assertEquals(AboutEffect.NavigateBack, opens(AboutIntent.Back))
    }

    @Test
    fun `a Turkish UI opens the Turkish legal pages`() = runTest {
        val tr = AppLanguage.TR
        assertEquals(
            AboutEffect.OpenUrl("https://talhabayhn.github.io/lexi/tr/privacy/"),
            opens(AboutIntent.OpenPrivacy, language = tr),
        )
        assertEquals(
            AboutEffect.OpenUrl("https://talhabayhn.github.io/lexi/tr/terms/"),
            opens(AboutIntent.OpenTerms, language = tr),
        )
        assertEquals(
            AboutEffect.OpenUrl("https://talhabayhn.github.io/lexi/tr/support/"),
            opens(AboutIntent.OpenSupport, language = tr),
        )
    }

    @Test
    fun `an English UI opens the English legal pages even when the device is Turkish`() = runTest {
        assertEquals(
            AboutEffect.OpenUrl("https://talhabayhn.github.io/lexi/privacy/"),
            opens(AboutIntent.OpenPrivacy, language = AppLanguage.EN),
        )
        assertEquals(
            AboutEffect.OpenUrl("https://talhabayhn.github.io/lexi/support/"),
            opens(AboutIntent.OpenSupport, language = AppLanguage.EN),
        )
    }

    @Test
    fun `rate without a store listing opens nothing`() = runTest {
        val viewModel = AboutViewModel(FakeReviewRequester(storePageUrl = ""), FakeSettingsRepository())
        viewModel.effects.test {
            viewModel.onIntent(AboutIntent.Rate)
            expectNoEvents()
        }
    }

    @Test
    fun `every link is an https page of the app site or a store`() {
        val links = listOf(
            AppLinks.PRIVACY_POLICY,
            AppLinks.TERMS_OF_SERVICE,
            AppLinks.SUPPORT,
            AppLinks.PRIVACY_POLICY_TR,
            AppLinks.TERMS_OF_SERVICE_TR,
            AppLinks.SUPPORT_TR,
        )
        links.forEach { assertTrue(it.startsWith("https://talhabayhn.github.io/lexi/"), it) }
        assertTrue(StoreLinks.ANDROID_STORE_URL.startsWith("https://play.google.com/"))
        assertTrue(StoreLinks.IOS_STORE_URL.startsWith("https://apps.apple.com/"))
    }
}
