package dev.talhabayhan.lexi.core.domain.platform

import dev.talhabayhan.lexi.core.domain.settings.AppLanguage
import kotlin.test.Test
import kotlin.test.assertEquals

class AppLinksTest {
    @Test
    fun `English links keep the root site URLs`() {
        assertEquals("https://talhabayhn.github.io/lexi/privacy/", AppLinks.privacyPolicy(turkish = false))
        assertEquals("https://talhabayhn.github.io/lexi/terms/", AppLinks.termsOfService(turkish = false))
        assertEquals("https://talhabayhn.github.io/lexi/support/", AppLinks.support(turkish = false))
        assertEquals("https://talhabayhn.github.io/lexi/", AppLinks.landing(turkish = false))
    }

    @Test
    fun `Turkish links point to the tr site`() {
        assertEquals("https://talhabayhn.github.io/lexi/tr/privacy/", AppLinks.privacyPolicy(turkish = true))
        assertEquals("https://talhabayhn.github.io/lexi/tr/terms/", AppLinks.termsOfService(turkish = true))
        assertEquals("https://talhabayhn.github.io/lexi/tr/support/", AppLinks.support(turkish = true))
        assertEquals("https://talhabayhn.github.io/lexi/tr/", AppLinks.landing(turkish = true))
    }

    @Test
    fun `a chosen language wins over the device language`() {
        assertEquals(true, AppLanguage.TR.isTurkish("en-US"))
        assertEquals(false, AppLanguage.EN.isTurkish("tr-TR"))
    }

    @Test
    fun `the system language follows the device language`() {
        assertEquals(true, AppLanguage.SYSTEM.isTurkish("tr-TR"))
        assertEquals(true, AppLanguage.SYSTEM.isTurkish("tr"))
        assertEquals(false, AppLanguage.SYSTEM.isTurkish("en-US"))
        assertEquals(false, AppLanguage.SYSTEM.isTurkish("de-DE"))
    }
}
