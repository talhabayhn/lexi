package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.modifier.lexiSurface
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme

/** Game HUD geometry (`04-game.html`). */
public object GameHudDefaults {
    /** 48: top bar height (pause, timer, score). */
    public val BarHeight: Dp = 48.dp

    /** Clue card: `padding: 10px 14px`, gap 2, right column 64 + 3dp border. */
    internal val CluePaddingHorizontal: Dp = 14.dp
    internal val CluePaddingVertical: Dp = 10.dp
    internal val ClueSideWidth: Dp = 64.dp

    /** Timer text inset from the bar's left edge. */
    internal val TimerTextInset: Dp = 10.dp
}

/**
 * The clue card: white, 3dp border, s6; label (e.g. "İPUCU · İSİM") over the Turkish clue on the left, the blue
 * 64dp column with level and letter count ("A1", "7 HARF") on the right.
 */
@Composable
public fun ClueCard(label: String, clue: String, level: String, letterCount: String, modifier: Modifier = Modifier) {
    val colors = LexiTheme.colors
    val typography = LexiTheme.typography
    Row(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .lexiSurface(colors.white, BorderStroke(LexiTheme.borders.regular, colors.ink), LexiTheme.shadows.s6),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .semantics(mergeDescendants = true) {}
                .padding(
                    horizontal = GameHudDefaults.CluePaddingHorizontal,
                    vertical = GameHudDefaults.CluePaddingVertical,
                ),
            verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap2),
        ) {
            Text(label, style = typography.label12)
            Text(clue, style = typography.clue)
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
            Text(level, style = typography.heading20, color = colors.white)
            Text(letterCount, style = typography.caption12Bold, color = colors.white)
        }
    }
}

/**
 * Countdown bar: 48dp, 3dp border, white track; the fill ([progress] 0..1) is yellow with a 3dp right edge, red in
 * the last 10 s ([warning]), when the counter pulses 1 → 1.18 → 1 on every change of [timeText] (motion.md #8; no
 * scale under reduced motion). The counter (18sp/800) sits 10dp from the left over the fill.
 */
@Composable
public fun TimerBar(progress: Float, timeText: String, modifier: Modifier = Modifier, warning: Boolean = false) {
    val colors = LexiTheme.colors
    val motion = LexiTheme.motion
    val border = LexiTheme.borders.regular
    val pulse = remember { Animatable(1f) }
    LaunchedEffect(timeText, warning, motion) {
        if (!warning || motion.reducedMotion) {
            pulse.snapTo(1f)
            return@LaunchedEffect
        }
        pulse.animateTo(motion.timeWarningPulseScale, tween(motion.timeWarningPulseUpMillis, easing = LinearEasing))
        pulse.animateTo(1f, tween(motion.timeWarningPulseDownMillis, easing = LinearEasing))
    }
    Box(
        modifier = modifier
            .height(GameHudDefaults.BarHeight)
            .lexiSurface(colors.white, BorderStroke(border, colors.ink)),
        contentAlignment = Alignment.CenterStart,
    ) {
        val fraction = progress.coerceIn(0f, 1f)
        if (fraction > 0f) {
            Row(Modifier.fillMaxWidth(fraction).fillMaxHeight()) {
                Box(Modifier.weight(1f).fillMaxHeight().background(if (warning) colors.red else colors.yellow))
                Box(Modifier.width(border).fillMaxHeight().background(colors.ink))
            }
        }
        Text(
            text = timeText,
            style = LexiTheme.typography.heading18,
            modifier = Modifier
                .padding(start = GameHudDefaults.TimerTextInset)
                .graphicsLayer {
                    scaleX = pulse.value
                    scaleY = pulse.value
                },
        )
    }
}

/** Score badge: 48dp blue circle, 3dp border, white 15sp/800. */
@Composable
public fun ScoreBadge(score: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(GameHudDefaults.BarHeight)
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
