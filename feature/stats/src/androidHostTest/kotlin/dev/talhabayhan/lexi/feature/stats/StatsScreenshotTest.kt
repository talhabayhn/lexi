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

    /** Expanded tier (`LexiViewport`): the design scaled, the column centred and at most 430dp (× scale) wide. */
    @Test
    @Config(qualifiers = "tr-w600dp-h1100dp-mdpi")
    fun expandedW600h1100() {
        captureRoboImage("src/androidHostTest/screenshots/Stats_w600h1100.png") {
            LexiTheme(reducedMotion = false) { LexiViewport { StatsScreen(state = StatsSampleState, onIntent = {}) } }
        }
    }
}
