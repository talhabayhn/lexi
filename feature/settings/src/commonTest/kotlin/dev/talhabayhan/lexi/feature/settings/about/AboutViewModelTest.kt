package dev.talhabayhan.lexi.feature.settings.about

import app.cash.turbine.test
import dev.talhabayhan.lexi.core.domain.platform.AppLinks
import dev.talhabayhan.lexi.core.domain.platform.StoreLinks
import dev.talhabayhan.lexi.core.testing.FakeReviewRequester
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

    private suspend fun opens(intent: AboutIntent, storeUrl: String = StoreLinks.ANDROID_STORE_URL): AboutEffect {
        val viewModel = AboutViewModel(FakeReviewRequester(storePageUrl = storeUrl))
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
    fun `rate without a store listing opens nothing`() = runTest {
        val viewModel = AboutViewModel(FakeReviewRequester(storePageUrl = ""))
        viewModel.effects.test {
            viewModel.onIntent(AboutIntent.Rate)
            expectNoEvents()
        }
    }

    @Test
    fun `every link is an https page of the app site or a store`() {
        val links = listOf(AppLinks.PRIVACY_POLICY, AppLinks.TERMS_OF_SERVICE, AppLinks.SUPPORT)
        links.forEach { assertTrue(it.startsWith("https://talhabayhn.github.io/lexi/"), it) }
        assertTrue(StoreLinks.ANDROID_STORE_URL.startsWith("https://play.google.com/"))
        assertTrue(StoreLinks.IOS_STORE_URL.startsWith("https://apps.apple.com/"))
    }
}
