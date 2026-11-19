package dev.talhabayhan.lexi.splash

import android.graphics.Bitmap
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runComposeUiTest
import dev.talhabayhan.lexi.core.designsystem.component.SplashDefaults
import dev.talhabayhan.lexi.core.designsystem.feedback.LocalHaptics
import dev.talhabayhan.lexi.core.designsystem.feedback.LocalSoundPlayer
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.AppInfo
import dev.talhabayhan.lexi.core.domain.feedback.Haptics
import dev.talhabayhan.lexi.core.domain.feedback.SoundPlayer
import dev.talhabayhan.lexi.core.domain.repository.SettingsRepository
import dev.talhabayhan.lexi.core.domain.settings.Settings
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
    fun `the warm-up is drawn above the splash under an inset cover and its first frame stays plain`() =
        runComposeUiTest {
            mainClock.autoAdvance = false
            var cream = 0
            setSplash(reducedMotion = false) { cream = it }
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
            // Hidden: the warm-up frame (splash poses, game board, card flip, dialog, cards) is pixel for pixel the
            // splash's own first frame (the first one after it, at 0).
            val withWarmUp = onRoot().captureToImage().asAndroidBitmap().pixels()
            var frames = 0
            while (warmUpShown() && frames++ < SPLASH_WARM_UP_FRAMES + 2) mainClock.advanceTimeByFrame()
            assertTrue(!warmUpShown(), "the warm-up never ends")
            assertTrue(landings.isEmpty())
            val firstFrame = onRoot().captureToImage().asAndroidBitmap().pixels()
            val other = withWarmUp.indices.firstOrNull { withWarmUp[it] != firstFrame[it] }
            assertNull(other, "the warm-up is visible at pixel $other")
            assertTrue(cream in firstFrame, "nothing drawn")
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
        onCream: (Int) -> Unit = {},
    ) {
        setContent {
            onCream(LexiTheme.colors.cream.toArgb())
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
                        settings = settings,
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
        const val DECISION_FRAMES = 3

        /**
         * Frames 1.1.1 added on every launch before a landing could be seen: 2 pre-roll frames, the animation's first
         * frame (its time 0), and the frame a landing is sampled on.
         */
        const val OLD_PRE_ROLL_FRAMES = 4
    }
}
