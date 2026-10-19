package dev.talhabayhan.lexi.core.platform.feedback

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import dev.talhabayhan.lexi.core.domain.feedback.Speaker
import dev.talhabayhan.lexi.core.domain.settings.Accent
import java.util.Locale
import java.util.concurrent.Executors
import kotlin.time.TimeSource

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

private class TextToSpeechEngine(
    context: Context,
    onInit: (Boolean) -> Unit,
    private val timingLog: ((String) -> Unit)?,
) : SpeechEngine {
    private val tts = TextToSpeech(context) { status -> onInit(status == TextToSpeech.SUCCESS) }

    @Volatile
    private var spokenAt: TimeSource.Monotonic.ValueTimeMark? = null

    init {
        if (timingLog != null) {
            tts.setOnUtteranceProgressListener(
                object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        spokenAt?.let {
                            timingLog("audio started ${it.elapsedNow().inWholeMilliseconds} ms after speak")
                        }
                    }

                    override fun onDone(utteranceId: String?) = Unit

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) = Unit
                },
            )
        }
    }

    override fun setLanguage(locale: Locale): Boolean {
        val result = tts.setLanguage(locale)
        return result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
    }

    override fun speak(word: String) {
        if (timingLog != null) spokenAt = TimeSource.Monotonic.markNow()
        // QUEUE_FLUSH: a new word cuts the previous one off instead of waiting behind it.
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
 * [Speaker] on `TextToSpeech`, created by [warmUp] (app start) or else lazily on the first [speak]. The engine's
 * language is set once it is ready and again only when the accent changes (setLanguage can take tens of ms, so not per
 * word). The word asked for while the engine starts is spoken once it is ready; a missing engine or English voice
 * leaves the speaker silent. [timingLog] (debug builds only) gets the speak-to-audio latency.
 */
public class AndroidSpeaker internal constructor(
    private val factory: SpeechEngineFactory,
    private val accent: () -> Accent,
    private val timingLog: ((String) -> Unit)? = null,
    private val background: (Runnable) -> Unit = BackgroundExecutor::execute,
) : Speaker,
    AutoCloseable {
    public constructor(context: Context, accent: () -> Accent, timingLog: ((String) -> Unit)? = null) :
        this({ onInit -> TextToSpeechEngine(context, onInit, timingLog) }, accent, timingLog)

    private var engine: SpeechEngine? = null
    private var ready = false
    private var failed = false
    private var pending: String? = null
    private var language: Locale? = null
    private var startedAt: TimeSource.Monotonic.ValueTimeMark? = null

    override val isReady: Boolean
        @Synchronized get() {
            val current = engine
            return ready && !failed && current != null && applyLanguage(current)
        }

    @Synchronized
    override fun warmUp() {
        if (failed || engine != null) return
        start()
    }

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
        language = null
        pending = null
    }

    private fun start() {
        if (timingLog != null) startedAt = TimeSource.Monotonic.markNow()
        engine = runCatching { factory.create { success -> onInit(success) } }.getOrElse {
            failed = true
            pending = null
            null
        }
    }

    /**
     * TextToSpeech reports on the main thread; setting the language is a binder call that can take tens of ms, so the
     * rest runs on [background] and never costs the UI a frame.
     */
    private fun onInit(success: Boolean) {
        background { onInitOffMain(success) }
    }

    @Synchronized
    private fun onInitOffMain(success: Boolean) {
        val current = engine
        if (!success || current == null) {
            failed = true
            pending = null
            return
        }
        ready = true
        startedAt?.let { timingLog?.invoke("engine ready ${it.elapsedNow().inWholeMilliseconds} ms after start") }
        applyLanguage(current)
        pending?.let { word ->
            pending = null
            say(current, word)
        }
    }

    private fun say(engine: SpeechEngine, word: String) {
        if (applyLanguage(engine)) engine.speak(word)
    }

    /** Sets the accent's locale unless it is already set; false when the engine has no voice for it. */
    private fun applyLanguage(engine: SpeechEngine): Boolean {
        val locale = if (accent() == Accent.UK) Locale.UK else Locale.US
        if (locale == language) return true
        if (!engine.setLanguage(locale)) return false
        language = locale
        return true
    }
}

/** One daemon thread for the speech engine's slow calls (language setup). */
private object BackgroundExecutor {
    private val executor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "lexi-speech").apply { isDaemon = true }
    }

    fun execute(runnable: Runnable) = executor.execute(runnable)
}
