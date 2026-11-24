package dev.talhabayhan.lexi.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The first launch of a version on the iOS simulator's Skia renderer (`docs/PERF.md` § Launch logo): the held logo,
 * the hidden warm-up at real size (onboarding, Home, game board, flow; Skia aborted the app on a negative line height
 * when the board was laid out too small, `SplashWarmUpIosTest`), a frame that looks unchanged while it is drawn, then
 * the animation and the stored version. A later launch does not hold.
 */
@OptIn(ExperimentalTestApi::class)
class SplashFirstLaunchIosTest {
    private var landings = 0
    private var finished: Boolean? = null

    @Test
    fun theFirstLaunchHoldsTheLogoWarmsUpHiddenThenAnimatesAndStoresTheVersion() = runComposeUiTest {
        mainClock.autoAdvance = false
        SplashState.finished.value = false
        val settings = FakeSettingsRepository(Settings(warmedUpVersionCode = 0))
        setSplash(settings)
        val heldLogo = onRoot().captureToImage().toPixelMap()

        var frames = 0
        while (!warmUpShown() && frames++ < DECISION_FRAMES) mainClock.advanceTimeByFrame()
        assertTrue(warmUpShown(), "no warm-up on the first launch of this version")
        val withWarmUp = onRoot().captureToImage().toPixelMap()
        assertEquals(heldLogo.width, withWarmUp.width)
        var differing = 0
        for (x in 0 until heldLogo.width) {
            for (y in 0 until heldLogo.height) if (heldLogo[x, y] != withWarmUp[x, y]) differing++
        }
        assertEquals(0, differing, "the warm-up is visible")

        frames = 0
        while (warmUpShown() && frames++ < SPLASH_WARM_UP_FRAMES + 2) mainClock.advanceTimeByFrame()
        assertTrue(!warmUpShown(), "the warm-up never ends")
        assertEquals(0, landings, "a landing during the hold")
        mainClock.advanceTimeBy(SPLASH_MILLIS + SLACK_MILLIS)

        assertEquals(LANDINGS, landings)
        assertEquals(false, finished)
        assertEquals(AppInfo.BUILD_NUMBER, settings.settings.value.warmedUpVersionCode)
    }

    @Test
    fun aLaterLaunchNeverShowsTheWarmUp() = runComposeUiTest {
        mainClock.autoAdvance = false
        SplashState.finished.value = false
        setSplash(FakeSettingsRepository(Settings(warmedUpVersionCode = AppInfo.BUILD_NUMBER)))

        repeat(((SPLASH_MILLIS + SLACK_MILLIS) / FRAME_MILLIS).toInt()) {
            assertTrue(!warmUpShown(), "a warm-up on a later launch")
            mainClock.advanceTimeByFrame()
        }
        assertEquals(LANDINGS, landings)
    }

    private fun ComposeUiTest.warmUpShown(): Boolean =
        onAllNodesWithTag(SPLASH_WARM_UP_TAG, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()

    private fun ComposeUiTest.setSplash(settings: FakeSettingsRepository) {
        setContent {
            CompositionLocalProvider(
                LocalHaptics provides Haptics { landings++ },
                LocalSoundPlayer provides SoundPlayer { },
                LocalMotionPolicy provides MotionPolicy(FrameMonitor()),
            ) {
                LexiTheme(reducedMotion = false) {
                    Box(Modifier.size(PHONE_WIDTH, PHONE_HEIGHT)) {
                        SplashRoute(
                            onFinished = { finished = it },
                            content = FakeContentVersionRepository(),
                            onboarding = FakeOnboardingRepository(),
                            analytics = FakeAnalyticsTracker(),
                            settings = settings,
                            clock = FakeAppClock(),
                            telemetry = TransitionTelemetry(FakeAnalyticsTracker(), TransitionSession()),
                        )
                    }
                }
            }
        }
    }

    private companion object {
        val PHONE_WIDTH = 390.dp
        val PHONE_HEIGHT = 844.dp
        const val DECISION_FRAMES = 3
        const val SPLASH_MILLIS = 1_200L
        const val SLACK_MILLIS = 200L
        const val FRAME_MILLIS = 16L
        const val LANDINGS = 4
    }
}
