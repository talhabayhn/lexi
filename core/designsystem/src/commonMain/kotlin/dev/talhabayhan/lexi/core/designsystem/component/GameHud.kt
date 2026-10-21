package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.talhabayhan.lexi.core.designsystem.modifier.lexiSurface
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.resized
import kotlin.math.roundToInt

/** Game HUD geometry (`04-game.html`). */
public object GameHudDefaults {
    /** 48: top bar height (pause, timer, score). */
    public val BarHeight: Dp = 48.dp

    /** Clue card: `padding: 10px 14px`, gap 2, right column 64 + 3dp border. */
    internal val CluePaddingHorizontal: Dp = 14.dp
    internal val CluePaddingVertical: Dp = 10.dp
    internal val ClueSideWidth: Dp = 64.dp

    /**
     * Smallest sizes the clue and the joker's example sentence shrink to in a fixed clue area (no design: the design's
     * clue is one 30sp line; the label is 12sp).
     */
    public val ClueMinFontSize: TextUnit = 12.sp
    public val ExampleMinFontSize: TextUnit = 11.sp

    /** The clue wraps onto at most this many lines in a fixed clue area. */
    public const val CLUE_MAX_LINES: Int = 3

    /** The joker's example sentence wraps onto at most this many lines in a fixed clue area. */
    public const val EXAMPLE_MAX_LINES: Int = 2

    /** Timer text inset from the bar's left edge. */
    internal val TimerTextInset: Dp = 10.dp
}

/**
 * Heights that size a [ClueCard] with a fixed clue area: [chrome] is everything but the clue area (borders, padding,
 * the one-line label and its gap), [clueLine] one line of the clue at its full size (30sp × font scale).
 */
@Immutable
public data class ClueCardMetrics(val chrome: Dp, val clueLine: Dp) {
    /** The card's height around a clue area of [clueArea]. */
    public fun cardHeight(clueArea: Dp): Dp = chrome + clueArea
}

/** [ClueCardMetrics] at the current density and font scale. */
@Composable
public fun rememberClueCardMetrics(): ClueCardMetrics {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val typography = LexiTheme.typography
    val border = LexiTheme.borders.regular
    val gap = LexiTheme.dimens.gap2
    return remember(measurer, density, typography, border, gap) {
        with(density) {
            val label = measurer.measure(METRICS_SAMPLE, typography.label12, maxLines = 1).size.height
            val line = measurer.measure(METRICS_SAMPLE, typography.clue, maxLines = 1).size.height
            val chrome = 2 * border.roundToPx() + 2 * GameHudDefaults.CluePaddingVertical.roundToPx() + label +
                gap.roundToPx()
            ClueCardMetrics(chrome = chrome.toDp(), clueLine = line.toDp())
        }
    }
}

private const val METRICS_SAMPLE = "A"

/**
 * The clue card: white, 3dp border, s6; label (e.g. "İPUCU · İSİM") over the Turkish clue on the left, the blue
 * 64dp column with level and letter count ("A1", "7 HARF") on the right. [example] is the blanked example sentence
 * of the "Örnek cümle" joker (DESIGN-DEBT: no design shows it; an italic 14/500 line under the clue).
 *
 * With a [clueAreaHeight] (the game screen) the card never changes its height with the text: the label stays on one
 * line, and the clue (up to [GameHudDefaults.CLUE_MAX_LINES] lines) and the example shrink from their full sizes down
 * to [GameHudDefaults.ClueMinFontSize] / [GameHudDefaults.ExampleMinFontSize] until they fit, centred vertically in the
 * area; at the minimum they are cut with an ellipsis. Without it the card wraps its content.
 */
@Composable
public fun ClueCard(
    label: String,
    clue: String,
    level: String,
    letterCount: String,
    modifier: Modifier = Modifier,
    example: String? = null,
    clueAreaHeight: Dp? = null,
) {
    val colors = LexiTheme.colors
    val typography = LexiTheme.typography
    val height = if (clueAreaHeight != null) {
        Modifier.height(rememberClueCardMetrics().cardHeight(clueAreaHeight))
    } else {
        Modifier.height(IntrinsicSize.Min)
    }
    Row(
        modifier = modifier
            .then(height)
            .lexiSurface(colors.white, BorderStroke(LexiTheme.borders.regular, colors.ink), LexiTheme.shadows.s6),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .semantics(mergeDescendants = true) {}
                .padding(
                    horizontal = GameHudDefaults.CluePaddingHorizontal,
                    vertical = GameHudDefaults.CluePaddingVertical,
                ),
            verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap2),
        ) {
            if (clueAreaHeight == null) {
                Text(label, style = typography.label12)
                Text(clue, style = typography.clue)
                if (example != null) Text(example, style = typography.caption14, fontStyle = FontStyle.Italic)
            } else {
                Text(label, style = typography.label12, maxLines = 1, overflow = TextOverflow.Ellipsis)
                FittedClue(clue = clue, example = example, modifier = Modifier.weight(1f).fillMaxWidth())
            }
        }
        Box(Modifier.width(LexiTheme.borders.regular).fillMaxHeight().background(colors.ink))
        Column(
            modifier = Modifier
                .width(GameHudDefaults.ClueSideWidth)
                .fillMaxHeight()
                .semantics(mergeDescendants = true) {}
                .background(colors.blue),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(level, style = typography.heading20, color = colors.white, textAlign = TextAlign.Center)
            // Centred also when a long label ("10 LETTERS") wraps onto two lines.
            Text(letterCount, style = typography.caption12Bold, color = colors.white, textAlign = TextAlign.Center)
        }
    }
}

/** The clue (and example) at the largest sizes that fit the area this is given, centred vertically. */
@Composable
private fun FittedClue(clue: String, example: String?, modifier: Modifier = Modifier) {
    val typography = LexiTheme.typography
    val measurer = rememberTextMeasurer()
    val gap = LexiTheme.dimens.gap2
    val gapPx = with(LocalDensity.current) { gap.roundToPx() }
    BoxWithConstraints(modifier, contentAlignment = Alignment.CenterStart) {
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        val fit = remember(clue, example, width, height, gapPx, typography, measurer) {
            fitClue(
                measurer = measurer,
                clue = clue,
                clueStyle = typography.clue,
                example = example,
                exampleStyle = typography.caption14.copy(fontStyle = FontStyle.Italic),
                width = width,
                height = height,
                gapPx = gapPx,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(gap)) {
            Text(
                clue,
                style = fit.clueStyle,
                maxLines = GameHudDefaults.CLUE_MAX_LINES,
                overflow = TextOverflow.Ellipsis,
            )
            if (example != null) {
                Text(
                    example,
                    style = fit.exampleStyle,
                    maxLines = GameHudDefaults.EXAMPLE_MAX_LINES,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private class ClueFit(val clueStyle: TextStyle, val exampleStyle: TextStyle)

/**
 * The largest clue size (1sp steps from the style's size down to the minimum), then the largest example size not above
 * it, whose texts fit [width] × [height] within their line limits; the minimum sizes when nothing fits.
 */
private fun fitClue(
    measurer: TextMeasurer,
    clue: String,
    clueStyle: TextStyle,
    example: String?,
    exampleStyle: TextStyle,
    width: Int,
    height: Int,
    gapPx: Int,
): ClueFit {
    val constraints = Constraints(maxWidth = width)
    fun sizes(style: TextStyle, min: TextUnit): List<TextStyle> {
        val max = style.fontSize.value.toInt()
        return (max downTo min.value.toInt()).map { if (it == max) style else style.resized(it.sp) }
    }
    fun heightOf(text: String, style: TextStyle, maxLines: Int): Int? {
        val result = measurer.measure(text, style, maxLines = maxLines, constraints = constraints)
        return if (result.hasVisualOverflow) null else result.size.height
    }
    val clueStyles = sizes(clueStyle, GameHudDefaults.ClueMinFontSize)
    val exampleStyles = sizes(exampleStyle, GameHudDefaults.ExampleMinFontSize)
    // Each example size with the height it takes under the clue (gap included), largest first; none without example.
    val exampleHeights = if (example == null) {
        listOf(exampleStyle to 0)
    } else {
        exampleStyles.mapNotNull { style ->
            heightOf(example, style, GameHudDefaults.EXAMPLE_MAX_LINES)?.let { style to it + gapPx }
        }
    }
    for (style in clueStyles) {
        val clueHeight = heightOf(clue, style, GameHudDefaults.CLUE_MAX_LINES) ?: continue
        // The example is never larger than the clue.
        val match = exampleHeights.firstOrNull { (candidate, exampleHeight) ->
            (example == null || candidate.fontSize <= style.fontSize) && clueHeight + exampleHeight <= height
        }
        if (match != null) return ClueFit(style, match.first)
    }
    return ClueFit(clueStyles.last(), exampleStyles.last())
}

/**
 * Countdown bar: 48dp, 3dp border, white track; the fill ([progress] 0..1) is yellow with a 3dp right edge and the
 * counter (18sp/800, 10dp from the left) is ink. In the last 10 s ([warning], motion.md #8) fill and counter switch
 * to red at once (no fade: state changes are not faded) and the counter pulses 1 → 1.12 → 1 (600 ms, linear) each
 * time it changes, i.e. once per second with the tick; under reduced motion only the colours change.
 */
@Composable
public fun TimerBar(progress: Float, timeText: String, modifier: Modifier = Modifier, warning: Boolean = false) {
    TimerBar(progress = { progress }, timeText = timeText, modifier = modifier, warning = warning)
}

/**
 * [TimerBar] whose fill reads [progress] only in the draw phase (ADR 0019): a running clock redraws the bar on every
 * tick without recomposing it. [contentDescription] replaces the counter for screen readers ("Kalan süre: 42 sn");
 * it is never a live region, so a ticking clock is not read out every second.
 */
@Composable
public fun TimerBar(
    progress: () -> Float,
    timeText: String,
    modifier: Modifier = Modifier,
    warning: Boolean = false,
    height: Dp = GameHudDefaults.BarHeight,
    contentDescription: String? = null,
) {
    val colors = LexiTheme.colors
    val motion = LexiTheme.motion
    val border = LexiTheme.borders.regular
    val pulse = remember { Animatable(1f) }
    // One pulse whenever the counter changes (each second) or the warning starts; nothing on first composition, so a
    // restored or paused warning rests at scale 1 and a stopped clock stops pulsing.
    val composed = remember { PulseGate() }
    LaunchedEffect(warning, timeText, motion) {
        val first = !composed.seen
        composed.seen = true
        pulse.snapTo(1f)
        if (first || !warning || motion.reducedMotion) return@LaunchedEffect
        val half = tween<Float>(motion.timeWarningPulseMillis / 2, easing = motion.linear)
        pulse.animateTo(motion.timeWarningPulseScale, half)
        pulse.animateTo(1f, half)
    }
    val description = if (contentDescription != null) {
        Modifier.clearAndSetSemantics { this.contentDescription = contentDescription }
    } else {
        Modifier
    }
    Box(
        modifier = modifier
            .height(height)
            .then(description)
            .lexiSurface(colors.white, BorderStroke(border, colors.ink)),
        contentAlignment = Alignment.CenterStart,
    ) {
        val fill = if (warning) colors.red else colors.yellow
        Box(
            Modifier.fillMaxSize().drawBehind {
                // Like `width: N%` in CSS: whole pixels, the 3dp right edge inside the fill.
                val width = (size.width * progress().coerceIn(0f, 1f)).roundToInt().toFloat()
                if (width > 0f) {
                    val edge = minOf(border.toPx().roundToInt().toFloat(), width)
                    drawRect(fill, size = Size(width - edge, size.height))
                    drawRect(colors.ink, topLeft = Offset(width - edge, 0f), size = Size(edge, size.height))
                }
            },
        )
        Text(
            text = timeText,
            style = LexiTheme.typography.heading18,
            color = if (warning) colors.red else colors.ink,
            modifier = Modifier
                .padding(start = GameHudDefaults.TimerTextInset)
                .graphicsLayer {
                    scaleX = pulse.value
                    scaleY = pulse.value
                },
        )
    }
}

/** Remembers whether the timer's pulse effect has run once (not snapshot state: it never triggers recomposition). */
private class PulseGate {
    var seen: Boolean = false
}

/** Score badge: 48dp ([size]) blue circle, 3dp border, white 15sp/800. */
@Composable
public fun ScoreBadge(score: String, modifier: Modifier = Modifier, size: Dp = GameHudDefaults.BarHeight) {
    Box(
        modifier = modifier
            .size(size)
            .lexiSurface(
                color = LexiTheme.colors.blue,
                border = BorderStroke(LexiTheme.borders.regular, LexiTheme.colors.ink),
                shape = LexiTheme.shapes.circle,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(score, style = LexiTheme.typography.bodyStrong15, color = LexiTheme.colors.white)
    }
}

// design sample, not user-facing
@Preview(widthDp = 390)
@Composable
private fun GameHudPreview() {
    LexiTheme(reducedMotion = false) {
        Column(
            modifier = Modifier.background(LexiTheme.colors.cream).padding(LexiTheme.dimens.gap20),
            verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap16),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap8)) {
                TimerBar(progress = 0.62f, timeText = "1:47", modifier = Modifier.weight(1f))
                ScoreBadge("120")
            }
            TimerBar(progress = 0.1f, timeText = "0:09", warning = true, modifier = Modifier.fillMaxWidth())
            ClueCard(label = "İPUCU · İSİM", clue = "mutfak", level = "A1", letterCount = "7 HARF")
        }
    }
}
