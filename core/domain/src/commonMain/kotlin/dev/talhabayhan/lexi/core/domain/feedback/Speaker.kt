package dev.talhabayhan.lexi.core.domain.feedback

/**
 * Text-to-speech for English answers (`Correct` with TTS on, the Listen joker; ADR 0019). Implementations
 * (`TextToSpeech` / `AVSpeechSynthesizer`, `core:platform`) speak English regardless of the UI language and fail
 * silently when no voice is available.
 */
public interface Speaker {
    /** Speaks [word], interrupting anything still being spoken. */
    public fun speak(word: String)

    /** Stops speaking at once (pause, leaving the screen). */
    public fun stop()
}
