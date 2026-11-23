package dev.talhabayhan.lexi.core.designsystem.motion

import kotlin.math.PI
import kotlin.math.sin

/** Where a letter morph (`motion.md` #16) is; time runs from the OYNA tap. */
public enum class LetterMorphPhase {
    /** No morph. */
    Idle,

    /** The button's press, then the ink grows from the button to the screen and the big letter pops in. */
    Expanding,

    /** The ink covers the screen (the game is composed under it); the shortest hold before the shrink. */
    Holding,

    /** The hold is over but the game is not ready yet: the big letter pulses gently until it is. */
    Waiting,

    /** The ink shrinks into the wheel marker; the board's pieces start coming in. */
    Shrinking,

    /** The ink has landed; the board entrance is still playing. Input and the clock wait. */
    Settling,

    /** Everything has landed: the game clock and input may start. */
    Finished,

    /** Left mid-morph (system back, app in the background): the overlay fades out, nothing else happens. */
    Cancelling,

    /** The cancel fade is over: the overlay is gone; the game (if it was opened) starts as without a morph. */
    Cancelled,
}

/**
 * The letter morph's sequencing as a pure state machine (`motion.md` #16, `video-flow-notes.md` §5): no Compose and no
 * clock of its own, the caller feeds frame times to [tick], so the whole sequence is unit-tested with a fake clock.
 *
 * - The ink covers the screen at [FlowMotion.inkExpand]'s end ([covered]): the game may be navigated to under it.
 * - The shrink starts at [FlowMotion.inkShrink]'s delay at the earliest, and only once the game is ready
 *   ([markReady]) and the wheel marker is laid out ([setTargetLaidOut]). Until then the letter pulses ([pulseScale]):
 *   no freeze, and the pulse fades out with the shrink, so the letter never jumps.
 * - The board entrance plays from the shrink start; [clockMayStart] after [FlowMotion.boardSettledMillis].
 * - [cancel] at any time fades the overlay out over [FlowMotion.reducedCrossfadeMillis]; [clockMayStart] then too.
 * - Fail-safe: the wait never lasts forever. [WAIT_LIMIT_MILLIS] after the hold end without a shrink (the game never
 *   got ready or its wheel marker was never laid out) the machine cancels itself ([timedOut]): the overlay fades out
 *   and the game starts without the morph.
 * - The tier is latched where it matters, so a downgrade mid-run never makes anything jump: the letter pop at
 *   [start] (Lite: the letter only fades in), the board stagger at the shrink start (Lite: groups enter together).
 *   [MotionTier.Minimal] has no morph at all ([start] refuses it: the caller cross-fades instead).
 */
public class LetterMorphMachine(private val flow: FlowMotion) {
    public var phase: LetterMorphPhase = LetterMorphPhase.Idle
        private set

    /** Milliseconds since [start] at the last [tick]. */
    public var elapsedMillis: Long = 0L
        private set

    /** When the shrink started (ms since [start]), null before. */
    public var shrinkStartMillis: Long? = null
        private set

    /** When [cancel] was called (ms since [start]), null when not cancelled. */
    public var cancelStartMillis: Long? = null
        private set

    /** The last run was ended by the wait limit ([WAIT_LIMIT_MILLIS]), not by the game or the caller. */
    public var timedOut: Boolean = false
        private set

    /** Full: the big letter pops 0.85 → 1; Lite: it only fades in. Latched at [start]. */
    public var letterPop: Boolean = true
        private set

    /** Full: board pieces enter one after another; Lite: each group at once. Latched at the shrink start. */
    public var boardStagger: Boolean = true
        private set

    private var startMillis = 0L
    private var tier = MotionTier.Full
    private var ready = false
    private var targetLaidOut = false

    /** A morph is on screen or about to finish (not [LetterMorphPhase.Idle], finished or cancelled). */
    public val isActive: Boolean
        get() = phase != LetterMorphPhase.Idle && phase != LetterMorphPhase.Finished &&
            phase != LetterMorphPhase.Cancelled

    /** The ink covers the whole screen: the navigation to the game may happen under it. */
    public val covered: Boolean
        get() = when (phase) {
            LetterMorphPhase.Holding, LetterMorphPhase.Waiting -> true
            LetterMorphPhase.Expanding -> elapsedMillis >= flow.inkExpand.endMillis
            else -> false
        }

    /** The game clock and input may start: the board has landed, or the morph was cancelled. */
    public val clockMayStart: Boolean
        get() = phase == LetterMorphPhase.Finished || phase == LetterMorphPhase.Cancelled

    /** Milliseconds since the shrink started (the board entrance's clock), null before. */
    public val boardElapsedMillis: Long? get() = shrinkStartMillis?.let { elapsedMillis - it }

    /** The earliest shrink start: the end of the shortest hold. */
    public val holdEndMillis: Long get() = flow.inkShrink.delayMillis.toLong()

    /**
     * Starts a morph at frame time [nowMillis] in [tier]. False (nothing happens) while one is active or in
     * [MotionTier.Minimal] (no morph: cross-fade).
     */
    public fun start(nowMillis: Long, tier: MotionTier): Boolean {
        if (isActive || tier == MotionTier.Minimal) return false
        startMillis = nowMillis
        elapsedMillis = 0L
        shrinkStartMillis = null
        cancelStartMillis = null
        timedOut = false
        ready = false
        targetLaidOut = false
        this.tier = tier
        letterPop = tier == MotionTier.Full
        boardStagger = tier == MotionTier.Full
        phase = LetterMorphPhase.Expanding
        return true
    }

    /** The session's tier changed (e.g. a slow streak ⇒ Lite); applied at the next latch point. */
    public fun setTier(tier: MotionTier) {
        this.tier = tier
    }

    /** The game is ready (data loaded, board composed under the ink). */
    public fun markReady() {
        if (isActive) ready = true
    }

    /** Whether the wheel marker (the shrink's target) is laid out. */
    public fun setTargetLaidOut(laidOut: Boolean) {
        targetLaidOut = laidOut
    }

    /** Leaves the morph: the overlay fades out. Ignored when nothing runs or it is cancelling already. */
    public fun cancel() {
        if (!isActive || phase == LetterMorphPhase.Cancelling) return
        cancelStartMillis = elapsedMillis
        phase = LetterMorphPhase.Cancelling
    }

    /** Back to [LetterMorphPhase.Idle] after a finished or cancelled morph (the host has hidden the overlay). */
    public fun reset() {
        if (isActive) return
        phase = LetterMorphPhase.Idle
        shrinkStartMillis = null
        cancelStartMillis = null
        ready = false
        targetLaidOut = false
    }

    /** One frame at [nowMillis] (same clock as [start]); returns the new phase. Time never runs backwards. */
    public fun tick(nowMillis: Long): LetterMorphPhase {
        if (!isActive) return phase
        elapsedMillis = maxOf(elapsedMillis, nowMillis - startMillis)
        val cancelStart = cancelStartMillis
        if (cancelStart != null) {
            if (elapsedMillis >= cancelStart + flow.reducedCrossfadeMillis) phase = LetterMorphPhase.Cancelled
            return phase
        }
        if (shrinkStartMillis == null && ready && targetLaidOut && elapsedMillis >= holdEndMillis) {
            shrinkStartMillis = elapsedMillis
            boardStagger = tier == MotionTier.Full
        }
        if (shrinkStartMillis == null && elapsedMillis >= holdEndMillis + WAIT_LIMIT_MILLIS) {
            timedOut = true
            cancel()
            return phase
        }
        val shrinkStart = shrinkStartMillis
        phase = when {
            shrinkStart != null && elapsedMillis >= shrinkStart + flow.boardSettledMillis -> LetterMorphPhase.Finished

            shrinkStart != null && elapsedMillis >= shrinkStart + flow.inkShrink.durationMillis ->
                LetterMorphPhase.Settling

            shrinkStart != null -> LetterMorphPhase.Shrinking

            elapsedMillis < flow.inkExpand.endMillis -> LetterMorphPhase.Expanding

            elapsedMillis < holdEndMillis -> LetterMorphPhase.Holding

            else -> LetterMorphPhase.Waiting
        }
        return phase
    }

    /** Linear 0..1 progress of the shrink (apply the easing on top); 0 before it starts. */
    public fun shrinkProgress(): Float {
        val start = shrinkStartMillis ?: return 0f
        return flow.inkShrink.copy(delayMillis = 0).progressAt(elapsedMillis - start)
    }

    /**
     * Multiplier of the big letter's scale: 1 until the hold is over, then a gentle pulse
     * ([FlowMotion.letterPulseScale] every [FlowMotion.letterPulseMillis]) while waiting for the game, fading out with
     * the shrink (no jump at its start).
     */
    public fun pulseScale(): Float {
        if (elapsedMillis <= holdEndMillis) return 1f
        val wave = sin(PI * (elapsedMillis - holdEndMillis) / flow.letterPulseMillis).toFloat()
        val amplitude = (flow.letterPulseScale - 1f) * wave * wave
        return 1f + amplitude * (1f - shrinkProgress())
    }

    public companion object {
        /**
         * Longest wait after the hold for the game and its wheel marker (fail-safe, not a design value): far above a
         * normal start (the game is prepared in at most 250 ms before the ink even covers the screen).
         */
        public const val WAIT_LIMIT_MILLIS: Long = 3_000L
    }

    /** Opacity of the whole overlay: 1, fading to 0 while cancelling. */
    public fun overlayAlpha(): Float {
        val cancelStart = cancelStartMillis ?: return 1f
        val fade = MotionStep(0, flow.reducedCrossfadeMillis).progressAt(elapsedMillis - cancelStart)
        return 1f - fade
    }
}
