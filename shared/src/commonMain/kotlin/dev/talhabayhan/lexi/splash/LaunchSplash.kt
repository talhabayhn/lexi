package dev.talhabayhan.lexi.splash

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * A splash the platform draws above the Compose content (ADR 0028): on iOS a Core Animation overlay that plays the
 * splash (`motion.md` #9) outside the main thread's frame work while Compose starts up and warms up behind it. Android
 * has none ([NoLaunchSplash]): there the Compose splash plays.
 */
interface LaunchSplash {
    /** True when the platform plays the splash: `SplashRoute` then draws only the static, silent splash under it. */
    val isNative: Boolean

    /**
     * Milliseconds since the native animation started, negative while it has not started yet (its landings' haptics
     * and "tık" follow this clock).
     */
    fun elapsedMillis(): Long

    /**
     * The first real screen (Onboarding or Home) has been drawn: the overlay leaves once its animation has also ended.
     * Returns true when the overlay had already left through its time limit (the fail-safe fired). Idempotent.
     */
    fun contentReady(): Boolean
}

/** No platform splash (Android, tests, previews): the Compose splash plays. */
object NoLaunchSplash : LaunchSplash {
    override val isNative: Boolean = false

    override fun elapsedMillis(): Long = 0L

    override fun contentReady(): Boolean = false
}

/** The app's [LaunchSplash], provided by `App`. */
val LocalLaunchSplash: ProvidableCompositionLocal<LaunchSplash> = staticCompositionLocalOf { NoLaunchSplash }

/** Why the native splash left ([NativeSplashGate]). */
internal enum class SplashLeaveReason {
    /** The animation (or the reduced-motion hold) ended and Compose was ready. */
    Ready,

    /** [NativeSplashGate.capMillis] passed first: the overlay never stays for ever. */
    TimedOut,
}

/**
 * When the native splash overlay leaves (ADR 0028): once BOTH its animation has ended ([animationEnded]) AND Compose
 * has drawn its first real screen ([contentReady]), or after [capMillis] from [start], whichever comes first. [onLeave]
 * runs exactly once. [schedule] runs an action on the main thread after a delay (a dispatch timer on iOS, a fake in
 * tests). Not thread-safe: every call comes from the main thread.
 */
internal class NativeSplashGate(
    val capMillis: Long,
    private val schedule: (delayMillis: Long, action: () -> Unit) -> Unit,
    private val onLeave: (SplashLeaveReason) -> Unit,
) {
    private var started = false
    private var animationDone = false
    private var ready = false

    /** How it left, or null while the overlay is still up. */
    var leaveReason: SplashLeaveReason? = null
        private set

    /** Starts the time limit. Call once, when the animation starts. */
    fun start() {
        if (started) return
        started = true
        schedule(capMillis) { leave(SplashLeaveReason.TimedOut) }
    }

    fun animationEnded() {
        animationDone = true
        if (ready) leave(SplashLeaveReason.Ready)
    }

    /** See [LaunchSplash.contentReady]. */
    fun contentReady(): Boolean {
        ready = true
        if (animationDone) leave(SplashLeaveReason.Ready)
        return leaveReason == SplashLeaveReason.TimedOut
    }

    private fun leave(reason: SplashLeaveReason) {
        if (leaveReason != null) return
        leaveReason = reason
        onLeave(reason)
    }

    companion object {
        /** The hard limit (ADR 0028): not a design value, only a fail-safe. */
        const val CAP_MILLIS: Long = 3_000L
    }
}
