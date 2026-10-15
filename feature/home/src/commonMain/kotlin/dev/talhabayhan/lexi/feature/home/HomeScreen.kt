package dev.talhabayhan.lexi.feature.home

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.component.DailyCard
import dev.talhabayhan.lexi.core.designsystem.component.DecorShape
import dev.talhabayhan.lexi.core.designsystem.component.DecorShapeKind
import dev.talhabayhan.lexi.core.designsystem.component.IconButtonDefaults
import dev.talhabayhan.lexi.core.designsystem.component.LexiLogo
import dev.talhabayhan.lexi.core.designsystem.component.MenuTile
import dev.talhabayhan.lexi.core.designsystem.component.NavSquareButton
import dev.talhabayhan.lexi.core.designsystem.component.PlayButton
import dev.talhabayhan.lexi.core.designsystem.component.StreakChip
import dev.talhabayhan.lexi.core.designsystem.icon.LexiIcons
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.feature.home.generated.resources.Res
import dev.talhabayhan.lexi.feature.home.generated.resources.home_daily_label
import dev.talhabayhan.lexi.feature.home.generated.resources.home_daily_title
import dev.talhabayhan.lexi.feature.home.generated.resources.home_mode_classic
import dev.talhabayhan.lexi.feature.home.generated.resources.home_mode_hardcore
import dev.talhabayhan.lexi.feature.home.generated.resources.home_mode_practice
import dev.talhabayhan.lexi.feature.home.generated.resources.home_months
import dev.talhabayhan.lexi.feature.home.generated.resources.home_play
import dev.talhabayhan.lexi.feature.home.generated.resources.home_play_badge
import dev.talhabayhan.lexi.feature.home.generated.resources.home_settings
import dev.talhabayhan.lexi.feature.home.generated.resources.home_streak
import dev.talhabayhan.lexi.feature.home.generated.resources.home_tagline
import dev.talhabayhan.lexi.feature.home.generated.resources.home_tile_exams
import dev.talhabayhan.lexi.feature.home.generated.resources.home_tile_exams_subtitle
import dev.talhabayhan.lexi.feature.home.generated.resources.home_tile_notebook
import dev.talhabayhan.lexi.feature.home.generated.resources.home_tile_notebook_due
import dev.talhabayhan.lexi.feature.home.generated.resources.home_tile_practice
import dev.talhabayhan.lexi.feature.home.generated.resources.home_tile_practice_subtitle
import dev.talhabayhan.lexi.feature.home.generated.resources.home_tile_stats
import dev.talhabayhan.lexi.feature.home.generated.resources.home_tile_stats_learned
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

/**
 * `02-home`: streak chip and settings, the logo, the daily card, OYNA and the 2 × 2 menu tiles. The blue circle and
 * red half-pill behind are edge-to-edge decoration at 210dp from the top / bottom of the screen; the content keeps the
 * design paddings inside the system-bar insets and scrolls only when the screen is too short for it.
 */
@Composable
internal fun HomeScreen(state: HomeUiState, onIntent: (HomeIntent) -> Unit, modifier: Modifier = Modifier) {
    val colors = LexiTheme.colors
    val dimens = LexiTheme.dimens
    Box(modifier.fillMaxSize().background(colors.cream)) {
        DecorShape(
            kind = DecorShapeKind.Circle,
            color = colors.blue,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = HomeScreenDefaults.CircleBleed, y = HomeScreenDefaults.DecorOffset)
                .size(HomeScreenDefaults.CircleSize),
        )
        DecorShape(
            kind = DecorShapeKind.HalfPill,
            color = colors.red,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = -HomeScreenDefaults.PillBleed, y = -HomeScreenDefaults.DecorOffset)
                .size(HomeScreenDefaults.PillWidth, HomeScreenDefaults.PillHeight),
        )
        BoxWithConstraints(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
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
                verticalArrangement = Arrangement.spacedBy(dimens.gap20),
            ) {
                TopBar(state, onIntent)
                Column(Modifier.padding(top = dimens.gap8), verticalArrangement = Arrangement.spacedBy(dimens.gap6)) {
                    LexiLogo()
                    Text(stringResource(Res.string.home_tagline), style = LexiTheme.typography.label15)
                }
                DailyCard(
                    day = state.dailyDay.toString(),
                    label = stringResource(
                        Res.string.home_daily_label,
                        stringArrayResource(Res.array.home_months)[state.dailyMonth - 1],
                    ),
                    title = stringResource(Res.string.home_daily_title),
                    onClick = { onIntent(HomeIntent.OpenDaily) },
                )
                PlayButton(
                    label = stringResource(Res.string.home_play),
                    badge = stringResource(
                        Res.string.home_play_badge,
                        state.playLevel.name,
                        stringResource(state.playMode.nameRes()),
                    ),
                    onClick = { onIntent(HomeIntent.Play) },
                )
                Spacer(Modifier.weight(1f))
                MenuGrid(state, onIntent)
            }
        }
    }
}

@Composable
private fun TopBar(state: HomeUiState, onIntent: (HomeIntent) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StreakChip(pluralStringResource(Res.plurals.home_streak, state.streakDays, state.streakDays))
        NavSquareButton(
            icon = LexiIcons.Settings,
            contentDescription = stringResource(Res.string.home_settings),
            onClick = { onIntent(HomeIntent.OpenSettings) },
            iconSize = IconButtonDefaults.IconSizeMedium,
        )
    }
}

@Composable
private fun MenuGrid(state: HomeUiState, onIntent: (HomeIntent) -> Unit) {
    val colors = LexiTheme.colors
    val gap = LexiTheme.dimens.gap12
    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
            MenuTile(
                title = stringResource(Res.string.home_tile_notebook),
                subtitle = pluralStringResource(Res.plurals.home_tile_notebook_due, state.dueReviews, state.dueReviews),
                marker = DecorShapeKind.Circle,
                markerColor = colors.blue,
                onClick = { onIntent(HomeIntent.OpenNotebook) },
                modifier = Modifier.weight(1f),
            )
            MenuTile(
                title = stringResource(Res.string.home_tile_stats),
                subtitle = pluralStringResource(
                    Res.plurals.home_tile_stats_learned,
                    state.learnedWords,
                    state.learnedWords,
                ),
                marker = DecorShapeKind.Square,
                markerColor = colors.red,
                onClick = { onIntent(HomeIntent.OpenStats) },
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
            MenuTile(
                title = stringResource(Res.string.home_tile_practice),
                subtitle = stringResource(Res.string.home_tile_practice_subtitle),
                marker = DecorShapeKind.Leaf,
                markerColor = colors.yellow,
                onClick = { onIntent(HomeIntent.OpenPractice) },
                modifier = Modifier.weight(1f),
            )
            MenuTile(
                title = stringResource(Res.string.home_tile_exams),
                subtitle = stringResource(Res.string.home_tile_exams_subtitle),
                marker = DecorShapeKind.Diamond,
                markerColor = colors.yellow,
                onClick = { onIntent(HomeIntent.OpenExamPacks) },
                modifier = Modifier.weight(1f),
                inverted = true,
            )
        }
    }
}

private fun GameMode.nameRes() = when (this) {
    GameMode.PRACTICE -> Res.string.home_mode_practice
    GameMode.HARDCORE -> Res.string.home_mode_hardcore
    else -> Res.string.home_mode_classic
}

/** Background shapes of `02-home`. */
private object HomeScreenDefaults {
    /** Blue circle 140 at `right: -70; top: 210`: half of it leaves the screen. */
    val CircleSize = 140.dp
    val CircleBleed = 70.dp

    /** Red half-pill 60 × 120 at `left: -30; bottom: 210`. */
    val PillWidth = 60.dp
    val PillHeight = 120.dp
    val PillBleed = 30.dp

    /** 210 from the top (circle) and from the bottom (pill). */
    val DecorOffset = 210.dp
}

// design sample, not user-facing
internal val HomeSampleState = HomeUiState(
    streakDays = 7,
    dueReviews = 24,
    learnedWords = 412,
    dailyDay = 14,
    dailyMonth = 10,
)

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun HomeScreenPreview() {
    LexiTheme {
        HomeScreen(state = HomeSampleState, onIntent = {})
    }
}
