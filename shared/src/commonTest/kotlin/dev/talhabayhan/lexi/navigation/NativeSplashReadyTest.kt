package dev.talhabayhan.lexi.navigation

import androidx.compose.runtime.BroadcastFrameClock
import dev.talhabayhan.lexi.splash.LaunchSplash
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The first screen tells the iOS native splash it is ready (ADR 0028): only once the splash's entry has left and
 * [READY_FRAMES] frames were drawn, exactly once; a timed-out overlay is reported.
 */
class NativeSplashReadyTest {
    private class FakeSplash(private val timedOut: Boolean) : LaunchSplash {
        var readyCalls = 0
        override val isNative = true

        override fun elapsedMillis() = 0L

        override fun contentReady(): Boolean {
            readyCalls++
            return timedOut
        }
    }

    @Test
    fun readyOnlyAfterTheSplashLeftAndTwoFramesWereDrawn() = runTest {
        val clock = BroadcastFrameClock()
        val splashShown = MutableStateFlow(true)
        val splash = FakeSplash(timedOut = false)
        var timeouts = 0
        val job = launch(clock) { signalNativeSplashReady(splashShown, splash) { timeouts++ } }
        runCurrent()
        repeat(3) {
            clock.sendFrame(0L)
            runCurrent()
        }
        assertEquals(0, splash.readyCalls, "ready while the splash entry is still shown")

        splashShown.value = false
        runCurrent()
        repeat(READY_FRAMES - 1) {
            clock.sendFrame(0L)
            runCurrent()
        }
        assertEquals(0, splash.readyCalls, "ready before the first screen was drawn")
        clock.sendFrame(0L)
        runCurrent()
        assertEquals(1, splash.readyCalls)
        assertEquals(0, timeouts)
        job.join()
    }

    @Test
    fun aTimedOutOverlayIsReported() = runTest {
        val clock = BroadcastFrameClock()
        val splash = FakeSplash(timedOut = true)
        var timeouts = 0
        val job = launch(clock) { signalNativeSplashReady(MutableStateFlow(false), splash) { timeouts++ } }
        runCurrent()
        repeat(READY_FRAMES) {
            clock.sendFrame(0L)
            runCurrent()
        }
        job.join()
        assertEquals(1, splash.readyCalls)
        assertEquals(1, timeouts)
    }
}
