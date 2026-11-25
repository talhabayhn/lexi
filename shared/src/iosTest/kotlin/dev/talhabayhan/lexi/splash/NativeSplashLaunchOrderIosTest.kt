@file:OptIn(ExperimentalForeignApi::class)

package dev.talhabayhan.lexi.splash

import dev.talhabayhan.lexi.core.designsystem.theme.StandardLexiMotion
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.delay
import platform.CoreFoundation.CFRunLoopRunInMode
import platform.CoreFoundation.kCFRunLoopDefaultMode
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSNumber
import platform.Foundation.NSThread
import platform.Foundation.valueForKeyPath
import platform.QuartzCore.CACurrentMediaTime
import platform.QuartzCore.CALayer
import platform.QuartzCore.CATransaction
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIImage
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.beginAppearanceTransition
import platform.UIKit.endAppearanceTransition
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * The launch order with the real main run loop, real Core Animation commits and a real main-thread stall (S7): the
 * Compose stand-in blocks the main thread for [STALL_SECONDS] the moment it is created, like Compose's first
 * composition, synchronous first Metal draw and first-launch warm-up on a fresh install. The animation must have been
 * committed before that (its begin time known and anchored at the commit, not at the overlay's creation), so the
 * render server plays it from its first frame while the main thread is blocked.
 *
 * Not proven here: that the render server really draws those frames on a device (the simulator renders on the CPU and
 * its compositor is not the iPhone's), and real Metal pipeline builds; checked on an iPhone (`docs/LOCAL-DEVICE-TEST.md`).
 */
class NativeSplashLaunchOrderIosTest {
    private var window: UIWindow? = null
    private val splashes = mutableListOf<IosLaunchSplash>()

    @AfterTest
    fun tearDown() {
        // Leaves no live splash behind: its timers (3 s cap, exit, poll) would otherwise fire during later tests.
        splashes.forEach {
            it.onAnimationEnded()
            it.contentReady()
        }
        runLoopUntil(TIMEOUT_SECONDS) { splashes.all { it.isRemoved } }
        splashes.clear()
        window?.setHidden(true)
        window?.rootViewController = null
        window = null
    }

    @Test
    fun theAnimationIsCommittedBeforeTheComposeStallAndItsTimelineIsNotMovedByIt() {
        val overlay = NativeSplashOverlay(launchLogo = testLogo(), reducedMotion = false, fontScale = 1f)
        val createdAt = CACurrentMediaTime()
        val splash = testSplash(overlay)
        var madeAt: Double? = null
        var beginWhenMade: Double? = null
        var stallEndedAt: Double? = null
        val host = NativeSplashHostViewController(
            makeContent = {
                madeAt = CACurrentMediaTime()
                beginWhenMade = overlay.animationBeginTime()
                NSThread.sleepForTimeInterval(STALL_SECONDS)
                stallEndedAt = CACurrentMediaTime()
                UIViewController(nibName = null, bundle = null)
            },
            splash = splash,
        )
        // The overlay is created well before it is shown (Kotlin/Native and the app's own start-up run in between).
        NSThread.sleepForTimeInterval(PRE_SHOW_SECONDS)
        val shownAt = CACurrentMediaTime()
        show(host)
        runLoopUntil(TIMEOUT_SECONDS) { stallEndedAt != null }

        val made = assertNotNull(madeAt, "the Compose stand-in was never created")
        val begin = assertNotNull(beginWhenMade, "Compose was created before the animation was committed")
        // Anchored at the commit (when shown), never at the overlay's creation.
        assertTrue(begin >= shownAt - EPSILON, "begin $begin is before the overlay was shown ($shownAt)")
        assertTrue(begin - createdAt >= PRE_SHOW_SECONDS - EPSILON, "begin $begin anchored at creation $createdAt")
        // Committed before the stall, and phase 2 came promptly (a run-loop pass or two, not a timer's worth).
        assertTrue(begin <= made, "committed at $begin, after Compose was created at $made")
        assertTrue(made - begin < PROMPT_SECONDS, "Compose created ${made - begin} s after the commit")
        // The stall does not move the timeline: when it ends, the render server's clock has played it to the end.
        assertEquals(begin, overlay.animationBeginTime() ?: begin, "the begin time moved during the stall")
        val total = (IosLaunchSplash.LAUNCH_HAND_OFF_MILLIS + StandardLexiMotion.splashDurationMillis) / 1000.0
        assertTrue(stallEndedAt!! - begin > total, "the stall ended before the timeline's end")
        // The content is embedded under the overlay.
        assertTrue(host.view.subviews.firstOrNull() == host.content?.view, "the content is not under the overlay")
        assertTrue(host.view.subviews.lastOrNull() == overlay.view, "the overlay is not on top")
    }

    @Test
    fun duringTheStallTheCommittedTimelineMovesOn() {
        val overlay = NativeSplashOverlay(launchLogo = testLogo(), reducedMotion = false, fontScale = 1f)
        val splash = testSplash(overlay)
        var startedAfter: Double? = null
        var atStart: Double? = null
        var late: Double? = null
        val host = NativeSplashHostViewController(
            makeContent = {
                // The main thread stays blocked from here on: only Core Animation's clock moves.
                val begin = assertNotNull(overlay.animationBeginTime(), "Compose created before the commit")
                startedAfter = CACurrentMediaTime() - begin
                val tagline = overlay.taglineView.layer
                atStart = tagline.presentationValue("opacity")
                sleepUntil(begin + timelineSeconds(LATE_FRACTION))
                late = tagline.presentationValue("opacity")
                UIViewController(nibName = null, bundle = null)
            },
            splash = splash,
        )
        show(host)
        runLoopUntil(TIMEOUT_SECONDS) { host.content != null }
        // The tagline fades in at 48-58 % of the animation (≈ 0.93-1.05 s after the commit): hidden when phase 2 starts
        // (well before), fully visible late in the animation. Both were read while the main thread was blocked, so the
        // timeline moved on without the main thread. Wide windows: a shared CI runner's sleeps are not precise.
        val after = assertNotNull(startedAfter, "Compose was never created")
        assertTrue(after < timelineSeconds(FADE_START_FRACTION), "phase 2 came only $after s after the commit")
        val start = assertNotNull(atStart, "no presentation layer during the stall")
        assertTrue(start < 0.01, "the tagline is visible when phase 2 starts (opacity $start)")
        assertTrue(assertNotNull(late) > 0.99, "the tagline is not visible late in the animation (opacity $late)")
    }

    @Test
    fun theTaglineIsOnTheOverlayBeforeTheComposeStall() {
        val overlay = NativeSplashOverlay(launchLogo = testLogo(), reducedMotion = false, fontScale = 1f)
        val splash = IosLaunchSplash(
            overlay = overlay,
            reducedMotion = false,
            // The string resources are read asynchronously; slower than the commit on purpose.
            loadTagline = {
                delay(TAGLINE_DELAY_MILLIS)
                TAGLINE
            },
            isAppActive = { true },
        ).also { splashes += it }
        var textWhenMade: String? = null
        val host = NativeSplashHostViewController(
            makeContent = {
                textWhenMade = overlay.taglineView.attributedText?.string
                NSThread.sleepForTimeInterval(STALL_SECONDS / 4)
                UIViewController(nibName = null, bundle = null)
            },
            splash = splash,
        )
        show(host)
        runLoopUntil(TIMEOUT_SECONDS) { host.content != null }
        // Set (and committed) before Compose blocks the main thread: its fade-in is part of the animation.
        assertEquals(TAGLINE, textWhenMade, "Compose was created before the tagline was on the overlay")
    }

    /** The real splash (real timers, clock and commits), with the app active and no tagline to load. */
    private fun testSplash(overlay: NativeSplashOverlay) = IosLaunchSplash(
        overlay = overlay,
        reducedMotion = false,
        loadTagline = null,
        isAppActive = { true },
    ).also { splashes += it }

    /** Seconds from the timeline's begin to the animation's [fraction] (after the hand-off). */
    private fun timelineSeconds(fraction: Double): Double =
        (IosLaunchSplash.LAUNCH_HAND_OFF_MILLIS + fraction * StandardLexiMotion.splashDurationMillis) / 1000.0

    private fun sleepUntil(time: Double) {
        val wait = time - CACurrentMediaTime()
        if (wait > 0) NSThread.sleepForTimeInterval(wait)
    }

    private fun show(host: NativeSplashHostViewController) {
        val shown = UIWindow(frame = CGRectMake(0.0, 0.0, WIDTH, HEIGHT))
        window = shown
        shown.rootViewController = host
        shown.setHidden(false)
        // Without a window scene the test host may not send appearance callbacks: send them (start() runs once).
        if (host.content == null && host.view.subviews.size == 1) {
            host.beginAppearanceTransition(true, animated = false)
            host.endAppearanceTransition()
        }
    }

    private fun runLoopUntil(seconds: Double, done: () -> Boolean) {
        val end = CACurrentMediaTime() + seconds
        while (!done() && CACurrentMediaTime() < end) {
            CFRunLoopRunInMode(kCFRunLoopDefaultMode, RUN_LOOP_SLICE_SECONDS, false)
        }
    }

    /**
     * The on-screen value of [keyPath] now. A main-thread pass reads Core Animation's time once and keeps it: the empty commit before
     * the read makes it a fresh one (it sends nothing, so the main thread stays as blocked as before).
     */
    private fun CALayer.presentationValue(keyPath: String): Double? {
        CATransaction.flush()
        return presentationLayer()?.valueForKeyPath(keyPath)?.let { value ->
            (value as NSNumber).doubleValue
        }
    }

    private fun testLogo(): UIImage =
        UIGraphicsImageRenderer(size = platform.CoreGraphics.CGSizeMake(LOGO_WIDTH, LOGO_HEIGHT)).imageWithActions { }

    private companion object {
        const val WIDTH = 390.0
        const val HEIGHT = 844.0
        const val LOGO_WIDTH = 320.0
        const val LOGO_HEIGHT = 118.0
        const val STALL_SECONDS = 2.0
        const val TAGLINE = "A'DAN Z'YE İNGİLİZCE"
        const val TAGLINE_DELAY_MILLIS = 200L
        const val PRE_SHOW_SECONDS = 0.3
        const val PROMPT_SECONDS = 0.5
        const val TIMEOUT_SECONDS = 6.0
        const val RUN_LOOP_SLICE_SECONDS = 0.01
        const val EPSILON = 0.005

        /** The tagline's fade-in starts at 48 % (`SplashTimeline.taglineAlpha`); well after it, it is fully visible. */
        const val FADE_START_FRACTION = 0.48
        const val LATE_FRACTION = 0.9
    }
}
