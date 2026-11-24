package dev.talhabayhan.lexi.splash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.runComposeUiTest
import dev.talhabayhan.lexi.core.designsystem.component.SplashDefaults
import dev.talhabayhan.lexi.core.designsystem.feedback.LocalHaptics
import dev.talhabayhan.lexi.core.designsystem.feedback.LocalSoundPlayer
import dev.talhabayhan.lexi.core.designsystem.motion.FrameMonitor
import dev.talhabayhan.lexi.core.designsystem.motion.LocalMotionPolicy
import dev.talhabayhan.lexi.core.designsystem.motion.MotionPolicy
import dev.talhabayhan.lexi.core.designsystem.motion.TransitionSession
import dev.talhabayhan.lexi.core.designsystem.motion.TransitionTelemetry
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.AppInfo
import dev.talhabayhan.lexi.core.domain.feedback.Haptics
import dev.talhabayhan.lexi.core.domain.feedback.SoundPlayer
import dev.talhabayhan.lexi.core.domain.settings.Settings
import dev.talhabayhan.lexi.core.testing.FakeAnalyticsTracker
import dev.talhabayhan.lexi.core.testing.FakeAppClock
import dev.talhabayhan.lexi.core.testing.FakeContentVersionRepository
import dev.talhabayhan.lexi.core.testing.FakeOnboardingRepository
import dev.talhabayhan.lexi.core.testing.FakeSettingsRepository
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * `SplashRoute` under the iOS native splash (ADR 0028), on the host renderer with a fake [LaunchSplash]: nothing
 * animates or lands in Compose, the first-launch warm-up still runs (behind the overlay) and stores the version, the app
 * moves on without waiting for the 1.2 s animation, and nothing announces the logo (the overlay does).
 * [NativeSplashFeedback] plays the four landings on the overlay's clock, also after the splash screen has gone.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class NativeSplashRouteTest {
    private val landings = mutableListOf<Long>()
    private var finished: Boolean? = null

    @Before
    fun reset() {
        SplashState.finished.value = false
    }

    private class FakeNativeSplash(private val clock: () -> Long) : LaunchSplash {
        var readyCalls = 0
        override val isNative: Boolean = true

        override fun elapsedMillis(): Long = clock()

        override fun contentReady(): Boolean {
            readyCalls++
            return false
        }
    }

    @Test
    fun `a later launch moves on at once with no Compose animation and no landings`() = runComposeUiTest {
        mainClock.autoAdvance = false
        currentTime = { mainClock.currentTime }
        val start = mainClock.currentTime
        setRoute(
            Settings(warmedUpVersionCode = AppInfo.BUILD_NUMBER),
            FakeNativeSplash {
                mainClock.currentTime - start
            },
        )

        var frames = 0
        while (finished == null && frames++ < QUICK_FRAMES) mainClock.advanceTimeByFrame()
        assertNotNull(finished, "waited for the animation although the overlay plays it")
        assertTrue(
            mainClock.currentTime - start < SPLASH_MILLIS / 2,
            "moved on only after ${mainClock.currentTime - start} ms",
        )
        assertTrue(landings.isEmpty(), "Compose played landings under the native splash")
        assertTrue(SplashState.finished.value)
        assertEquals(0, onAllNodesWithContentDescription(AppInfo.NAME).fetchSemanticsNodes().size, "announced twice")
    }

    @Test
    fun `the first launch of a version still warms up behind the overlay and stores the version`() = runComposeUiTest {
        mainClock.autoAdvance = false
        currentTime = { mainClock.currentTime }
        val settings = FakeSettingsRepository(Settings(warmedUpVersionCode = 0))
        setRoute(settings = settings, splash = FakeNativeSplash { 0L })

        var frames = 0
        while (!warmUpShown() && frames++ < DECISION_FRAMES) mainClock.advanceTimeByFrame()
        assertTrue(warmUpShown(), "no warm-up on the first launch of this version")
        frames = 0
        while (finished == null && frames++ < QUICK_FRAMES) mainClock.advanceTimeByFrame()
        assertNotNull(finished, "the app never moved on")
        assertEquals(AppInfo.BUILD_NUMBER, settings.settings.value.warmedUpVersionCode, "version not recorded")
        assertTrue(landings.isEmpty())
    }

    @Test
    fun `the landings follow the overlay clock and outlive the splash screen`() = runComposeUiTest {
        mainClock.autoAdvance = false
        currentTime = { mainClock.currentTime }
        val start = mainClock.currentTime
        val splash = FakeNativeSplash { mainClock.currentTime - start }
        var showRoute by mutableStateOf(true)
        setContent {
            Locals {
                LexiTheme(reducedMotion = false) {
                    NativeSplashFeedback(splash)
                    if (showRoute) {
                        route(
                            FakeSettingsRepository(Settings(warmedUpVersionCode = AppInfo.BUILD_NUMBER)),
                            splash,
                        )
                    }
                }
            }
        }
        mainClock.advanceTimeBy(FRAME_MILLIS * 4)
        showRoute = false
        mainClock.advanceTimeBy(SPLASH_MILLIS)

        assertEquals(SplashDefaults.LandingFractions.size, landings.size)
        SplashDefaults.LandingFractions.forEachIndexed { index, fraction ->
            val expected = (fraction * SPLASH_MILLIS).toLong()
            val at = landings[index] - start
            assertTrue(at in expected..expected + 2 * FRAME_MILLIS, "landing $index at $at, expected $expected")
        }
    }

    @Test
    fun `no landings with reduced motion`() = runComposeUiTest {
        mainClock.autoAdvance = false
        currentTime = { mainClock.currentTime }
        val start = mainClock.currentTime
        val splash = FakeNativeSplash { mainClock.currentTime - start }
        setContent {
            Locals { LexiTheme(reducedMotion = true) { NativeSplashFeedback(splash) } }
        }
        mainClock.advanceTimeBy(SPLASH_MILLIS * 2)
        assertTrue(landings.isEmpty())
    }

    @Test
    fun `without a native splash the feedback does nothing`() = runComposeUiTest {
        mainClock.autoAdvance = false
        currentTime = { mainClock.currentTime }
        setContent { Locals { LexiTheme(reducedMotion = false) { NativeSplashFeedback(NoLaunchSplash) } } }
        mainClock.advanceTimeBy(SPLASH_MILLIS * 2)
        assertTrue(landings.isEmpty())
    }

    private fun ComposeUiTest.warmUpShown(): Boolean =
        onAllNodesWithTag(SPLASH_WARM_UP_TAG, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()

    private fun ComposeUiTest.setRoute(settings: Settings, splash: LaunchSplash) =
        setRoute(FakeSettingsRepository(settings), splash)

    private fun ComposeUiTest.setRoute(settings: FakeSettingsRepository, splash: LaunchSplash) {
        setContent { Locals { LexiTheme(reducedMotion = false) { route(settings, splash) } } }
    }

    @Composable
    private fun Locals(content: @Composable () -> Unit) {
        CompositionLocalProvider(
            LocalHaptics provides Haptics { landings += currentTime() },
            LocalSoundPlayer provides SoundPlayer { },
            LocalMotionPolicy provides MotionPolicy(FrameMonitor()),
            content = content,
        )
    }

    @Composable
    private fun route(settings: FakeSettingsRepository, splash: LaunchSplash) {
        SplashRoute(
            onFinished = { finished = it },
            content = FakeContentVersionRepository(),
            onboarding = FakeOnboardingRepository(),
            analytics = FakeAnalyticsTracker(),
            settings = settings,
            clock = FakeAppClock(),
            telemetry = TransitionTelemetry(FakeAnalyticsTracker(), TransitionSession()),
            launchSplash = splash,
        )
    }

    private var currentTime: () -> Long = { 0L }

    private companion object {
        const val SPLASH_MILLIS = 1_200L
        const val FRAME_MILLIS = 16L
        const val DECISION_FRAMES = 3
        const val QUICK_FRAMES = 20
    }
}
