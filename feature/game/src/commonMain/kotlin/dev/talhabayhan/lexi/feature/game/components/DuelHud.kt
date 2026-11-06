package dev.talhabayhan.lexi.feature.game.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import dev.talhabayhan.lexi.core.designsystem.component.DecorShape
import dev.talhabayhan.lexi.core.designsystem.component.DecorShapeKind
import dev.talhabayhan.lexi.core.designsystem.component.DuelHudDefaults
import dev.talhabayhan.lexi.core.designsystem.component.GameHudDefaults
import dev.talhabayhan.lexi.core.designsystem.component.IconButtonDefaults
import dev.talhabayhan.lexi.core.designsystem.component.NavSquareButton
import dev.talhabayhan.lexi.core.designsystem.format.formatClock
import dev.talhabayhan.lexi.core.designsystem.icon.LexiIcons
import dev.talhabayhan.lexi.core.designsystem.modifier.lexiSurface
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.feature.game.GameClockUi
import dev.talhabayhan.lexi.feature.game.duel.DuelPlayerUi
import dev.talhabayhan.lexi.feature.game.duel.duelPlayerName
import dev.talhabayhan.lexi.feature.game.generated.resources.Res
import dev.talhabayhan.lexi.feature.game.generated.resources.duel_game_player_active
import dev.talhabayhan.lexi.feature.game.generated.resources.duel_game_player_description
import dev.talhabayhan.lexi.feature.game.generated.resources.duel_game_turn
import dev.talhabayhan.lexi.feature.game.generated.resources.game_announce_join
import dev.talhabayhan.lexi.feature.game.generated.resources.game_button_pause
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource

/** The duel's HUD content: both players and whose turn it is ([activePlayer], 0-based). */
@Immutable
internal data class DuelHudUi(val players: ImmutableList<DuelPlayerUi>, val activePlayer: Int)

/**
 * `19-duel-game`'s top bar in place of the solo timer and score badge: pause (48 × [height]) and one card per player.
 * The active card is yellow with an s3 shadow and "SIRA", the other white with muted text; each shows the marker
 * (player 1 red square, player 2 blue circle), the name, the correct answers and the player's own clock. Only the active
 * clock runs: it reads [clock] in derived state (once per second); the other shows its stopped time. Each card is read
 * as one sentence (name, correct, time left, and whose turn it is), never announced by itself.
 */
@Composable
internal fun DuelHudBar(
    hud: DuelHudUi,
    clock: () -> GameClockUi,
    onPause: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = DuelHudDefaults.Height,
) {
    val activeSeconds by remember(clock) { derivedStateOf { clock().wholeSeconds } }
    Row(modifier.height(height), horizontalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap8)) {
        NavSquareButton(
            icon = LexiIcons.Pause,
            contentDescription = stringResource(Res.string.game_button_pause),
            onClick = onPause,
            size = GameHudDefaults.BarHeight,
            height = height,
            iconSize = IconButtonDefaults.IconSizeSmall,
            containerColor = LexiTheme.colors.cream,
        )
        hud.players.forEachIndexed { index, player ->
            val active = index == hud.activePlayer
            DuelPlayerCard(
                index = index,
                player = player,
                active = active,
                seconds = if (active) activeSeconds else wholeSeconds(player.remainingMs),
                compact = height < DuelHudDefaults.Height,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }
    }
}

@Composable
private fun DuelPlayerCard(
    index: Int,
    player: DuelPlayerUi,
    active: Boolean,
    seconds: Int,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = LexiTheme.colors
    val typography = LexiTheme.typography
    val content = if (active) colors.ink else colors.inkMuted
    val name = duelPlayerName(player.name, index)
    val description = stringResource(
        Res.string.duel_game_player_description,
        name,
        player.correct,
        seconds / SECONDS_PER_MINUTE,
        seconds % SECONDS_PER_MINUTE,
    ).let {
        val turn = stringResource(Res.string.duel_game_player_active)
        if (active) stringResource(Res.string.game_announce_join, it, turn) else it
    }
    Column(
        modifier = modifier
            .lexiSurface(
                color = if (active) colors.yellow else colors.white,
                border = BorderStroke(LexiTheme.borders.regular, colors.ink),
                shadow = if (active) LexiTheme.shadows.s3 else null,
            )
            .padding(
                horizontal = DuelHudDefaults.CardPaddingHorizontal,
                vertical = if (compact) {
                    DuelHudDefaults.CompactCardPaddingVertical
                } else {
                    DuelHudDefaults.CardPaddingVertical
                },
            )
            .clearAndSetSemantics { contentDescription = description },
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap6),
        ) {
            DecorShape(
                kind = if (index == 0) DecorShapeKind.Square else DecorShapeKind.Circle,
                color = if (index == 0) colors.red else colors.blue,
                border = BorderStroke(DuelHudDefaults.MarkerBorder, colors.ink),
                modifier = Modifier.size(DuelHudDefaults.MarkerSize),
            )
            Text(
                name,
                style = typography.bodyStrong14,
                color = content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(player.correct.toString(), style = typography.bodyStrong15, color = content, maxLines = 1)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                formatClock(seconds),
                style = typography.heading20,
                color = content,
                maxLines = 1,
                modifier = Modifier.weight(1f),
            )
            if (active) {
                Text(
                    stringResource(Res.string.duel_game_turn),
                    style = typography.label12.copy(fontSize = DuelHudDefaults.TurnLabelSize),
                    color = content,
                    maxLines = 1,
                )
            }
        }
    }
}

/** Whole seconds rounded up, like the game clock ([GameClockUi.wholeSeconds]). */
private fun wholeSeconds(remainingMs: Long): Int =
    ((remainingMs.coerceAtLeast(0L) + MS_PER_SECOND - 1) / MS_PER_SECOND).toInt()

private const val SECONDS_PER_MINUTE = 60
private const val MS_PER_SECOND = 1_000L
