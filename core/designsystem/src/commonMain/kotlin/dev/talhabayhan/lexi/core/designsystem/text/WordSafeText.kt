package dev.talhabayhan.lexi.core.designsystem.text

import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.modifiers.TextAutoSizeLayoutScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import dev.talhabayhan.lexi.core.designsystem.component.FlowDefaults

/**
 * True when a line of this layout starts inside a word ("Klasi" / "k", "12." / "480", "B2'" / "ye"): the text breaks a
 * word because one word is wider than its box. Wrapping at a space or after a hyphen / slash is fine.
 */
public fun TextLayoutResult.breaksInsideWord(): Boolean =
    lineStartsInsideWord(layoutInput.text.text, (1 until lineCount).map { getLineStart(it) })

/** True when one of [lineStarts] is inside a word of [text] (a run of non-whitespace without a break opportunity). */
internal fun lineStartsInsideWord(text: String, lineStarts: List<Int>): Boolean = lineStarts.any { start ->
    start in 1 until text.length && !text[start - 1].isWhitespace() && text[start - 1] !in BreakAfter &&
        !text[start].isWhitespace()
}

/** Characters a line may break after inside a run of non-whitespace: hyphens, dashes, slash, soft hyphen. */
private val BreakAfter = setOf('-', '\u2010', '\u2013', '\u2014', '/', '\u00AD')

/**
 * True when part of the text is not shown: an ellipsis, or lines dropped by `maxLines`. (Not `hasVisualOverflow`: a
 * CSS line height or a glyph centred in a smaller box overflows its box without hiding any text.)
 */
public fun TextLayoutResult.isCut(): Boolean =
    multiParagraph.didExceedMaxLines || (0 until lineCount).any { isLineEllipsized(it) }

/** What is wrong with this layout of a pinned label, or null: a word broken across lines, or text cut off. */
public fun TextLayoutResult.wordSafetyProblem(): String? {
    val text = layoutInput.text.text
    return when {
        breaksInsideWord() -> "\"$text\" breaks inside a word"
        isCut() -> "\"$text\" is cut off"
        else -> null
    }
}

/**
 * Receives the final layout of the design system's pinned labels (control text, badges, chips, card names, the play
 * card's texts), which are hidden from the semantics tree behind their component's content description. Layout tests
 * provide a [WordSplitRecorder] through [LocalTextLayoutProbe]; the app provides nothing (no cost).
 */
public fun interface TextLayoutProbe {
    public fun onTextLayout(layout: TextLayoutResult)
}

public val LocalTextLayoutProbe: ProvidableCompositionLocal<TextLayoutProbe?> = staticCompositionLocalOf { null }

/** The `onTextLayout` of a pinned label: reports to [LocalTextLayoutProbe] when a test provides one. */
@Composable
public fun rememberTextLayoutReport(): (TextLayoutResult) -> Unit {
    val probe = LocalTextLayoutProbe.current
    return remember(probe) { { layout -> probe?.onTextLayout(layout) } }
}

/**
 * Collects the [wordSafetyProblem]s of the labels laid out under it (a problem, once seen, stays). Layout tests check
 * [problems] together with [textWordProblems] of the semantics tree.
 */
public class WordSplitRecorder : TextLayoutProbe {
    private val found = LinkedHashSet<String>()

    public val problems: List<String> get() = found.toList()

    override fun onTextLayout(layout: TextLayoutResult) {
        layout.wordSafetyProblem()?.let(found::add)
    }
}

/** [wordSafetyProblem]s of every text in this (unmerged) semantics subtree. */
public fun SemanticsNode.textWordProblems(): List<String> {
    val own = config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.let { action ->
        val results = mutableListOf<TextLayoutResult>()
        action(results)
        results.mapNotNull { it.wordSafetyProblem() }
    }.orEmpty()
    return own + children.flatMap { it.textWordProblems() }
}

/**
 * Shrinks from [max] in [step]s (down to [min]) until no word breaks across lines and nothing is cut: a long word
 * ("İngilizcesi" at a large font size) gets smaller instead of being split or ellipsized. Text that fits keeps exactly
 * [max] (no sp → px → sp round trip, so goldens at 1.0 do not move).
 */
private data class WordSafeAutoSize(val max: TextUnit, val min: TextUnit, val step: TextUnit) : TextAutoSize {
    override fun TextAutoSizeLayoutScope.getFontSize(constraints: Constraints, text: AnnotatedString): TextUnit {
        if (!max.isSp || max <= min) return max
        if (performLayout(constraints, text, max).isWordSafe()) return max
        val stepPx = step.toPx()
        val minPx = min.toPx()
        var size = max.toPx() - stepPx
        while (size > minPx) {
            if (performLayout(constraints, text, size.toSp()).isWordSafe()) return size.toSp()
            size -= stepPx
        }
        return min
    }

    private fun TextLayoutResult.isWordSafe() = !breaksInsideWord() && !isCut()
}

private val WordSafeStep = 0.5.sp

/**
 * A pinned label that may wrap between words but not inside one: when a word does not fit (or the text needs more than
 * [maxLines]) it shrinks, down to [FlowDefaults.MinControlTextSize]; anything still broken there is flagged by the
 * layout tests ([WordSplitRecorder]). Reports its layout to [LocalTextLayoutProbe].
 */
@Composable
public fun WordSafeText(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    textAlign: TextAlign = TextAlign.Unspecified,
) {
    val autoSize = remember(style.fontSize) {
        WordSafeAutoSize(style.fontSize, FlowDefaults.MinControlTextSize, WordSafeStep)
    }
    BasicText(
        text = text,
        modifier = modifier,
        style = style.copy(color = color, textAlign = textAlign),
        onTextLayout = rememberTextLayoutReport(),
        overflow = TextOverflow.Ellipsis,
        maxLines = maxLines,
        autoSize = autoSize,
    )
}
