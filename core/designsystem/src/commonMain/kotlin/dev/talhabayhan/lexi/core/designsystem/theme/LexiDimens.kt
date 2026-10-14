package dev.talhabayhan.lexi.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing (`docs/design/tokens.md` § Spacing). 1 CSS px = 1.dp. Use the exact gap from the screen HTML.
 * Screen paddings are design paddings; `WindowInsets.safeDrawing` is added on top of them.
 */
@Immutable
public class LexiDimens internal constructor(
    public val screenPaddingHorizontal: Dp,
    public val screenPaddingTop: Dp,
    /** Game screen top padding. */
    public val screenPaddingTopGame: Dp,
    public val screenPaddingBottom: Dp,
    /** List screens' bottom padding. */
    public val screenPaddingBottomList: Dp,
    public val gap2: Dp,
    public val gap4: Dp,
    public val gap6: Dp,
    public val gap8: Dp,
    public val gap10: Dp,
    public val gap12: Dp,
    public val gap14: Dp,
    public val gap16: Dp,
    public val gap18: Dp,
    public val gap20: Dp,
    public val gap24: Dp,
    /** Minimum touch target edge (44 × 44). */
    public val minTouchTarget: Dp,
)

internal val DefaultLexiDimens: LexiDimens = LexiDimens(
    screenPaddingHorizontal = 20.dp,
    screenPaddingTop = 24.dp,
    screenPaddingTopGame = 20.dp,
    screenPaddingBottom = 28.dp,
    screenPaddingBottomList = 20.dp,
    gap2 = 2.dp,
    gap4 = 4.dp,
    gap6 = 6.dp,
    gap8 = 8.dp,
    gap10 = 10.dp,
    gap12 = 12.dp,
    gap14 = 14.dp,
    gap16 = 16.dp,
    gap18 = 18.dp,
    gap20 = 20.dp,
    gap24 = 24.dp,
    minTouchTarget = 44.dp,
)

/** Border widths (`docs/design/tokens.md` § Borders). All borders are ink unless stated (active slot = red). */
@Immutable
public class LexiBorders internal constructor(
    /** 1: chart grid (dashed). */
    public val hairline: Dp,
    /** 2: chips, wheel letters, list dividers, inner cells. */
    public val thin: Dp,
    /** 3: default for buttons, cards, inputs, keys, slots. */
    public val regular: Dp,
    /** 4: splash logo blocks, bottom-sheet top edge. */
    public val thick: Dp,
    /** 5: large logo. */
    public val heavy: Dp,
)

internal val DefaultLexiBorders: LexiBorders = LexiBorders(
    hairline = 1.dp,
    thin = 2.dp,
    regular = 3.dp,
    thick = 4.dp,
    heavy = 5.dp,
)
