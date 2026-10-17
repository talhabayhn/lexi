package dev.talhabayhan.lexi.feature.home

import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.LexiViewport
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

    @Test
    fun dailyAlreadyPlayed() {
        captureRoboImage("src/androidHostTest/screenshots/HomeScreenDailyPlayed.png") {
            LexiTheme(reducedMotion = false) {
                HomeScreen(
                    state = HomeSampleState.copy(
                        dailyState = DailyCardState.AlreadyPlayed(
                            score = 310,
                            hoursUntilNext = 6,
                            minutesUntilNext = 42,
                        ),
                    ),
                    onIntent = {},
                )
            }
        }
    }

    @Test
    fun dailyUnavailable() {
        captureRoboImage("src/androidHostTest/screenshots/HomeScreenDailyUnavailable.png") {
            LexiTheme(reducedMotion = false) {
                HomeScreen(
                    state = HomeSampleState.copy(dailyState = DailyCardState.Unavailable),
                    onIntent = {},
                )
            }
        }
    }

    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi")
    fun w320h568() = capture("HomeLayout_w320h568")

    @Test
    @Config(qualifiers = "tr-w360dp-h640dp-xhdpi")
    fun w360h640() = capture("HomeLayout_w360h640")

    @Test
    @Config(qualifiers = "tr-w360dp-h780dp-xhdpi")
    fun w360h780() = capture("HomeLayout_w360h780")

    @Test
    fun w390h844() = capture("HomeLayout_w390h844")

    @Test
    @Config(qualifiers = "tr-w412dp-h915dp-xhdpi")
    fun w412h915() = capture("HomeLayout_w412h915")

    @Test
    @Config(fontScale = 1.15f)
    fun fontScale115() = capture("HomeLayout_fontScale115")

    @Test
    @Config(fontScale = 1.3f)
    fun fontScale130() = capture("HomeLayout_fontScale130")

    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi", fontScale = 1.3f)
    fun w320h568FontScale130() = capture("HomeLayout_w320h568_fontScale130")

    /** Expanded tier (`LexiViewport`): large viewports ("smallest display size") draw the design scaled. */
    @Test
    @Config(qualifiers = "tr-w480dp-h1000dp-mdpi")
    fun w480h1000() = capture("HomeLayout_w480h1000")

    @Test
    @Config(qualifiers = "tr-w600dp-h1100dp-mdpi")
    fun w600h1100() = capture("HomeLayout_w600h1100")

    @Test
    @Config(qualifiers = "tr-w600dp-h960dp-mdpi")
    fun w600h960() = capture("HomeLayout_w600h960")

    private fun capture(name: String) {
        captureRoboImage("src/androidHostTest/screenshots/$name.png") {
            LexiTheme(reducedMotion = false) { LexiViewport { HomeScreen(state = HomeSampleState, onIntent = {}) } }
        }
    }
}
