package dev.talhabayhan.lexi.core.testing

import dev.talhabayhan.lexi.core.domain.feedback.Speaker

/** Records what would be spoken; also serves as a silent [Speaker] in previews and tests. */
public class FakeSpeaker : Speaker {
    private val _spoken = mutableListOf<String>()

    /** Every word passed to [speak], in order. */
    public val spoken: List<String> get() = _spoken.toList()

    public var stopCount: Int = 0
        private set

    public var warmUpCount: Int = 0
        private set

    /** What [isReady] reports; false simulates an engine still starting or without a voice. */
    public var ready: Boolean = true

    override val isReady: Boolean get() = ready

    override fun warmUp() {
        warmUpCount++
    }

    override fun speak(word: String) {
        _spoken += word
    }

    override fun stop() {
        stopCount++
    }
}
