package dev.talhabayhan.lexi.feature.levels

import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.LexiViewport
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.Level
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The stateless categories screen at the size and scale of `docs/design/screens/07-categories.png` (390 × 844dp,
 * xhdpi = 2×), in Turkish like the design.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class CategoriesScreenshotTest {

    @Test
    fun designSample() {
        captureRoboImage("src/androidHostTest/screenshots/Categories.png") {
            LexiTheme(reducedMotion = false) { CategoriesScreen(state = CategoriesSampleState, onIntent = {}) }
        }
    }

    /** Long English names wrap to two lines, an odd last card keeps half width, NAT has its own categories. */
    @Test
    @Config(qualifiers = "en-w390dp-h844dp-xhdpi")
    fun longNamesEnglish() {
        captureRoboImage("src/androidHostTest/screenshots/CategoriesLongNamesEn.png") {
            LexiTheme(reducedMotion = false) {
                CategoriesScreen(
                    state = CategoriesUiState(
                        level = Level.B2,
                        levelTotal = 1_400,
                        isLoaded = true,
                        categories = persistentListOf(
                            CategoryUi(CategoryId("false-friends"), "Yalancı eşdeğerler", "False friends", 12, 140),
                            CategoryUi(CategoryId("food-drink"), "Yiyecek ve içecek", "Food and drink", 61, 95),
                            CategoryUi(
                                CategoryId("business"),
                                "İş İngilizcesi",
                                "Business English for meetings and email",
                                140,
                                140,
                            ),
                            CategoryUi(CategoryId("film-tv"), "Film ve dizi", "Film and TV", 0, 120),
                            CategoryUi(CategoryId("idioms"), "Deyimler", "Idioms", 7, 150),
                        ),
                    ),
                    onIntent = {},
                )
            }
        }
    }

    @Test
    fun emptyLevel() {
        captureRoboImage("src/androidHostTest/screenshots/CategoriesEmpty.png") {
            LexiTheme(reducedMotion = false) {
                CategoriesScreen(
                    state = CategoriesUiState(level = Level.NAT, isLoaded = true),
                    onIntent = {},
                )
            }
        }
    }

    /** Design debt: the level's content could not be read; an empty state with a retry. */
    @Test
    fun readFailed() {
        captureRoboImage("src/androidHostTest/screenshots/CategoriesError.png") {
            LexiTheme(reducedMotion = false) {
                CategoriesScreen(state = CategoriesUiState(isLoaded = true, hasError = true), onIntent = {})
            }
        }
    }

    /** Expanded tier (`LexiViewport`): the design scaled, the column centred and at most 430dp (× scale) wide. */
    @Test
    @Config(qualifiers = "tr-w600dp-h1100dp-mdpi")
    fun expandedW600h1100() {
        captureRoboImage("src/androidHostTest/screenshots/Categories_w600h1100.png") {
            LexiTheme(reducedMotion = false) {
                LexiViewport { CategoriesScreen(state = CategoriesSampleState, onIntent = {}) }
            }
        }
    }

    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi")
    fun w320h568() = capture("Categories_w320h568")

    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi", fontScale = 1.3f)
    fun w320h568FontScale130() = capture("Categories_w320h568_fontScale130")

    @Test
    @Config(qualifiers = "tr-w360dp-h640dp-xhdpi")
    fun w360h640() = capture("Categories_w360h640")

    @Test
    @Config(qualifiers = "tr-w360dp-h640dp-xhdpi", fontScale = 1.3f)
    fun w360h640FontScale130() = capture("Categories_w360h640_fontScale130")

    @Test
    @Config(qualifiers = "tr-w600dp-h960dp-mdpi")
    fun w600h960() = capture("Categories_w600h960")

    @Test
    @Config(qualifiers = "tr-w600dp-h960dp-mdpi", fontScale = 1.3f)
    fun w600h960FontScale130() = capture("Categories_w600h960_fontScale130")

    /** Largest system font size (accessibility): nothing may clip or overlap. */
    @Test
    @Config(qualifiers = "tr-w390dp-h844dp-xhdpi", fontScale = 2.0f)
    fun fontScale200() = capture("Categories_fontScale200")

    private fun capture(name: String) {
        captureRoboImage("src/androidHostTest/screenshots/$name.png") {
            LexiTheme(reducedMotion = false) {
                LexiViewport { CategoriesScreen(state = CategoriesSampleState, onIntent = {}) }
            }
        }
    }
}
