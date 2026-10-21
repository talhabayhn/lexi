package dev.talhabayhan.lexi.feature.game.result

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import dev.talhabayhan.lexi.core.designsystem.component.AdaptiveTrailingRow
import dev.talhabayhan.lexi.core.designsystem.component.DecorShape
import dev.talhabayhan.lexi.core.designsystem.component.DecorShapeKind
import dev.talhabayhan.lexi.core.designsystem.component.EmptyState
import dev.talhabayhan.lexi.core.designsystem.component.LexiButton
import dev.talhabayhan.lexi.core.designsystem.component.LexiButtonDefaults
import dev.talhabayhan.lexi.core.designsystem.component.LexiButtonVariant
import dev.talhabayhan.lexi.core.designsystem.component.LexiTextLink
import dev.talhabayhan.lexi.core.designsystem.component.ListCard
import dev.talhabayhan.lexi.core.designsystem.component.ResultDefaults
import dev.talhabayhan.lexi.core.designsystem.component.ReviewMoreRow
import dev.talhabayhan.lexi.core.designsystem.component.ReviewOutcome
import dev.talhabayhan.lexi.core.designsystem.component.ReviewRow
import dev.talhabayhan.lexi.core.designsystem.component.SlidingToast
import dev.talhabayhan.lexi.core.designsystem.component.StatGrid
import dev.talhabayhan.lexi.core.designsystem.component.StatTile
import dev.talhabayhan.lexi.core.designsystem.format.formatClock
import dev.talhabayhan.lexi.core.designsystem.modifier.lexiHeading
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.lexiContentWidth
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.game.GameOverReason
import dev.talhabayhan.lexi.feature.game.generated.resources.Res
import dev.talhabayhan.lexi.feature.game.generated.resources.result_add_to_notebook
import dev.talhabayhan.lexi.feature.game.generated.resources.result_added
import dev.talhabayhan.lexi.feature.game.generated.resources.result_added_to_notebook
import dev.talhabayhan.lexi.feature.game.generated.resources.result_best_streak
import dev.talhabayhan.lexi.feature.game.generated.resources.result_correct
import dev.talhabayhan.lexi.feature.game.generated.resources.result_empty
import dev.talhabayhan.lexi.feature.game.generated.resources.result_group_correct
import dev.talhabayhan.lexi.feature.game.generated.resources.result_group_passed
import dev.talhabayhan.lexi.feature.game.generated.resources.result_group_time_up
import dev.talhabayhan.lexi.feature.game.generated.resources.result_group_wrong
import dev.talhabayhan.lexi.feature.game.generated.resources.result_header
import dev.talhabayhan.lexi.feature.game.generated.resources.result_home
import dev.talhabayhan.lexi.feature.game.generated.resources.result_mode_classic
import dev.talhabayhan.lexi.feature.game.generated.resources.result_mode_daily
import dev.talhabayhan.lexi.feature.game.generated.resources.result_mode_duel
import dev.talhabayhan.lexi.feature.game.generated.resources.result_mode_hardcore
import dev.talhabayhan.lexi.feature.game.generated.resources.result_mode_level_test
import dev.talhabayhan.lexi.feature.game.generated.resources.result_mode_practice
import dev.talhabayhan.lexi.feature.game.generated.resources.result_mode_review
import dev.talhabayhan.lexi.feature.game.generated.resources.result_more
import dev.talhabayhan.lexi.feature.game.generated.resources.result_outcome_correct
import dev.talhabayhan.lexi.feature.game.generated.resources.result_outcome_passed
import dev.talhabayhan.lexi.feature.game.generated.resources.result_outcome_time_up
import dev.talhabayhan.lexi.feature.game.generated.resources.result_outcome_wrong
import dev.talhabayhan.lexi.feature.game.generated.resources.result_passed
import dev.talhabayhan.lexi.feature.game.generated.resources.result_play_again
import dev.talhabayhan.lexi.feature.game.generated.resources.result_points
import dev.talhabayhan.lexi.feature.game.generated.resources.result_reason_all_answered
import dev.talhabayhan.lexi.feature.game.generated.resources.result_reason_empty_wheel
import dev.talhabayhan.lexi.feature.game.generated.resources.result_reason_forfeit
import dev.talhabayhan.lexi.feature.game.generated.resources.result_reason_time_up
import dev.talhabayhan.lexi.feature.game.generated.resources.result_reason_wrong_in_hardcore
import dev.talhabayhan.lexi.feature.game.generated.resources.result_retry
import dev.talhabayhan.lexi.feature.game.generated.resources.result_review_error
import dev.talhabayhan.lexi.feature.game.generated.resources.result_review_title
import dev.talhabayhan.lexi.feature.game.generated.resources.result_share
import dev.talhabayhan.lexi.feature.game.generated.resources.result_show_less
import dev.talhabayhan.lexi.feature.game.generated.resources.result_speak
import dev.talhabayhan.lexi.feature.game.generated.resources.result_time_left
import dev.talhabayhan.lexi.feature.game.generated.resources.result_wrong
import dev.talhabayhan.lexi.feature.game.generated.resources.share_error
import dev.talhabayhan.lexi.feature.game.result.share.ShareCardCapture
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/** Typographic separator of the summary line, the same in every language (like the clock digits). */
private const val SEPARATOR = " · "

/**
 * `06-result`, stateless: header (level · mode · outcome, the score, best streak and time left), the 1 × 3 stats, the
 * "Tekrar edilecekler" list (groups of the first [ReviewGroupUi.PREVIEW_ROWS] rows + "Daha fazla göster") and the CTAs at the bottom (`margin-top: auto`). The yellow
 * circle is edge-to-edge decoration moving with the top inset; the content keeps the design paddings inside the
 * safe-drawing insets and scrolls when it is taller than the screen (all rows shown, large fonts).
 *
 * While [ResultUiState.isSharing], the share card is captured off screen ([ShareCardCapture]); [shareErrorVisible]
 * shows "Paylaşım başlatılamadı." as an info toast at the top (motion #11) until [onShareErrorDismissed].
 */
@Composable
internal fun ResultScreen(
    state: ResultUiState,
    onIntent: (ResultIntent) -> Unit,
    modifier: Modifier = Modifier,
    shareErrorVisible: Boolean = false,
    onShareErrorDismissed: () -> Unit = {},
) {
    val colors = LexiTheme.colors
    val dimens = LexiTheme.dimens
    Box(modifier.fillMaxSize().background(colors.cream)) {
        val shareCard = state.shareCard
        if (state.isSharing && shareCard != null) ShareCardCapture(shareCard, onIntent)
        Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))) {
            DecorShape(
                kind = DecorShapeKind.Circle,
                color = colors.yellow,
                border = BorderStroke(LexiTheme.borders.regular, colors.ink),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = ResultDefaults.DecorCircleOverhang, y = -ResultDefaults.DecorCircleOverhang)
                    .size(ResultDefaults.DecorCircleSize),
            )
        }
        BoxWithConstraints(
            Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).lexiContentWidth(),
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
                Header(state)
                Stats(state)
                Review(state, onIntent)
                Spacer(Modifier.weight(1f))
                Actions(state.canPlayAgain, onIntent)
                LexiTextLink(
                    text = stringResource(Res.string.result_home),
                    onClick = { onIntent(ResultIntent.Home) },
                    contentPadding = ResultDefaults.HomeLinkPadding,
                    minHeight = Dp.Unspecified,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
        }
        if (shareErrorVisible) {
            // DESIGN-DEBT: no design for a failed share; the component library's info toast at the top.
            ShareErrorToast(
                onDismissed = onShareErrorDismissed,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .lexiContentWidth()
                    .padding(horizontal = dimens.screenPaddingHorizontal, vertical = dimens.screenPaddingTop),
            )
        }
    }
}

/** Info toast "Paylaşım başlatılamadı.": slides down 16dp in 180 ms (no slide with reduced motion), gone after 2.5 s. */
@Composable
private fun ShareErrorToast(onDismissed: () -> Unit, modifier: Modifier = Modifier) {
    SlidingToast(
        message = stringResource(Res.string.share_error),
        onDismissed = onDismissed,
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun Header(state: ResultUiState) {
    val typography = LexiTheme.typography
    val summary = buildList {
        add(stringResource(if (state.isScored) Res.string.result_points else Res.string.result_correct))
        if (state.longestStreak > 0) add(stringResource(Res.string.result_best_streak, state.longestStreak))
        state.remainingSeconds?.let { add(stringResource(Res.string.result_time_left, formatClock(it))) }
    }.joinToString(SEPARATOR)
    Column(verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap2)) {
        Text(
            text = stringResource(
                Res.string.result_header,
                state.level.name,
                stringResource(state.mode.label()),
                stringResource(state.reason.label()),
            ),
            style = typography.label13,
            modifier = Modifier.lexiHeading(),
        )
        Text((if (state.isScored) state.score else state.correct).toString(), style = typography.scoreHero)
        Text(summary, style = typography.body15SemiBold)
    }
}

@Composable
private fun Stats(state: ResultUiState) {
    val colors = LexiTheme.colors
    StatGrid(columns = 3, dividerWidth = LexiTheme.borders.regular, modifier = Modifier.fillMaxWidth()) {
        StatTile(
            value = state.correct.toString(),
            label = stringResource(Res.string.result_correct),
            marker = DecorShapeKind.Circle,
            markerColor = colors.blue,
            markerSize = ResultDefaults.StatMarkerSize,
            gap = ResultDefaults.StatGap,
        )
        StatTile(
            value = state.wrong.toString(),
            label = stringResource(Res.string.result_wrong),
            marker = DecorShapeKind.Square,
            markerColor = colors.red,
            markerSize = ResultDefaults.StatMarkerSize,
            gap = ResultDefaults.StatGap,
        )
        StatTile(
            value = state.passed.toString(),
            label = stringResource(Res.string.result_passed),
            marker = DecorShapeKind.Leaf,
            markerColor = colors.yellow,
            markerSize = ResultDefaults.StatMarkerSize,
            gap = ResultDefaults.StatGap,
        )
    }
}

@Composable
private fun Review(state: ResultUiState, onIntent: (ResultIntent) -> Unit) {
    val typography = LexiTheme.typography
    Column(verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap8)) {
        // Large fonts on narrow screens: the "added" caption moves under the title instead of squeezing it.
        AdaptiveTrailingRow(
            gap = LexiTheme.dimens.gap8,
            stackedGap = LexiTheme.dimens.gap4,
            alignBaselines = true,
            modifier = Modifier.fillMaxWidth(),
            main = {
                Text(
                    text = stringResource(Res.string.result_review_title),
                    style = typography.heading20,
                    modifier = Modifier.lexiHeading(),
                )
            },
            trailing = {
                if (state.addedToNotebook > 0) {
                    Text(
                        text = pluralStringResource(
                            Res.plurals.result_added,
                            state.addedToNotebook,
                            state.addedToNotebook,
                        ),
                        style = typography.caption13SemiBold,
                    )
                }
            },
        )
        val groups = when {
            state.groups.isNotEmpty() -> state.groups

            state.rows.isNotEmpty() -> {
                val order = listOf(
                    ReviewOutcome.Correct,
                    ReviewOutcome.Wrong,
                    ReviewOutcome.Passed,
                    ReviewOutcome.TimeUp,
                )
                order.mapNotNull { outcome ->
                    val matching = state.rows.filter { it.outcome == outcome }
                    if (matching.isNotEmpty()) ReviewGroupUi(outcome, matching.toImmutableList()) else null
                }.toImmutableList()
            }

            else -> persistentListOf()
        }
        when {
            state.isLoading -> Unit

            // DESIGN-DEBT: no design for a failed read; the component library's empty state with a retry.
            state.reviewError -> EmptyState(
                message = stringResource(Res.string.result_review_error),
                actionText = stringResource(Res.string.result_retry),
                onAction = { onIntent(ResultIntent.Retry) },
                modifier = Modifier.fillMaxWidth(),
            )

            // DESIGN-DEBT: no design for a wheel without misses; the component library's empty state.
            groups.isEmpty() -> EmptyState(stringResource(Res.string.result_empty), Modifier.fillMaxWidth())

            else -> Column(verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap16)) {
                groups.forEach { group ->
                    Column(verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap6)) {
                        Text(
                            text = stringResource(group.outcome.titleRes()),
                            style = typography.heading18,
                        )
                        val expanded = group.outcome in state.expandedGroups
                        val hidden = group.rows.size - ReviewGroupUi.PREVIEW_ROWS
                        ListCard(Modifier.fillMaxWidth(), shadow = LexiTheme.shadows.s6) {
                            (if (expanded) group.rows else group.rows.take(ReviewGroupUi.PREVIEW_ROWS)).forEach { row ->
                                ReviewRow(
                                    letter = row.letter,
                                    word = row.word,
                                    clue = row.clue,
                                    example = row.example,
                                    outcome = row.outcome,
                                    outcomeDescription = stringResource(row.outcome.descriptionRes()),
                                    speakContentDescription = stringResource(Res.string.result_speak, row.word),
                                    onSpeak = { onIntent(ResultIntent.Speak(row.word)) },
                                    level = if (state.mode == GameMode.DAILY) row.level?.name else null,
                                    onAddToNotebook = if (
                                        row.outcome == ReviewOutcome.TimeUp &&
                                        !row.isAddedToNotebook
                                    ) {
                                        { onIntent(ResultIntent.AddToNotebook(row.cardId)) }
                                    } else {
                                        null
                                    },
                                    addToNotebookText = if (row.outcome == ReviewOutcome.TimeUp) {
                                        stringResource(
                                            if (row.isAddedToNotebook) {
                                                Res.string.result_added_to_notebook
                                            } else {
                                                Res.string.result_add_to_notebook
                                            },
                                        )
                                    } else {
                                        null
                                    },
                                    isAddedToNotebook = row.isAddedToNotebook,
                                )
                            }
                            if (hidden > 0) {
                                ReviewMoreRow(
                                    text = if (expanded) {
                                        stringResource(Res.string.result_show_less)
                                    } else {
                                        pluralStringResource(Res.plurals.result_more, hidden, hidden)
                                    },
                                    onClick = { onIntent(ResultIntent.ToggleGroup(group.outcome)) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Action buttons: 3-column grid, gap 10: TEKRAR OYNA spans two columns, Paylaş one (both 60dp, s5). Without
 * [canPlayAgain] (a finished Daily, deviation from the design) Paylaş takes the whole row.
 */
@Composable
private fun Actions(canPlayAgain: Boolean, onIntent: (ResultIntent) -> Unit) {
    val gap = LexiTheme.dimens.gap10
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val column = (maxWidth - gap * (ResultDefaults.CTA_COLUMNS - 1)) / ResultDefaults.CTA_COLUMNS
        Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
            if (canPlayAgain) {
                LexiButton(
                    text = stringResource(Res.string.result_play_again),
                    onClick = { onIntent(ResultIntent.PlayAgain) },
                    textStyle = LexiTheme.typography.button20Wide,
                    height = LexiButtonDefaults.LargerHeight,
                    modifier = Modifier.width(
                        column * ResultDefaults.CTA_PRIMARY_SPAN + gap * (ResultDefaults.CTA_PRIMARY_SPAN - 1),
                    ),
                )
            }
            LexiButton(
                text = stringResource(Res.string.result_share),
                onClick = { onIntent(ResultIntent.Share) },
                variant = LexiButtonVariant.Secondary,
                height = LexiButtonDefaults.LargerHeight,
                modifier = if (canPlayAgain) Modifier.width(column) else Modifier.fillMaxWidth(),
            )
        }
    }
}

internal fun GameMode.label(): StringResource = when (this) {
    GameMode.CLASSIC -> Res.string.result_mode_classic
    GameMode.PRACTICE -> Res.string.result_mode_practice
    GameMode.HARDCORE -> Res.string.result_mode_hardcore
    GameMode.DAILY -> Res.string.result_mode_daily
    GameMode.REVIEW -> Res.string.result_mode_review
    GameMode.LEVEL_TEST -> Res.string.result_mode_level_test
    GameMode.DUEL -> Res.string.result_mode_duel
}

private fun GameOverReason.label(): StringResource = when (this) {
    GameOverReason.ALL_ANSWERED -> Res.string.result_reason_all_answered
    GameOverReason.TIME_UP -> Res.string.result_reason_time_up
    GameOverReason.WRONG_IN_HARDCORE -> Res.string.result_reason_wrong_in_hardcore
    GameOverReason.FORFEIT -> Res.string.result_reason_forfeit
    GameOverReason.EMPTY_WHEEL -> Res.string.result_reason_empty_wheel
}

private fun ReviewOutcome.titleRes(): StringResource = when (this) {
    ReviewOutcome.Correct -> Res.string.result_group_correct
    ReviewOutcome.Wrong -> Res.string.result_group_wrong
    ReviewOutcome.Passed -> Res.string.result_group_passed
    ReviewOutcome.TimeUp -> Res.string.result_group_time_up
}

private fun ReviewOutcome.descriptionRes(): StringResource = when (this) {
    ReviewOutcome.Correct -> Res.string.result_outcome_correct
    ReviewOutcome.Wrong -> Res.string.result_outcome_wrong
    ReviewOutcome.Passed -> Res.string.result_outcome_passed
    ReviewOutcome.TimeUp -> Res.string.result_outcome_time_up
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun ResultScreenPreview() {
    LexiTheme(reducedMotion = false) { ResultScreen(state = ResultSamples.design, onIntent = {}) }
}
