package dev.talhabayhan.lexi.feature.levels.picker

import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * High-resolution sources for the store images (xxxhdpi = 4x), not regression tests: "Çarkını seç" as in the goldens,
 * rendered at 4x so `tools/store-graphics` downsamples. Written to `build/store-hires/<lang>/`; runs only with
 * `LEXI_STORE_FOOTAGE=1`.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xxxhdpi")
class StoreHiResCapture {
    @Before
    fun onlyForStore() = assumeTrue(System.getenv("LEXI_STORE_FOOTAGE") == "1")

    @Test
    fun pickerTr() = capture("tr/CategoryPicker")

    @Test
    @Config(qualifiers = "en-w390dp-h844dp-xxxhdpi")
    fun pickerEn() = capture("en/CategoryPickerEn")

    private fun capture(name: String) {
        captureRoboImage("build/store-hires/$name.png") {
            LexiTheme(reducedMotion = false) { CategoryPickerScreen(state = PickerSampleState, onIntent = {}) }
        }
    }
}
