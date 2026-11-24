@file:OptIn(ExperimentalForeignApi::class)

package dev.talhabayhan.lexi.splash

import dev.talhabayhan.lexi.core.designsystem.component.SplashTimeline
import dev.talhabayhan.lexi.core.designsystem.theme.StandardLexiMotion
import dev.talhabayhan.lexi.core.domain.AppInfo
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSNumber
import platform.QuartzCore.CAKeyframeAnimation
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIImage
import platform.UIKit.UIView
import platform.UIKit.accessibilityLabel
import platform.UIKit.accessibilityViewIsModal
import platform.UIKit.isAccessibilityElement
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The iOS native splash overlay's lifecycle on the simulator's UIKit (ADR 0028): shown above the content, animated
 * with Core Animation from [SplashTimeline] (same stops, duration and curve as the Compose splash), left only when the
 * animation has ended and the first screen is ready, or after the 3 s limit, then removed from the view hierarchy.
 * Timers are a fake ([schedule]); Core Animation's completion is simulated with [IosLaunchSplash.onAnimationEnded].
 *
 * Not proven here: real Metal stalls and the render server keeping the animation smooth while the main thread is
 * blocked (the simulator renders on the CPU; check on an iPhone, `docs/LOCAL-DEVICE-TEST.md`).
 */
class NativeSplashOverlayIosTest {
    private val timers = mutableListOf<Pair<Long, () -> Unit>>()
    private var clock = 0.0

    private fun schedule(delay: Long, action: () -> Unit) {
        timers += delay to action
    }

    /** Fires the pending timers due [millis] from now (in order), like the main queue would. */
    private fun fire(delayMillis: Long) {
        val due = timers.filter { it.first == delayMillis }
        timers.removeAll(due)
        due.forEach { it.second() }
    }

    private fun splash(reducedMotion: Boolean = false, logo: UIImage? = testLogo()): Pair<IosLaunchSplash, UIView> {
        val host = UIView(frame = CGRectMake(0.0, 0.0, WIDTH, HEIGHT))
        val overlay = NativeSplashOverlay(launchLogo = logo, reducedMotion = reducedMotion, fontScale = 1f)
        host.addSubview(UIView(frame = host.bounds))
        host.addSubview(overlay.view)
        overlay.layout(WIDTH, HEIGHT, safeArea = null)
        val splash = IosLaunchSplash(
            overlay = overlay,
            reducedMotion = reducedMotion,
            schedule = ::schedule,
            now = { clock },
            loadTagline = null,
            readBeginTime = { null },
        )
        return splash to host
    }

    @Test
    fun showsAboveTheContentAsOneModalImageElement() {
        val (splash, host) = splash()
        assertTrue(host.subviews.last() == splash.overlay.view, "the overlay is not on top")
        val view = splash.overlay.view
        assertTrue(view.isAccessibilityElement)
        assertTrue(view.accessibilityViewIsModal, "VoiceOver could reach the screen under the splash")
        assertEquals(AppInfo.NAME, view.accessibilityLabel)
        splash.overlay.setTagline("A'DAN Z'YE İNGİLİZCE")
        assertEquals("${AppInfo.NAME}, A'DAN Z'YE İNGİLİZCE", view.accessibilityLabel)
        assertTrue(view.userInteractionEnabled, "touches reach the screen under the splash")
    }

    @Test
    fun theBlocksRestWhereTheLaunchImageHasThem() {
        val (splash, _) = splash()
        val row = splash.overlay.blockViews
        assertEquals(4, row.size)
        // The launch image (320 x 118 pt) is centred on the screen; its blocks are at its top, 82 pt apart.
        val imageLeft = (WIDTH - LOGO_WIDTH) / 2
        val imageTop = (HEIGHT - LOGO_HEIGHT) / 2
        row.forEachIndexed { index, block ->
            block.frame.useContents {
                assertClose(imageLeft + index * BLOCK_STEP, origin.x, "block $index x")
                assertClose(imageTop, origin.y, "block $index y")
                assertClose(BLOCK, size.width, "block $index width")
            }
            assertNotNull((block as platform.UIKit.UIImageView).image, "block $index has no image")
        }
    }

    @Test
    fun holdsTheLaunchPoseThenPlaysTheSplashKeyframesWithCoreAnimation() {
        val (splash, _) = splash()
        // Before the animation: the launch screen's pose (blocks at rest, no shapes, no tagline).
        assertEquals(0f, splash.overlay.taglineView.layer.opacity, "the tagline shows in the launch pose")
        splash.start()
        val l = splash.overlay.blockViews[0].layer.animationForKey(
            "splash.transform.translation.x",
        ) as CAKeyframeAnimation
        val handOff = IosLaunchSplash.LAUNCH_HAND_OFF_MILLIS / 1000.0
        val duration = StandardLexiMotion.splashDurationMillis / 1000.0
        assertClose(handOff + duration, l.duration, "duration")
        val times = l.keyTimes!!.map { (it as NSNumber).doubleValue }
        val values = l.values!!.map { (it as NSNumber).doubleValue }
        // The launch pose (L at rest) is held for the hand-off, then the CSS stops follow at their fractions.
        assertClose(0.0, times[0], "launch pose start")
        assertClose(0.0, values[0], "launch pose value")
        assertClose(0.0, values[1], "launch pose held")
        val track = SplashTimeline.blockL
        for (i in 0 until track.size) {
            val expected = (handOff + track.fractionAt(i) * duration) / (handOff + duration)
            assertClose(expected, times[i + 2], "key time $i")
            assertClose(track.valueAt(i).toDouble(), values[i + 2], "value $i")
        }
        assertClose(1.0, times.last(), "the last value is not held to the end")
        assertNotNull(splash.overlay.blockViews[2].layer.animationForKey("splash.transform.rotation.z"))
        assertNotNull(splash.overlay.sunView.layer.animationForKey("splash.transform.scale"))
        assertNotNull(splash.overlay.taglineView.layer.animationForKey("splash.opacity"))
        // The model values are the resting pose (what stays when Core Animation removes the animation early).
        assertEquals(1f, splash.overlay.taglineView.layer.opacity, "the model value is not the resting pose")
    }

    @Test
    fun everyTrackIsAValidKeyframeAnimationOnOneTimeline() {
        val (splash, _) = splash()
        clock = 5.0
        splash.start()
        val overlay = splash.overlay
        val layers = overlay.blockViews.map { it.layer } + overlay.sunView.layer + overlay.blueView.layer +
            overlay.taglineView.layer
        var tracks = 0
        for (layer in layers) {
            for (key in layer.animationKeys().orEmpty().map { it as String }) {
                val animation = layer.animationForKey(key) as CAKeyframeAnimation
                val values = animation.values!!
                val times = animation.keyTimes!!.map { (it as NSNumber).doubleValue }
                assertEquals(values.size, times.size, "$key: values and key times")
                assertEquals(values.size - 1, animation.timingFunctions!!.size, "$key: timing functions")
                assertClose(0.0, times.first(), "$key: first key time")
                assertClose(1.0, times.last(), "$key: last key time")
                times.zipWithNext().forEach { (a, b) -> assertTrue(b > a, "$key: key times not increasing") }
                // Begins when committed (the first frame shown), never at a fixed time that may be past already.
                assertClose(0.0, animation.beginTime, "$key: begin time")
                tracks++
            }
        }
        // L, E, X spin + scale, I, two shapes, tagline fade + rise.
        assertEquals(9, tracks)
    }

    @Test
    fun endsOnlyAfterTheAnimationAndTheFirstScreen() {
        val (splash, host) = splash()
        splash.start()
        assertFalse(splash.contentReady())
        assertNull(splash.leaveReason, "left before the animation ended")
        splash.onAnimationEnded()
        assertEquals(SplashLeaveReason.Ready, splash.leaveReason)
        assertTrue(splash.overlay.isLeaving)
        assertFalse(splash.overlay.view.userInteractionEnabled, "touches still blocked while it leaves")
        assertNotNull(splash.overlay.view.layer.animationForKey("leave.opacity"))
        assertTrue(splash.overlay.view.superview == host, "removed before its exit was played")
        fire(StandardLexiMotion.screenTransitionDurationMillis.toLong())
        assertTrue(splash.isRemoved)
        assertNull(splash.overlay.view.superview, "still in the view hierarchy")
        assertFalse(host.subviews.contains(splash.overlay.view))
    }

    @Test
    fun theThreeSecondLimitRemovesItWhenTheFirstScreenNeverComes() {
        val (splash, _) = splash()
        splash.start()
        splash.onAnimationEnded()
        assertNull(splash.leaveReason)
        fire(NativeSplashGate.CAP_MILLIS)
        assertEquals(SplashLeaveReason.TimedOut, splash.leaveReason)
        fire(StandardLexiMotion.screenTransitionDurationMillis.toLong())
        assertTrue(splash.isRemoved)
        assertNull(splash.overlay.view.superview)
        // The late first screen learns that the fail-safe fired (logged as motion_failsafe), once.
        assertTrue(splash.contentReady())
        assertFalse(splash.contentReady())
    }

    @Test
    fun theLimitAlsoEndsAnAnimationThatNeverCompletes() {
        val (splash, _) = splash()
        splash.start()
        splash.contentReady()
        fire(NativeSplashGate.CAP_MILLIS)
        assertEquals(SplashLeaveReason.TimedOut, splash.leaveReason)
        fire(StandardLexiMotion.screenTransitionDurationMillis.toLong())
        assertNull(splash.overlay.view.superview)
    }

    @Test
    fun reducedMotionHoldsTheStaticLogoThenCrossfades() {
        val (splash, _) = splash(reducedMotion = true)
        splash.start()
        assertNull(splash.overlay.blockViews[0].layer.animationKeys(), "animated with reduced motion")
        splash.contentReady()
        assertNull(splash.leaveReason, "left before the static hold")
        fire(StandardLexiMotion.splashReducedHoldMillis.toLong())
        assertEquals(SplashLeaveReason.Ready, splash.leaveReason)
        assertNull(splash.overlay.view.layer.animationForKey("leave.slide"), "a slide with reduced motion")
        fire(StandardLexiMotion.screenTransitionReducedCrossfadeMillis.toLong())
        assertTrue(splash.isRemoved)
    }

    @Test
    fun elapsedTimeFollowsTheOverlayClock() {
        val (splash, _) = splash()
        assertTrue(splash.elapsedMillis() < 0, "started before start()")
        clock = 9.0
        splash.start()
        clock = 10.0
        fire(0L)
        val handOff = IosLaunchSplash.LAUNCH_HAND_OFF_MILLIS
        assertEquals(-handOff, splash.elapsedMillis(), "the animation starts after the hand-off")
        clock = 10.0 + (handOff + 312) / 1000.0
        assertEquals(312L, splash.elapsedMillis())
    }

    @Test
    fun withoutTheLaunchImageItStillRunsAndEnds() {
        val (splash, _) = splash(logo = null)
        splash.start()
        splash.onAnimationEnded()
        splash.contentReady()
        fire(StandardLexiMotion.screenTransitionDurationMillis.toLong())
        assertTrue(splash.isRemoved)
    }

    @Test
    fun theHostPutsTheOverlayAboveTheComposeView() {
        val (splash, _) = splash()
        splash.overlay.view.removeFromSuperview()
        val content = platform.UIKit.UIViewController(nibName = null, bundle = null)
        val host = NativeSplashHostViewController(content, splash)
        val views = host.view.subviews
        assertEquals(2, views.size)
        assertTrue(views[0] == content.view, "the Compose view is not under the overlay")
        assertTrue(views[1] == splash.overlay.view, "the overlay is not on top")
        assertTrue(content.parentViewController == host)
    }

    private fun assertClose(expected: Double, actual: Double, what: String) =
        assertTrue(abs(expected - actual) < 0.01, "$what: expected $expected, was $actual")

    /** A stand-in for `LaunchLogo` (not in the test bundle): 320 x 118 pt at 3x. */
    private fun testLogo(): UIImage =
        UIGraphicsImageRenderer(size = platform.CoreGraphics.CGSizeMake(LOGO_WIDTH, LOGO_HEIGHT)).imageWithActions { }

    private companion object {
        const val WIDTH = 390.0
        const val HEIGHT = 844.0
        const val LOGO_WIDTH = 320.0
        const val LOGO_HEIGHT = 118.0
        const val BLOCK = 74.0
        const val BLOCK_STEP = 82.0
    }
}
