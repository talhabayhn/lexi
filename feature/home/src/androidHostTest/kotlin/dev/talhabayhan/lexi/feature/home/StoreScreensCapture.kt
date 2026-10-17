package dev.talhabayhan.lexi.feature.home

import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** English home for the store set (`tools/store-graphics`); runs only with `LEXI_STORE_FOOTAGE=1`. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "en-w390dp-h844dp-xhdpi")
class StoreScreensCapture {

    @Test
    fun home() {
        assumeTrue(System.getenv("LEXI_STORE_FOOTAGE") == "1")
        captureRoboImage("build/store-screens/en/Home.png") {
            LexiTheme(reducedMotion = false) { HomeScreen(state = HomeSampleState, onIntent = {}) }
        }
    }
}
