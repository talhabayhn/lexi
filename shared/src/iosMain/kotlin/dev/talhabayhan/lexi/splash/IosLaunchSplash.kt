@file:OptIn(ExperimentalForeignApi::class)

package dev.talhabayhan.lexi.splash

import dev.talhabayhan.lexi.core.designsystem.theme.StandardLexiMotion
import dev.talhabayhan.lexi.shared.generated.resources.Res
import dev.talhabayhan.lexi.shared.generated.resources.splash_tagline
import dev.talhabayhan.lexi.startup.StartupTrace
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import platform.QuartzCore.CACurrentMediaTime
import platform.UIKit.UIAccessibilityPostNotification
import platform.UIKit.UIAccessibilityScreenChangedNotification
import platform.UIKit.UIViewAutoresizingFlexibleHeight
import platform.UIKit.UIViewAutoresizingFlexibleWidth
import platform.UIKit.UIViewController
import platform.UIKit.addChildViewController
import platform.UIKit.didMoveToParentViewController
import platform.darwin.DISPATCH_TIME_NOW
import platform.darwin.dispatch_after
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_time
import kotlin.math.roundToLong

/**
 * The iOS [LaunchSplash] (ADR 0028): owns the [NativeSplashOverlay] above the Compose view and decides with a
 * [NativeSplashGate] when it leaves: when its animation (after [LAUNCH_HAND_OFF_MILLIS] of launch pose) has ended (Core
 * Animation's completion; with reduced motion, after [StandardLexiMotion.splashReducedHoldMillis] of static logo) and
 * Compose has drawn its first real screen ([contentReady]), or after [NativeSplashGate.CAP_MILLIS] whatever happens.
 * Then it plays its exit and takes itself out of the view hierarchy. Every call is on the main thread. [schedule] and
 * [now] are injected for tests.
 */
internal class IosLaunchSplash(
    val overlay: NativeSplashOverlay,
    private val reducedMotion: Boolean,
    private val schedule: (delayMillis: Long, action: () -> Unit) -> Unit = ::onMainAfter,
    private val now: () -> Double = ::CACurrentMediaTime,
    private val loadTagline: (suspend () -> String)? = { getString(Res.string.splash_tagline) },
    private val readBeginTime: () -> Double? = { overlay.animationBeginTime() },
) : LaunchSplash {
    private val gate = NativeSplashGate(NativeSplashGate.CAP_MILLIS, schedule, ::leave)
    private var started = false

    /** When the timeline (hand-off, then animation) began on screen, in [now]'s seconds; null until known. */
    private var startedAt: Double? = null
    private val scope = MainScope()

    /** True once the overlay has been taken out of the hierarchy. */
    var isRemoved: Boolean = false
        private set

    /** How the overlay left, or null while it is up. */
    internal val leaveReason: SplashLeaveReason? get() = gate.leaveReason

    override val isNative: Boolean = true

    /** Negative during the hand-off; until the timeline's begin is known, as if it began now. */
    override fun elapsedMillis(): Long {
        val start = startedAt ?: return -handOffMillis
        return ((now() - start) * MILLIS_PER_SECOND).roundToLong() - handOffMillis
    }

    /** The launch pose's hold before the animation (none with reduced motion: no animation). */
    private val handOffMillis: Long = if (reducedMotion) 0L else LAUNCH_HAND_OFF_MILLIS

    override fun contentReady(): Boolean = gate.contentReady()

    /** Starts the animation and the time limit; called when the host's view is about to appear. Once only. */
    fun start() {
        if (started) return
        started = true
        StartupTrace.mark("native splash started")
        gate.start()
        if (reducedMotion) {
            schedule(StandardLexiMotion.splashReducedHoldMillis.toLong()) { gate.animationEnded() }
        } else {
            overlay.play(handOffMillis) { gate.animationEnded() }
            // Runs after this run loop turn's commit: the time the timeline really began (the landings' clock).
            schedule(0L) { startedAt = readBeginTime() ?: now() }
        }
        val load = loadTagline ?: return
        scope.launch {
            val text = try {
                load()
            } catch (e: CancellationException) {
                throw e
            } catch (@Suppress("TooGenericExceptionCaught") _: Exception) {
                // No tagline is better than no splash.
                return@launch
            }
            if (!isRemoved) overlay.setTagline(text)
        }
    }

    /** Core Animation finished the animation (tests call it directly). */
    internal fun onAnimationEnded() = gate.animationEnded()

    private fun leave(reason: SplashLeaveReason) {
        StartupTrace.mark("native splash leaves: $reason")
        val millis = overlay.leave()
        schedule(millis) { remove() }
    }

    private fun remove() {
        if (isRemoved) return
        isRemoved = true
        scope.cancel()
        overlay.remove()
        // VoiceOver moves on to the screen that is now shown.
        UIAccessibilityPostNotification(UIAccessibilityScreenChangedNotification, null)
    }

    companion object {
        private const val MILLIS_PER_SECOND = 1000.0

        /**
         * The launch pose is held this long before the animation starts: iOS cross-fades from the launch screen to the
         * app's first frame over ≈ 330 ms (measured on the iOS 26 simulator, 2026-11-24), and an animation started
         * under that fade shows the launch screen's logo as a ghost over the moving blocks. Not a design value;
         * re-check on a device (`docs/LOCAL-DEVICE-TEST.md`).
         */
        const val LAUNCH_HAND_OFF_MILLIS: Long = 350L
    }
}

/**
 * The app's root view controller on iOS: the Compose view controller as its child, the native splash's overlay above
 * it. The splash starts when the view is about to appear: its timeline begins with the first frame after the launch
 * screen, in the launch pose.
 */
internal class NativeSplashHostViewController(
    private val content: UIViewController,
    private val splash: IosLaunchSplash,
) : UIViewController(nibName = null, bundle = null) {
    override fun viewDidLoad() {
        super.viewDidLoad()
        addChildViewController(content)
        content.view.setFrame(view.bounds)
        content.view.autoresizingMask = UIViewAutoresizingFlexibleWidth or UIViewAutoresizingFlexibleHeight
        view.addSubview(content.view)
        content.didMoveToParentViewController(this)
        if (!splash.isRemoved) view.addSubview(splash.overlay.view)
    }

    override fun viewWillAppear(animated: Boolean) {
        super.viewWillAppear(animated)
        // Before the first frame: the Core Animation timeline then begins with the frame that ends the launch screen.
        splash.start()
    }

    override fun viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        if (splash.isRemoved || splash.overlay.isLeaving) return
        view.bounds.useContents {
            splash.overlay.layout(size.width, size.height, view.safeAreaInsets)
        }
    }
}

/** Runs [action] on the main queue after [delayMillis]. */
private fun onMainAfter(delayMillis: Long, action: () -> Unit) {
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, delayMillis * NANOS_PER_MILLI), dispatch_get_main_queue(), action)
}

private const val NANOS_PER_MILLI = 1_000_000L
