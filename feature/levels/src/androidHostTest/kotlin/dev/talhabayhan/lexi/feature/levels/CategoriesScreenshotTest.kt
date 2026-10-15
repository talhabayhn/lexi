package dev.talhabayhan.lexi.feature.levels

import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
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
}
