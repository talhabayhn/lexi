package dev.talhabayhan.lexi.feature.home

import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.Level
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * High-resolution sources for the store images (xxxhdpi = 4x, 1560 x 3376 px), not regression tests: the same screens
 * as the Roborazzi goldens, rendered at 4x so `tools/store-graphics` downsamples instead of upscaling. Written to
 * `build/store-hires/<lang>/`; runs only with `LEXI_STORE_FOOTAGE=1`. The goldens in `src/androidHostTest/screenshots`
 * stay at 2x and are not touched.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xxxhdpi")
class StoreHiResCapture {
    @Before
    fun onlyForStore() = assumeTrue(System.getenv("LEXI_STORE_FOOTAGE") == "1")

    @Test
    fun homeTr() = capture("tr/HomeScreen")

    @Test
    @Config(qualifiers = "en-w390dp-h844dp-xxxhdpi")
    fun homeEn() = capture("en/Home")

    /** "A1'den ana dile altı seviye": the level chips with A2 selected (the `HomeScreenDue120` golden). */
    @Test
    fun levelsTr() = capture(
        "tr/HomeScreenDue120",
        HomeSampleState.copy(streakDays = 3, dueReviews = 120, selectedLevel = Level.A2),
    )

    /** "Six levels, A1 to native": B2 and Hardcore selected (the `HomeScreenEnB2` golden). */
    @Test
    @Config(qualifiers = "en-w390dp-h844dp-xxxhdpi")
    fun levelsEn() = capture(
        "en/HomeScreenEnB2",
        HomeSampleState.copy(
            streakDays = 1,
            selectedLevel = Level.B2,
            selectedMode = GameMode.HARDCORE,
            selectedCategory = CategoryId("food-drink"),
        ),
    )

    private fun capture(name: String, state: HomeUiState = HomeSampleState) {
        captureRoboImage("build/store-hires/$name.png") {
            LexiTheme(reducedMotion = false) { HomeScreen(state = state, onIntent = {}) }
        }
    }
}
