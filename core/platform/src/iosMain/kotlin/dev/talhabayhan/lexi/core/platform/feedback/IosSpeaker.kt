package dev.talhabayhan.lexi.core.platform.feedback

import dev.talhabayhan.lexi.core.domain.feedback.Speaker
import dev.talhabayhan.lexi.core.domain.settings.Accent
import platform.AVFAudio.AVSpeechBoundary
import platform.AVFAudio.AVSpeechSynthesisVoice
import platform.AVFAudio.AVSpeechSynthesizer
import platform.AVFAudio.AVSpeechSynthesizerDelegateProtocol
import platform.AVFAudio.AVSpeechUtterance
import platform.darwin.NSObject
import kotlin.time.TimeSource

/**
 * [Speaker] on one reused `AVSpeechSynthesizer` with an en-US / en-GB voice; a missing voice falls back to the default.
 * The voice is looked up once per accent, and [warmUp] (app start) speaks a silent utterance so the synthesizer has
 * loaded it before the first answer. [timingLog] (debug builds only) gets the speak-to-audio latency.
 */
public class IosSpeaker(private val accent: () -> Accent, timingLog: ((String) -> Unit)? = null) : Speaker {
    private val synthesizer = AVSpeechSynthesizer()
    private val voices = mutableMapOf<Accent, AVSpeechSynthesisVoice?>()
    private var warmedUp = false

    // The synthesizer's delegate is weak: the speaker keeps it alive.
    private val timing = timingLog?.let(::TimingDelegate)?.also { synthesizer.delegate = it }

    /** The synthesizer needs no start-up; it can speak once the accent has a voice. */
    override val isReady: Boolean get() = voice() != null

    override fun warmUp() {
        if (warmedUp) return
        warmedUp = true
        val utterance = AVSpeechUtterance.speechUtteranceWithString(" ")
        utterance.volume = 0f
        voice()?.let { utterance.voice = it }
        synthesizer.speakUtterance(utterance)
    }

    override fun speak(word: String) {
        synthesizer.stopSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
        val utterance = AVSpeechUtterance.speechUtteranceWithString(word)
        voice()?.let { utterance.voice = it }
        timing?.spokenAt = TimeSource.Monotonic.markNow()
        synthesizer.speakUtterance(utterance)
    }

    override fun stop() {
        synthesizer.stopSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
    }

    private fun voice(): AVSpeechSynthesisVoice? = accent().let { current ->
        voices.getOrPut(current) {
            AVSpeechSynthesisVoice.voiceWithLanguage(if (current == Accent.UK) "en-GB" else "en-US")
        }
    }
}

private class TimingDelegate(private val log: (String) -> Unit) :
    NSObject(),
    AVSpeechSynthesizerDelegateProtocol {
    var spokenAt: TimeSource.Monotonic.ValueTimeMark? = null

    override fun speechSynthesizer(synthesizer: AVSpeechSynthesizer, didStartSpeechUtterance: AVSpeechUtterance) {
        spokenAt?.let { log("audio started ${it.elapsedNow().inWholeMilliseconds} ms after speak") }
        spokenAt = null
    }
}
