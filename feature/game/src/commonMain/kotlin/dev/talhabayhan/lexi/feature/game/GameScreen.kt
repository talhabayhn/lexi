package dev.talhabayhan.lexi.feature.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.min
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import dev.talhabayhan.lexi.core.designsystem.component.ActionCircleButton
import dev.talhabayhan.lexi.core.designsystem.component.ClueCard
import dev.talhabayhan.lexi.core.designsystem.component.DecorShape
import dev.talhabayhan.lexi.core.designsystem.component.DecorShapeKind
import dev.talhabayhan.lexi.core.designsystem.component.EmptyState
import dev.talhabayhan.lexi.core.designsystem.component.GameScreenDefaults
import dev.talhabayhan.lexi.core.designsystem.component.IconSquareButton
import dev.talhabayhan.lexi.core.designsystem.component.LetterWheel
import dev.talhabayhan.lexi.core.designsystem.component.LexiButton
import dev.talhabayhan.lexi.core.designsystem.component.LexiButtonVariant
import dev.talhabayhan.lexi.core.designsystem.component.rememberClueCardMetrics
import dev.talhabayhan.lexi.core.designsystem.icon.LexiIcons
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.ViewportDefaults
import dev.talhabayhan.lexi.core.designsystem.theme.lexiContentWidth
import dev.talhabayhan.lexi.core.domain.model.LetterRule
import dev.talhabayhan.lexi.core.domain.model.PartOfSpeech
import dev.talhabayhan.lexi.feature.game.components.AnswerSlots
import dev.talhabayhan.lexi.feature.game.components.GameColumn
import dev.talhabayhan.lexi.feature.game.components.GameHudBar
import dev.talhabayhan.lexi.feature.game.components.GameMotion
import dev.talhabayhan.lexi.feature.game.components.GameMotionLayer
import dev.talhabayhan.lexi.feature.game.components.JokerSheet
import dev.talhabayhan.lexi.feature.game.components.KeyGrid
import dev.talhabayhan.lexi.feature.game.components.PauseDialog
import dev.talhabayhan.lexi.feature.game.components.gameLayout
import dev.talhabayhan.lexi.feature.game.components.rememberGameMotion
import dev.talhabayhan.lexi.feature.game.generated.resources.Res
import dev.talhabayhan.lexi.feature.game.generated.resources.game_button_backspace
import dev.talhabayhan.lexi.feature.game.generated.resources.game_button_joker
import dev.talhabayhan.lexi.feature.game.generated.resources.game_button_pass
import dev.talhabayhan.lexi.feature.game.generated.resources.game_clue_label
import dev.talhabayhan.lexi.feature.game.generated.resources.game_error_back
import dev.talhabayhan.lexi.feature.game.generated.resources.game_error_no_cards
import dev.talhabayhan.lexi.feature.game.generated.resources.game_error_retry
import dev.talhabayhan.lexi.feature.game.generated.resources.game_error_unreadable
import dev.talhabayhan.lexi.feature.game.generated.resources.game_letter_count
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
 * The game screen (`04-game`), stateless: the resting look comes from [state]; [clock] is read only by the HUD timer
 * and the joker sheet's status pill; [motion] plays transient effects (`motion.md` #2–#8) over the resting look, partly
 * in [GameMotionLayer] above the content and below the sheet and dialog. Lifecycle ON_PAUSE / ON_RESUME are forwarded
 * as background / foreground intents (ADR 0019). Insets: `safeDrawing` on top of the design paddings; the background
 * shapes reach the screen edges. Overlays: the joker sheet (`05-joker-sheet`) and the pause dialog (design debt).
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

    BoxWithConstraints(modifier.fillMaxSize().background(LexiTheme.colors.cream)) {
        CompositionLocalProvider(LocalDensity provides LocalDensity.current.withCappedFontScale()) {
            GameDecor()
            val loadError = state.loadError
            if (loadError != null) {
                GameLoadErrorContent(loadError, onIntent)
            } else if (!state.isLoading) {
                GameContent(state, clock, onIntent, motion, maxWidth, maxHeight)
            }
            GameMotionLayer(motion)
        }
        JokerSheet(
            visible = state.isJokerSheetOpen,
            jokers = state.jokers,
            adReward = state.jokerAdReward.takeIf { state.showJokerAd },
            clock = clock,
            onIntent = onIntent,
        )
        if (state.isPaused) {
            PauseDialog(
                quitConfirmation = state.isQuitConfirmation,
                restartConfirmation = state.isRestartConfirmation,
                canRestart = state.canRestart,
                onIntent = onIntent,
            )
        }
    }
}

/**
 * The playing board: [gameLayout] is computed once from the space inside the safe-drawing insets and the (capped)
 * font scale, so the clue, answer and keys of a turn never move the other rows (`screen-map.md`).
 */
@Composable
private fun GameContent(
    state: GameUiState,
    clock: () -> GameClockUi,
    onIntent: (GameIntent) -> Unit,
    motion: GameMotion,
    maxWidth: Dp,
    maxHeight: Dp,
) {
    val dimens = LexiTheme.dimens
    val density = LocalDensity.current
    val insets = WindowInsets.safeDrawing
    val width = with(density) {
        min(
            maxWidth - insets.getLeft(density, LayoutDirection.Ltr).toDp() -
                insets.getRight(density, LayoutDirection.Ltr).toDp(),
            ViewportDefaults.ContentMaxWidth,
        )
    }
    val height = with(density) { maxHeight - insets.getTop(density).toDp() - insets.getBottom(density).toDp() }
    val clueMetrics = rememberClueCardMetrics()
    val layout = remember(width, height, dimens, clueMetrics) { gameLayout(width, height, dimens, clueMetrics) }
    val turn = state.turn
    GameColumn(
        gap = layout.gap,
        answerTopMargin = layout.answerTopMargin,
        keysHeight = layout.keysHeight,
        keysActionsGap = layout.keysActionsGap,
        maxWheelSize = layout.maxWheelSize,
        hud = {
            GameHudBar(
                score = state.score,
                correct = state.correct,
                isScored = state.isScored,
                clock = clock,
                onPause = { onIntent(GameIntent.Pause) },
                modifier = Modifier.testTag(GameTestTags.HUD).onPlaced(motion::onHudPlaced),
                height = layout.hudHeight,
            )
        },
        wheel = { size ->
            LetterWheel(
                letters = state.wheel,
                centerLetter = turn?.letter ?: ' ',
                ruleLabel = turn?.let { stringResource(it.rule.label()) }.orEmpty(),
                size = size,
                chipMotion = motion::chipMotion,
                modifier = Modifier.testTag(GameTestTags.WHEEL),
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
                    example = turn.example,
                    clueAreaHeight = layout.clueAreaHeight,
                    modifier = Modifier.testTag(GameTestTags.CLUE),
                )
            }
        },
        answer = {
            if (turn != null) {
                AnswerSlots(
                    words = turn.words,
                    offset = { motion.slotsOffset },
                    height = layout.slotHeight,
                    flyingCells = motion::flyingCells,
                    onCellPlaced = motion::onCellPlaced,
                    modifier = Modifier.testTag(GameTestTags.ANSWER),
                )
            }
        },
        keys = {
            if (turn != null) {
                KeyGrid(
                    keys = turn.keys,
                    enabled = state.inputEnabled,
                    onKey = { onIntent(GameIntent.KeyTapped(it)) },
                    keyHeight = layout.keyHeight,
                    gap = layout.keyGap,
                    onKeyPlaced = motion::onKeyPlaced,
                    modifier = Modifier.testTag(GameTestTags.KEYS),
                )
            }
        },
        actions = {
            GameActions(
                enabled = state.inputEnabled,
                size = layout.actionSize,
                onIntent = onIntent,
                modifier = Modifier.testTag(GameTestTags.ACTIONS),
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .lexiContentWidth()
            .padding(
                start = dimens.screenPaddingHorizontal,
                top = layout.paddingTop,
                end = dimens.screenPaddingHorizontal,
                bottom = layout.paddingBottom,
            ),
    )
}

/** Test tags of the game board's rows (layout tests). */
internal object GameTestTags {
    const val HUD: String = "game_hud"
    const val WHEEL: String = "game_wheel"
    const val CLUE: String = "game_clue"
    const val ANSWER: String = "game_answer"
    const val KEYS: String = "game_keys"
    const val ACTIONS: String = "game_actions"
}

/** The game screen honours the system font size up to [GameScreenDefaults.MAX_FONT_SCALE] (`screen-map.md`). */
private fun Density.withCappedFontScale(): Density = if (fontScale <= GameScreenDefaults.MAX_FONT_SCALE) {
    this
} else {
    Density(density, GameScreenDefaults.MAX_FONT_SCALE)
}

/**
 * A game that could not be loaded (DESIGN-DEBT: no design; `EmptyState` with actions from the component library) at
 * the top of the screen with the design paddings: unreadable content offers "Tekrar dene" and "Geri dön", a wheel
 * without cards only "Geri dön".
 */
@Composable
private fun GameLoadErrorContent(error: GameLoadError, onIntent: (GameIntent) -> Unit) {
    val dimens = LexiTheme.dimens
    val modifier = Modifier
        .fillMaxWidth()
        .windowInsetsPadding(WindowInsets.safeDrawing)
        .padding(
            start = dimens.screenPaddingHorizontal,
            top = dimens.screenPaddingTop,
            end = dimens.screenPaddingHorizontal,
            bottom = dimens.screenPaddingBottom,
        )
    when (error) {
        GameLoadError.Unreadable -> EmptyState(
            message = stringResource(Res.string.game_error_unreadable),
            actionText = stringResource(Res.string.game_error_retry),
            onAction = { onIntent(GameIntent.Retry) },
            secondaryText = stringResource(Res.string.game_error_back),
            onSecondary = { onIntent(GameIntent.Leave) },
            modifier = modifier,
        )

        GameLoadError.NoCards -> EmptyState(
            message = stringResource(Res.string.game_error_no_cards),
            actionText = stringResource(Res.string.game_error_back),
            onAction = { onIntent(GameIntent.Leave) },
            modifier = modifier,
        )
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

/**
 * Joker (red circle), backspace (square) and PAS, gap 10, [size] high (56; 48 in the compact tier). Not [enabled] while input is blocked (wrong-answer reveal):
 * disabled for touch and accessibility and drawn disabled (PAS: the button's disabled state; the others at 50 %).
 */
@Composable
private fun GameActions(enabled: Boolean, size: Dp, onIntent: (GameIntent) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap10),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ActionCircleButton(
            icon = LexiIcons.Bulb,
            contentDescription = stringResource(Res.string.game_button_joker),
            onClick = { onIntent(GameIntent.OpenJokers) },
            size = size,
            enabled = enabled,
        )
        IconSquareButton(
            icon = LexiIcons.Backspace,
            contentDescription = stringResource(Res.string.game_button_backspace),
            onClick = { onIntent(GameIntent.Backspace) },
            size = size,
            enabled = enabled,
        )
        LexiButton(
            text = stringResource(Res.string.game_button_pass),
            onClick = { onIntent(GameIntent.Pass) },
            enabled = enabled,
            variant = LexiButtonVariant.Pass,
            textStyle = LexiTheme.typography.button22,
            height = size,
            modifier = Modifier.weight(1f),
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
private fun GameScreenJokerSheetPreview() {
    LexiTheme(reducedMotion = false) {
        GameScreen(state = GameSamples.jokerSheet, clock = { GameSamples.jokerSheetClock }, onIntent = {})
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun GameScreenPracticePreview() {
    LexiTheme(reducedMotion = false) {
        GameScreen(state = GameSamples.practice, clock = { GameClockUi.Untimed }, onIntent = {})
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun GameScreenLoadErrorPreview() {
    LexiTheme(reducedMotion = false) {
        GameScreen(state = GameSamples.loadError, clock = { GameClockUi.Untimed }, onIntent = {})
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun GameScreenPausedPreview() {
    LexiTheme(reducedMotion = false) {
        GameScreen(state = GameSamples.paused, clock = { GameSamples.designClock }, onIntent = {})
    }
}
