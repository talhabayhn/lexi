package dev.talhabayhan.lexi.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import dev.talhabayhan.lexi.core.designsystem.component.CardDefaults
import dev.talhabayhan.lexi.core.designsystem.component.DailyCard
import dev.talhabayhan.lexi.core.designsystem.component.DecorShape
import dev.talhabayhan.lexi.core.designsystem.component.DecorShapeKind
import dev.talhabayhan.lexi.core.designsystem.component.HomeDefaults
import dev.talhabayhan.lexi.core.designsystem.component.IconButtonDefaults
import dev.talhabayhan.lexi.core.designsystem.component.LexiLogo
import dev.talhabayhan.lexi.core.designsystem.component.LexiLogoSize
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
 * design paddings inside the system-bar insets and never scrolls: on short phones it steps down [HomeTier]s.
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
                .offset(x = HomeDefaults.CircleBleed, y = HomeDefaults.DecorOffset)
                .size(HomeDefaults.CircleSize),
        )
        DecorShape(
            kind = DecorShapeKind.HalfPill,
            color = colors.red,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = -HomeDefaults.PillBleed, y = -HomeDefaults.DecorOffset)
                .size(HomeDefaults.PillWidth, HomeDefaults.PillHeight),
        )
        CompositionLocalProvider(LocalDensity provides LocalDensity.current.withCappedFontScale()) {
            FitToHeight(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) { tier ->
                HomeContent(tier, state, onIntent)
            }
        }
    }
}

/** Heights of one layout tier (`screen-map.md`): regular is the design, the others are DESIGN-DEBT scalings of it. */
internal enum class HomeTier { Regular, Compact, Tight }

private class HomeMetrics(
    val paddingTop: Dp,
    val paddingBottom: Dp,
    val gap: Dp,
    val logo: LexiLogoSize,
    val playHeight: Dp,
    val tileHeight: Dp,
    val tileGap: Dp,
    val compactDaily: Boolean,
    val compactTiles: Boolean = false,
)

@Composable
private fun HomeTier.metrics(): HomeMetrics {
    val dimens = LexiTheme.dimens
    return when (this) {
        HomeTier.Regular -> HomeMetrics(
            dimens.screenPaddingTop,
            dimens.screenPaddingBottom,
            dimens.gap20,
            LexiLogoSize.Menu,
            HomeDefaults.RegularPlayHeight,
            CardDefaults.MenuTileHeight,
            dimens.gap12,
            compactDaily = false,
        )

        HomeTier.Compact -> HomeMetrics(
            HomeDefaults.CompactPaddingTop,
            HomeDefaults.CompactPaddingBottom,
            HomeDefaults.CompactGap,
            LexiLogoSize.MenuCompact,
            HomeDefaults.CompactPlayHeight,
            HomeDefaults.CompactTileHeight,
            HomeDefaults.CompactTileGap,
            compactDaily = true,
        )

        HomeTier.Tight -> HomeMetrics(
            HomeDefaults.TightPaddingTop, HomeDefaults.TightPaddingBottom, HomeDefaults.TightGap,
            LexiLogoSize.MenuCompact, HomeDefaults.TightPlayHeight, HomeDefaults.TightTileHeight,
            HomeDefaults.TightTileGap, compactDaily = true, compactTiles = true,
        )
    }
}

/**
 * Composes [content] for each tier in turn, measures its natural height and keeps the first tier that fits the
 * available height (the last one otherwise; the measured probes are hidden from accessibility), then lays it out to exactly the available height: the home never scrolls.
 */
@Composable
private fun FitToHeight(modifier: Modifier, content: @Composable (HomeTier) -> Unit) {
    SubcomposeLayout(modifier) { constraints ->
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        val natural = Constraints(maxWidth = width)
        val tier = HomeTier.entries.firstOrNull { candidate ->
            subcompose(candidate to false) {
                Box(Modifier.clearAndSetSemantics {}) { content(candidate) }
            }.maxOf { it.measure(natural).height } <=
                height
        } ?: HomeTier.entries.last()
        val placeables = subcompose(tier to true) { content(tier) }.map { it.measure(Constraints.fixed(width, height)) }
        layout(width, height) { placeables.forEach { it.place(0, 0) } }
    }
}

@Composable
private fun HomeContent(tier: HomeTier, state: HomeUiState, onIntent: (HomeIntent) -> Unit) {
    val dimens = LexiTheme.dimens
    val metrics = tier.metrics()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = dimens.screenPaddingHorizontal,
                top = metrics.paddingTop,
                end = dimens.screenPaddingHorizontal,
                bottom = metrics.paddingBottom,
            ),
        verticalArrangement = Arrangement.spacedBy(metrics.gap),
    ) {
        TopBar(state, onIntent)
        Column(
            Modifier.padding(top = if (tier == HomeTier.Regular) dimens.gap8 else dimens.gap4),
            verticalArrangement = Arrangement.spacedBy(dimens.gap6),
        ) {
            LexiLogo(size = metrics.logo)
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
            compact = metrics.compactDaily,
        )
        PlayButton(
            label = stringResource(Res.string.home_play),
            badge = stringResource(
                Res.string.home_play_badge,
                state.playLevel.name,
                stringResource(state.playMode.nameRes()),
            ),
            onClick = { onIntent(HomeIntent.Play) },
            height = metrics.playHeight,
        )
        Spacer(Modifier.weight(1f))
        MenuGrid(state, onIntent, metrics)
    }
}

/** The home honours the system font size up to [HomeDefaults.MAX_FONT_SCALE] (`screen-map.md`). */
private fun Density.withCappedFontScale(): Density = if (fontScale <= HomeDefaults.MAX_FONT_SCALE) {
    this
} else {
    Density(density, HomeDefaults.MAX_FONT_SCALE)
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
private fun MenuGrid(state: HomeUiState, onIntent: (HomeIntent) -> Unit, metrics: HomeMetrics) {
    val colors = LexiTheme.colors
    val gap = metrics.tileGap
    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(gap)) {
            MenuTile(
                title = stringResource(Res.string.home_tile_notebook),
                subtitle = pluralStringResource(Res.plurals.home_tile_notebook_due, state.dueReviews, state.dueReviews),
                marker = DecorShapeKind.Circle,
                markerColor = colors.blue,
                onClick = { onIntent(HomeIntent.OpenNotebook) },
                modifier = Modifier.weight(1f).fillMaxHeight(),
                minHeight = metrics.tileHeight,
                compact = metrics.compactTiles,
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
                modifier = Modifier.weight(1f).fillMaxHeight(),
                minHeight = metrics.tileHeight,
                compact = metrics.compactTiles,
            )
        }
        Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(gap)) {
            MenuTile(
                title = stringResource(Res.string.home_tile_practice),
                subtitle = stringResource(Res.string.home_tile_practice_subtitle),
                marker = DecorShapeKind.Leaf,
                markerColor = colors.yellow,
                onClick = { onIntent(HomeIntent.OpenPractice) },
                modifier = Modifier.weight(1f).fillMaxHeight(),
                minHeight = metrics.tileHeight,
                compact = metrics.compactTiles,
            )
            MenuTile(
                title = stringResource(Res.string.home_tile_exams),
                subtitle = stringResource(Res.string.home_tile_exams_subtitle),
                marker = DecorShapeKind.Diamond,
                markerColor = colors.yellow,
                onClick = { onIntent(HomeIntent.OpenExamPacks) },
                modifier = Modifier.weight(1f).fillMaxHeight(),
                minHeight = metrics.tileHeight,
                compact = metrics.compactTiles,
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
