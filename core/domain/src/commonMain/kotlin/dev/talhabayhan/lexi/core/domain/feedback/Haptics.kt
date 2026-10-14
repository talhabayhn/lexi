package dev.talhabayhan.lexi.core.domain.feedback

/** Haptic patterns from `docs/design/motion.md` § Haptic map; platform mapping lives in the implementations. */
public enum class HapticKind {
    /** Button press, key tap, splash landing. */
    Light,

    /** Pass. */
    Soft,

    /** Combo. */
    Heavy,

    /** Correct answer. */
    Success,

    /** Wrong answer. */
    Error,

    /** Each second of the time warning. */
    Tick,
}

/**
 * Plays haptic feedback. Implementations (M4, `View.performHapticFeedback` / `UIFeedbackGenerator`) respect the
 * in-app "Titreşim" setting, so callers never check it themselves.
 */
public fun interface Haptics {
    public fun perform(kind: HapticKind)
}
