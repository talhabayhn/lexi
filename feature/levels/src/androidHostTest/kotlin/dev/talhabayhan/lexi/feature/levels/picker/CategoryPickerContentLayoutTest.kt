package dev.talhabayhan.lexi.feature.levels.picker

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.text.TextLayoutResult
import dev.talhabayhan.lexi.core.designsystem.text.LocalTextLayoutProbe
import dev.talhabayhan.lexi.core.designsystem.text.TextLayoutProbe
import dev.talhabayhan.lexi.core.designsystem.text.textWordProblems
import dev.talhabayhan.lexi.core.designsystem.text.wordSafetyProblem
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.LexiViewport
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.Level
import kotlinx.collections.immutable.toImmutableList
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Every shipped category of every level (names from `content/categories.csv`, the list the picker shows: declared for
 * the level and with cards there) at 320 × 568, 360 × 640 and 390 × 844, font scale 1.0 / 1.3 / 2.0, Turkish and
 * English (ported from the old categories screen's `CategoriesLayoutTest`): every card is listed once, the grid scrolls
 * to its last category and to the last exam card, and no card name is cut, ellipsized or split inside a word. Names
 * may shrink (`WordSafeText`) but never below the size they have at font scale 1.0, so a larger font scale never makes
 * a name smaller than the default.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
abstract class CategoryPickerContentLayoutChecks(private val turkish: Boolean, private val fontScale: Float) {
    @Test
    @Config(qualifiers = "+w320dp-h568dp")
    fun w320h568() = checkAllLevels()

    @Test
    @Config(qualifiers = "+w360dp-h640dp")
    fun w360h640() = checkAllLevels()

    @Test
    @Config(qualifiers = "+w390dp-h844dp")
    fun w390h844() = checkAllLevels()

    private fun checkAllLevels() {
        val problems = ShippedPicker.levels.flatMap { (level, categories) -> check(level, categories) }
        assertTrue(problems.isEmpty(), problems.joinToString("\n"))
    }

    private fun check(level: Level, categories: List<PickerCategoryUi>): List<String> {
        val problems = mutableListOf<String>()
        runComposeUiTest {
            val names = NameProbe(categories.map { it.name() }.toSet())
            setContent {
                CompositionLocalProvider(LocalTextLayoutProbe provides names) {
                    LexiTheme(reducedMotion = true) {
                        LexiViewport { CategoryPickerScreen(ShippedPicker.state(level, categories), {}) }
                    }
                }
            }
            val label = "$level ${if (turkish) "tr" else "en"} fontScale $fontScale"
            val grid = onNode(hasScrollToNodeAction())
            val height = onRoot().fetchSemanticsNode().size.height.toFloat()
            // Every card is there once (also those below the fold), and each can be scrolled fully into view.
            categories.forEach { card ->
                val name = card.name()
                // The card's description starts with its exact name ("<name>, 3 / 40 ..."), so "Verbs" never
                // matches "Phrasal verbs".
                val matcher = SemanticsMatcher("card '$name'") { node ->
                    node.config.getOrNull(SemanticsProperties.ContentDescription).orEmpty()
                        .any { it.startsWith("$name, ") }
                }
                grid.performScrollToNode(matcher)
                waitForIdle()
                val nodes = onAllNodes(matcher).fetchSemanticsNodes()
                if (nodes.size != 1) {
                    problems += "$label '$name' listed ${nodes.size} times"
                    return@forEach
                }
                val bounds = nodes.single().boundsInRoot
                if (bounds.top < -1f || bounds.bottom > height + 1f) problems += "$label '$name' not reachable"
            }
            grid.performScrollToNode(hasContentDescription(LOCKED_LAST, substring = true))
            waitForIdle()
            problems += onRoot(useUnmergedTree = true).fetchSemanticsNode().textWordProblems().map { "$label $it" }
            problems += names.problems.map { "$label $it" }
            categories.map { it.name() }.forEach { name ->
                val sp = names.sizeSp[name]
                when {
                    sp == null -> problems += "$label '$name' never laid out"

                    // Rendered size (sp × font scale) below the 1.0 size: the larger font made the name smaller.
                    sp * fontScale < NAME_SP - SIZE_TOLERANCE ->
                        problems += "$label '$name' shrinks to ${"%.1f".format(sp * fontScale)}sp (< $NAME_SP)"
                }
            }
            ShippedPicker.report("$label: " + names.sizeSp.entries.joinToString { "${it.key}=${it.value}" })
        }
        return problems
    }

    private fun PickerCategoryUi.name() = if (turkish) nameTr else nameEn

    /** Records the card names' final font size (the auto-size result, in sp before the font scale) and problems. */
    private class NameProbe(private val names: Set<String>) : TextLayoutProbe {
        val sizeSp = LinkedHashMap<String, Float>()
        val problems = LinkedHashSet<String>()

        override fun onTextLayout(layout: TextLayoutResult) {
            layout.wordSafetyProblem()?.let(problems::add)
            val text = layout.layoutInput.text.text
            if (text in names) sizeSp[text] = layout.layoutInput.style.fontSize.value
        }
    }

    private companion object {
        /** `bodyStrong14`, the card name's design size. */
        const val NAME_SP = 14f
        const val SIZE_TOLERANCE = 0.01f

        /** The last locked exam card (`LockedExamIds`); its description is the same in both languages. */
        const val LOCKED_LAST = "IELTS"
    }
}

/** The picker's list per level, built from the shipped `content/` like `CategoryPickerViewModel` builds it. */
internal object ShippedPicker {
    private val contentDir: File = generateSequence(File("").absoluteFile) { it.parentFile }
        .map { File(it, "content") }.firstOrNull { File(it, "categories.csv").isFile }
        ?: error("content/categories.csv not found above ${File("").absolutePath}")

    private val cardCounts: Map<Pair<String, String>, Int> = Level.entries.flatMap { level ->
        File(contentDir, "cards/${level.name}").listFiles { f -> f.extension == "csv" }.orEmpty().flatMap { file ->
            val lines = file.readLines().filter { it.isNotBlank() }
            // The category is the last column, after the quoted free text, so the last comma finds it.
            check(lines.first().substringAfterLast(',') == "category") { "category is not the last column of $file" }
            lines.drop(1).map { level.name to it.substringAfterLast(',').trim() }
        }
    }.groupingBy { it }.eachCount()

    /** A1–C1 (NAT has only Karışık): Karışık first, then the level's categories with cards in `sort` order. */
    val levels: List<Pair<Level, List<PickerCategoryUi>>> = run {
        val rows = File(contentDir, "categories.csv").readLines().drop(1).filter { it.isNotBlank() }
            .map { it.split(',') }
            .onEach { check(it.size == 6) { "unexpected categories.csv row: $it" } }
            .sortedBy { it[4].toInt() }
        val mixed = rows.first { it[0] == CategoryId.MIXED.value }
        listOf(Level.A1, Level.A2, Level.B1, Level.B2, Level.C1).map { level ->
            val listed = rows.filter { row ->
                row[0] != CategoryId.MIXED.value && row[0] != CategoryId.GENERAL.value &&
                    level.name in row[3].split('|') && (cardCounts[level.name to row[0]] ?: 0) > 0
            }
            val cards = (listOf(mixed) + listed).mapIndexed { index, row ->
                val total = 40 + index * 37
                PickerCategoryUi(CategoryId(row[0]), row[1], row[2], learned = total / (index + 2), total = total)
            }
            level to cards
        }
    }

    fun state(level: Level, categories: List<PickerCategoryUi>): CategoryPickerUiState =
        PickerSampleState.copy(level = level, categories = categories.toImmutableList())

    /** The measured name sizes, in the test report's standard output (for a layout review with numbers). */
    fun report(line: String) = println(line)
}

private val EXPECTED_COUNTS = mapOf(Level.A1 to 14, Level.A2 to 14, Level.B1 to 15, Level.B2 to 12, Level.C1 to 9)

/** The shipped list sizes the picker must show (Karışık not counted); a content change that moves them fails here. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ShippedPickerCountsTest {
    @Test
    fun `every level lists all of its categories`() {
        val counts = ShippedPicker.levels.associate { (level, cards) -> level to cards.size - 1 }
        assertEquals(EXPECTED_COUNTS, counts)
    }
}

@Config(sdk = [36], qualifiers = "tr-xhdpi")
class CategoryPickerContentLayoutTr100Test : CategoryPickerContentLayoutChecks(turkish = true, fontScale = 1.0f)

@Config(sdk = [36], qualifiers = "tr-xhdpi", fontScale = 1.3f)
class CategoryPickerContentLayoutTr130Test : CategoryPickerContentLayoutChecks(turkish = true, fontScale = 1.3f)

@Config(sdk = [36], qualifiers = "tr-xhdpi", fontScale = 2.0f)
class CategoryPickerContentLayoutTr200Test : CategoryPickerContentLayoutChecks(turkish = true, fontScale = 2.0f)

@Config(sdk = [36], qualifiers = "en-xhdpi")
class CategoryPickerContentLayoutEn100Test : CategoryPickerContentLayoutChecks(turkish = false, fontScale = 1.0f)

@Config(sdk = [36], qualifiers = "en-xhdpi", fontScale = 1.3f)
class CategoryPickerContentLayoutEn130Test : CategoryPickerContentLayoutChecks(turkish = false, fontScale = 1.3f)

@Config(sdk = [36], qualifiers = "en-xhdpi", fontScale = 2.0f)
class CategoryPickerContentLayoutEn200Test : CategoryPickerContentLayoutChecks(turkish = false, fontScale = 2.0f)
