package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Screen-level geometry of `04-game.html` that is not part of a single component, plus the responsive rules of
 * `screen-map.md` for the game screen.
 */
public object GameScreenDefaults {
    /** Red background circle: 120dp, `right: -60px; top: 120px`. */
    public val DecorCircleSize: Dp = 120.dp
    public val DecorCircleEndOverhang: Dp = 60.dp
    public val DecorCircleTop: Dp = 120.dp

    /** Blue background half pill: 80 × 160dp, `left: -40px; top: 300px`. */
    public val DecorPillWidth: Dp = 80.dp
    public val DecorPillHeight: Dp = 160.dp
    public val DecorPillStartOverhang: Dp = 40.dp
    public val DecorPillTop: Dp = 300.dp

    /**
     * Gap between the words of a multi-word answer (spaces are fixed gaps, `GAME_RULES.md` §3). No design yet:
     * three slot gaps.
     */
    public val AnswerWordGap: Dp = 18.dp

    /** Short phones (`screen-map.md`): below this height the wheel is at most 280dp and vertical gaps shrink 25 %. */
    public val ShortScreenHeight: Dp = 760.dp
    public val ShortScreenWheelSize: Dp = 280.dp
    public const val SHORT_SCREEN_GAP_SCALE: Float = 0.75f

    /**
     * Minimum gap between the reserved third key row and the actions. The layout always reserves three key rows (15
     * keys, `GAME_RULES.md` §4) so the keys never move between turns; at 390 × 844 the design's free space above the
     * actions (70dp) holds the third row (58dp) with this gap, so the wheel keeps 320dp.
     */
    public val KeysActionsMinGap: Dp = 12.dp

    /** A layout whose wheel would be smaller than this falls back to the next denser tier (short, then compact). */
    public val ComfortableWheelSize: Dp = 240.dp

    /**
     * Compact tier for very short phones (`screen-map.md`, e.g. 320 × 568, 360 × 640): smaller paddings and gaps,
     * 44dp HUD and keys (the touch target minimum), 40dp answer boxes, 48dp actions. No design: derived from `04-game`.
     */
    public val CompactPaddingTop: Dp = 12.dp
    public val CompactPaddingBottom: Dp = 16.dp
    public val CompactGap: Dp = 8.dp
    public val CompactAnswerTopMargin: Dp = 2.dp
    public val CompactHudHeight: Dp = 44.dp
    public val CompactSlotHeight: Dp = 40.dp
    public val CompactKeyHeight: Dp = 44.dp
    public val CompactKeyGap: Dp = 6.dp
    public val CompactActionSize: Dp = 48.dp

    /** The game screen (keys, answer boxes, wheel, HUD) honours the system font size up to 1.15× (`screen-map.md`). */
    public const val MAX_FONT_SCALE: Float = 1.15f

    /** Rows of keys the layout reserves (15 keys at most, rows of 5). */
    public const val RESERVED_KEY_ROWS: Int = 3
}
