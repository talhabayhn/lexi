package dev.talhabayhan.lexi.feature.game.duel

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import dev.talhabayhan.lexi.core.designsystem.component.DecorShape
import dev.talhabayhan.lexi.core.designsystem.component.DecorShapeKind
import dev.talhabayhan.lexi.core.designsystem.component.DuelDefaults
import dev.talhabayhan.lexi.core.designsystem.component.LexiButton
import dev.talhabayhan.lexi.core.designsystem.component.LexiTextLink
import dev.talhabayhan.lexi.core.designsystem.format.formatClock
import dev.talhabayhan.lexi.core.designsystem.modifier.lexiHeading
import dev.talhabayhan.lexi.core.designsystem.modifier.lexiSurface
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.lexiContentWidth
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.feature.game.generated.resources.Res
import dev.talhabayhan.lexi.feature.game.generated.resources.duel_result_draw
import dev.talhabayhan.lexi.feature.game.generated.resources.duel_result_forfeit
import dev.talhabayhan.lexi.feature.game.generated.resources.duel_result_score
import dev.talhabayhan.lexi.feature.game.generated.resources.duel_result_tiebreak_time
import dev.talhabayhan.lexi.feature.game.generated.resources.duel_result_tiebreak_wrong
import dev.talhabayhan.lexi.feature.game.generated.resources.duel_result_time_left
import dev.talhabayhan.lexi.feature.game.generated.resources.duel_result_winner
import dev.talhabayhan.lexi.feature.game.generated.resources.duel_result_winner_badge
import dev.talhabayhan.lexi.feature.game.generated.resources.result_correct
import dev.talhabayhan.lexi.feature.game.generated.resources.result_header
import dev.talhabayhan.lexi.feature.game.generated.resources.result_home
import dev.talhabayhan.lexi.feature.game.generated.resources.result_mode_duel
import dev.talhabayhan.lexi.feature.game.generated.resources.result_passed
import dev.talhabayhan.lexi.feature.game.generated.resources.result_play_again
import dev.talhabayhan.lexi.feature.game.generated.resources.result_reason_all_answered
import dev.talhabayhan.lexi.feature.game.generated.resources.result_reason_forfeit
import dev.talhabayhan.lexi.feature.game.generated.resources.result_reason_time_up
import dev.talhabayhan.lexi.feature.game.generated.resources.result_wrong
import org.jetbrains.compose.resources.stringResource

/**
 * `20-duel-result`: header (level · DÜELLO · reason), the winner headline (or the draw), the score line and why, one
 * card per player (the winner's yellow with the KAZANAN badge) with the counts, the clock left and the outcome strip of
 * their wheel, TEKRAR OYNA and "Ana menüye dön" at the bottom. Scrolls when it does not fit.
 */
@Composable
internal fun DuelResultScreen(
    outcome: DuelOutcome,
    onPlayAgain: () -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ui = DuelResultUi.from(outcome)
    val colors = LexiTheme.colors
    val dimens = LexiTheme.dimens
    val first = ui.players[0]
    val second = ui.players[1]
    val names = ui.players.mapIndexed { index, player -> duelPlayerName(player.name, index) }
    val name = { index: Int -> names[index] }
    Box(modifier.fillMaxSize().background(colors.cream)) {
        Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))) {
            DecorShape(
                kind = DecorShapeKind.Circle,
                color = colors.yellow,
                border = BorderStroke(LexiTheme.borders.regular, colors.ink),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = DuelDefaults.ResultCircleEndBleed, y = -DuelDefaults.ResultCircleTopBleed)
                    .size(DuelDefaults.ResultCircleSize),
            )
        }
        BoxWithConstraints(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).lexiContentWidth()) {
            val screenHeight = maxHeight
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = screenHeight)
                    .padding(
                        start = dimens.screenPaddingHorizontal,
                        top = dimens.screenPaddingTop,
                        end = dimens.screenPaddingHorizontal,
                        bottom = dimens.screenPaddingBottom,
                    ),
                verticalArrangement = Arrangement.spacedBy(dimens.gap16),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(dimens.gap2)) {
                    Text(
                        text = stringResource(
                            Res.string.result_header,
                            ui.level.name,
                            stringResource(Res.string.result_mode_duel),
                            stringResource(ui.end.reasonRes()),
                        ),
                        style = LexiTheme.typography.label13,
                    )
                    val winner = ui.winner
                    Text(
                        text = if (winner != null) {
                            stringResource(Res.string.duel_result_winner, name(winner))
                        } else {
                            stringResource(Res.string.duel_result_draw)
                        },
                        style = LexiTheme.typography.display46,
                        modifier = Modifier.lexiHeading(),
                    )
                    Text(
                        text = stringResource(Res.string.duel_result_score, first.correct, second.correct),
                        style = LexiTheme.typography.body15SemiBold,
                        modifier = Modifier.padding(top = DuelDefaults.ResultScoreTop),
                    )
                    val explanation = when (ui.explanation) {
                        DuelExplanation.None -> null

                        DuelExplanation.TiebreakWrong -> stringResource(Res.string.duel_result_tiebreak_wrong)

                        DuelExplanation.TiebreakTime -> stringResource(Res.string.duel_result_tiebreak_time)

                        DuelExplanation.Forfeit ->
                            stringResource(Res.string.duel_result_forfeit, name(checkNotNull(ui.forfeitedPlayer)))
                    }
                    if (explanation != null) {
                        Text(
                            text = explanation,
                            style = LexiTheme.typography.caption14,
                            modifier = Modifier.padding(top = DuelDefaults.ResultExplanationTop),
                        )
                    }
                }
                ui.players.forEachIndexed { index, player ->
                    PlayerResultCard(index = index, name = name(index), player = player)
                }
                Spacer(Modifier.weight(1f))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(dimens.gap12),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    LexiButton(
                        text = stringResource(Res.string.result_play_again),
                        onClick = onPlayAgain,
                        textStyle = LexiTheme.typography.button24,
                        height = DuelDefaults.ResultPlayAgainHeight,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    LexiTextLink(
                        text = stringResource(Res.string.result_home),
                        onClick = onHome,
                        textStyle = LexiTheme.typography.link16,
                        contentPadding = PaddingValues(DuelDefaults.ResultHomeLinkPadding),
                        minHeight = Dp.Unspecified,
                    )
                }
            }
        }
    }
}

private fun DuelEnd.reasonRes() = when (this) {
    DuelEnd.ALL_ANSWERED -> Res.string.result_reason_all_answered
    DuelEnd.TIME_UP -> Res.string.result_reason_time_up
    DuelEnd.FORFEIT -> Res.string.result_reason_forfeit
}

/**
 * A player card: marker (player 1 red square, 2 blue circle), name (heading), KAZANAN for the winner, the counts, the
 * clock left and the strip. The counts and strip are one readable summary; the strip alone is decoration.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlayerResultCard(index: Int, name: String, player: DuelResultPlayerUi) {
    val colors = LexiTheme.colors
    val dimens = LexiTheme.dimens
    val shadow = if (player.isWinner) LexiTheme.shadows.s6 else LexiTheme.shadows.s4
    val correctLabel = stringResource(Res.string.result_correct)
    val wrongLabel = stringResource(Res.string.result_wrong)
    val passedLabel = stringResource(Res.string.result_passed)
    val timeLabel = stringResource(Res.string.duel_result_time_left)
    val clock = formatClock(player.remainingSeconds)
    val summary = "${player.correct} $correctLabel, ${player.wrong} $wrongLabel, ${player.passed} $passedLabel, " +
        "$timeLabel $clock"
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .lexiSurface(
                if (player.isWinner) colors.yellow else colors.white,
                BorderStroke(LexiTheme.borders.regular, colors.ink),
                shadow,
            )
            .padding(
                horizontal = DuelDefaults.ResultCardPaddingHorizontal,
                vertical = DuelDefaults.ResultCardPaddingVertical,
            ),
        verticalArrangement = Arrangement.spacedBy(dimens.gap10),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimens.gap10),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DecorShape(
                kind = if (index == 0) DecorShapeKind.Square else DecorShapeKind.Circle,
                color = if (index == 0) colors.red else colors.blue,
                border = BorderStroke(LexiTheme.borders.thin, colors.ink),
                modifier = Modifier.size(DuelDefaults.ResultMarkerSize),
            )
            Text(
                name,
                style = LexiTheme.typography.heading22Plain,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).lexiHeading(),
            )
            if (player.isWinner) {
                Text(
                    stringResource(Res.string.duel_result_winner_badge),
                    style = LexiTheme.typography.label12ExtraBold,
                    color = colors.yellow,
                    modifier = Modifier
                        .background(colors.ink)
                        .padding(
                            horizontal = DuelDefaults.ResultBadgePaddingHorizontal,
                            vertical = DuelDefaults.ResultBadgePaddingVertical,
                        ),
                )
            }
        }
        Column(
            modifier = Modifier.clearAndSetSemantics { contentDescription = summary },
            verticalArrangement = Arrangement.spacedBy(dimens.gap10),
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DuelDefaults.ResultStatsGap),
                verticalArrangement = Arrangement.spacedBy(dimens.gap8),
                itemVerticalAlignment = Alignment.Bottom,
            ) {
                Stat(player.correct.toString(), correctLabel, hero = true)
                Stat(player.wrong.toString(), wrongLabel)
                Stat(player.passed.toString(), passedLabel)
                Stat(clock, timeLabel, modifier = Modifier.weight(1f), end = true)
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(DuelDefaults.ResultStripGap)) {
                player.slots.forEach { SlotShape(it, onYellow = player.isWinner) }
            }
        }
    }
}

@Composable
private fun Stat(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    hero: Boolean = false,
    end: Boolean = false,
) {
    Column(modifier, horizontalAlignment = if (end) Alignment.End else Alignment.Start) {
        Text(value, style = if (hero) LexiTheme.typography.statHero56 else LexiTheme.typography.statValue28)
        Text(label, style = LexiTheme.typography.caption13SemiBold)
    }
}

/** One 10dp shape of the strip: correct blue circle, wrong red square, passed leaf (white on the yellow card). */
@Composable
private fun SlotShape(slot: DuelSlotOutcome, onYellow: Boolean) {
    val colors = LexiTheme.colors
    val (kind, color) = when (slot) {
        DuelSlotOutcome.CORRECT -> DecorShapeKind.Circle to colors.blue
        DuelSlotOutcome.WRONG -> DecorShapeKind.Square to colors.red
        DuelSlotOutcome.PASSED -> DecorShapeKind.Leaf to if (onYellow) colors.white else colors.yellow
        DuelSlotOutcome.OPEN -> DecorShapeKind.Circle to colors.white
    }
    DecorShape(
        kind = kind,
        color = color,
        border = BorderStroke(DuelDefaults.ResultStripBorder, colors.ink),
        modifier = Modifier.size(DuelDefaults.ResultStripSize),
    )
}

// design sample, not user-facing: 20-duel-result
internal object DuelResultSamples {
    private fun slots(codes: String) = DuelSlotOutcome.parse(codes)

    /** Equal correct counts, fewer wrong wins (the design). */
    val tieBreakWrong = DuelOutcome(
        level = Level.A1,
        clockSeconds = 300,
        end = DuelEnd.ALL_ANSWERED,
        winner = 0,
        players = listOf(
            DuelPlayerOutcome("", 18, 4, 4, 12_000L, slots("wcccccccpcccpwcpccccwpwccc")),
            DuelPlayerOutcome("", 18, 6, 2, 0L, slots("ccwcpwcccwcccpcccccccccwccw".take(26))),
        ),
    )

    val draw = tieBreakWrong.copy(
        winner = null,
        players = tieBreakWrong.players.map { it.copy(wrong = 4, passed = 4) },
    )

    val forfeit = tieBreakWrong.copy(
        end = DuelEnd.FORFEIT,
        winner = 1,
        players = listOf(
            DuelPlayerOutcome("Ayşe", 3, 1, 0, 90_000L, slots("cccwoooooooooooooooooooooo")),
            DuelPlayerOutcome("Mehmetcan Y.", 9, 2, 1, 40_000L, slots("cccccccccwwpoooooooooooooo")),
        ),
    )
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun DuelResultScreenPreview() {
    LexiTheme { DuelResultScreen(DuelResultSamples.tieBreakWrong, onPlayAgain = {}, onHome = {}) }
}
