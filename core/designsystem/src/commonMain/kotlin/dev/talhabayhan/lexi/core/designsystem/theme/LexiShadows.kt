package dev.talhabayhan.lexi.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A hard (non-blurred) shadow: the element's shape filled with [color], offset by [offset] on both axes
 * (CSS `box-shadow: 5px 5px 0 #111111`). Drawn with `Modifier.hardShadow`.
 */
@Immutable
public data class LexiShadow(public val offset: Dp, public val color: Color)

/**
 * Hard shadows (`docs/design/tokens.md` § Hard shadows). Pressed state: the element translates by the offset and
 * the shadow collapses to 0 (`PressableSurface`).
 */
@Immutable
public class LexiShadows internal constructor(
    /** 3,3: letter keys, list cards, small buttons. */
    public val s3: LexiShadow,
    /** 4,4: menu tiles. */
    public val s4: LexiShadow,
    /** 5,5: PAS, primary/secondary buttons, result CTAs. */
    public val s5: LexiShadow,
    /** 6,6: hero cards (clue card, daily card, OYNA, selected level card). */
    public val s6: LexiShadow,
    /** 8,8: dialogs. */
    public val s8: LexiShadow,
    /** 5,5 red: Pro card in settings. */
    public val s5Red: LexiShadow,
    /** 4,4 red: exam-packs tile on home. */
    public val s4Red: LexiShadow,
)

internal fun lexiShadows(colors: LexiColors): LexiShadows = LexiShadows(
    s3 = LexiShadow(3.dp, colors.ink),
    s4 = LexiShadow(4.dp, colors.ink),
    s5 = LexiShadow(5.dp, colors.ink),
    s6 = LexiShadow(6.dp, colors.ink),
    s8 = LexiShadow(8.dp, colors.ink),
    s5Red = LexiShadow(5.dp, colors.red),
    s4Red = LexiShadow(4.dp, colors.red),
)

internal val DefaultLexiShadows: LexiShadows = lexiShadows(LightLexiColors)
