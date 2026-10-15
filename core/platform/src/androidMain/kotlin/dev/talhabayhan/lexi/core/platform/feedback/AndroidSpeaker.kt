package dev.talhabayhan.lexi.core.platform.feedback

import android.content.Context
import android.speech.tts.TextToSpeech
import dev.talhabayhan.lexi.core.domain.feedback.Speaker
import dev.talhabayhan.lexi.core.domain.settings.Accent
import java.util.Locale

/** The slice of `TextToSpeech` the speaker uses; a seam for host tests. */
internal interface SpeechEngine {
    /** False when the engine has no voice for [locale]. */
    fun setLanguage(locale: Locale): Boolean

    fun speak(word: String)

    fun stop()

    fun shutdown()
}

/** Creates an engine; [onInit] reports asynchronously whether it started. */
internal fun interface SpeechEngineFactory {
    fun create(onInit: (success: Boolean) -> Unit): SpeechEngine
}

private class TextToSpeechEngine(context: Context, onInit: (Boolean) -> Unit) : SpeechEngine {
    private val tts = TextToSpeech(context) { status -> onInit(status == TextToSpeech.SUCCESS) }

    override fun setLanguage(locale: Locale): Boolean {
        val result = tts.setLanguage(locale)
        return result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
    }

    override fun speak(word: String) {
        tts.speak(word, TextToSpeech.QUEUE_FLUSH, null, "lexi-word")
    }

    override fun stop() {
        tts.stop()
    }

    override fun shutdown() {
        tts.shutdown()
    }
}

/**
 * [Speaker] on `TextToSpeech`, created lazily on the first [speak]. The word asked for while the engine starts is
 * spoken once it is ready; a missing engine or English voice leaves the speaker silent.
 */
public class AndroidSpeaker internal constructor(
    private val factory: SpeechEngineFactory,
    private val accent: () -> Accent,
) : Speaker,
    AutoCloseable {
    public constructor(context: Context, accent: () -> Accent) :
        this({ onInit -> TextToSpeechEngine(context, onInit) }, accent)

    private var engine: SpeechEngine? = null
    private var ready = false
    private var failed = false
    private var pending: String? = null

    @Synchronized
    override fun speak(word: String) {
        if (failed) return
        val current = engine
        if (current == null) {
            pending = word
            start()
        } else if (ready) {
            say(current, word)
        } else {
            pending = word
        }
    }

    @Synchronized
    override fun stop() {
        pending = null
        if (ready) engine?.stop()
    }

    @Synchronized
    override fun close() {
        engine?.shutdown()
        engine = null
        ready = false
        pending = null
    }

    private fun start() {
        engine = runCatching { factory.create { success -> onInit(success) } }.getOrElse {
            failed = true
            pending = null
            null
        }
    }

    @Synchronized
    private fun onInit(success: Boolean) {
        val current = engine
        if (!success || current == null) {
            failed = true
            pending = null
            return
        }
        ready = true
        pending?.let { word ->
            pending = null
            say(current, word)
        }
    }

    private fun say(engine: SpeechEngine, word: String) {
        if (engine.setLanguage(if (accent() == Accent.UK) Locale.UK else Locale.US)) engine.speak(word)
    }
}
