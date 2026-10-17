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
import dev.talhabayhan.lexi.core.designsystem.component.StatGrid
import dev.talhabayhan.lexi.core.designsystem.component.StatTile
import dev.talhabayhan.lexi.core.designsystem.format.formatClock
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.lexiContentWidth
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.game.GameOverReason
import dev.talhabayhan.lexi.feature.game.generated.resources.Res
import dev.talhabayhan.lexi.feature.game.generated.resources.result_added
import dev.talhabayhan.lexi.feature.game.generated.resources.result_best_streak
import dev.talhabayhan.lexi.feature.game.generated.resources.result_correct
import dev.talhabayhan.lexi.feature.game.generated.resources.result_empty
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
import dev.talhabayhan.lexi.feature.game.generated.resources.result_outcome_passed
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
import dev.talhabayhan.lexi.feature.game.generated.resources.result_speak
import dev.talhabayhan.lexi.feature.game.generated.resources.result_time_left
import dev.talhabayhan.lexi.feature.game.generated.resources.result_wrong
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/** Typographic separator of the summary line, the same in every language (like the clock digits). */
private const val SEPARATOR = " · "

/**
 * `06-result`, stateless: header (level · mode · outcome, the score, best streak and time left), the 1 × 3 stats, the
 * "Tekrar edilecekler" list (first rows + "Tümünü gör") and the CTAs at the bottom (`margin-top: auto`). The yellow
 * circle is edge-to-edge decoration moving with the top inset; the content keeps the design paddings inside the
 * safe-drawing insets and scrolls when it is taller than the screen (all rows shown, large fonts).
 */
@Composable
internal fun ResultScreen(state: ResultUiState, onIntent: (ResultIntent) -> Unit, modifier: Modifier = Modifier) {
    val colors = LexiTheme.colors
    val dimens = LexiTheme.dimens
    Box(modifier.fillMaxSize().background(colors.cream)) {
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
    }
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
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = stringResource(Res.string.result_review_title),
                style = typography.heading20,
                modifier = Modifier.alignByBaseline(),
            )
            if (state.addedToNotebook > 0) {
                Text(
                    text = pluralStringResource(Res.plurals.result_added, state.addedToNotebook, state.addedToNotebook),
                    style = typography.caption13SemiBold,
                    modifier = Modifier.alignByBaseline(),
                )
            }
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
            state.rows.isEmpty() -> EmptyState(stringResource(Res.string.result_empty), Modifier.fillMaxWidth())

            else -> ListCard(Modifier.fillMaxWidth(), shadow = LexiTheme.shadows.s6) {
                state.rows.forEach { row ->
                    ReviewRow(
                        letter = row.letter,
                        word = row.word,
                        clue = row.clue,
                        example = row.example,
                        outcome = row.outcome,
                        outcomeDescription = stringResource(
                            if (row.outcome == ReviewOutcome.Wrong) {
                                Res.string.result_outcome_wrong
                            } else {
                                Res.string.result_outcome_passed
                            },
                        ),
                        speakContentDescription = stringResource(Res.string.result_speak, row.word),
                        onSpeak = { onIntent(ResultIntent.Speak(row.word)) },
                        level = if (state.mode == GameMode.DAILY) row.level?.name else null,
                    )
                }
                if (state.hiddenRows > 0) {
                    ReviewMoreRow(
                        text = pluralStringResource(Res.plurals.result_more, state.hiddenRows, state.hiddenRows),
                        onClick = { onIntent(ResultIntent.ShowAll) },
                    )
                }
            }
        }
    }
}

/**
 * 3-column grid, gap 10: TEKRAR OYNA spans two columns, Paylaş one (both 60dp, s5). Without [canPlayAgain] (a finished
 * Daily, deviation from the design) Paylaş takes the whole row.
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

private fun GameMode.label(): StringResource = when (this) {
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

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun ResultScreenPreview() {
    LexiTheme(reducedMotion = false) { ResultScreen(state = ResultSamples.design, onIntent = {}) }
}
