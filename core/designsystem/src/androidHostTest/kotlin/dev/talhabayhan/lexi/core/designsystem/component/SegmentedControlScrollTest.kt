package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w390dp-h844dp-xxhdpi")
class SegmentedControlScrollTest {

    @Test
    fun `selected last tab is scrolled into view on first composition at 200 percent font`() = runComposeUiTest {
        val levels = listOf("A1", "A2", "B1", "B2", "C1", "NAT")
        setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = 2f)) {
                LexiTheme(reducedMotion = false) {
                    Box(Modifier.width(358.dp)) {
                        SegmentedControl(
                            segments = levels.map { Segment(it) },
                            selectedIndex = levels.lastIndex,
                            onSelect = {},
                            scrollable = true,
                        )
                    }
                }
            }
        }
        waitForIdle()
        val nat = onNodeWithText("NAT")
        nat.assertIsDisplayed()
        assertTrue(nat.getUnclippedBoundsInRoot().right <= 358.dp, "selected tab must be inside the viewport")
    }
}
