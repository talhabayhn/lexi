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
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationState
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
 * Then it plays its exit and takes itself out of the view hierarchy. Every call is on the main thread. [schedule],
 * [now], [commit] and [isAppActive] are injected for tests.
 *
 * Launch order (S7): [start] adds the animation and commits it at once ([commit]); the Compose content is created only
 * when [start]'s `onCommitted` runs: once the timeline's begin time has been read back from a committed transaction
 * and the app is active (its launch has finished, so the system shows the app's frames instead of the launch screen),
 * on a later main-queue pass. Compose's first composition, its synchronous first Metal draw and the first-launch
 * warm-up then block the main thread while the render server already shows the animation. Before, they ran in the
 * same main-thread pass as the commit and right after it, before the launch finished: the timeline ran on behind the
 * launch screen and only its tail was seen.
 */
internal class IosLaunchSplash(
    val overlay: NativeSplashOverlay,
    private val reducedMotion: Boolean,
    private val schedule: (delayMillis: Long, action: () -> Unit) -> Unit = ::onMainAfter,
    private val now: () -> Double = ::CACurrentMediaTime,
    private val loadTagline: (suspend () -> String)? = { getString(Res.string.splash_tagline) },
    private val readBeginTime: () -> Double? = { overlay.animationBeginTime() },
    private val commit: () -> Unit = { overlay.commitNow() },
    private val isAppActive: () -> Boolean = ::appIsActive,
) : LaunchSplash {
    private val gate = NativeSplashGate(NativeSplashGate.CAP_MILLIS, schedule, ::leave)
    private var started = false

    /** [start]'s `onCommitted`, until it has run (once). */
    private var onCommitted: (() -> Unit)? = null

    /** True once the app's activation has been traced (once). */
    private var activeTraced = false

    /** True once the tagline is on the overlay and committed, or will not come (no loader, failed). */
    private var taglineSettled = loadTagline == null

    /** When the timeline (hand-off, then animation) began on screen, `CACurrentMediaTime` seconds; null until known. */
    private var startedAt: Double? = null
    private val scope = MainScope()

    /** True once the overlay has been taken out of the hierarchy. */
    var isRemoved: Boolean = false
        private set

    /** How the overlay left, or null while it is up. */
    internal val leaveReason: SplashLeaveReason? get() = gate.leaveReason

    override val isNative: Boolean = true

    /** Negative during the hand-off; `-handOff` until the timeline's begin is known (the landings wait). */
    override fun elapsedMillis(): Long {
        val start = startedAt ?: return -handOffMillis
        return ((now() - start) * MILLIS_PER_SECOND).roundToLong() - handOffMillis
    }

    /** The launch pose's hold before the animation (none with reduced motion: no animation). */
    private val handOffMillis: Long = if (reducedMotion) 0L else LAUNCH_HAND_OFF_MILLIS

    override fun contentReady(): Boolean = gate.contentReady()

    /**
     * Starts the animation and the time limit; called when the host's view is about to appear. Once only.
     * [onCommitted] (the heavy start-up: creating the Compose content) runs once, on a later main-queue pass than the
     * commit of the animation: when its begin time is known, the tagline is on the overlay and the app is active (with
     * reduced motion only the tagline and the app's activation are waited for); after [CONTENT_FALLBACK_MILLIS] if that is not seen (the content is never held
     * back for good); or when the overlay leaves first.
     */
    fun start(onCommitted: () -> Unit = {}) {
        if (started) return
        started = true
        this.onCommitted = onCommitted
        StartupTrace.mark("native splash started")
        gate.start()
        if (reducedMotion) {
            schedule(StandardLexiMotion.splashReducedHoldMillis.toLong()) { gate.animationEnded() }
            commit()
        } else {
            overlay.play(handOffMillis) { gate.animationEnded() }
            // Commits the animation now (if the view is in a window), alone: nothing heavy is in this transaction.
            commit()
        }
        readBeginTimeWhenCommitted()
        schedule(CONTENT_FALLBACK_MILLIS) { committed("fallback") }
        val load = loadTagline ?: return
        scope.launch {
            val text = try {
                load()
            } catch (e: CancellationException) {
                throw e
            } catch (@Suppress("TooGenericExceptionCaught") _: Exception) {
                // No tagline is better than no splash.
                taglineSettled = true
                StartupTrace.mark("native splash tagline failed")
                return@launch
            }
            if (!isRemoved) {
                overlay.setTagline(text)
                // Its text is drawn on the main thread when committed: before the content can block that thread.
                commit()
            }
            taglineSettled = true
            StartupTrace.mark("native splash tagline set")
        }
    }

    /**
     * The time the timeline really began (the landings' clock) is known only once its transaction is committed, which
     * may be long after [start] on a slow first launch, and a main-queue block may run before that commit: read it
     * again every frame until it is there (never guess: [elapsedMillis] stays before the animation meanwhile, so the
     * landings wait rather than play early). Then, once the app is active and the tagline is on the overlay too (its
     * text is loaded asynchronously and drawn on the main thread: under a blocked main thread it would miss its fade),
     * the content starts. Stops when that is done or the overlay has left.
     */
    private fun readBeginTimeWhenCommitted() {
        schedule(BEGIN_TIME_POLL_MILLIS) {
            if (isRemoved || gate.leaveReason != null) {
                committed("left")
                return@schedule
            }
            if (!reducedMotion && startedAt == null) {
                readBeginTime()?.let {
                    startedAt = it
                    StartupTrace.mark("native splash committed")
                }
            }
            val active = isAppActive()
            if (active && !activeTraced) {
                activeTraced = true
                // The device trace shows how long after the commit the launch finished (the hand-off is 350 ms).
                val afterCommit = startedAt?.let { ((now() - it) * MILLIS_PER_SECOND).roundToLong() }
                StartupTrace.mark("native splash app active, $afterCommit ms after the commit")
            }
            val shown = (reducedMotion || startedAt != null) && taglineSettled && active
            if (shown) committed("shown") else readBeginTimeWhenCommitted()
        }
    }

    /** Runs [start]'s `onCommitted` the first time only; [why] goes to the start-up trace. */
    private fun committed(why: String) {
        val action = onCommitted ?: return
        onCommitted = null
        StartupTrace.mark("native splash $why: content starts")
        action()
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

        /** How often the timeline's begin time is looked for until the first frame is committed (one 60 Hz frame). */
        internal const val BEGIN_TIME_POLL_MILLIS: Long = 16L

        /**
         * The content is created after this even if the commit of the animation or the app's activation was not seen
         * (e.g. launched behind a system alert): an overlay-only commit takes a frame or two, so this only guards
         * against holding Compose back for ever.
         */
        internal const val CONTENT_FALLBACK_MILLIS: Long = 1_000L
    }
}

/**
 * The app's root view controller on iOS: the native splash's overlay, with the Compose view controller ([makeContent])
 * as its child under it. Two phases (S7): when the view is about to appear the splash starts and its animation is
 * committed on its own; the Compose controller is created and embedded only after that commit (its first composition
 * and its synchronous first Metal draw would otherwise run inside the commit that carries the animation, and the
 * animation would reach the screen only after them, mostly over).
 */
internal class NativeSplashHostViewController(
    private val makeContent: () -> UIViewController,
    private val splash: IosLaunchSplash,
) : UIViewController(nibName = null, bundle = null) {
    /** The Compose controller once embedded (phase 2), null before. */
    internal var content: UIViewController? = null
        private set

    override fun viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = splash.overlay.view.backgroundColor
        if (!splash.isRemoved) view.addSubview(splash.overlay.view)
    }

    override fun viewWillAppear(animated: Boolean) {
        super.viewWillAppear(animated)
        // Phase 1, before the first frame: the Core Animation timeline begins with the frame that ends the launch
        // screen. Phase 2 (the content) once that is committed.
        splash.start(onCommitted = ::embedContent)
    }

    /** Phase 2: creates the Compose controller and puts its view under the overlay. Once only. */
    private fun embedContent() {
        if (content != null) return
        val child = makeContent()
        content = child
        addChildViewController(child)
        child.view.setFrame(view.bounds)
        child.view.autoresizingMask = UIViewAutoresizingFlexibleWidth or UIViewAutoresizingFlexibleHeight
        view.insertSubview(child.view, atIndex = 0)
        child.didMoveToParentViewController(this)
    }

    override fun viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        if (splash.isRemoved || splash.overlay.isLeaving) return
        view.bounds.useContents {
            splash.overlay.layout(size.width, size.height, view.safeAreaInsets)
        }
    }
}

/** True once the app's launch has finished and it is in the foreground (its frames, not the launch screen, are shown). */
private fun appIsActive(): Boolean =
    UIApplication.sharedApplication.applicationState == UIApplicationState.UIApplicationStateActive

/** Runs [action] on the main queue after [delayMillis]. */
private fun onMainAfter(delayMillis: Long, action: () -> Unit) {
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, delayMillis * NANOS_PER_MILLI), dispatch_get_main_queue(), action)
}

private const val NANOS_PER_MILLI = 1_000_000L
