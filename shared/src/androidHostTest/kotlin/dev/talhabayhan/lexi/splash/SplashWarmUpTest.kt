package dev.talhabayhan.lexi.splash

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.runComposeUiTest
import dev.talhabayhan.lexi.core.designsystem.component.SplashDefaults
import dev.talhabayhan.lexi.core.designsystem.feedback.LocalHaptics
import dev.talhabayhan.lexi.core.designsystem.feedback.LocalSoundPlayer
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.feedback.Haptics
import dev.talhabayhan.lexi.core.domain.feedback.SoundPlayer
import dev.talhabayhan.lexi.core.testing.FakeAnalyticsTracker
import dev.talhabayhan.lexi.core.testing.FakeAppClock
import dev.talhabayhan.lexi.core.testing.FakeContentVersionRepository
import dev.talhabayhan.lexi.core.testing.FakeOnboardingRepository
import dev.talhabayhan.lexi.core.testing.FakeSettingsRepository
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
 * The splash pre-roll (iOS first-launch stutter, `docs/PERF.md`): the logo's poses are drawn for the warm-up frames
 * *before* the 1.2 s clock starts, so first-use GPU work never lands inside the animation; the animation itself keeps
 * its duration and landings.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class SplashWarmUpTest {
    private val landings = mutableListOf<Long>()
    private var finished: Boolean? = null

    @Before
    fun reset() {
        SplashState.finished.value = false
    }

    @Test
    fun `the animation starts only after the pre-roll and keeps its timing`() = runComposeUiTest {
        mainClock.autoAdvance = false
        setSplash(reducedMotion = false)

        var frames = 0
        while (warmUpShown()) {
            assertTrue(landings.isEmpty(), "a landing during the pre-roll")
            assertTrue(frames < SPLASH_WARM_UP_FRAMES + 2, "the pre-roll never ends")
            mainClock.advanceTimeByFrame()
            frames++
        }
        assertTrue(frames >= 1, "the pre-roll was never drawn")
        val clockStart = mainClock.currentTime

        mainClock.advanceTimeBy(SPLASH_MILLIS + SLACK_MILLIS)

        assertEquals(SplashDefaults.LandingFractions.size, landings.size)
        // The first landing (L at 26 %) is timed from the animation's start, not from the first frame.
        val firstLanding = landings.first() - clockStart
        val expected = (SplashDefaults.LandingFractions.first() * SPLASH_MILLIS).toLong()
        assertTrue(
            firstLanding in expected - FRAME_MILLIS..expected + 2 * FRAME_MILLIS,
            "first landing at $firstLanding",
        )
        assertEquals(false, finished)
        assertTrue(!warmUpShown(), "the pre-roll came back")
    }

    @Test
    fun `reduced motion has no pre-roll`() = runComposeUiTest {
        mainClock.autoAdvance = false
        setSplash(reducedMotion = true)

        assertTrue(!warmUpShown())
        mainClock.advanceTimeByFrame()
        assertTrue(!warmUpShown())
        assertNull(finished)
        mainClock.advanceTimeBy(SPLASH_MILLIS)

        assertEquals(false, finished)
        assertTrue(landings.isEmpty())
    }

    private fun ComposeUiTest.warmUpShown(): Boolean =
        onAllNodesWithTag(SPLASH_WARM_UP_TAG, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()

    private fun ComposeUiTest.setSplash(reducedMotion: Boolean) {
        setContent {
            CompositionLocalProvider(
                LocalHaptics provides Haptics { landings += mainClock.currentTime },
                LocalSoundPlayer provides SoundPlayer { },
            ) {
                LexiTheme(reducedMotion = reducedMotion) {
                    SplashRoute(
                        onFinished = { finished = it },
                        content = FakeContentVersionRepository(),
                        onboarding = FakeOnboardingRepository(),
                        analytics = FakeAnalyticsTracker(),
                        settings = FakeSettingsRepository(),
                        clock = FakeAppClock(),
                    )
                }
            }
        }
    }

    private companion object {
        const val SPLASH_MILLIS = 1_200L
        const val FRAME_MILLIS = 16L
        const val SLACK_MILLIS = 200L
    }
}
