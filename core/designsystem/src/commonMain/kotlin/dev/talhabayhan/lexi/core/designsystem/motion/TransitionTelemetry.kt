package dev.talhabayhan.lexi.core.designsystem.motion

import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsEvent
import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsEvent.FrameTimeBucket
import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsEvent.MotionTierCode
import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsEvent.PerfTransition
import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsTracker

/**
 * [FrameMonitor] keys of the transitions measured in the field (`transition_frames`, `docs/ANALYTICS.md`). The
 * screens register these keys; [TransitionTelemetry] maps them to the event's transition names.
 */
public object TransitionKeys {
    /** The splash animation (#9), from its clock's start to its end. */
    public const val SPLASH: String = "splash"

    /** The navigation from onboarding (or the level-test result) to Home, while both screens are visible. */
    public const val ONBOARDING_TO_HOME: String = "nav.onboardingToHome"

    /** The picker's grid entrance (#18): the picker's first open of a session. */
    public const val PICKER_GRID_ENTRANCE: String = "picker.gridEntrance"

    /** The category card's flip into the mode / time dialog (#13–#15) and back. */
    public const val DIALOG_OPEN: String = "picker.flip.open"
    public const val DIALOG_CLOSE: String = "picker.flip.close"

    /** The letter morph's expand (#16) and its shrink onto the game board, which lands and starts the game. */
    public const val MORPH_EXPAND: String = "morph.expand"
    public const val MORPH_SHRINK: String = "morph.shrink"

    internal fun perfTransition(key: String): PerfTransition? = when (key) {
        SPLASH -> PerfTransition.SPLASH
        ONBOARDING_TO_HOME -> PerfTransition.ONBOARDING_TO_HOME
        PICKER_GRID_ENTRANCE -> PerfTransition.PICKER_OPEN
        DIALOG_OPEN -> PerfTransition.DIALOG_OPEN
        DIALOG_CLOSE -> PerfTransition.DIALOG_CLOSE
        MORPH_EXPAND -> PerfTransition.MORPH
        MORPH_SHRINK -> PerfTransition.GAME_START
        else -> null
    }
}

/**
 * Sends `transition_frames` through the app's [AnalyticsTracker] (consent-gated there; tests use a fake): for each
 * named transition ([TransitionKeys]) at most once per [session] (default: the app process, whatever Koin or the
 * composition is recreated), with the bucket of its worst frame, the motion tier and
 * [TransitionSession.firstLaunchOfVersion]. UI thread only.
 */
public class TransitionTelemetry(
    private val analytics: AnalyticsTracker,
    private val session: TransitionSession = TransitionSession.Process,
) {
    /** Whether this process is the first launch of the running app version (set by the splash before any transition). */
    public var firstLaunchOfVersion: Boolean
        get() = session.firstLaunchOfVersion
        set(value) {
            session.firstLaunchOfVersion = value
        }

    /** One measured span: every named key in [keys] not yet sent this session is sent with [worstFrameNanos]. */
    public fun onTransitionMeasured(keys: Set<String>, worstFrameNanos: Long, tier: MotionTier) {
        for (key in keys) {
            val transition = TransitionKeys.perfTransition(key) ?: continue
            if (!session.reported.add(transition)) continue
            analytics.track(
                AnalyticsEvent.TransitionFrames(
                    transition = transition,
                    worstFrame = FrameTimeBucket.of(worstFrameNanos),
                    motionTier = tier.code,
                    firstLaunchOfVersion = firstLaunchOfVersion,
                ),
            )
        }
    }

    /** A [FrameMonitor] listener feeding this telemetry; [tier] is read when a span ends. */
    public fun listener(tier: () -> MotionTier): TransitionFramesListener =
        TransitionFramesListener { keys, worst, _ -> onTransitionMeasured(keys, worst, tier()) }
}

/** What [TransitionTelemetry] has sent so far; one per app process ([Process]), tests make their own. */
public class TransitionSession {
    internal var firstLaunchOfVersion: Boolean = false
    internal val reported: MutableSet<PerfTransition> = mutableSetOf()

    public companion object {
        public val Process: TransitionSession = TransitionSession()
    }
}

private val MotionTier.code: MotionTierCode
    get() = when (this) {
        MotionTier.Full -> MotionTierCode.FULL
        MotionTier.Lite -> MotionTierCode.LITE
        MotionTier.Minimal -> MotionTierCode.MINIMAL
    }
