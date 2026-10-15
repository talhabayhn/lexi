package dev.talhabayhan.lexi.core.platform.feedback

import android.content.Context
import android.speech.tts.TextToSpeech
import dev.talhabayhan.lexi.core.domain.feedback.Speaker
import dev.talhabayhan.lexi.core.domain.settings.Accent
import java.util.Locale

/**
 * [Speaker] on `TextToSpeech`, created lazily on the first [speak]. The word asked for while the engine starts is
 * spoken once it is ready; a missing engine or English voice leaves the speaker silent.
 */
public class AndroidSpeaker(private val context: Context, private val accent: () -> Accent) :
    Speaker,
    AutoCloseable {
    private var tts: TextToSpeech? = null
    private var ready = false
    private var failed = false
    private var pending: String? = null

    @Synchronized
    override fun speak(word: String) {
        if (failed) return
        val engine = tts
        if (engine == null) {
            pending = word
            start()
        } else if (ready) {
            say(engine, word)
        } else {
            pending = word
        }
    }

    @Synchronized
    override fun stop() {
        pending = null
        if (ready) tts?.stop()
    }

    @Synchronized
    override fun close() {
        tts?.shutdown()
        tts = null
        ready = false
    }

    private fun start() {
        tts = runCatching { TextToSpeech(context) { status -> onInit(status) } }.getOrElse {
            failed = true
            null
        }
    }

    @Synchronized
    private fun onInit(status: Int) {
        val engine = tts
        if (status != TextToSpeech.SUCCESS || engine == null) {
            failed = true
            return
        }
        ready = true
        pending?.let { word ->
            pending = null
            say(engine, word)
        }
    }

    private fun say(engine: TextToSpeech, word: String) {
        val locale = if (accent() == Accent.UK) Locale.UK else Locale.US
        val result = engine.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) return
        engine.speak(word, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
    }

    private companion object {
        const val UTTERANCE_ID = "lexi-word"
    }
}
