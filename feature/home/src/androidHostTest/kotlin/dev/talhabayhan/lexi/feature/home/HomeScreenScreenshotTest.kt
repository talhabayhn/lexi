package dev.talhabayhan.lexi.feature.home

import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The video-flow Home at the size of `drafts/video-flow/home_v5.png` (390 × 844dp, xhdpi = 2×). Goldens are recorded on
 * CI (`screenshots-record`), never on macOS.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class HomeScreenScreenshotTest {

    @Test
    fun designSample() = capture("HomeScreen", HomeSampleState)

    @Test
    fun nothingPlayedYet() = capture("HomeScreenEmpty", HomeUiState())

    @Test
    fun dailyAlreadyPlayed() = capture(
        "HomeScreenDailyPlayed",
        HomeSampleState.copy(
            dailyState = DailyCardState.AlreadyPlayed(score = 310, hoursUntilNext = 6, minutesUntilNext = 42),
        ),
    )

    @Test
    fun dailyUnavailable() =
        capture("HomeScreenDailyUnavailable", HomeSampleState.copy(dailyState = DailyCardState.Unavailable))

    @Test
    fun giftNotice() = capture(
        "HomeScreenDailyGiftNotice",
        HomeSampleState.copy(dailyGiftNotice = DailyGiftNotice(DailyGiftNoticeType.Combined, 2)),
    )

    @Test
    @Config(qualifiers = "en-w390dp-h844dp-xhdpi")
    fun english() = capture("HomeScreenEn", HomeSampleState)

    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi", fontScale = 2.0f)
    fun smallPhoneLargeFont() = capture("HomeScreen_w320h568_fontScale200", HomeSampleState)

    @Test
    @Config(qualifiers = "tr-w360dp-h640dp-xhdpi", fontScale = 1.3f)
    fun w360FontScale130() = capture("HomeScreen_w360h640_fontScale130", HomeSampleState)

    private fun capture(name: String, state: HomeUiState) {
        captureRoboImage("src/androidHostTest/screenshots/$name.png") {
            LexiTheme(reducedMotion = false) { HomeScreen(state = state, onIntent = {}) }
        }
    }
}
