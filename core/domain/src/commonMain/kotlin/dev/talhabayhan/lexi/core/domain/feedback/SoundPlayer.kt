package dev.talhabayhan.lexi.core.domain.feedback

/** Sound effects from `docs/design/motion.md` § Sound assets (one asset each, ≤ 300 ms). */
public enum class SoundEffect {
    /** "tık": any button press. */
    Tap,

    /** Letter placed in a slot. */
    Place,

    /** Two rising notes. */
    Correct,

    /** Descending "bzzt". */
    Wrong,

    /** "vınn". */
    Pass,

    /** Short fanfare. */
    Combo,

    /** "tik" each second of the time warning. */
    Tick,
    Win,
    Lose,
}

/**
 * Plays short preloaded sound effects. Implementations (M4, `SoundPool` / `AVAudioPlayer`) respect the in-app
 * "Ses efektleri" setting and the iOS silent switch, so callers never check them themselves.
 */
public fun interface SoundPlayer {
    public fun play(effect: SoundEffect)
}
