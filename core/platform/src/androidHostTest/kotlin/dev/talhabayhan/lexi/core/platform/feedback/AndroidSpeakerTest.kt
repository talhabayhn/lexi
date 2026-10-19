package dev.talhabayhan.lexi.core.platform.feedback

import dev.talhabayhan.lexi.core.domain.settings.Accent
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AndroidSpeakerTest {
    private class FakeEngine(var hasVoice: Boolean = true) : SpeechEngine {
        val spoken = mutableListOf<String>()
        val locales = mutableListOf<Locale>()
        var shutDown = false
        var stops = 0

        override fun setLanguage(locale: Locale): Boolean {
            locales += locale
            return hasVoice
        }

        override fun speak(word: String) {
            spoken += word
        }

        override fun stop() {
            stops++
        }

        override fun shutdown() {
            shutDown = true
        }
    }

    private class Harness(accent: Accent = Accent.US, hasVoice: Boolean = true) {
        val engine = FakeEngine(hasVoice)
        var created = 0
        lateinit var init: (Boolean) -> Unit
        val speaker = AndroidSpeaker(
            factory = {
                created++
                init = it
                engine
            },
            accent = { accent },
        )
    }

    @Test
    fun wordAskedBeforeInitIsSpokenOnceReady() {
        val h = Harness()
        h.speaker.speak("apple")
        h.speaker.speak("pear")
        assertTrue(h.engine.spoken.isEmpty())
        h.init(true)
        assertEquals(listOf("pear"), h.engine.spoken)
        h.speaker.speak("fig")
        assertEquals(listOf("pear", "fig"), h.engine.spoken)
        assertEquals(1, h.created)
    }

    @Test
    fun failedInitStaysSilent() {
        val h = Harness()
        h.speaker.speak("apple")
        h.init(false)
        h.speaker.speak("pear")
        assertTrue(h.engine.spoken.isEmpty())
        assertEquals(1, h.created)
    }

    @Test
    fun missingVoiceStaysSilentAndUkAccentPicksBritishLocale() {
        val silent = Harness(hasVoice = false)
        silent.speaker.speak("apple")
        silent.init(true)
        assertTrue(silent.engine.spoken.isEmpty())

        val uk = Harness(accent = Accent.UK)
        uk.speaker.speak("apple")
        uk.init(true)
        assertEquals(listOf(Locale.UK), uk.engine.locales)
    }

    @Test
    fun warmUpStartsTheEngineAndSetsTheLanguageBeforeTheFirstWord() {
        val h = Harness()
        h.speaker.warmUp()
        h.speaker.warmUp()
        assertEquals(1, h.created)
        h.init(true)
        assertEquals(listOf(Locale.US), h.engine.locales)

        h.speaker.speak("apple")
        h.speaker.speak("pear")

        assertEquals(listOf("apple", "pear"), h.engine.spoken)
        assertEquals(listOf(Locale.US), h.engine.locales, "the language is set once, not per word")
    }

    @Test
    fun readyOnlyOnceStartedAndWithAVoice() {
        val h = Harness()
        assertFalse(h.speaker.isReady)
        h.speaker.warmUp()
        assertFalse(h.speaker.isReady, "still starting")
        h.init(true)
        assertTrue(h.speaker.isReady)

        val silent = Harness(hasVoice = false)
        silent.speaker.warmUp()
        silent.init(true)
        assertFalse(silent.speaker.isReady, "no voice for the accent")

        val failed = Harness()
        failed.speaker.warmUp()
        failed.init(false)
        assertFalse(failed.speaker.isReady)
    }

    @Test
    fun accentChangeSetsTheNewLanguageOnTheNextWord() {
        var accent = Accent.US
        val engine = FakeEngine()
        lateinit var init: (Boolean) -> Unit
        val speaker = AndroidSpeaker(factory = {
            init = it
            engine
        }, accent = { accent })
        speaker.warmUp()
        init(true)

        accent = Accent.UK
        speaker.speak("colour")

        assertEquals(listOf(Locale.US, Locale.UK), engine.locales)
        assertEquals(listOf("colour"), engine.spoken)
    }

    @Test
    fun stopDropsPendingWordAndCloseShutsTheEngineDown() {
        val h = Harness()
        h.speaker.speak("apple")
        h.speaker.stop()
        h.init(true)
        assertTrue(h.engine.spoken.isEmpty())
        h.speaker.stop()
        assertEquals(1, h.engine.stops)
        h.speaker.close()
        assertTrue(h.engine.shutDown)
    }
}
