package dev.talhabayhan.lexi.core.platform.uri

import dev.talhabayhan.lexi.core.domain.platform.AppLinks
import dev.talhabayhan.lexi.core.domain.platform.StoreLinks
import platform.Foundation.NSURL
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IosUrlOpenerTest {
    private val launched = mutableListOf<String?>()
    private val opener = IosUrlOpener { url: NSURL -> launched += url.absoluteString }

    // Regression: links were gated by canOpenURL and sent to the deprecated openURL:, which does nothing on iOS 18+.
    @Test
    fun `every app link is handed to the launcher`() {
        val links = listOf(
            AppLinks.PRIVACY_POLICY,
            AppLinks.TERMS_OF_SERVICE,
            AppLinks.SUPPORT,
            StoreLinks.IOS_STORE_URL,
            StoreLinks.ANDROID_STORE_URL,
        )

        links.forEach { assertTrue(opener.openUrl(it), it) }

        assertEquals<List<String?>>(links, launched)
    }

    @Test
    fun `malformed and non-web links are refused without launching`() {
        assertFalse(opener.openUrl(""))
        assertFalse(opener.openUrl("not a url"))
        assertFalse(opener.openUrl("javascript:alert(1)"))
        assertTrue(launched.isEmpty())
    }
}
