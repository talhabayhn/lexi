package dev.talhabayhan.lexi.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Bauhaus Classic palette (`docs/design/tokens.md` § Colors). Light only (ADR 0010).
 *
 * Text on colors: on [red]/[blue]/[ink] use [white] or [cream]; on [yellow]/[white]/[cream] use [ink].
 * State is never color-only: correct = circle, wrong = square, pass = leaf, current = black square.
 */
@Immutable
public class LexiColors internal constructor(
    /** App background, "todo" wheel letters, secondary chips. */
    public val cream: Color,
    /** Cards, keys, inputs, list rows. */
    public val white: Color,
    /** Text, all borders, hard shadows, "current" letter, selected segment. */
    public val ink: Color,
    /** Primary CTA, **wrong** state, active answer-slot border. */
    public val red: Color,
    /** Small red TEXT on white/cream (>= 4.5:1 on both); [red] itself is for fills only. */
    public val redText: Color,
    /** **Correct** state, score badge, progress fill, toggle on. */
    public val blue: Color,
    /** **Pass** state, timer bar, highlights, selected-segment text on ink. */
    public val yellow: Color,
    /** Placeholder, disabled text. */
    public val inkMuted: Color,
    /** Dashed border of a disabled button / used key. */
    public val borderMuted: Color,
    /** Disabled button fill. */
    public val disabledFill: Color,
    /** Chart guide lines (dashed, hairline). */
    public val gridLine: Color,
    /** Behind bottom sheets and dialogs (ink at 55 %). */
    public val scrim: Color,
)

private val Ink = Color(0xFF1F242D)

internal val LightLexiColors: LexiColors = LexiColors(
    cream = Color(0xFFF7F4EE),
    white = Color(0xFFFFFFFF),
    ink = Ink,
    red = Color(0xFFE0534C),
    redText = Color(0xFFD42D25),
    blue = Color(0xFF3D68B2),
    yellow = Color(0xFFE29E2E),
    inkMuted = Color(0xFF606673),
    borderMuted = Color(0xFF8A8F9B),
    disabledFill = Color(0xFFEAE6DF),
    gridLine = Color(0xFFC8C4BC),
    scrim = Ink.copy(alpha = 0.55f),
)
