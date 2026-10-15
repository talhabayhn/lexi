package dev.talhabayhan.lexi.core.platform.feedback

import dev.talhabayhan.lexi.core.domain.feedback.Speaker
import dev.talhabayhan.lexi.core.domain.settings.Accent
import platform.AVFAudio.AVSpeechBoundary
import platform.AVFAudio.AVSpeechSynthesisVoice
import platform.AVFAudio.AVSpeechSynthesizer
import platform.AVFAudio.AVSpeechUtterance

/** [Speaker] on `AVSpeechSynthesizer` with an en-US / en-GB voice; a missing voice falls back to the default. */
public class IosSpeaker(private val accent: () -> Accent) : Speaker {
    private val synthesizer = AVSpeechSynthesizer()

    override fun speak(word: String) {
        synthesizer.stopSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
        val utterance = AVSpeechUtterance.speechUtteranceWithString(word)
        AVSpeechSynthesisVoice.voiceWithLanguage(if (accent() == Accent.UK) "en-GB" else "en-US")
            ?.let { utterance.voice = it }
        synthesizer.speakUtterance(utterance)
    }

    override fun stop() {
        synthesizer.stopSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
    }
}
