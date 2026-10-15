package dev.talhabayhan.lexi.feature.home

import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** The stateless home at the size and scale of `docs/design/screens/02-home.png` (390 × 844dp, xhdpi = 2×), in Turkish. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class HomeScreenScreenshotTest {

    @Test
    fun designSample() {
        captureRoboImage("src/androidHostTest/screenshots/HomeScreen.png") {
            LexiTheme(reducedMotion = false) { HomeScreen(state = HomeSampleState, onIntent = {}) }
        }
    }

    @Test
    fun nothingPlayedYet() {
        captureRoboImage("src/androidHostTest/screenshots/HomeScreenEmpty.png") {
            LexiTheme(reducedMotion = false) { HomeScreen(state = HomeUiState(), onIntent = {}) }
        }
    }
}
