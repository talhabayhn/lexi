package dev.talhabayhan.lexi.feature.levels.picker

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.runComposeUiTest
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.LexiViewport
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.math.roundToInt
import kotlin.test.assertTrue

/**
 * Cards of one row of "Çarkını seç" have the same height, whichever of their names wraps to a second line (S8): every
 * level and every exam row at 320 / 360 / 390 / 430 dp wide, font scale 1.0 / 1.3 / 2.0, Turkish and English.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
abstract class CategoryPickerRowHeightChecks {
    @Test
    @Config(qualifiers = "+w320dp-h568dp")
    fun w320() = check()

    @Test
    @Config(qualifiers = "+w360dp-h640dp")
    fun w360() = check()

    @Test
    @Config(qualifiers = "+w390dp-h844dp")
    fun w390() = check()

    @Test
    @Config(qualifiers = "+w430dp-h932dp")
    fun w430() = check()

    private fun check() {
        val problems = mutableListOf<String>()
        var rowsSeen = 0
        ShippedPicker.levels.forEach { (level, categories) ->
            runComposeUiTest {
                setContent {
                    LexiTheme(reducedMotion = true) {
                        LexiViewport { CategoryPickerScreen(ShippedPicker.state(level, categories), {}) }
                    }
                }
                val grid = onNode(hasScrollToNodeAction())
                val targets = categories.map { card ->
                    SemanticsMatcher("card ${card.id}") { node ->
                        node.config.getOrNull(SemanticsProperties.ContentDescription).orEmpty()
                            .any { it.startsWith("${card.nameTr}, ") || it.startsWith("${card.nameEn}, ") }
                    }
                } + hasContentDescription(LOCKED_LAST, substring = true)
                targets.forEach { target ->
                    grid.performScrollToNode(target)
                    waitForIdle()
                    val cards = onAllNodes(hasContentDescription("", substring = true)).fetchSemanticsNodes()
                        .filter { it.size.height >= MIN_CARD_PX }
                    cards.groupBy { it.positionInRoot.y.roundToInt() }.forEach { (top, row) ->
                        rowsSeen++
                        if (row.map { it.size.height }.distinct().size > 1) {
                            problems += "$level at y=$top: heights ${row.map { it.size.height }} " +
                                row.joinToString {
                                    it.config.getOrNull(SemanticsProperties.ContentDescription)?.first().orEmpty()
                                }
                        }
                    }
                }
            }
        }
        assertTrue(rowsSeen > 0, "no card row was measured")
        assertTrue(problems.isEmpty(), problems.distinct().joinToString("\n"))
    }

    private companion object {
        /** Smaller nodes (the pill, headers) are not cards: a card is ≥ 95dp art + info, at least 2x here. */
        const val MIN_CARD_PX = 190
        const val LOCKED_LAST = "IELTS"
    }
}

@Config(sdk = [36], qualifiers = "tr-xhdpi")
class CategoryPickerRowHeightTr100Test : CategoryPickerRowHeightChecks()

@Config(sdk = [36], qualifiers = "tr-xhdpi", fontScale = 1.3f)
class CategoryPickerRowHeightTr130Test : CategoryPickerRowHeightChecks()

@Config(sdk = [36], qualifiers = "tr-xhdpi", fontScale = 2.0f)
class CategoryPickerRowHeightTr200Test : CategoryPickerRowHeightChecks()

@Config(sdk = [36], qualifiers = "en-xhdpi")
class CategoryPickerRowHeightEn100Test : CategoryPickerRowHeightChecks()

@Config(sdk = [36], qualifiers = "en-xhdpi", fontScale = 1.3f)
class CategoryPickerRowHeightEn130Test : CategoryPickerRowHeightChecks()

@Config(sdk = [36], qualifiers = "en-xhdpi", fontScale = 2.0f)
class CategoryPickerRowHeightEn200Test : CategoryPickerRowHeightChecks()
