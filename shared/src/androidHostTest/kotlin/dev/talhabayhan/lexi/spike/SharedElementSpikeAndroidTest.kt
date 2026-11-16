package dev.talhabayhan.lexi.spike

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

/** ADR 0027 spike on Android (Robolectric, native graphics): shared element across NavHost destinations. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w390dp-h844dp-mdpi")
@OptIn(ExperimentalTestApi::class)
class SharedElementSpikeAndroidTest {
    @Test
    fun `shared element animates between two nav destinations`() = runComposeUiTest {
        val result = runSharedElementSpike()
        println("spike android: source=${result.source} middle=${result.middle} end=${result.end}")
        // mdpi: 1 dp = 1 px.
        val target = Rect(
            SPIKE_TARGET_X.toFloat(),
            SPIKE_TARGET_Y.toFloat(),
            (SPIKE_TARGET_X + SPIKE_TARGET_SIZE).toFloat(),
            (SPIKE_TARGET_Y + SPIKE_TARGET_SIZE).toFloat(),
        )
        result.assertAnimatedBetweenDestinations(target)
    }
}
