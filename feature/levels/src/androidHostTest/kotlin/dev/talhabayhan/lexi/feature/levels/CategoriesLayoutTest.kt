package dev.talhabayhan.lexi.feature.levels

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
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
 * The categories screen with the full B1 list (15 categories, the longest names, one finished and one untouched
 * category) at the three small phone sizes, font scales 1.0 / 1.3 / 2.0 and both languages: the page scrolls to its
 * last card, 1.3+ uses one column, no text is ellipsized, no word is split in the middle, the progress texts show the learned
 * counts, nothing leaves the screen horizontally and the mixed wheel card is unique.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class CategoriesLayoutTest {

    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi")
    fun turkishW320h568() = checkAllScales(turkish = true)

    @Test
    @Config(qualifiers = "tr-w360dp-h640dp-xhdpi")
    fun turkishW360h640() = checkAllScales(turkish = true)

    @Test
    fun turkishW390h844() = checkAllScales(turkish = true)

    @Test
    @Config(qualifiers = "en-w320dp-h568dp-xhdpi")
    fun englishW320h568() = checkAllScales(turkish = false)

    @Test
    @Config(qualifiers = "en-w360dp-h640dp-xhdpi")
    fun englishW360h640() = checkAllScales(turkish = false)

    @Test
    @Config(qualifiers = "en-w390dp-h844dp-xhdpi")
    fun englishW390h844() = checkAllScales(turkish = false)

    private fun checkAllScales(turkish: Boolean) {
        val problems = FONT_SCALES.flatMap { scale ->
            var found: List<String> = emptyList()
            runComposeUiTest { found = check(scale, turkish) }
            found
        }
        assertTrue(problems.isEmpty(), problems.joinToString("\n"))
    }

    private fun ComposeUiTest.check(fontScale: Float, turkish: Boolean): List<String> {
        val label = "fontScale $fontScale"
        setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                LexiTheme(reducedMotion = true) { CategoriesScreen(state = B1_STATE, onIntent = {}) }
            }
        }
        val screen = onRoot().fetchSemanticsNode().boundsInRoot

        // The mixed wheel card exists once.
        val mixed = if (turkish) "Karışık çark" else "Mixed wheel"
        assertEquals(1, onAllNodes(hasText(mixed), useUnmergedTree = true).fetchSemanticsNodes().size, "$label mixed")

        // Every progress text is there, with the learned count of its category.
        B1_CATEGORIES.forEach { c ->
            val caption = if (c.isComplete) {
                "${c.learned} / ${c.total} · ${if (turkish) "tamam" else "done"}"
            } else {
                "${c.learned} / ${c.total}"
            }
            assertTrue(
                onAllNodes(hasText(caption), useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty(),
                "$label caption '$caption' of ${c.id.value}",
            )
        }

        // Two columns below the single-column scale, one full-width column from it.
        val firstTwo = B1_CATEGORIES.take(2).map { c ->
            val name = if (turkish) c.nameTr else c.nameEn
            onAllNodes(hasText(name), useUnmergedTree = true)[0].fetchSemanticsNode().boundsInRoot
        }
        if (fontScale >= SINGLE_COLUMN_FONT_SCALE) {
            assertEquals(firstTwo[0].left, firstTwo[1].left, "$label expected one column: $firstTwo")
        } else {
            assertTrue(firstTwo[1].left > firstTwo[0].left, "$label expected two columns: $firstTwo")
        }

        // The page scrolls and its last card can be brought fully into view.
        val last = B1_CATEGORIES.last().let { if (turkish) it.nameTr else it.nameEn }
        assertTrue(onAllNodes(hasScrollAction()).fetchSemanticsNodes().isNotEmpty(), "$label no scroll container")
        onAllNodes(hasText(last), useUnmergedTree = true)[0].performScrollTo()
        waitForIdle()
        val lastBounds = onAllNodes(hasText(last), useUnmergedTree = true).fetchSemanticsNodes().first().boundsInRoot
        assertTrue(
            lastBounds.top >= -1f && lastBounds.bottom <= screen.bottom + 1f,
            "$label last card '$last' not reachable: $lastBounds / $screen",
        )

        // No text is clipped, ellipsized, split inside a word or outside the screen width.
        val texts = onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Text), useUnmergedTree = true)
            .fetchSemanticsNodes()
        assertTrue(texts.size >= B1_CATEGORIES.size * 2, "$label texts: ${texts.size}")
        val problems = mutableListOf<String>()
        texts.forEach { node ->
            val text = node.config.getOrNull(SemanticsProperties.Text)?.joinToString("") { it.text }.orEmpty()
            val r = node.boundsInRoot
            if (r.left < -1f || r.right > screen.right + 1f) problems += "$label '$text' leaves the width: $r"
            val layout = node.textLayout() ?: return@forEach
            val ellipsized = (0 until layout.lineCount).any { layout.isLineEllipsized(it) }
            if (ellipsized) {
                problems += "$label '$text' is ellipsized"
            }
            layout.splitWord()?.let { problems += "$label '$text' is split inside a word near '$it'" }
        }
        return problems
    }

    private fun SemanticsNode.textLayout(): TextLayoutResult? {
        val out = mutableListOf<TextLayoutResult>()
        config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(out)
        return out.firstOrNull()
    }

    /** The first line break that falls inside a word (a letter on both sides and no hyphen), or null. */
    private fun TextLayoutResult.splitWord(): String? {
        val text = layoutInput.text.text
        for (line in 0 until lineCount - 1) {
            val end = getLineEnd(line)
            if (end in 1 until text.length && text[end - 1].isLetter() && text[end].isLetter()) {
                return text.substring(maxOf(0, end - 6), minOf(text.length, end + 6))
            }
        }
        return null
    }

    private companion object {
        val FONT_SCALES = listOf(1.0f, 1.3f, 2.0f)

        /**
         * The B1 categories as shipped: ids and names come from `content/categories.csv` (so a new long name fails
         * here), totals and learned counts are samples (one finished, two untouched).
         */
        val B1_CATEGORIES: List<CategoryUi> = run {
            val file = generateSequence(File("").absoluteFile) { it.parentFile }
                .map { File(it, "content/categories.csv") }.firstOrNull { it.isFile }
                ?: error("content/categories.csv not found above ${File("").absolutePath}")
            val samples = listOf(40 to 125, 12 to 42, 35 to 35, 0 to 72, 101 to 134)
            file.readLines().drop(1).map { it.split(',') }
                .onEach { check(it.size == 6) { "unexpected categories.csv row: $it" } }
                .filter { it[0] != "mixed" && it[0] != "general" && "B1" in it[3].split('|') }
                .mapIndexed { index, cells ->
                    val (learned, total) = samples[index % samples.size]
                    CategoryUi(CategoryId(cells[0]), cells[1], cells[2], learned, total)
                }
        }

        val B1_STATE = CategoriesUiState(
            level = Level.B1,
            levelTotal = 1_050,
            isLoaded = true,
            categories = B1_CATEGORIES.toImmutableList(),
        )
    }
}
