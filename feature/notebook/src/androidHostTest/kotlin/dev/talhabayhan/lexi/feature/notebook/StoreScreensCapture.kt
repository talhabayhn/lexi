package dev.talhabayhan.lexi.feature.notebook

import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * English store still, not a regression test: the notebook's sample cards in the English UI, Turkish clues as the app
 * shows them. Written to `build/store-screens/en/` for `tools/store-graphics`; runs only with `LEXI_STORE_FOOTAGE=1`.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "en-w390dp-h844dp-xhdpi")
class StoreScreensCapture {

    @Test
    fun notebook() {
        assumeTrue(System.getenv("LEXI_STORE_FOOTAGE") == "1")
        captureRoboImage("build/store-screens/en/Notebook.png") {
            LexiTheme(reducedMotion = false) { NotebookScreen(state = NotebookSampleState, onIntent = {}) }
        }
    }
}
