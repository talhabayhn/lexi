package dev.talhabayhan.lexi.feature.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import dev.talhabayhan.lexi.core.designsystem.component.ActionCircleButton
import dev.talhabayhan.lexi.core.designsystem.component.ClueCard
import dev.talhabayhan.lexi.core.designsystem.component.ConfirmDialog
import dev.talhabayhan.lexi.core.designsystem.component.DecorShape
import dev.talhabayhan.lexi.core.designsystem.component.DecorShapeKind
import dev.talhabayhan.lexi.core.designsystem.component.GameScreenDefaults
import dev.talhabayhan.lexi.core.designsystem.component.IconSquareButton
import dev.talhabayhan.lexi.core.designsystem.component.LetterWheel
import dev.talhabayhan.lexi.core.designsystem.component.LetterWheelDefaults
import dev.talhabayhan.lexi.core.designsystem.component.LexiButton
import dev.talhabayhan.lexi.core.designsystem.component.LexiButtonDefaults
import dev.talhabayhan.lexi.core.designsystem.component.LexiButtonVariant
import dev.talhabayhan.lexi.core.designsystem.component.ModalScrim
import dev.talhabayhan.lexi.core.designsystem.icon.LexiIcons
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.model.LetterRule
import dev.talhabayhan.lexi.core.domain.model.PartOfSpeech
import dev.talhabayhan.lexi.feature.game.components.AnswerSlots
import dev.talhabayhan.lexi.feature.game.components.GameColumn
import dev.talhabayhan.lexi.feature.game.components.GameHudBar
import dev.talhabayhan.lexi.feature.game.components.GameMotion
import dev.talhabayhan.lexi.feature.game.components.KeyGrid
import dev.talhabayhan.lexi.feature.game.components.rememberGameMotion
import dev.talhabayhan.lexi.feature.game.generated.resources.Res
import dev.talhabayhan.lexi.feature.game.generated.resources.game_button_backspace
import dev.talhabayhan.lexi.feature.game.generated.resources.game_button_joker
import dev.talhabayhan.lexi.feature.game.generated.resources.game_button_pass
import dev.talhabayhan.lexi.feature.game.generated.resources.game_clue_label
import dev.talhabayhan.lexi.feature.game.generated.resources.game_letter_count
import dev.talhabayhan.lexi.feature.game.generated.resources.game_pause_message
import dev.talhabayhan.lexi.feature.game.generated.resources.game_pause_quit
import dev.talhabayhan.lexi.feature.game.generated.resources.game_pause_resume
import dev.talhabayhan.lexi.feature.game.generated.resources.game_pause_title
import dev.talhabayhan.lexi.feature.game.generated.resources.game_pos_adj
import dev.talhabayhan.lexi.feature.game.generated.resources.game_pos_adv
import dev.talhabayhan.lexi.feature.game.generated.resources.game_pos_idiom
import dev.talhabayhan.lexi.feature.game.generated.resources.game_pos_noun
import dev.talhabayhan.lexi.feature.game.generated.resources.game_pos_phrasal
import dev.talhabayhan.lexi.feature.game.generated.resources.game_pos_phrase
import dev.talhabayhan.lexi.feature.game.generated.resources.game_pos_verb
import dev.talhabayhan.lexi.feature.game.generated.resources.game_rule_contains
import dev.talhabayhan.lexi.feature.game.generated.resources.game_rule_starts
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * The game screen (`04-game`), stateless: the resting look comes from [state]; [clock] is read only by the HUD timer;
 * [motion] plays transient effects. Lifecycle ON_PAUSE / ON_RESUME are forwarded as background / foreground intents
 * (ADR 0019). Insets: `safeDrawing` on top of the design paddings; the background shapes reach the screen edges.
 */
@Composable
internal fun GameScreen(
    state: GameUiState,
    clock: () -> GameClockUi,
    onIntent: (GameIntent) -> Unit,
    modifier: Modifier = Modifier,
    motion: GameMotion = rememberGameMotion(LexiTheme.motion),
) {
    val currentOnIntent by rememberUpdatedState(onIntent)
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { currentOnIntent(GameIntent.Background) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { currentOnIntent(GameIntent.Foreground) }

    val dimens = LexiTheme.dimens
    BoxWithConstraints(modifier.fillMaxSize().background(LexiTheme.colors.cream)) {
        GameDecor()
        if (!state.isLoading) {
            val short = maxHeight < GameScreenDefaults.ShortScreenHeight
            val gapScale = if (short) GameScreenDefaults.SHORT_SCREEN_GAP_SCALE else 1f
            val turn = state.turn
            GameColumn(
                gap = dimens.gap16 * gapScale,
                answerTopMargin = dimens.gap4 * gapScale,
                maxWheelSize = if (short) GameScreenDefaults.ShortScreenWheelSize else LetterWheelDefaults.Size,
                hud = { GameHudBar(state.score, clock, onPause = { onIntent(GameIntent.Pause) }) },
                wheel = { size ->
                    LetterWheel(
                        letters = state.wheel,
                        centerLetter = turn?.letter ?: ' ',
                        ruleLabel = turn?.let { stringResource(it.rule.label()) }.orEmpty(),
                        size = size,
                    )
                },
                clue = {
                    if (turn != null) {
                        ClueCard(
                            label = stringResource(
                                Res.string.game_clue_label,
                                stringResource(turn.partOfSpeech.label()),
                            ),
                            clue = turn.clue,
                            level = turn.level.name,
                            letterCount = pluralStringResource(
                                Res.plurals.game_letter_count,
                                turn.letterCount,
                                turn.letterCount,
                            ),
                        )
                    }
                },
                answer = { if (turn != null) AnswerSlots(turn.words, offset = { motion.slotsOffset }) },
                keys = { if (turn != null) KeyGrid(turn.keys, onKey = { onIntent(GameIntent.KeyTapped(it)) }) },
                actions = { GameActions(onIntent) },
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(
                        start = dimens.screenPaddingHorizontal,
                        top = dimens.screenPaddingTopGame,
                        end = dimens.screenPaddingHorizontal,
                        bottom = dimens.screenPaddingBottom,
                    ),
            )
        }
        if (state.isPaused) PauseOverlay(onIntent)
    }
}

/** Red circle (top right) and blue half pill (left), behind the content; they move with the top inset. */
@Composable
private fun GameDecor() {
    val colors = LexiTheme.colors
    Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))) {
        DecorShape(
            kind = DecorShapeKind.Circle,
            color = colors.red,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = GameScreenDefaults.DecorCircleEndOverhang, y = GameScreenDefaults.DecorCircleTop)
                .size(GameScreenDefaults.DecorCircleSize),
        )
        DecorShape(
            kind = DecorShapeKind.HalfPill,
            color = colors.blue,
            modifier = Modifier
                .offset(x = -GameScreenDefaults.DecorPillStartOverhang, y = GameScreenDefaults.DecorPillTop)
                .size(GameScreenDefaults.DecorPillWidth, GameScreenDefaults.DecorPillHeight),
        )
    }
}

/** Joker (red circle), backspace (square) and PAS, gap 10. */
@Composable
private fun GameActions(onIntent: (GameIntent) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap10),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Joker sheet (05-joker-sheet) is a later M4 task; the intent is the hook.
        ActionCircleButton(
            icon = LexiIcons.Bulb,
            contentDescription = stringResource(Res.string.game_button_joker),
            onClick = { onIntent(GameIntent.OpenJokers) },
        )
        IconSquareButton(
            icon = LexiIcons.Backspace,
            contentDescription = stringResource(Res.string.game_button_backspace),
            onClick = { onIntent(GameIntent.Backspace) },
        )
        LexiButton(
            text = stringResource(Res.string.game_button_pass),
            onClick = { onIntent(GameIntent.Pass) },
            variant = LexiButtonVariant.Pass,
            textStyle = LexiTheme.typography.button22,
            height = LexiButtonDefaults.LargeHeight,
            modifier = Modifier.weight(1f),
        )
    }
}

/**
 * DESIGN-DEBT (`screen-map.md`: pause menu has no design): minimal paused state from `ConfirmDialog` until the pause
 * dialog task (Devam et / Yeniden başla / Ayarlar / Bırak). The scrim does not dismiss: the clock stays stopped until
 * the player continues.
 */
@Composable
private fun PauseOverlay(onIntent: (GameIntent) -> Unit) {
    Box(Modifier.fillMaxSize()) {
        ModalScrim(onDismissRequest = {})
        ConfirmDialog(
            title = stringResource(Res.string.game_pause_title),
            message = stringResource(Res.string.game_pause_message),
            confirmLabel = stringResource(Res.string.game_pause_resume),
            dismissLabel = stringResource(Res.string.game_pause_quit),
            onConfirm = { onIntent(GameIntent.Resume) },
            onDismiss = { onIntent(GameIntent.Forfeit) },
            modifier = Modifier
                .align(Alignment.Center)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = LexiTheme.dimens.screenPaddingHorizontal),
        )
    }
}

private fun LetterRule.label(): StringResource = when (this) {
    LetterRule.STARTS -> Res.string.game_rule_starts
    LetterRule.CONTAINS -> Res.string.game_rule_contains
}

private fun PartOfSpeech.label(): StringResource = when (this) {
    PartOfSpeech.NOUN -> Res.string.game_pos_noun
    PartOfSpeech.VERB -> Res.string.game_pos_verb
    PartOfSpeech.ADJ -> Res.string.game_pos_adj
    PartOfSpeech.ADV -> Res.string.game_pos_adv
    PartOfSpeech.PHRASE -> Res.string.game_pos_phrase
    PartOfSpeech.IDIOM -> Res.string.game_pos_idiom
    PartOfSpeech.PHRASAL -> Res.string.game_pos_phrasal
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun GameScreenPreview() {
    LexiTheme(reducedMotion = false) {
        GameScreen(state = GameSamples.design, clock = { GameSamples.designClock }, onIntent = {})
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun GameScreenWrongRevealPreview() {
    LexiTheme(reducedMotion = true) {
        GameScreen(state = GameSamples.wrongReveal, clock = { GameSamples.warningClock }, onIntent = {})
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun GameScreenPausedPreview() {
    LexiTheme(reducedMotion = false) {
        GameScreen(state = GameSamples.design.copy(isPaused = true), clock = { GameSamples.designClock }, onIntent = {})
    }
}
