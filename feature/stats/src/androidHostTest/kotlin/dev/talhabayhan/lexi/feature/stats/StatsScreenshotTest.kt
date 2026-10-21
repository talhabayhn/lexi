package dev.talhabayhan.lexi.feature.stats

import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.LexiViewport
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The stateless statistics screen at the size and scale of `docs/design/screens/09-stats.png` (390 x 844dp,
 * xhdpi = 2x), in Turkish like the design.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class StatsScreenshotTest {

    @Test
    fun designSample() {
        captureRoboImage("src/androidHostTest/screenshots/Stats.png") {
            LexiTheme(reducedMotion = false) { StatsScreen(state = StatsSampleState, onIntent = {}) }
        }
    }

    @Test
    @Config(qualifiers = "en-w390dp-h844dp-xhdpi")
    fun designSampleEnglish() {
        captureRoboImage("src/androidHostTest/screenshots/StatsEn.png") {
            LexiTheme(reducedMotion = false) { StatsScreen(state = StatsSampleState, onIntent = {}) }
        }
    }

    /** No game played yet. */
    @Test
    fun noGamesPlayed() {
        captureRoboImage("src/androidHostTest/screenshots/StatsEmpty.png") {
            LexiTheme(reducedMotion = false) {
                StatsScreen(state = StatsUiState(isLoaded = true, isEmpty = true), onIntent = {})
            }
        }
    }

    @Test
    fun readFailed() {
        captureRoboImage("src/androidHostTest/screenshots/StatsError.png") {
            LexiTheme(reducedMotion = false) {
                StatsScreen(state = StatsUiState(isLoaded = true, hasError = true), onIntent = {})
            }
        }
    }

    /** A first game: no accuracy letters yet, one level, nothing learned today. */
    @Test
    fun firstGame() {
        captureRoboImage("src/androidHostTest/screenshots/StatsFirstGame.png") {
            LexiTheme(reducedMotion = false) {
                StatsScreen(
                    state = StatsSampleState.copy(
                        learnedWords = 3,
                        accuracyPercent = null,
                        longestStreak = 1,
                        completedWheels = 1,
                        week = StatsSampleState.week.map { it.copy(words = if (it.weekday == 6) 3 else 0) }
                            .let { persistentListOf(*it.toTypedArray()) },
                        levels = StatsSampleState.levels.take(1).let { persistentListOf(*it.toTypedArray()) },
                        hardLetters = persistentListOf(),
                    ),
                    onIntent = {},
                )
            }
        }
    }

    @Test
    @Config(qualifiers = "tr-w600dp-h1100dp-mdpi")
    fun expandedW600h1100() {
        captureRoboImage("src/androidHostTest/screenshots/Stats_w600h1100.png") {
            LexiTheme(reducedMotion = false) { LexiViewport { StatsScreen(state = StatsSampleState, onIntent = {}) } }
        }
    }

    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi")
    fun w320h568() = capture("Stats_w320h568")

    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi", fontScale = 1.3f)
    fun w320h568FontScale130() = capture("Stats_w320h568_fontScale130")

    @Test
    @Config(qualifiers = "tr-w360dp-h640dp-xhdpi")
    fun w360h640() = capture("Stats_w360h640")

    @Test
    @Config(qualifiers = "tr-w360dp-h640dp-xhdpi", fontScale = 1.3f)
    fun w360h640FontScale130() = capture("Stats_w360h640_fontScale130")

    @Test
    @Config(qualifiers = "tr-w600dp-h960dp-mdpi")
    fun w600h960() = capture("Stats_w600h960")

    @Test
    @Config(qualifiers = "tr-w600dp-h960dp-mdpi", fontScale = 1.3f)
    fun w600h960FontScale130() = capture("Stats_w600h960_fontScale130")

    /** Largest system font size (accessibility): nothing may clip or overlap. */
    @Test
    @Config(qualifiers = "tr-w390dp-h844dp-xhdpi", fontScale = 2.0f)
    fun fontScale200() = capture("Stats_fontScale200")

    private fun capture(name: String) {
        captureRoboImage("src/androidHostTest/screenshots/$name.png") {
            LexiTheme(reducedMotion = false) {
                LexiViewport {
                    StatsScreen(state = StatsSampleState, onIntent = {})
                }
            }
        }
    }
}
