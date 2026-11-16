package dev.talhabayhan.lexi.spike

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test

/** ADR 0027 spike on the iOS simulator (Skiko): shared element across NavHost destinations. */
@OptIn(ExperimentalTestApi::class)
class SharedElementSpikeIosTest {
    @Test
    fun sharedElementAnimatesBetweenTwoNavDestinations() = runComposeUiTest {
        val result = runSharedElementSpike()
        println("spike ios: source=${result.source} middle=${result.middle} end=${result.end}")
        val scale = density.density
        val target = Rect(
            SPIKE_TARGET_X * scale,
            SPIKE_TARGET_Y * scale,
            (SPIKE_TARGET_X + SPIKE_TARGET_SIZE) * scale,
            (SPIKE_TARGET_Y + SPIKE_TARGET_SIZE) * scale,
        )
        result.assertAnimatedBetweenDestinations(target)
    }
}
