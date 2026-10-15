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
}
