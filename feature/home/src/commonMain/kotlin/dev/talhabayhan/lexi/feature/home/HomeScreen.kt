package dev.talhabayhan.lexi.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.min
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
import dev.talhabayhan.lexi.core.designsystem.theme.lexiContentWidth
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
            FitToHeight(
                Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).lexiContentWidth(),
            ) { tier, tileGrowth, fit ->
                HomeContent(tier, tileGrowth, fit, state, onIntent)
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
private fun HomeTier.metrics(tileGrowth: Dp): HomeMetrics {
    val dimens = LexiTheme.dimens
    return when (this) {
        HomeTier.Regular -> HomeMetrics(
            dimens.screenPaddingTop,
            dimens.screenPaddingBottom,
            dimens.gap20,
            LexiLogoSize.Menu,
            HomeDefaults.RegularPlayHeight,
            CardDefaults.MenuTileHeight + tileGrowth,
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

/** Natural height of one tier's content, written by [HomeColumn] while it is measured. */
private class HomeFit {
    var naturalHeight: Int = 0
}

/**
 * Measures the content of each tier in turn (from regular) at exactly the available size and keeps the first whose
 * natural height ([HomeColumn] reports it in the same measure pass) fits; the last one otherwise. The home never
 * scrolls. Each tier is composed once and reused, so the usual case (regular fits) costs a single composition: no
 * separate probe has to be composed on the first frame of a screen transition. Tiers measured but not chosen stay
 * unplaced (hidden from accessibility). In the regular tier, height beyond the design's OYNA–tiles space
 * ([HomeDefaults.RegularMaxSpare]) becomes tile growth (per tile row) instead of widening that hole.
 */
@Composable
private fun FitToHeight(modifier: Modifier, content: @Composable (HomeTier, Dp, HomeFit) -> Unit) {
    val fits = remember { HomeTier.entries.associateWith { HomeFit() } }
    val grownFit = remember { HomeFit() }
    // Stable slot lambdas: SubcomposeLayout recomposes a slot whenever it gets a new lambda instance.
    val slots = remember(content) {
        HomeTier.entries.associateWith { tier -> @Composable { content(tier, Dp(0f), fits.getValue(tier)) } }
    }
    SubcomposeLayout(modifier) { constraints ->
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        val fixed = Constraints.fixed(width, height)
        var tier = HomeTier.entries.last()
        var placeables = emptyList<Placeable>()
        for (candidate in HomeTier.entries) {
            placeables = subcompose(candidate, slots.getValue(candidate)).map { it.measure(fixed) }
            tier = candidate
            if (fits.getValue(candidate).naturalHeight <= height) break
        }
        if (tier == HomeTier.Regular) {
            val excess = height - fits.getValue(tier).naturalHeight - HomeDefaults.RegularMaxSpare.roundToPx()
            if (excess > 0) {
                val growth = min(excess.toDp() / 2, HomeDefaults.TileMaxGrowth)
                placeables = subcompose(tier to growth) { content(tier, growth, grownFit) }.map { it.measure(fixed) }
            }
        }
        layout(width, height) { placeables.forEach { it.place(0, 0) } }
    }
}

@Composable
private fun HomeContent(
    tier: HomeTier,
    tileGrowth: Dp,
    fit: HomeFit,
    state: HomeUiState,
    onIntent: (HomeIntent) -> Unit,
) {
    val dimens = LexiTheme.dimens
    val metrics = tier.metrics(tileGrowth)
    HomeColumn(
        fit = fit,
        paddingTop = metrics.paddingTop,
        paddingBottom = metrics.paddingBottom,
        gap = metrics.gap,
        modifier = Modifier.fillMaxSize().padding(horizontal = dimens.screenPaddingHorizontal),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(metrics.gap)) {
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
        }
        MenuGrid(state, onIntent, metrics)
    }
}

/**
 * `02-home`'s flex column: [top] (top bar, logo, daily card, OYNA) from [paddingTop], the tile grid at the bottom
 * (`margin-top: auto`) with at least two [gap]s above it (the column gap around the free space). Writes the natural
 * height (paddings, sections, gaps, grid) to [fit] while it is measured; content taller than the bounds overflows at
 * the bottom.
 */
@Composable
private fun HomeColumn(
    fit: HomeFit,
    paddingTop: Dp,
    paddingBottom: Dp,
    gap: Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Layout(content, modifier) { measurables, constraints ->
        val natural = Constraints(maxWidth = constraints.maxWidth)
        val top = measurables[0].measure(natural)
        val grid = measurables[1].measure(natural)
        val top0 = paddingTop.roundToPx()
        val gaps = gap.roundToPx() * 2
        val naturalHeight = top0 + top.height + gaps + grid.height + paddingBottom.roundToPx()
        fit.naturalHeight = naturalHeight
        val height = if (constraints.hasBoundedHeight) constraints.maxHeight else naturalHeight
        layout(constraints.maxWidth, height) {
            top.place(0, top0)
            grid.place(0, maxOf(top0 + top.height + gaps, height - paddingBottom.roundToPx() - grid.height))
        }
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
