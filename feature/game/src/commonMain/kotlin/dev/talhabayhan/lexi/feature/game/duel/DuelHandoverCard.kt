package dev.talhabayhan.lexi.feature.game.duel

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import dev.talhabayhan.lexi.core.designsystem.component.DecorShape
import dev.talhabayhan.lexi.core.designsystem.component.DecorShapeKind
import dev.talhabayhan.lexi.core.designsystem.component.DuelDefaults
import dev.talhabayhan.lexi.core.designsystem.component.LiveAnnouncement
import dev.talhabayhan.lexi.core.designsystem.component.PressableSurface
import dev.talhabayhan.lexi.core.designsystem.format.formatClock
import dev.talhabayhan.lexi.core.designsystem.modifier.lexiHeading
import dev.talhabayhan.lexi.core.designsystem.modifier.lexiSurface
import dev.talhabayhan.lexi.core.designsystem.theme.LexiShadow
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTextFit
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.lexiContentWidth
import dev.talhabayhan.lexi.feature.game.GameIntent
import dev.talhabayhan.lexi.feature.game.components.PauseDialog
import dev.talhabayhan.lexi.feature.game.generated.resources.Res
import dev.talhabayhan.lexi.feature.game.generated.resources.duel_announce_turn
import dev.talhabayhan.lexi.feature.game.generated.resources.duel_handover_body
import dev.talhabayhan.lexi.feature.game.generated.resources.duel_handover_eyebrow
import dev.talhabayhan.lexi.feature.game.generated.resources.duel_handover_letters
import dev.talhabayhan.lexi.feature.game.generated.resources.duel_handover_previous
import dev.talhabayhan.lexi.feature.game.generated.resources.duel_handover_ready
import dev.talhabayhan.lexi.feature.game.generated.resources.duel_handover_time
import dev.talhabayhan.lexi.feature.game.generated.resources.duel_handover_turn
import dev.talhabayhan.lexi.feature.game.generated.resources.game_announce_join
import dev.talhabayhan.lexi.feature.game.generated.resources.game_announce_time_up
import dev.talhabayhan.lexi.feature.game.generated.resources.game_pause_quit
import org.jetbrains.compose.resources.stringResource

/**
 * `18-duel-handover`: the full-screen card between turns, shown instead of the board (the next player must not see
 * it). Ink background, the next player's name, the clock and letters they have left, what the previous player did, and
 * HAZIRIM (the clock only starts when it is tapped). "PES ET" asks first: [DuelHandoverUi.isQuitConfirmation] puts the
 * quit confirmation of the pause menu over a scrim; the card is then hidden from accessibility. [names] are the names
 * as typed ("" = default). A long name shrinks to stay on one line.
 */
@Composable
internal fun DuelHandoverCard(
    handover: DuelHandoverUi,
    names: List<String>,
    onIntent: (DuelIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LexiTheme.colors
    val dimens = LexiTheme.dimens
    val name = duelPlayerName(names.getOrElse(handover.player) { "" }, handover.player)
    val previous = handover.previous
    val previousName = previous?.let { duelPlayerName(names.getOrElse(it.player) { "" }, it.player) }
    val turn = stringResource(Res.string.duel_announce_turn, name)
    val announcement = if (handover.previousTimeUp) {
        stringResource(Res.string.game_announce_join, stringResource(Res.string.game_announce_time_up), turn)
    } else {
        turn
    }
    Box(modifier.fillMaxSize().background(colors.ink)) {
        DecorShape(
            kind = DecorShapeKind.Circle,
            color = colors.blue,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = DuelDefaults.HandoverCircleEndBleed, y = -DuelDefaults.HandoverCircleTopBleed)
                .size(DuelDefaults.HandoverCircleSize),
        )
        DecorShape(
            kind = DecorShapeKind.HalfPill,
            color = colors.red,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = -DuelDefaults.HandoverPillBleed, y = -DuelDefaults.HandoverPillBottom)
                .size(DuelDefaults.HandoverPillWidth, DuelDefaults.HandoverPillHeight),
        )
        BoxWithConstraints(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .lexiContentWidth()
                .then(if (handover.isQuitConfirmation) Modifier.clearAndSetSemantics {} else Modifier),
        ) {
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
                Row(
                    modifier = Modifier.padding(top = DuelDefaults.HandoverEyebrowTop),
                    horizontalArrangement = Arrangement.spacedBy(dimens.gap8),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DecorShape(
                        DecorShapeKind.Square,
                        colors.yellow,
                        Modifier.size(DuelDefaults.HandoverEyebrowMarkerSize),
                    )
                    Text(
                        stringResource(Res.string.duel_handover_eyebrow),
                        style = LexiTheme.typography.label13,
                        color = colors.cream,
                    )
                }
                Column(
                    modifier = Modifier.padding(top = DuelDefaults.HandoverTurnTop),
                    verticalArrangement = Arrangement.spacedBy(dimens.gap6),
                ) {
                    Text(
                        stringResource(Res.string.duel_handover_turn),
                        style = LexiTheme.typography.label15,
                        color = colors.yellow,
                    )
                    val nameStyle = LexiTheme.typography.display64
                    BasicText(
                        text = name,
                        style = nameStyle,
                        color = { colors.white },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        autoSize = TextAutoSize.StepBased(
                            minFontSize = nameStyle.fontSize * DuelDefaults.HANDOVER_NAME_MIN_SCALE,
                            maxFontSize = nameStyle.fontSize,
                            stepSize = LexiTextFit.LabelStep,
                        ),
                        modifier = Modifier.lexiHeading(),
                    )
                    Text(
                        stringResource(Res.string.duel_handover_body),
                        style = LexiTheme.typography.body16,
                        color = colors.cream,
                    )
                }
                Row(
                    modifier = Modifier.padding(top = DuelDefaults.HandoverStatsTop),
                    horizontalArrangement = Arrangement.spacedBy(dimens.gap10),
                ) {
                    StatBox(
                        label = stringResource(Res.string.duel_handover_time),
                        value = formatClock(wholeSecondsUp(handover.remainingMs)),
                        modifier = Modifier.weight(1f),
                    )
                    StatBox(
                        label = stringResource(Res.string.duel_handover_letters),
                        value = handover.lettersLeft.toString(),
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.weight(1f))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(dimens.gap14),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (previous != null && previousName != null) {
                        Text(
                            stringResource(
                                Res.string.duel_handover_previous,
                                previousName,
                                previous.correct,
                                previous.wrong,
                            ),
                            style = LexiTheme.typography.body14,
                            color = colors.cream,
                            textAlign = TextAlign.Center,
                        )
                    }
                    ReadyButton(onClick = { onIntent(DuelIntent.Ready) })
                    PressableSurface(
                        onClick = { onIntent(DuelIntent.Board(GameIntent.RequestQuit)) },
                        // The design's box is the 19dp line; Compose extends the touch area to the platform minimum.
                        color = Color.Transparent,
                        border = null,
                        shadow = null,
                    ) {
                        Text(
                            stringResource(Res.string.game_pause_quit),
                            style = LexiTheme.typography.button13,
                            color = colors.cream.copy(alpha = DuelDefaults.HANDOVER_QUIT_ALPHA),
                            modifier = Modifier.padding(horizontal = dimens.gap8),
                        )
                    }
                }
            }
        }
        // Outside the hidden subtree: said once per card (the text only changes with the player / previous turn).
        LiveAnnouncement(announcement, Modifier.align(Alignment.TopStart))
        if (handover.isQuitConfirmation) {
            PauseDialog(
                quitConfirmation = true,
                restartConfirmation = false,
                canRestart = false,
                onIntent = { onIntent(DuelIntent.Board(it)) },
            )
        }
    }
}

/** The remaining time as whole seconds, rounded up (a started second still counts). */
private fun wholeSecondsUp(remainingMs: Long): Int =
    ((remainingMs.coerceAtLeast(0L) + MILLIS_PER_SECOND - 1) / MILLIS_PER_SECOND).toInt()

private const val MILLIS_PER_SECOND = 1_000L

@Composable
private fun StatBox(label: String, value: String, modifier: Modifier = Modifier) {
    val colors = LexiTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .lexiSurface(Color.Transparent, BorderStroke(LexiTheme.borders.regular, colors.cream))
            .padding(DuelDefaults.HandoverStatPadding),
        verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap2),
    ) {
        Text(label, style = LexiTheme.typography.label12, color = colors.white)
        Text(value, style = LexiTheme.typography.levelTitle, color = colors.white)
    }
}

@Composable
private fun ReadyButton(onClick: () -> Unit) {
    val colors = LexiTheme.colors
    PressableSurface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().heightIn(min = DuelDefaults.HandoverReadyHeight),
        color = colors.yellow,
        border = BorderStroke(LexiTheme.borders.regular, colors.cream),
        shadow = LexiShadow(LexiTheme.shadows.s6.offset, colors.cream),
        propagateMinConstraints = true,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                stringResource(Res.string.duel_handover_ready),
                style = LexiTheme.typography.button28,
                color = colors.ink,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// design sample, not user-facing
internal val DuelHandoverSample = DuelHandoverUi(
    player = 1,
    remainingMs = 150_000L,
    lettersLeft = 26,
    previous = DuelPreviousUi(player = 0, correct = 6, wrong = 1),
)

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun DuelHandoverCardPreview() {
    LexiTheme { DuelHandoverCard(DuelHandoverSample, names = listOf("", ""), onIntent = {}) }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun DuelHandoverQuitPreview() {
    LexiTheme {
        DuelHandoverCard(
            DuelHandoverSample.copy(isQuitConfirmation = true),
            names = listOf("", ""),
            onIntent = {},
        )
    }
}
