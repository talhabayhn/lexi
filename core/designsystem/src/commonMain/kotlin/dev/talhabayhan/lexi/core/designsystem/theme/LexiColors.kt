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

private val Ink = Color(0xFF111111)

internal val LightLexiColors: LexiColors = LexiColors(
    cream = Color(0xFFF2EBDD),
    white = Color(0xFFFFFFFF),
    ink = Ink,
    red = Color(0xFFD9281C),
    blue = Color(0xFF1F4FBF),
    yellow = Color(0xFFF2C200),
    inkMuted = Color(0xFF5A5A5A),
    borderMuted = Color(0xFF7A7468),
    disabledFill = Color(0xFFE2DACB),
    gridLine = Color(0xFFB8B0A0),
    scrim = Ink.copy(alpha = 0.55f),
)
