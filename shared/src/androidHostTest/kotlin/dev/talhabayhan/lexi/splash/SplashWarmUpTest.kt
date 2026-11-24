package dev.talhabayhan.lexi.splash

import android.graphics.Bitmap
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.component.SplashDefaults
import dev.talhabayhan.lexi.core.designsystem.feedback.LocalHaptics
import dev.talhabayhan.lexi.core.designsystem.feedback.LocalSoundPlayer
import dev.talhabayhan.lexi.core.designsystem.motion.FrameMonitor
import dev.talhabayhan.lexi.core.designsystem.motion.FrameMonitorEffect
import dev.talhabayhan.lexi.core.designsystem.motion.LocalMotionPolicy
import dev.talhabayhan.lexi.core.designsystem.motion.MotionPolicy
import dev.talhabayhan.lexi.core.designsystem.motion.MotionTier
import dev.talhabayhan.lexi.core.designsystem.motion.TransitionSession
import dev.talhabayhan.lexi.core.designsystem.motion.TransitionTelemetry
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.AppInfo
import dev.talhabayhan.lexi.core.domain.feedback.Haptics
import dev.talhabayhan.lexi.core.domain.feedback.SoundPlayer
import dev.talhabayhan.lexi.core.domain.repository.SettingsRepository
import dev.talhabayhan.lexi.core.domain.settings.Settings
import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsEvent
import dev.talhabayhan.lexi.core.testing.FakeAnalyticsTracker
import dev.talhabayhan.lexi.core.testing.FakeAppClock
import dev.talhabayhan.lexi.core.testing.FakeContentVersionRepository
import dev.talhabayhan.lexi.core.testing.FakeOnboardingRepository
import dev.talhabayhan.lexi.core.testing.FakeSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The first-launch warm-up (`docs/PERF.md`): on the first launch of each app version the splash poses, the game board,
 * the card flip, the dialog and the picker cards are drawn hidden *before* the 1.2 s clock starts, so first-use GPU work
 * never lands inside an animation; the version is then stored and later launches skip it (and are not slower than
 * before). The animation itself keeps its duration and landings.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class SplashWarmUpTest {
    private val landings = mutableListOf<Long>()
    private var red = 0
    private var yellow = 0
    private var finished: Boolean? = null

    @Before
    fun reset() {
        SplashState.finished.value = false
    }

    @Test
    fun `the first launch of a version warms up before the clock and records the version`() = runComposeUiTest {
        mainClock.autoAdvance = false
        val settings = FakeSettingsRepository(Settings(warmedUpVersionCode = 0))
        setSplash(reducedMotion = false, settings = settings)

        assertTrue(awaitWarmUp(), "no warm-up on the first launch of this version")
        var frames = 0
        while (warmUpShown()) {
            assertTrue(landings.isEmpty(), "a landing during the warm-up")
            assertTrue(frames < SPLASH_WARM_UP_FRAMES + 2, "the warm-up never ends")
            mainClock.advanceTimeByFrame()
            frames++
        }
        val clockStart = mainClock.currentTime

        mainClock.advanceTimeBy(SPLASH_MILLIS + SLACK_MILLIS)

        assertEquals(AppInfo.BUILD_NUMBER, settings.settings.value.warmedUpVersionCode, "version not recorded")
        assertEquals(SplashDefaults.LandingFractions.size, landings.size)
        // The first landing (L at 26 %) is timed from the animation's start, not from the first frame.
        val firstLanding = landings.first() - clockStart
        val expected = (SplashDefaults.LandingFractions.first() * SPLASH_MILLIS).toLong()
        assertTrue(
            firstLanding in expected - FRAME_MILLIS..expected + 2 * FRAME_MILLIS,
            "first landing at $firstLanding",
        )
        assertEquals(false, finished)
        assertTrue(!warmUpShown(), "the warm-up came back")
    }

    @Test
    fun `a later launch of the same version skips the warm-up and is not slower`() = runComposeUiTest {
        mainClock.autoAdvance = false
        val settings = FakeSettingsRepository(Settings(warmedUpVersionCode = AppInfo.BUILD_NUMBER))
        val launch = mainClock.currentTime
        setSplash(reducedMotion = false, settings = settings)

        repeat(((SPLASH_MILLIS + SLACK_MILLIS) / FRAME_MILLIS).toInt()) {
            assertTrue(!warmUpShown(), "a warm-up on a later launch")
            mainClock.advanceTimeByFrame()
        }

        assertEquals(SplashDefaults.LandingFractions.size, landings.size)
        // Up to 1.1.1 the clock started after a 2-frame pre-roll on every launch; now it never starts later.
        val expected = (SplashDefaults.LandingFractions.first() * SPLASH_MILLIS).toLong()
        val firstLanding = landings.first() - launch
        assertTrue(firstLanding <= expected + OLD_PRE_ROLL_FRAMES * FRAME_MILLIS, "first landing at $firstLanding")
        assertEquals(AppInfo.BUILD_NUMBER, settings.settings.value.warmedUpVersionCode)
    }

    @Test
    fun `a store not read within two frames skips the warm-up, stores nothing and is not slower`() = runComposeUiTest {
        mainClock.autoAdvance = false
        val stored = FakeSettingsRepository(Settings(warmedUpVersionCode = 0))
        val unread = SlowSettings(stored)
        val launch = mainClock.currentTime
        setSplash(reducedMotion = false, settings = unread)

        repeat(((SPLASH_MILLIS + SLACK_MILLIS) / FRAME_MILLIS).toInt()) {
            assertTrue(!warmUpShown(), "a warm-up although the stored version was not known")
            mainClock.advanceTimeByFrame()
        }

        assertEquals(SplashDefaults.LandingFractions.size, landings.size)
        val expected = (SplashDefaults.LandingFractions.first() * SPLASH_MILLIS).toLong()
        val firstLanding = landings.first() - launch
        assertTrue(firstLanding <= expected + OLD_PRE_ROLL_FRAMES * FRAME_MILLIS, "first landing at $firstLanding")
        assertEquals(0, stored.settings.value.warmedUpVersionCode, "recorded without a warm-up")
    }

    @Test
    fun `a store read after one frame still warms up`() = runComposeUiTest {
        mainClock.autoAdvance = false
        val stored = FakeSettingsRepository(Settings(warmedUpVersionCode = 0))
        val slow = SlowSettings(stored)
        setSplash(reducedMotion = false, settings = slow)
        mainClock.advanceTimeByFrame()
        slow.loaded.value = stored.settings.value

        assertTrue(awaitWarmUp(), "no warm-up once the stored version arrived")
        mainClock.advanceTimeBy(SPLASH_MILLIS + SLACK_MILLIS)
        assertEquals(AppInfo.BUILD_NUMBER, stored.settings.value.warmedUpVersionCode)
    }

    @Test
    fun `an update warms up again`() = runComposeUiTest {
        mainClock.autoAdvance = false
        val settings = FakeSettingsRepository(Settings(warmedUpVersionCode = AppInfo.BUILD_NUMBER - 1))
        setSplash(reducedMotion = false, settings = settings)

        assertTrue(awaitWarmUp(), "no warm-up after an update")
        mainClock.advanceTimeBy(SPLASH_MILLIS + SLACK_MILLIS)
        assertEquals(AppInfo.BUILD_NUMBER, settings.settings.value.warmedUpVersionCode)
    }

    @Test
    fun `the warm-up is drawn above the splash under an inset cover and the held logo looks unchanged`() =
        runComposeUiTest {
            mainClock.autoAdvance = false
            var cream = 0
            setSplash(reducedMotion = false) { cream = it }
            // The held logo of the first frame, before the warm-up is composed.
            val heldLogo = onRoot().captureToImage().asAndroidBitmap().pixels()
            assertTrue(awaitWarmUp(), "no warm-up")

            // Regression (1.1.1): drawn behind a full-screen opaque background, pre-draws were dropped by Skia's GPU
            // backends before they reached the GPU. A cover inside the screen's edges is an ordinary draw.
            val root = onRoot().getBoundsInRoot()
            val warmUp = onNodeWithTag(SPLASH_WARM_UP_TAG, useUnmergedTree = true).getBoundsInRoot()
            assertTrue(
                warmUp.left > root.left && warmUp.top > root.top && warmUp.right < root.right &&
                    warmUp.bottom < root.bottom,
                "the warm-up's cover spans the screen: $warmUp in $root",
            )
            // Hidden: the warm-up frame (splash poses, onboarding, Home, game board, card flip, dialog, cards, morph,
            // all at real size) is pixel for pixel the held logo (drawn again above the cover).
            val withWarmUp = onRoot().captureToImage().asAndroidBitmap().pixels()
            val other = withWarmUp.indices.firstOrNull { withWarmUp[it] != heldLogo[it] }
            assertNull(other, "the warm-up is visible at pixel $other")
            assertTrue(cream in heldLogo, "nothing drawn")
            assertTrue(logoAtRest(), "the held logo is not shown")
            var frames = 0
            while (warmUpShown() && frames++ < SPLASH_WARM_UP_FRAMES + 2) mainClock.advanceTimeByFrame()
            assertTrue(!warmUpShown(), "the warm-up never ends")
            assertTrue(landings.isEmpty())
        }

    @Test
    fun `the first frame holds the launch logo and the animation starts from its first pose`() = runComposeUiTest {
        mainClock.autoAdvance = false
        setSplash(reducedMotion = false, settings = FakeSettingsRepository(Settings(warmedUpVersionCode = 0)))

        // The native launch screen's image: the four blocks at rest, no shapes, no tagline.
        assertTrue(logoAtRest(), "the first frame does not show the launch logo")
        assertTrue(!shapesShown(), "the first frame shows the corner shapes")
        // Held through the warm-up, then the animation starts (the L is off screen at its start).
        assertTrue(awaitWarmUp())
        assertTrue(logoAtRest(), "the logo was not held during the warm-up")
        var frames = 0
        while (warmUpShown() && frames++ < SPLASH_WARM_UP_FRAMES + 2) mainClock.advanceTimeByFrame()
        mainClock.advanceTimeByFrame()
        assertTrue(!logoAtRest(), "the animation did not start after the warm-up")
    }

    @Test
    fun `a later launch shows the launch logo for its first frame only, without a hold`() = runComposeUiTest {
        mainClock.autoAdvance = false
        setSplash(
            reducedMotion = false,
            settings = FakeSettingsRepository(Settings(warmedUpVersionCode = AppInfo.BUILD_NUMBER)),
        )

        assertTrue(logoAtRest(), "the first frame does not show the launch logo")
        repeat(OLD_PRE_ROLL_FRAMES) { mainClock.advanceTimeByFrame() }
        assertTrue(!logoAtRest(), "a later launch held the logo")
    }

    @Test
    fun `a warm-up that does not complete within 2 seconds is cut, the animation starts and nothing is stored`() =
        runComposeUiTest {
            mainClock.autoAdvance = false
            val settings = FakeSettingsRepository(Settings(warmedUpVersionCode = 0))
            setSplash(reducedMotion = false, settings = settings, warmUpFrames = Int.MAX_VALUE)
            assertTrue(awaitWarmUp(), "no warm-up")
            val holdStart = mainClock.currentTime

            while (warmUpShown()) {
                assertTrue(landings.isEmpty(), "a landing during the hold")
                assertTrue(mainClock.currentTime - holdStart <= FIRST_LAUNCH_HOLD_CAP_MILLIS + 2 * FRAME_MILLIS)
                mainClock.advanceTimeByFrame()
            }
            assertTrue(mainClock.currentTime - holdStart >= FIRST_LAUNCH_HOLD_CAP_MILLIS - 2 * FRAME_MILLIS)
            mainClock.advanceTimeBy(SPLASH_MILLIS + SLACK_MILLIS)

            assertEquals(SplashDefaults.LandingFractions.size, landings.size, "the animation did not run")
            assertEquals(false, finished)
            assertEquals(0, settings.settings.value.warmedUpVersionCode, "a capped warm-up was recorded")
        }

    @Test
    fun `the splash animation is measured once with the first-launch flag`() = runComposeUiTest {
        mainClock.autoAdvance = false
        val events = FakeAnalyticsTracker()
        val telemetry = TransitionTelemetry(events, TransitionSession())
        val policy = MotionPolicy(FrameMonitor())
        policy.frameMonitor.onTransitionMeasured = telemetry.listener { MotionTier.Full }
        setSplash(
            reducedMotion = false,
            settings = FakeSettingsRepository(Settings(warmedUpVersionCode = 0)),
            telemetry = telemetry,
            policy = policy,
        )
        mainClock.advanceTimeBy(SPLASH_MILLIS + SLACK_MILLIS + FIRST_LAUNCH_HOLD_CAP_MILLIS)

        val splash = events.events.filterIsInstance<AnalyticsEvent.TransitionFrames>()
        assertEquals(1, splash.size, "events: ${events.events}")
        assertEquals(AnalyticsEvent.PerfTransition.SPLASH, splash.single().transition)
        assertEquals(true, splash.single().firstLaunchOfVersion)
        assertEquals(AnalyticsEvent.MotionTierCode.FULL, splash.single().motionTier)
    }

    @Test
    fun `reduced motion has no warm-up and records nothing`() = runComposeUiTest {
        mainClock.autoAdvance = false
        val settings = FakeSettingsRepository(Settings(warmedUpVersionCode = 0))
        setSplash(reducedMotion = true, settings = settings)

        assertTrue(!awaitWarmUp())
        assertNull(finished)
        mainClock.advanceTimeBy(SPLASH_MILLIS)

        assertEquals(false, finished)
        assertTrue(landings.isEmpty())
        assertEquals(0, settings.settings.value.warmedUpVersionCode)
    }

    private fun Bitmap.pixels(): IntArray = IntArray(width * height).also {
        getPixels(it, 0, width, 0, 0, width, height)
    }

    /** Whether the L block sits at rest at its place in the logo (red inside its border). */
    private fun ComposeUiTest.logoAtRest(): Boolean {
        val logo = onAllNodesWithContentDescription(AppInfo.NAME).fetchSemanticsNodes().first().boundsInRoot
        val inset = with(density) { LOGO_PROBE_INSET.toPx() }
        val pixel = onRoot().captureToImage().asAndroidBitmap()
            .getPixel((logo.left + inset).toInt(), (logo.top + inset).toInt())
        return pixel == red
    }

    /** Whether the yellow circle of the top-left corner is drawn. */
    private fun ComposeUiTest.shapesShown(): Boolean {
        val probe = with(density) { SHAPE_PROBE.toPx().toInt() }
        return onRoot().captureToImage().asAndroidBitmap().getPixel(probe, probe) == yellow
    }

    private fun ComposeUiTest.warmUpShown(): Boolean =
        onAllNodesWithTag(SPLASH_WARM_UP_TAG, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()

    /** Steps up to a few frames for the warm-up to appear (the decision reads the stored version first). */
    private fun ComposeUiTest.awaitWarmUp(): Boolean {
        repeat(DECISION_FRAMES) {
            if (warmUpShown()) return true
            mainClock.advanceTimeByFrame()
        }
        return warmUpShown()
    }

    /** A store whose first read has not arrived yet ([loaded] stays null until the test sets it). */
    private class SlowSettings(private val stored: FakeSettingsRepository) : SettingsRepository by stored {
        override val loaded: MutableStateFlow<Settings?> = MutableStateFlow(null)
    }

    private fun ComposeUiTest.setSplash(
        reducedMotion: Boolean,
        settings: SettingsRepository = FakeSettingsRepository(),
        warmUpFrames: Int = SPLASH_WARM_UP_FRAMES,
        telemetry: TransitionTelemetry = TransitionTelemetry(FakeAnalyticsTracker(), TransitionSession()),
        policy: MotionPolicy = MotionPolicy(FrameMonitor()),
        onCream: (Int) -> Unit = {},
    ) {
        setContent {
            onCream(LexiTheme.colors.cream.toArgb())
            red = LexiTheme.colors.red.toArgb()
            yellow = LexiTheme.colors.yellow.toArgb()
            CompositionLocalProvider(
                LocalHaptics provides Haptics { landings += mainClock.currentTime },
                LocalSoundPlayer provides SoundPlayer { },
                LocalMotionPolicy provides policy,
            ) {
                LexiTheme(reducedMotion = reducedMotion) {
                    FrameMonitorEffect(policy.frameMonitor)
                    SplashRoute(
                        onFinished = { finished = it },
                        content = FakeContentVersionRepository(),
                        onboarding = FakeOnboardingRepository(),
                        analytics = FakeAnalyticsTracker(),
                        settings = settings,
                        clock = FakeAppClock(),
                        telemetry = telemetry,
                        warmUpFrames = warmUpFrames,
                    )
                }
            }
        }
    }

    private companion object {
        const val SPLASH_MILLIS = 1_200L
        const val FRAME_MILLIS = 16L
        const val SLACK_MILLIS = 200L
        const val DECISION_FRAMES = 3

        /** Inside the L block's 4dp border, off its letter. */
        val LOGO_PROBE_INSET = 10.dp

        /** Inside the yellow circle's visible quarter (centre at 40dp, 40dp, radius 160dp). */
        val SHAPE_PROBE = 20.dp

        /**
         * Frames 1.1.1 added on every launch before a landing could be seen: 2 pre-roll frames, the animation's first
         * frame (its time 0), and the frame a landing is sampled on.
         */
        const val OLD_PRE_ROLL_FRAMES = 4
    }
}
