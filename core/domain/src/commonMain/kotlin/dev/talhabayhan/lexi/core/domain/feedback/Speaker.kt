package dev.talhabayhan.lexi.core.domain.feedback

/**
 * Text-to-speech for English answers (`Correct` with TTS on, the Listen joker; ADR 0019). Implementations
 * (`TextToSpeech` / `AVSpeechSynthesizer`, `core:platform`) speak English regardless of the UI language and fail
 * silently when no voice is available.
 */
public interface Speaker {
    /**
     * Starts the engine and prepares the voice ahead of the first [speak] (app start), so a correct answer is spoken
     * without the engine's start-up delay. Calling it again does nothing.
     */
    public fun warmUp() {}

    /**
     * True when [speak] starts at once: the engine is up and has a voice for the accent. False while it is still
     * starting, or when it failed or has no voice; callers that must not stay silent (the correct answer) then fall
     * back to a sound.
     */
    public val isReady: Boolean get() = true

    /** Speaks [word] at once, interrupting anything still being spoken. */
    public fun speak(word: String)

    /** Stops speaking at once (pause, leaving the screen). */
    public fun stop()
}
