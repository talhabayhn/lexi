package dev.talhabayhan.lexi.feature.game.components

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.min
import dev.talhabayhan.lexi.core.designsystem.component.AnswerSlotDefaults
import dev.talhabayhan.lexi.core.designsystem.component.ClueCardMetrics
import dev.talhabayhan.lexi.core.designsystem.component.GameHudDefaults
import dev.talhabayhan.lexi.core.designsystem.component.GameScreenDefaults
import dev.talhabayhan.lexi.core.designsystem.component.IconButtonDefaults
import dev.talhabayhan.lexi.core.designsystem.component.LetterKeyDefaults
import dev.talhabayhan.lexi.core.designsystem.component.LetterWheelDefaults
import dev.talhabayhan.lexi.core.designsystem.theme.LexiDimens

/** Density tiers of the game screen (`screen-map.md` § Responsive rules). */
internal enum class GameLayoutTier {
    /** The design (`04-game`): 16dp gaps, wheel up to 320dp. */
    Regular,

    /** Below 760dp: wheel up to 280dp, gaps −25 %. */
    Short,

    /** Very short phones: smaller paddings, gaps, HUD, keys, answer boxes and actions. */
    Compact,
}

/**
 * The game screen's geometry, computed once from the space inside the system bars (and the font scale through
 * [ClueCardMetrics]) and independent of the turn: the clue area is fixed (the text shrinks instead), three key rows
 * are always reserved, so nothing moves while clues, answers and keys change. [wheelSize] is what the column gives the
 * wheel; [clueAreaHeight] is the clue card's text area (one 30sp line, plus up to one more line on tall phones).
 */
@Immutable
internal data class GameLayout(
    val tier: GameLayoutTier,
    val paddingTop: Dp,
    val paddingBottom: Dp,
    val hudHeight: Dp,
    val gap: Dp,
    val answerTopMargin: Dp,
    val keysActionsGap: Dp,
    val maxWheelSize: Dp,
    val wheelSize: Dp,
    val clueAreaHeight: Dp,
    val clueCardHeight: Dp,
    val slotHeight: Dp,
    val keyHeight: Dp,
    val keyGap: Dp,
    val actionSize: Dp,
) {
    /** Height reserved for the keys: [GameScreenDefaults.RESERVED_KEY_ROWS] rows. */
    val keysHeight: Dp
        get() = keyHeight * GameScreenDefaults.RESERVED_KEY_ROWS + keyGap * (GameScreenDefaults.RESERVED_KEY_ROWS - 1)
}

/**
 * Lays the game screen out in [width] × [height] (the screen inside the safe-drawing insets). Tiers are tried from the
 * one the height allows (regular from 760dp) to compact until the wheel gets at least
 * [GameScreenDefaults.ComfortableWheelSize]; the wheel is `min(320 | 280, width − 40)` or what is left. Height left over
 * with the wheel at its maximum widens the clue area by up to one more clue line.
 */
internal fun gameLayout(width: Dp, height: Dp, dimens: LexiDimens, clue: ClueCardMetrics): GameLayout {
    val tiers = if (height >= GameScreenDefaults.ShortScreenHeight) {
        GameLayoutTier.entries
    } else {
        listOf(GameLayoutTier.Short, GameLayoutTier.Compact)
    }
    val columnWidth = width - dimens.screenPaddingHorizontal * 2
    var layout = tiers.first().layout(columnWidth, height, dimens, clue)
    for (tier in tiers.drop(1)) {
        if (layout.wheelSize >= GameScreenDefaults.ComfortableWheelSize) break
        layout = tier.layout(columnWidth, height, dimens, clue)
    }
    return layout
}

private fun GameLayoutTier.layout(columnWidth: Dp, height: Dp, dimens: LexiDimens, clue: ClueCardMetrics): GameLayout {
    val compact = this == GameLayoutTier.Compact
    val gapScale = if (this == GameLayoutTier.Short) GameScreenDefaults.SHORT_SCREEN_GAP_SCALE else 1f
    val base = GameLayout(
        tier = this,
        paddingTop = if (compact) GameScreenDefaults.CompactPaddingTop else dimens.screenPaddingTopGame,
        paddingBottom = if (compact) GameScreenDefaults.CompactPaddingBottom else dimens.screenPaddingBottom,
        hudHeight = if (compact) GameScreenDefaults.CompactHudHeight else GameHudDefaults.BarHeight,
        gap = if (compact) GameScreenDefaults.CompactGap else dimens.gap16 * gapScale,
        answerTopMargin = if (compact) GameScreenDefaults.CompactAnswerTopMargin else dimens.gap4 * gapScale,
        keysActionsGap = if (compact) {
            GameScreenDefaults.CompactGap
        } else {
            GameScreenDefaults.KeysActionsMinGap * gapScale
        },
        maxWheelSize = min(
            if (this == GameLayoutTier.Regular) LetterWheelDefaults.Size else GameScreenDefaults.ShortScreenWheelSize,
            columnWidth,
        ),
        wheelSize = LetterWheelDefaults.Size,
        clueAreaHeight = clue.clueLine,
        clueCardHeight = clue.cardHeight(clue.clueLine),
        slotHeight = if (compact) GameScreenDefaults.CompactSlotHeight else AnswerSlotDefaults.Height,
        keyHeight = if (compact) GameScreenDefaults.CompactKeyHeight else LetterKeyDefaults.Height,
        keyGap = if (compact) GameScreenDefaults.CompactKeyGap else LetterKeyDefaults.GridGap,
        actionSize = if (compact) GameScreenDefaults.CompactActionSize else IconButtonDefaults.ActionSize,
    )
    // hud | wheel | clue | answer | keys | actions: the clue | answer gap carries the answer margin, keys | actions is
    // the minimum gap before the actions' `margin-top: auto`.
    val fixed = base.paddingTop + base.hudHeight + base.gap * 4 + base.clueCardHeight + base.answerTopMargin +
        base.slotHeight + base.keysHeight + base.keysActionsGap + base.actionSize + base.paddingBottom
    val free = (height - fixed).coerceAtLeast(Dp(0f))
    val wheel = min(base.maxWheelSize, free)
    val extra = min(free - wheel, clue.clueLine)
    return base.copy(
        wheelSize = wheel,
        clueAreaHeight = clue.clueLine + extra,
        clueCardHeight = clue.cardHeight(clue.clueLine + extra),
    )
}
