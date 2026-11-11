package dev.talhabayhan.lexi.core.platform.language

import android.app.Activity
import android.app.LocaleManager
import android.content.res.Resources
import android.os.LocaleList
import dev.talhabayhan.lexi.core.domain.settings.AppLanguage
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Locale
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class AndroidPlatformLanguageTest {
    private val tr = Locale.forLanguageTag("tr")
    private val en = Locale.forLanguageTag("en")
    private val de = Locale.forLanguageTag("de")
    private val defaultLocale = Locale.getDefault()

    @After
    fun tearDown() {
        Locale.setDefault(defaultLocale)
    }

    @Test
    fun perAppLanguageFromTheSystemSettingsWins() {
        assertEquals(tr, systemLanguageLocale(listOf(tr), listOf(en), de))
    }

    @Test
    fun withoutAPerAppLanguageTheDeviceLanguageIsUsed() {
        assertEquals(en, systemLanguageLocale(emptyList(), listOf(en, tr), de))
    }

    @Test
    fun withoutAnyLocaleTheFallbackIsUsed() {
        assertEquals(de, systemLanguageLocale(emptyList(), emptyList(), de))
    }

    @Test
    @Config(sdk = [33])
    fun systemLanguageFollowsThePerAppLanguageOnAndroid13() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        Resources.getSystem().configuration.setLocales(LocaleList(en))
        activity.getSystemService(LocaleManager::class.java).applicationLocales = LocaleList(tr)

        AndroidPlatformLanguage.applyLanguage(activity, AppLanguage.SYSTEM)

        assertEquals(tr, activity.resources.configuration.locales[0])
        assertEquals(tr, Locale.getDefault())
    }

    @Test
    @Config(sdk = [33])
    fun systemLanguageUsesTheDeviceLanguageWithoutAPerAppChoice() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        val device = Resources.getSystem().configuration.locales[0]
        activity.getSystemService(LocaleManager::class.java).applicationLocales = LocaleList.getEmptyLocaleList()

        AndroidPlatformLanguage.applyLanguage(activity, AppLanguage.SYSTEM)

        assertEquals(device, activity.resources.configuration.locales[0])
    }
}
