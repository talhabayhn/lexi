package dev.talhabayhan.lexi.reminder

import dev.talhabayhan.lexi.core.domain.reminder.ReminderVariant
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Locale
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class ComposeReminderTextProviderTest {
    private val original = Locale.getDefault()
    private val provider = ComposeReminderTextProvider()

    @After
    fun tearDown() {
        Locale.setDefault(original)
    }

    private fun assertEveryVariantResolves(language: String) = runBlocking {
        Locale.setDefault(Locale.forLanguageTag(language))
        for (variant in ReminderVariant.entries) {
            val text = provider.text(variant, 12)
            for (part in listOf(text.title, text.body)) {
                assertTrue(part.isNotBlank(), "$language $variant is blank")
                assertFalse(part.contains("\\"), "$language $variant has a literal backslash: $part")
                assertFalse(part.contains("{n}"), "$language $variant leaves the count token: $part")
            }
        }
    }

    @Test
    fun everyVariantResolvesInTurkish() = assertEveryVariantResolves("tr")

    @Test
    fun everyVariantResolvesInEnglish() = assertEveryVariantResolves("en")

    @Test
    fun countVariantsInsertTheNumber() = runBlocking {
        Locale.setDefault(Locale.forLanguageTag("en"))
        assertTrue(provider.text(ReminderVariant.STREAK_1, 12).title.contains("12"))
        assertTrue(provider.text(ReminderVariant.STREAK_3, 12).title.contains("12"))
        assertTrue(provider.text(ReminderVariant.REVIEW_1, 12).title.contains("12"))
        assertTrue(provider.text(ReminderVariant.REVIEW_2, 12).body.contains("12"))
    }

    @Test
    fun turkishAndEnglishDiffer() = runBlocking {
        Locale.setDefault(Locale.forLanguageTag("tr"))
        val tr = provider.text(ReminderVariant.DAILY_1, null)
        Locale.setDefault(Locale.forLanguageTag("en"))
        val en = provider.text(ReminderVariant.DAILY_1, null)
        assertEquals(false, tr == en)
    }
}
