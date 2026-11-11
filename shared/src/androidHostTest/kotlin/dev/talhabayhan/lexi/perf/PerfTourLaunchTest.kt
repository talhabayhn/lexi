package dev.talhabayhan.lexi.perf

import android.content.Intent
import android.net.Uri
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class PerfTourLaunchTest {
    private val shell = Uri.parse("android-app://com.android.shell")

    private fun tour(loops: Int? = null) = Intent(Intent.ACTION_MAIN).putExtra(PerfTourLaunch.EXTRA_PERF_TOUR, true)
        .apply { if (loops != null) putExtra(PerfTourLaunch.EXTRA_PERF_TOUR_LOOPS, loops) }

    @Test
    fun releaseBuildIgnoresTheFlagFromAnotherApp() {
        assertNull(PerfTourLaunch.perfTourConfig(tour(), debuggable = false, fromShell = false))
    }

    @Test
    fun releaseBuildRunsTheTourFromTheShell() {
        assertEquals(PerfTourConfig(5), PerfTourLaunch.perfTourConfig(tour(), debuggable = false, fromShell = true))
    }

    @Test
    fun debugBuildRunsTheTourFromAnyCaller() {
        assertEquals(PerfTourConfig(3), PerfTourLaunch.perfTourConfig(tour(3), debuggable = true, fromShell = false))
    }

    @Test
    fun noFlagNoTour() {
        assertNull(PerfTourLaunch.perfTourConfig(Intent(Intent.ACTION_MAIN), debuggable = true, fromShell = true))
    }

    @Test
    fun loopsAreClampedToOneToTwenty() {
        assertEquals(PerfTourConfig(1), PerfTourLaunch.perfTourConfig(tour(0), debuggable = true, fromShell = false))
        assertEquals(PerfTourConfig(1), PerfTourLaunch.perfTourConfig(tour(-7), debuggable = true, fromShell = false))
        assertEquals(
            PerfTourConfig(PerfTourLaunch.MAX_LOOPS),
            PerfTourLaunch.perfTourConfig(tour(Int.MAX_VALUE), debuggable = true, fromShell = false),
        )
    }

    @Test
    fun shellReferrerCountsOnlyWithoutCallerSuppliedReferrerExtras() {
        assertTrue(PerfTourLaunch.isShellLaunch(tour(), shell))
        assertFalse(PerfTourLaunch.isShellLaunch(tour(), Uri.parse("android-app://com.example.other")))
        assertFalse(PerfTourLaunch.isShellLaunch(tour(), null))
        assertFalse(PerfTourLaunch.isShellLaunch(tour().putExtra(Intent.EXTRA_REFERRER, shell), shell))
        assertFalse(
            PerfTourLaunch.isShellLaunch(tour().putExtra(Intent.EXTRA_REFERRER_NAME, shell.toString()), shell),
        )
    }
}
