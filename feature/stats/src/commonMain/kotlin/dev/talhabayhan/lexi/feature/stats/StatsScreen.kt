package dev.talhabayhan.lexi.feature.stats

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.talhabayhan.lexi.core.designsystem.component.BarChart
import dev.talhabayhan.lexi.core.designsystem.component.BarChartDefaults
import dev.talhabayhan.lexi.core.designsystem.component.DecorShapeKind
import dev.talhabayhan.lexi.core.designsystem.component.EmptyState
import dev.talhabayhan.lexi.core.designsystem.component.HardLetterChip
import dev.talhabayhan.lexi.core.designsystem.component.LevelProgressRow
import dev.talhabayhan.lexi.core.designsystem.component.NavSquareButton
import dev.talhabayhan.lexi.core.designsystem.component.StatGrid
import dev.talhabayhan.lexi.core.designsystem.component.StatTile
import dev.talhabayhan.lexi.core.designsystem.component.StatsDefaults
import dev.talhabayhan.lexi.core.designsystem.icon.LexiIcons
import dev.talhabayhan.lexi.core.designsystem.modifier.lexiSurface
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.feature.stats.generated.resources.Res
import dev.talhabayhan.lexi.feature.stats.generated.resources.stats_accuracy
import dev.talhabayhan.lexi.feature.stats.generated.resources.stats_accuracy_none
import dev.talhabayhan.lexi.feature.stats.generated.resources.stats_back
import dev.talhabayhan.lexi.feature.stats.generated.resources.stats_day_fri
import dev.talhabayhan.lexi.feature.stats.generated.resources.stats_day_mon
import dev.talhabayhan.lexi.feature.stats.generated.resources.stats_day_sat
import dev.talhabayhan.lexi.feature.stats.generated.resources.stats_day_sun
import dev.talhabayhan.lexi.feature.stats.generated.resources.stats_day_thu
import dev.talhabayhan.lexi.feature.stats.generated.resources.stats_day_tue
import dev.talhabayhan.lexi.feature.stats.generated.resources.stats_day_wed
import dev.talhabayhan.lexi.feature.stats.generated.resources.stats_empty
import dev.talhabayhan.lexi.feature.stats.generated.resources.stats_error
import dev.talhabayhan.lexi.feature.stats.generated.resources.stats_learned
import dev.talhabayhan.lexi.feature.stats.generated.resources.stats_letter_correct
import dev.talhabayhan.lexi.feature.stats.generated.resources.stats_letters_empty
import dev.talhabayhan.lexi.feature.stats.generated.resources.stats_letters_title
import dev.talhabayhan.lexi.feature.stats.generated.resources.stats_level_count
import dev.talhabayhan.lexi.feature.stats.generated.resources.stats_levels_title
import dev.talhabayhan.lexi.feature.stats.generated.resources.stats_percent
import dev.talhabayhan.lexi.feature.stats.generated.resources.stats_retry
import dev.talhabayhan.lexi.feature.stats.generated.resources.stats_streak_label
import dev.talhabayhan.lexi.feature.stats.generated.resources.stats_streak_value
import dev.talhabayhan.lexi.feature.stats.generated.resources.stats_title
import dev.talhabayhan.lexi.feature.stats.generated.resources.stats_week_description
import dev.talhabayhan.lexi.feature.stats.generated.resources.stats_week_title
import dev.talhabayhan.lexi.feature.stats.generated.resources.stats_week_total
import dev.talhabayhan.lexi.feature.stats.generated.resources.stats_wheels
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * `09-stats`: back button and title, the 2x2 number grid, the weekly bar chart, the level progress bars and the
 * hardest letters (a row that scrolls sideways: the design's four chips are wider than the screen). A player who has
 * not played yet sees an [EmptyState] instead; so does a failed read. The whole page scrolls on small screens.
 */
@Composable
internal fun StatsScreen(state: StatsUiState, onIntent: (StatsIntent) -> Unit, modifier: Modifier = Modifier) {
    val dimens = LexiTheme.dimens
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LexiTheme.colors.cream)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(
                start = dimens.screenPaddingHorizontal,
                top = dimens.screenPaddingTop,
                end = dimens.screenPaddingHorizontal,
                bottom = dimens.screenPaddingBottomList,
            ),
        verticalArrangement = Arrangement.spacedBy(dimens.gap14),
    ) {
        Header(onBack = { onIntent(StatsIntent.Back) })
        when {
            !state.isLoaded -> Unit

            // DESIGN-DEBT: no design for a failed read; the component library's empty state with a retry.
            state.hasError -> EmptyState(
                message = stringResource(Res.string.stats_error),
                actionText = stringResource(Res.string.stats_retry),
                onAction = { onIntent(StatsIntent.Retry) },
                modifier = Modifier.fillMaxWidth(),
            )

            state.isEmpty -> EmptyState(stringResource(Res.string.stats_empty), Modifier.fillMaxWidth())

            else -> {
                Numbers(state)
                WeekCard(state)
                Levels(state)
                HardestLetters(state)
            }
        }
    }
}

@Composable
private fun Header(onBack: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NavSquareButton(
            icon = LexiIcons.Back,
            contentDescription = stringResource(Res.string.stats_back),
            onClick = onBack,
        )
        Text(stringResource(Res.string.stats_title), style = LexiTheme.typography.title)
    }
}

@Composable
private fun Numbers(state: StatsUiState) {
    val colors = LexiTheme.colors
    StatGrid(columns = 2, modifier = Modifier.fillMaxWidth()) {
        StatTile(
            state.learnedWords.toString(),
            stringResource(Res.string.stats_learned),
            DecorShapeKind.Circle,
            colors.blue,
        )
        StatTile(
            state.accuracyPercent?.let { stringResource(Res.string.stats_percent, it) }
                ?: stringResource(Res.string.stats_accuracy_none),
            stringResource(Res.string.stats_accuracy),
            DecorShapeKind.Square,
            colors.red,
        )
        StatTile(
            pluralStringResource(Res.plurals.stats_streak_value, state.longestStreak, state.longestStreak),
            stringResource(Res.string.stats_streak_label),
            DecorShapeKind.Leaf,
            colors.yellow,
        )
        StatTile(
            state.completedWheels.toString(),
            stringResource(Res.string.stats_wheels),
            DecorShapeKind.Square,
            colors.ink,
        )
    }
}

@Composable
private fun WeekCard(state: StatsUiState) {
    val colors = LexiTheme.colors
    val typography = LexiTheme.typography
    val labels = state.week.map { stringResource(it.weekday.dayRes()) }
    val values = state.week.map { it.words }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .lexiSurface(colors.white, BorderStroke(LexiTheme.borders.regular, colors.ink))
            .padding(horizontal = LexiTheme.dimens.gap14, vertical = LexiTheme.dimens.gap12),
        verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap10),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                stringResource(Res.string.stats_week_title),
                style = typography.bodyStrong16,
                modifier = Modifier.alignByBaseline(),
            )
            Text(
                stringResource(Res.string.stats_week_total, state.weekTotal),
                style = typography.caption13SemiBold,
                modifier = Modifier.alignByBaseline(),
            )
        }
        BarChart(
            values = values,
            labels = labels,
            highlightIndex = values.lastIndex,
            contentDescription = stringResource(
                Res.string.stats_week_description,
                labels.zip(values) { label, value -> "$label $value" }.joinToString(", "),
            ),
            // The design scale tops out at 50 words a day; a busier day stretches it instead of being clipped.
            maxValue = maxOf(BarChartDefaults.MAX_VALUE, values.maxOrNull() ?: 0),
        )
    }
}

@Composable
private fun Levels(state: StatsUiState) {
    if (state.levels.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap8)) {
        Text(stringResource(Res.string.stats_levels_title), style = LexiTheme.typography.label12)
        state.levels.forEach { entry ->
            LevelProgressRow(
                level = entry.level.name,
                progress = entry.fraction,
                count = stringResource(Res.string.stats_level_count, entry.learned, entry.total),
            )
        }
    }
}

@Composable
private fun HardestLetters(state: StatsUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap8)) {
        Text(stringResource(Res.string.stats_letters_title), style = LexiTheme.typography.label12)
        if (state.hardLetters.isEmpty()) {
            EmptyState(stringResource(Res.string.stats_letters_empty), Modifier.fillMaxWidth())
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(StatsDefaults.LetterChipGap),
            ) {
                state.hardLetters.forEach { entry ->
                    HardLetterChip(
                        letter = entry.letter.toString(),
                        percent = stringResource(Res.string.stats_percent, entry.percent),
                        label = stringResource(Res.string.stats_letter_correct),
                    )
                }
            }
        }
    }
}

private fun Int.dayRes(): StringResource = when (this) {
    0 -> Res.string.stats_day_mon
    1 -> Res.string.stats_day_tue
    2 -> Res.string.stats_day_wed
    3 -> Res.string.stats_day_thu
    4 -> Res.string.stats_day_fri
    5 -> Res.string.stats_day_sat
    else -> Res.string.stats_day_sun
}

internal val StatsSampleState: StatsUiState = StatsUiState(
    isLoaded = true,
    learnedWords = 412,
    accuracyPercent = 78,
    longestStreak = 12,
    completedWheels = 63,
    week = persistentListOf(
        DayWordsUi(0, 18),
        DayWordsUi(1, 26),
        DayWordsUi(2, 12),
        DayWordsUi(3, 31),
        DayWordsUi(4, 22),
        DayWordsUi(5, 40),
        DayWordsUi(6, 28),
    ),
    levels = persistentListOf(
        LevelStatUi(Level.A1, 412, 850),
        LevelStatUi(Level.A2, 96, 900),
        LevelStatUi(Level.B1, 0, 1200),
    ),
    hardLetters = persistentListOf(
        HardLetterUi('X', 32),
        HardLetterUi('Q', 45),
        HardLetterUi('Y', 58),
        HardLetterUi('V', 61),
    ),
)

// design sample, not user-facing
@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun StatsScreenPreview() {
    LexiTheme(reducedMotion = false) { StatsScreen(state = StatsSampleState, onIntent = {}) }
}
