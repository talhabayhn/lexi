package dev.talhabayhan.lexi.feature.home

import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.LexiViewport
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.goal.DailyGoalProgress
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.Level
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The stateless home at the size and scale of `docs/design/screens/02c-home-v2.png` (390 × 844dp, xhdpi = 2×): the
 * board's frames (TR default, EN B2, daily completed, category sheet, 0 / 120 due reviews, 360 × 640 at 100 % and
 * 200 % font) and the other states.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class HomeScreenScreenshotTest {

    /** Board frame 01. */
    @Test
    fun designSample() = capture("HomeScreen", HomeSampleState)

    /** Board frame 02. */
    @Test
    @Config(qualifiers = "en-w390dp-h844dp-xhdpi")
    fun englishB2() = capture(
        "HomeScreenEnB2",
        HomeSampleState.copy(
            streakDays = 1,
            selectedLevel = Level.B2,
            selectedMode = GameMode.HARDCORE,
            selectedCategory = CategoryId("food-drink"),
        ),
    )

    /** Board frame 03. */
    @Test
    fun dailyCompleted() = capture(
        "HomeScreenDailyPlayed",
        HomeSampleState.copy(
            streakDays = 8,
            dailyState = DailyCardState.AlreadyPlayed(score = 1840, hoursUntilNext = 5, minutesUntilNext = 12),
            dailyGoalProgress = DailyGoalProgress(done = 1, goal = 1, reached = true),
        ),
    )

    /** Board frame 04 (reduced motion: the sheet is already in place). */
    @Test
    fun categorySheet() = capture("HomeScreenCategorySheet", HomeSampleState.copy(categorySheetOpen = true))

    /** Board frame 05: no due reviews (no badge), NAT (no category row), Practice, no streak (no pill). */
    @Test
    fun natNothingDue() = capture(
        "HomeScreenNatNothingDue",
        HomeSampleState.copy(
            streakDays = 0,
            dueReviews = 0,
            selectedLevel = Level.NAT,
            selectedMode = GameMode.PRACTICE,
            categories = CategoriesState.Loaded(persistentListOf()),
        ),
    )

    /** Board frame 06: 120 due reviews show "99+". */
    @Test
    fun manyDue() = capture(
        "HomeScreenDue120",
        HomeSampleState.copy(streakDays = 3, dueReviews = 120, selectedLevel = Level.A2),
    )

    /** The sheet with a category selected (ink card, yellow check badge; Karışık back to red). */
    @Test
    fun categorySheetCategorySelected() = capture(
        "HomeScreenCategorySheetSelected",
        HomeSampleState.copy(categorySheetOpen = true, selectedCategory = CategoryId("food-drink")),
    )

    /** From font scale 1.3 the cards stack in one column. */
    @Test
    @Config(fontScale = 2.0f)
    fun categorySheetOneColumn() =
        capture("HomeScreenCategorySheet_fontScale200", HomeSampleState.copy(categorySheetOpen = true))

    @Test
    @Config(qualifiers = "en-w390dp-h844dp-xhdpi")
    fun categorySheetEnglish() = capture(
        "HomeScreenCategorySheetEn",
        HomeSampleState.copy(categorySheetOpen = true, selectedCategory = CategoryId("home")),
    )

    @Test
    fun dailyUnavailable() =
        capture("HomeScreenDailyUnavailable", HomeSampleState.copy(dailyState = DailyCardState.Unavailable))

    @Test
    fun nothingPlayedYet() = capture("HomeScreenEmpty", HomeUiState())

    @Test
    fun categorySheetError() = capture(
        "HomeScreenCategoryError",
        HomeSampleState.copy(categories = CategoriesState.Error, categorySheetOpen = true),
    )

    /** The gift toast floats over the unchanged home (reduced motion: already in place, no mid-slide frame). */
    @Test
    fun dailyGiftNotice() = capture(
        "HomeScreenDailyGiftNotice",
        HomeSampleState.copy(dailyGiftNotice = DailyGiftNotice(DailyGiftNoticeType.Daily, 1)),
    )

    /** Board frame 07. */
    @Test
    @Config(qualifiers = "tr-w360dp-h640dp-xhdpi")
    fun w360h640() = capture("HomeLayout_w360h640")

    /** Board frame 08. */
    @Test
    @Config(qualifiers = "tr-w360dp-h640dp-xhdpi", fontScale = 2.0f)
    fun w360h640FontScale200() =
        capture("HomeLayout_w360h640_fontScale200", HomeSampleState.copy(selectedLevel = Level.B2))

    @Test
    @Config(qualifiers = "tr-w360dp-h640dp-xhdpi", fontScale = 1.3f)
    fun w360h640FontScale130() = capture("HomeLayout_w360h640_fontScale130")

    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi")
    fun w320h568() = capture("HomeLayout_w320h568")

    @Test
    @Config(qualifiers = "tr-w412dp-h915dp-xhdpi")
    fun w412h915() = capture("HomeLayout_w412h915")

    @Test
    @Config(fontScale = 1.3f)
    fun fontScale130() = capture("HomeLayout_fontScale130")

    @Test
    @Config(fontScale = 2.0f)
    fun fontScale200() = capture("HomeLayout_fontScale200")

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

    private fun capture(name: String, state: HomeUiState = HomeSampleState) {
        captureRoboImage("src/androidHostTest/screenshots/$name.png") {
            LexiTheme(reducedMotion = true) { LexiViewport { HomeScreen(state = state, onIntent = {}) } }
        }
    }
}
