package dev.talhabayhan.lexi.feature.game.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dev.talhabayhan.lexi.core.designsystem.component.BottomSheet
import dev.talhabayhan.lexi.core.designsystem.component.DecorShapeKind
import dev.talhabayhan.lexi.core.designsystem.component.JokerRow
import dev.talhabayhan.lexi.core.designsystem.component.JokerRowDefaults
import dev.talhabayhan.lexi.core.designsystem.component.LexiButton
import dev.talhabayhan.lexi.core.designsystem.component.LexiButtonDefaults
import dev.talhabayhan.lexi.core.designsystem.component.LexiButtonVariant
import dev.talhabayhan.lexi.core.designsystem.component.StatusPill
import dev.talhabayhan.lexi.core.designsystem.format.formatClock
import dev.talhabayhan.lexi.core.designsystem.icon.LexiIcons
import dev.talhabayhan.lexi.core.designsystem.theme.LexiColors
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.game.JokerType
import dev.talhabayhan.lexi.feature.game.GameClockUi
import dev.talhabayhan.lexi.feature.game.GameIntent
import dev.talhabayhan.lexi.feature.game.JokerCostUi
import dev.talhabayhan.lexi.feature.game.JokerUi
import dev.talhabayhan.lexi.feature.game.generated.resources.Res
import dev.talhabayhan.lexi.feature.game.generated.resources.game_joker_cost_free
import dev.talhabayhan.lexi.feature.game.generated.resources.game_joker_cost_points
import dev.talhabayhan.lexi.feature.game.generated.resources.game_joker_cost_seconds
import dev.talhabayhan.lexi.feature.game.generated.resources.game_joker_count
import dev.talhabayhan.lexi.feature.game.generated.resources.game_joker_count_none
import dev.talhabayhan.lexi.feature.game.generated.resources.game_joker_example
import dev.talhabayhan.lexi.feature.game.generated.resources.game_joker_example_text
import dev.talhabayhan.lexi.feature.game.generated.resources.game_joker_listen
import dev.talhabayhan.lexi.feature.game.generated.resources.game_joker_listen_text
import dev.talhabayhan.lexi.feature.game.generated.resources.game_joker_remove
import dev.talhabayhan.lexi.feature.game.generated.resources.game_joker_remove_text
import dev.talhabayhan.lexi.feature.game.generated.resources.game_joker_reveal
import dev.talhabayhan.lexi.feature.game.generated.resources.game_joker_reveal_text
import dev.talhabayhan.lexi.feature.game.generated.resources.game_jokers_ad
import dev.talhabayhan.lexi.feature.game.generated.resources.game_jokers_clock_running
import dev.talhabayhan.lexi.feature.game.generated.resources.game_jokers_close
import dev.talhabayhan.lexi.feature.game.generated.resources.game_jokers_title
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * The joker sheet (`05-joker-sheet`): bottom sheet (motion.md #10) with the four jokers and the ad button, plus the
 * "Süre işlemeye devam ediyor · 1:31" pill at the top, because the clock keeps running (`GAME_RULES.md` §5; untimed
 * modes show no pill). [adReward] is `GameConfig.rewardedAdJokerReward`; null hides the ad button (feature flag). Fills its parent; renders nothing while
 * not [visible].
 */
@Composable
internal fun JokerSheet(
    visible: Boolean,
    jokers: ImmutableList<JokerUi>,
    adReward: Int?,
    clock: () -> GameClockUi,
    onIntent: (GameIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val motion = LexiTheme.motion
    val dimens = LexiTheme.dimens
    Box(modifier.fillMaxSize()) {
        BottomSheet(
            visible = visible,
            onDismissRequest = { onIntent(GameIntent.CloseJokers) },
            title = stringResource(Res.string.game_jokers_title),
            closeContentDescription = stringResource(Res.string.game_jokers_close),
        ) {
            jokers.forEach { joker -> JokerOption(joker, onClick = { onIntent(GameIntent.UseJoker(joker.type)) }) }
            if (adReward != null) {
                LexiButton(
                    text = stringResource(Res.string.game_jokers_ad, adReward),
                    // TODO(M7): rewarded ad with UMP consent (`ROADMAP.md` M7); behind `FeatureFlags.rewardedAdJokers`.
                    onClick = {},
                    variant = LexiButtonVariant.Ink,
                    shadow = null,
                    leadingIcon = LexiIcons.Play,
                    height = LexiButtonDefaults.LargeHeight,
                    modifier = Modifier.fillMaxWidth().padding(top = JokerRowDefaults.AdButtonTopMargin),
                )
            }
        }
        val timed by remember(clock) { derivedStateOf { clock().isTimed } }
        val fade = tween<Float>(motion.bottomSheetDurationMillis, easing = motion.mechanical)
        AnimatedVisibility(
            visible = visible && timed,
            enter = fadeIn(fade),
            exit = fadeOut(fade),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                )
                .padding(
                    start = dimens.screenPaddingHorizontal,
                    top = dimens.screenPaddingTop,
                    end = dimens.screenPaddingHorizontal,
                ),
        ) {
            ClockPill(clock)
        }
    }
}

/** The pill reads the clock through derived state: it recomposes once per second. */
@Composable
private fun ClockPill(clock: () -> GameClockUi) {
    val seconds by remember(clock) { derivedStateOf { clock().wholeSeconds } }
    StatusPill(stringResource(Res.string.game_jokers_clock_running, formatClock(seconds)))
}

@Composable
private fun JokerOption(joker: JokerUi, onClick: () -> Unit) {
    val colors = LexiTheme.colors
    val look = joker.type.look()
    JokerRow(
        count = joker.count.toString(),
        countDescription = if (joker.count > 0) {
            pluralStringResource(Res.plurals.game_joker_count, joker.count, joker.count)
        } else {
            stringResource(Res.string.game_joker_count_none)
        },
        title = stringResource(look.title),
        description = stringResource(look.text),
        cost = when (val cost = joker.cost) {
            is JokerCostUi.Seconds -> stringResource(Res.string.game_joker_cost_seconds, cost.seconds)
            is JokerCostUi.Points -> stringResource(Res.string.game_joker_cost_points, cost.points)
            JokerCostUi.Free -> stringResource(Res.string.game_joker_cost_free)
        },
        marker = look.marker,
        markerColor = look.color(colors),
        markerContentColor = if (look.inkText) colors.ink else colors.white,
        onClick = onClick,
        enabled = joker.enabled,
    )
}

/** Count chip and copy per joker (`source/05-joker-sheet.dc.html`). */
private class JokerLook(
    val title: StringResource,
    val text: StringResource,
    val marker: DecorShapeKind,
    val color: (LexiColors) -> Color,
    val inkText: Boolean,
)

private fun JokerType.look(): JokerLook = when (this) {
    JokerType.REVEAL_LETTER -> JokerLook(
        title = Res.string.game_joker_reveal,
        text = Res.string.game_joker_reveal_text,
        marker = DecorShapeKind.Circle,
        color = { it.blue },
        inkText = false,
    )

    JokerType.REMOVE_DISTRACTORS -> JokerLook(
        title = Res.string.game_joker_remove,
        text = Res.string.game_joker_remove_text,
        marker = DecorShapeKind.Square,
        color = { it.red },
        inkText = false,
    )

    JokerType.EXAMPLE -> JokerLook(
        title = Res.string.game_joker_example,
        text = Res.string.game_joker_example_text,
        marker = DecorShapeKind.Leaf,
        color = { it.yellow },
        inkText = true,
    )

    JokerType.LISTEN -> JokerLook(
        title = Res.string.game_joker_listen,
        text = Res.string.game_joker_listen_text,
        marker = DecorShapeKind.Square,
        color = { it.white },
        inkText = true,
    )
}
