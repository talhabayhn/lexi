package dev.talhabayhan.lexi.core.designsystem.text

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.component.FitLine
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Regression for the video flow at 2× font size on a 320dp phone: "Klasi / k" in the dialog's segments, "12.48 / 0" in
 * Home's points, "İngiliz…" on a category card. The detector finds the broken word; [WordSafeText] and [FitLine]
 * keep every word whole.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w320dp-h568dp-xhdpi", fontScale = 2.0f)
class WordSafeTextTest {

    @Test
    fun `a word too wide for its box is detected in plain text and shrinks in the word-safe one`() = runComposeUiTest {
        val recorder = WordSplitRecorder()
        var plain: TextLayoutResult? = null
        setContent {
            LexiTheme(reducedMotion = true) {
                CompositionLocalProvider(LocalTextLayoutProbe provides recorder) {
                    Column(Modifier.width(NARROW)) {
                        Text(CARD_NAME, style = LexiTheme.typography.bodyStrong14, onTextLayout = { plain = it })
                        WordSafeText(CARD_NAME, LexiTheme.typography.bodyStrong14, LexiTheme.colors.ink, maxLines = 2)
                        FitLine(POINTS, LexiTheme.typography.heading24, LexiTheme.colors.ink)
                    }
                }
            }
        }
        waitForIdle()
        val broken = assertNotNull(plain)
        assertTrue(broken.breaksInsideWord(), "the plain text should break \"İngilizcesi\" at this size")
        assertEquals(emptyList(), recorder.problems)
    }

    @Test
    fun `wrapping between words is not a problem`() = runComposeUiTest {
        var layout: TextLayoutResult? = null
        setContent {
            LexiTheme(reducedMotion = true) {
                Column(Modifier.width(WIDE)) {
                    Text(SENTENCE, style = LexiTheme.typography.label12, onTextLayout = { layout = it })
                }
            }
        }
        waitForIdle()
        val wrapped = assertNotNull(layout)
        assertTrue(wrapped.lineCount > 1, "the sentence should wrap at this size")
        assertEquals(null, wrapped.wordSafetyProblem())
    }

    @Test
    fun `a line start is inside a word unless it follows a space a hyphen or a slash`() {
        assertTrue(lineStartsInsideWord("12.480", listOf(3)), "12. / 480")
        assertTrue(lineStartsInsideWord("7:00", listOf(2)), "7: / 00")
        assertTrue(lineStartsInsideWord("B2'ye", listOf(3)), "B2' / ye")
        assertTrue(lineStartsInsideWord("İş İngilizcesi", listOf(10)), "İş İngiliz / cesi")
        assertTrue(!lineStartsInsideWord("İş İngilizcesi", listOf(3)), "İş / İngilizcesi")
        assertTrue(!lineStartsInsideWord("A1-B2", listOf(3)), "A1- / B2")
        assertTrue(!lineStartsInsideWord("A1/B2", listOf(3)), "A1/ / B2")
    }

    @Test
    @Config(fontScale = 1.0f)
    fun `at font scale 1 text that fits keeps exactly its style's size`() = runComposeUiTest {
        var layout: TextLayoutResult? = null
        var expected = TextUnit.Unspecified
        setContent {
            LexiTheme(reducedMotion = true) {
                expected = LexiTheme.typography.bodyStrong14.fontSize
                CompositionLocalProvider(LocalTextLayoutProbe provides TextLayoutProbe { layout = it }) {
                    Column(Modifier.width(WIDE)) {
                        WordSafeText(CARD_NAME, LexiTheme.typography.bodyStrong14, LexiTheme.colors.ink, maxLines = 2)
                    }
                }
            }
        }
        waitForIdle()
        assertEquals(expected, assertNotNull(layout).layoutInput.style.fontSize)
    }

    private companion object {
        const val CARD_NAME = "İş İngilizcesi"
        const val POINTS = "12.480"
        const val SENTENCE = "SEVİYENİ, KATEGORİNİ VE MODUNU SEÇ"
        val NARROW = 96.dp
        val WIDE = 220.dp
    }
}
