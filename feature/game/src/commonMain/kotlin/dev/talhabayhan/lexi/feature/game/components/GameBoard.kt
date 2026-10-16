package dev.talhabayhan.lexi.feature.game.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.min
import dev.talhabayhan.lexi.core.designsystem.component.AnswerSlot
import dev.talhabayhan.lexi.core.designsystem.component.AnswerSlotDefaults
import dev.talhabayhan.lexi.core.designsystem.component.AnswerSlotState
import dev.talhabayhan.lexi.core.designsystem.component.GameHudDefaults
import dev.talhabayhan.lexi.core.designsystem.component.GameScreenDefaults
import dev.talhabayhan.lexi.core.designsystem.component.IconButtonDefaults
import dev.talhabayhan.lexi.core.designsystem.component.LetterKey
import dev.talhabayhan.lexi.core.designsystem.component.LetterKeyDefaults
import dev.talhabayhan.lexi.core.designsystem.component.NavSquareButton
import dev.talhabayhan.lexi.core.designsystem.component.ScoreBadge
import dev.talhabayhan.lexi.core.designsystem.component.TimerBar
import dev.talhabayhan.lexi.core.designsystem.format.formatClock
import dev.talhabayhan.lexi.core.designsystem.icon.LexiIcons
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.resized
import dev.talhabayhan.lexi.feature.game.AnswerCellUi
import dev.talhabayhan.lexi.feature.game.GameClockUi
import dev.talhabayhan.lexi.feature.game.KeyUi
import dev.talhabayhan.lexi.feature.game.generated.resources.Res
import dev.talhabayhan.lexi.feature.game.generated.resources.game_button_pause
import dev.talhabayhan.lexi.feature.game.generated.resources.game_correct_description
import dev.talhabayhan.lexi.feature.game.generated.resources.game_score_description
import dev.talhabayhan.lexi.feature.game.generated.resources.game_timer_untimed
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource

/**
 * Top bar: pause (48dp square on cream), the timer and the score badge, gap 8. Only this composable reads the
 * clock (ADR 0019 §3): the fill in the draw phase, the counter and warning colour through derived state, i.e. once
 * per second. Unscored modes ([isScored] false: Practice, Review) have no design (DESIGN-DEBT): the badge shows
 * [correct] instead of the score (always 0 there), and an untimed bar stays empty with "SÜRESİZ" in place of the clock.
 */
@Composable
internal fun GameHudBar(
    score: Int,
    correct: Int,
    isScored: Boolean,
    clock: () -> GameClockUi,
    onPause: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val seconds by remember(clock) { derivedStateOf { clock().wholeSeconds } }
    val timed by remember(clock) { derivedStateOf { clock().isTimed } }
    val warning by remember(clock) { derivedStateOf { clock().isWarning } }
    val badge = if (isScored) score else correct
    val badgeDescription = stringResource(
        if (isScored) Res.string.game_score_description else Res.string.game_correct_description,
        badge,
    )
    Row(
        modifier = modifier.height(GameHudDefaults.BarHeight),
        horizontalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap8),
    ) {
        NavSquareButton(
            icon = LexiIcons.Pause,
            contentDescription = stringResource(Res.string.game_button_pause),
            onClick = onPause,
            size = IconButtonDefaults.NavSizeLarge,
            iconSize = IconButtonDefaults.IconSizeSmall,
            containerColor = LexiTheme.colors.cream,
        )
        TimerBar(
            // DESIGN-DEBT: untimed modes (Practice, Review) have no timer design; the bar stays empty.
            progress = { clock().fraction },
            timeText = if (timed) formatClock(seconds) else stringResource(Res.string.game_timer_untimed),
            warning = warning,
            modifier = Modifier.weight(1f),
        )
        ScoreBadge(
            score = badge.toString(),
            modifier = Modifier.clearAndSetSemantics { contentDescription = badgeDescription },
        )
    }
}

/**
 * Answer boxes, centred, gap 6; words of a multi-word answer are separated by [GameScreenDefaults.AnswerWordGap].
 * Boxes keep 40dp up to the width of the column, then narrow (with the letter) so 14-letter answers still fit.
 * [offset] is the #4 shake, applied in the layer phase. [flyingCells] are cells whose letter is still in the air (#2):
 * they keep their look from before it lands, so the first of them is the red-bordered next box and the rest (with the
 * state's next box) stay empty. [onCellPlaced] reports each cell (letters only, in answer order) for the flight.
 */
@Composable
internal fun AnswerSlots(
    words: ImmutableList<ImmutableList<AnswerCellUi>>,
    offset: () -> Dp,
    modifier: Modifier = Modifier,
    flyingCells: () -> Set<Int> = { emptySet() },
    onCellPlaced: (index: Int, coordinates: LayoutCoordinates) -> Unit = { _, _ -> },
) {
    val flying = flyingCells()
    val firstFlying = flying.minOrNull()
    BoxWithConstraints(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val cells = words.sumOf { it.size }
        val gaps = AnswerSlotDefaults.Gap * (cells - words.size) + GameScreenDefaults.AnswerWordGap * (words.size - 1)
        val slotWidth = min(AnswerSlotDefaults.Width, (maxWidth - gaps) / cells.coerceAtLeast(1))
        val baseStyle = LexiTheme.typography.slotLetter
        val textStyle = if (slotWidth < AnswerSlotDefaults.Width) {
            baseStyle.resized(baseStyle.fontSize * (slotWidth / AnswerSlotDefaults.Width))
        } else {
            baseStyle
        }
        Row(
            modifier = Modifier.graphicsLayer { translationX = offset().toPx() },
            horizontalArrangement = Arrangement.spacedBy(GameScreenDefaults.AnswerWordGap),
        ) {
            var index = 0
            words.forEach { word ->
                Row(horizontalArrangement = Arrangement.spacedBy(AnswerSlotDefaults.Gap)) {
                    word.forEach { cell ->
                        val cellIndex = index++
                        val state = when {
                            firstFlying == null -> cell.state
                            cellIndex == firstFlying -> AnswerSlotState.Active
                            cellIndex in flying || cell.state == AnswerSlotState.Active -> AnswerSlotState.Empty
                            else -> cell.state
                        }
                        AnswerSlot(
                            letter = cell.letter,
                            state = state,
                            modifier = Modifier.width(slotWidth).onPlaced { onCellPlaced(cellIndex, it) },
                            textStyle = textStyle,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Letter keys in rows of 5 (gap 8); keys keep their order for the whole slot (`GAME_RULES.md` §4). Not [enabled]
 * while input is blocked (wrong-answer reveal): the keys are disabled and drawn at 50 %. [onKeyPlaced] reports each
 * key for the #2 flight.
 */
@Composable
internal fun KeyGrid(
    keys: ImmutableList<KeyUi>,
    enabled: Boolean,
    onKey: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onKeyPlaced: (index: Int, coordinates: LayoutCoordinates) -> Unit = { _, _ -> },
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(LetterKeyDefaults.GridGap)) {
        keys.indices.chunked(LetterKeyDefaults.COLUMNS).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(LetterKeyDefaults.GridGap)) {
                row.forEach { index ->
                    LetterKey(
                        letter = keys[index].letter,
                        state = keys[index].state,
                        enabled = enabled,
                        onClick = { onKey(index) },
                        modifier = Modifier.weight(1f).onPlaced { onKeyPlaced(index, it) },
                    )
                }
                // A short last row keeps the 5-column grid (key counts are multiples of 5 today).
                repeat(LetterKeyDefaults.COLUMNS - row.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}
