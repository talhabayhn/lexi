package dev.talhabayhan.lexi.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import dev.talhabayhan.lexi.core.designsystem.component.BottomSheet
import dev.talhabayhan.lexi.core.designsystem.component.CategoryCard
import dev.talhabayhan.lexi.core.designsystem.component.CategoryCardDefaults
import dev.talhabayhan.lexi.core.designsystem.component.DailyCard
import dev.talhabayhan.lexi.core.designsystem.component.DailyCardStatus
import dev.talhabayhan.lexi.core.designsystem.component.DecorShapeKind
import dev.talhabayhan.lexi.core.designsystem.component.EmptyState
import dev.talhabayhan.lexi.core.designsystem.component.HomeDefaults
import dev.talhabayhan.lexi.core.designsystem.component.HomePlayButton
import dev.talhabayhan.lexi.core.designsystem.component.HomeTierSize
import dev.talhabayhan.lexi.core.designsystem.component.IconButtonDefaults
import dev.talhabayhan.lexi.core.designsystem.component.KeyTile
import dev.talhabayhan.lexi.core.designsystem.component.LevelChipRow
import dev.talhabayhan.lexi.core.designsystem.component.LevelHeroCard
import dev.talhabayhan.lexi.core.designsystem.component.LexiLogo
import dev.talhabayhan.lexi.core.designsystem.component.MixedWheelCard
import dev.talhabayhan.lexi.core.designsystem.component.NavSquareButton
import dev.talhabayhan.lexi.core.designsystem.component.PickerRow
import dev.talhabayhan.lexi.core.designsystem.component.Segment
import dev.talhabayhan.lexi.core.designsystem.component.SegmentedControl
import dev.talhabayhan.lexi.core.designsystem.component.SlidingToast
import dev.talhabayhan.lexi.core.designsystem.format.formatClock
import dev.talhabayhan.lexi.core.designsystem.icon.LexiIcons
import dev.talhabayhan.lexi.core.designsystem.modifier.lexiHeading
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.WithCappedFontScale
import dev.talhabayhan.lexi.core.designsystem.theme.lexiContentWidth
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.goal.DailyGoalProgress
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.settings.Settings
import dev.talhabayhan.lexi.feature.home.generated.resources.Res
import dev.talhabayhan.lexi.feature.home.generated.resources.home_build_label
import dev.talhabayhan.lexi.feature.home.generated.resources.home_caption_classic
import dev.talhabayhan.lexi.feature.home.generated.resources.home_caption_hardcore
import dev.talhabayhan.lexi.feature.home.generated.resources.home_caption_practice
import dev.talhabayhan.lexi.feature.home.generated.resources.home_category_card_desc
import dev.talhabayhan.lexi.feature.home.generated.resources.home_category_error
import dev.talhabayhan.lexi.feature.home.generated.resources.home_category_label
import dev.talhabayhan.lexi.feature.home.generated.resources.home_category_mixed
import dev.talhabayhan.lexi.feature.home.generated.resources.home_category_mixed_subtitle
import dev.talhabayhan.lexi.feature.home.generated.resources.home_category_mixed_title
import dev.talhabayhan.lexi.feature.home.generated.resources.home_category_progress
import dev.talhabayhan.lexi.feature.home.generated.resources.home_category_progress_done
import dev.talhabayhan.lexi.feature.home.generated.resources.home_category_row_desc
import dev.talhabayhan.lexi.feature.home.generated.resources.home_category_sheet_close
import dev.talhabayhan.lexi.feature.home.generated.resources.home_category_sheet_hint
import dev.talhabayhan.lexi.feature.home.generated.resources.home_category_sheet_title
import dev.talhabayhan.lexi.feature.home.generated.resources.home_daily_badge_done
import dev.talhabayhan.lexi.feature.home.generated.resources.home_daily_badge_solve
import dev.talhabayhan.lexi.feature.home.generated.resources.home_daily_card_title
import dev.talhabayhan.lexi.feature.home.generated.resources.home_daily_desc_done
import dev.talhabayhan.lexi.feature.home.generated.resources.home_daily_desc_solve
import dev.talhabayhan.lexi.feature.home.generated.resources.home_daily_desc_unavailable
import dev.talhabayhan.lexi.feature.home.generated.resources.home_daily_done_hours_minutes
import dev.talhabayhan.lexi.feature.home.generated.resources.home_daily_done_minutes
import dev.talhabayhan.lexi.feature.home.generated.resources.home_daily_title
import dev.talhabayhan.lexi.feature.home.generated.resources.home_daily_unavailable
import dev.talhabayhan.lexi.feature.home.generated.resources.home_joker_gift_combined
import dev.talhabayhan.lexi.feature.home.generated.resources.home_joker_gift_daily
import dev.talhabayhan.lexi.feature.home.generated.resources.home_joker_gift_streak
import dev.talhabayhan.lexi.feature.home.generated.resources.home_key_badge_overflow
import dev.talhabayhan.lexi.feature.home.generated.resources.home_key_duel
import dev.talhabayhan.lexi.feature.home.generated.resources.home_key_duel_desc
import dev.talhabayhan.lexi.feature.home.generated.resources.home_key_exams
import dev.talhabayhan.lexi.feature.home.generated.resources.home_key_exams_desc
import dev.talhabayhan.lexi.feature.home.generated.resources.home_key_notebook
import dev.talhabayhan.lexi.feature.home.generated.resources.home_key_notebook_desc
import dev.talhabayhan.lexi.feature.home.generated.resources.home_key_notebook_desc_none
import dev.talhabayhan.lexi.feature.home.generated.resources.home_key_stats
import dev.talhabayhan.lexi.feature.home.generated.resources.home_key_stats_desc
import dev.talhabayhan.lexi.feature.home.generated.resources.home_level_a1
import dev.talhabayhan.lexi.feature.home.generated.resources.home_level_a2
import dev.talhabayhan.lexi.feature.home.generated.resources.home_level_b1
import dev.talhabayhan.lexi.feature.home.generated.resources.home_level_b2
import dev.talhabayhan.lexi.feature.home.generated.resources.home_level_c1
import dev.talhabayhan.lexi.feature.home.generated.resources.home_level_chip_desc
import dev.talhabayhan.lexi.feature.home.generated.resources.home_level_hero_desc
import dev.talhabayhan.lexi.feature.home.generated.resources.home_level_nat
import dev.talhabayhan.lexi.feature.home.generated.resources.home_level_progress
import dev.talhabayhan.lexi.feature.home.generated.resources.home_mode_classic
import dev.talhabayhan.lexi.feature.home.generated.resources.home_mode_hardcore
import dev.talhabayhan.lexi.feature.home.generated.resources.home_mode_hardcore_hint
import dev.talhabayhan.lexi.feature.home.generated.resources.home_mode_practice
import dev.talhabayhan.lexi.feature.home.generated.resources.home_mode_practice_hint
import dev.talhabayhan.lexi.feature.home.generated.resources.home_notice_dismiss
import dev.talhabayhan.lexi.feature.home.generated.resources.home_play
import dev.talhabayhan.lexi.feature.home.generated.resources.home_play_badge
import dev.talhabayhan.lexi.feature.home.generated.resources.home_retry
import dev.talhabayhan.lexi.feature.home.generated.resources.home_settings
import dev.talhabayhan.lexi.feature.home.generated.resources.home_tagline
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * `02c-home-v2`: logo and settings, the daily card (title, description, one status badge), "ÇARKINI KUR" (the selected
 * level's hero card, six level chips, the mode control, the category row) with OYNA right under it, and the four footer
 * key tiles pinned at the bottom. When even the tightest [HomeTierSize] does not fit, OYNA is pinned above the footer
 * and the part above it scrolls ([HomeLayout], `home-v2-notes.md` §1).
 * The blue circle and red half-pill are edge-to-edge decoration drawn behind everything at the screen's right edge
 * beside the daily card and at its left edge beside the footer, so they never sit behind a chip or the hero card. The
 * daily gift is a floating toast shown only while [noticeVisible] (the screen is resumed); it never affects the layout.
 */
@Composable
internal fun HomeScreen(
    state: HomeUiState,
    onIntent: (HomeIntent) -> Unit,
    modifier: Modifier = Modifier,
    noticeVisible: Boolean = true,
) {
    val colors = LexiTheme.colors
    val decor = remember { DecorAnchors() }
    val circle = LexiTheme.shapes.circle
    val halfPill = LexiTheme.shapes.halfPill
    Box(
        modifier
            .fillMaxSize()
            .background(colors.cream)
            .onGloballyPositioned { decor.root = it }
            .drawBehind { drawDecor(decor, circle, halfPill, colors.blue, colors.red) },
    ) {
        FitToHeight(
            Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).lexiContentWidth(),
            onChosen = { decor.chosen.value = it },
        ) { layout, fit -> HomeContent(layout, fit, state, onIntent, decor) }
        // Floats above the content: it takes no layout space, so the gift never resizes or moves Home's parts.
        val notice = state.dailyGiftNotice
        if (notice != null && noticeVisible) {
            val tier = decor.chosen.value.tier
            GiftToast(
                notice = notice,
                onDismiss = { onIntent(HomeIntent.DismissNotice) },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .lexiContentWidth()
                    .padding(
                        start = tier.paddingHorizontal,
                        end = tier.paddingHorizontal,
                        top = tier.paddingTop + HomeDefaults.HeaderHeight + LexiTheme.dimens.gap8,
                    ),
            )
        }
        CategorySheet(state, onIntent)
    }
    // System back closes the category sheet instead of leaving Home.
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = state.categorySheetOpen,
        onBackCompleted = { onIntent(HomeIntent.CloseCategories) },
    )
}

/**
 * One layout [FitToHeight] tries, in order: each tier with OYNA right under the category row (nothing scrolls), and
 * as the last resort the tightest tier with OYNA pinned above the footer and the part above it scrolling
 * (`home-v2-notes.md` §1).
 */
internal enum class HomeLayout(val tier: HomeTierSize, val pinnedPlay: Boolean) {
    Regular(HomeTierSize.Regular, pinnedPlay = false),
    Compact(HomeTierSize.Compact, pinnedPlay = false),
    Tight(HomeTierSize.Tight, pinnedPlay = false),
    TightPinned(HomeTierSize.Tight, pinnedPlay = true),
}

/** Where the decoration goes: the daily card and the footer row, in the screen's coordinates (read while drawing). */
@Stable
private class DecorAnchors {
    var root: LayoutCoordinates? = null

    /**
     * The tier [FitToHeight] placed (written while it measures). Tiers measured but not chosen stay attached with stale
     * positions, so only the chosen tier's anchors are drawn.
     */
    val chosen = mutableStateOf(HomeLayout.Regular)
    private val daily = mutableMapOf<HomeLayout, LayoutCoordinates>()
    private val footer = mutableMapOf<HomeLayout, LayoutCoordinates>()

    /** Bumped on every new position, so the decoration is redrawn in the same frame. */
    val version = mutableIntStateOf(0)

    fun onDaily(layout: HomeLayout, coordinates: LayoutCoordinates) {
        daily[layout] = coordinates
        version.intValue++
    }

    fun onFooter(layout: HomeLayout, coordinates: LayoutCoordinates) {
        footer[layout] = coordinates
        version.intValue++
    }

    /** Vertical centre of [anchor] in the screen's coordinates; null while not laid out. */
    fun centreY(anchor: (DecorAnchors) -> LayoutCoordinates?): Float? {
        val screen = root?.takeIf { it.isAttached } ?: return null
        val coordinates = anchor(this)?.takeIf { it.isAttached } ?: return null
        return screen.localPositionOf(coordinates, Offset.Zero).y + coordinates.size.height / 2f
    }

    val dailyAnchor: (DecorAnchors) -> LayoutCoordinates? = { it.daily[it.chosen.value] }
    val footerAnchor: (DecorAnchors) -> LayoutCoordinates? = { it.footer[it.chosen.value] }
}

private fun DrawScope.drawDecor(decor: DecorAnchors, circle: Shape, halfPill: Shape, blue: Color, red: Color) {
    decor.version.intValue
    decor.centreY(decor.dailyAnchor)?.let { y ->
        val diameter = HomeDefaults.CircleSize.toPx()
        translate(left = size.width - diameter / 2, top = y - diameter / 2) {
            drawOutline(circle.createOutline(Size(diameter, diameter), layoutDirection, this), blue)
        }
    }
    decor.centreY(decor.footerAnchor)?.let { y ->
        val width = HomeDefaults.PillWidth.toPx()
        val height = HomeDefaults.PillHeight.toPx()
        translate(left = HomeDefaults.PillVisible.toPx() - width, top = y - height / 2) {
            drawOutline(halfPill.createOutline(Size(width, height), layoutDirection, this), red)
        }
    }
}

/** The daily joker gift as an info toast below the logo (never over the gear); tap to dismiss. */
@Composable
private fun GiftToast(notice: DailyGiftNotice, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    val message = when (notice.type) {
        DailyGiftNoticeType.Daily -> stringResource(Res.string.home_joker_gift_daily, notice.amount)
        DailyGiftNoticeType.Streak -> stringResource(Res.string.home_joker_gift_streak, notice.amount)
        DailyGiftNoticeType.Combined -> stringResource(Res.string.home_joker_gift_combined, notice.amount)
    }
    SlidingToast(
        message = message,
        onDismissed = onDismiss,
        visibleMillis = NOTICE_VISIBLE_MILLIS,
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                onClickLabel = stringResource(Res.string.home_notice_dismiss),
                role = Role.Button,
                onClick = onDismiss,
            ),
    )
}

/** Natural height of one tier's content, written by [HomeColumn] while it is measured. */
private class HomeFit {
    var naturalHeight: Int = 0
}

/**
 * Measures the content of each tier in turn (from regular) at exactly the available size and keeps the first whose
 * natural height fits; the last one (whose upper part then scrolls) otherwise. Each tier is composed once and reused,
 * so the usual case (regular fits) costs a single composition. Tiers measured but not chosen stay unplaced (hidden
 * from accessibility).
 */
@Composable
private fun FitToHeight(
    modifier: Modifier,
    onChosen: (HomeLayout) -> Unit,
    content: @Composable (HomeLayout, HomeFit) -> Unit,
) {
    val fits = remember { HomeLayout.entries.associateWith { HomeFit() } }
    // Stable slot lambdas: SubcomposeLayout recomposes a slot whenever it gets a new lambda instance.
    val slots = remember(content) {
        HomeLayout.entries.associateWith { layout -> @Composable { content(layout, fits.getValue(layout)) } }
    }
    SubcomposeLayout(modifier) { constraints ->
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        val fixed = Constraints.fixed(width, height)
        var placeables = emptyList<Placeable>()
        var chosen = HomeLayout.entries.last()
        for (candidate in HomeLayout.entries) {
            placeables = subcompose(candidate, slots.getValue(candidate)).map { it.measure(fixed) }
            chosen = candidate
            if (fits.getValue(candidate).naturalHeight <= height) break
        }
        onChosen(chosen)
        layout(width, height) { placeables.forEach { it.place(0, 0) } }
    }
}

/**
 * The flex column: the first child (the scrolling part, OYNA included unless pinned) from the top padding, the second
 * (the footer, with OYNA when pinned) above the bottom padding with at least [gap] to the first; the free height in
 * between stays empty (the design's spacer). Writes the natural height to [fit] while it is measured.
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
        val width = constraints.maxWidth
        val pinned = measurables[1].measure(Constraints(maxWidth = width))
        val top = paddingTop.roundToPx()
        val gapPx = gap.roundToPx()
        val bottomPad = paddingBottom.roundToPx()
        fit.naturalHeight = top + measurables[0].maxIntrinsicHeight(width) + gapPx + pinned.height + bottomPad
        val height = if (constraints.hasBoundedHeight) constraints.maxHeight else fit.naturalHeight
        val room = (height - top - gapPx - pinned.height - bottomPad).coerceAtLeast(0)
        val upper = measurables[0].measure(Constraints(maxWidth = width, maxHeight = room))
        layout(width, height) {
            upper.place(0, top)
            pinned.place(0, height - bottomPad - pinned.height)
        }
    }
}

@Composable
private fun HomeContent(
    layout: HomeLayout,
    fit: HomeFit,
    state: HomeUiState,
    onIntent: (HomeIntent) -> Unit,
    decor: DecorAnchors,
) {
    val tier = layout.tier
    val play: @Composable () -> Unit = {
        WithCappedFontScale(HomeDefaults.CONTROL_FONT_SCALE_CAP) {
            HomePlayButton(
                label = stringResource(Res.string.home_play),
                badge = stringResource(
                    Res.string.home_play_badge,
                    state.selectedLevel.name,
                    stringResource(state.selectedMode.captionRes()),
                    state.letterCount,
                ),
                onClick = { onIntent(HomeIntent.Play) },
                tier = tier,
                // The design's OYNA leaves its 6dp shadow inside the content width.
                modifier = Modifier.padding(end = LexiTheme.shadows.s6.offset),
            )
        }
    }
    HomeColumn(
        fit = fit,
        paddingTop = tier.paddingTop,
        paddingBottom = tier.paddingBottom,
        gap = tier.blockGap,
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = tier.paddingHorizontal)
                // The room under the last row keeps its hard shadow (OYNA's s6 or the hero's s5) out of the clip.
                .padding(bottom = LexiTheme.shadows.s6.offset),
        ) {
            Header(onIntent, tier)
            Spacer(Modifier.height(tier.headerGap))
            WithCappedFontScale(HomeDefaults.DAILY_FONT_SCALE_CAP) {
                DailyWheelCard(state, onIntent, tier, Modifier.onGloballyPositioned { decor.onDaily(layout, it) })
            }
            Spacer(Modifier.height(tier.blockGap))
            Text(
                stringResource(Res.string.home_build_label),
                style = LexiTheme.typography.label12,
                modifier = Modifier.lexiHeading(),
            )
            Spacer(Modifier.height(tier.labelGap))
            BuildBlock(state, onIntent, tier)
            if (!layout.pinnedPlay) {
                // OYNA follows the category row; the free height stays under it, above the footer.
                Spacer(Modifier.height(tier.blockGap))
                play()
            }
        }
        Column(
            Modifier.padding(horizontal = tier.paddingHorizontal),
            verticalArrangement = Arrangement.spacedBy(tier.blockGap),
        ) {
            // Too short for everything: OYNA is pinned above the footer so it is never off screen.
            if (layout.pinnedPlay) play()
            WithCappedFontScale(HomeDefaults.CONTROL_FONT_SCALE_CAP) {
                Footer(state, onIntent, tier, Modifier.onGloballyPositioned { decor.onFooter(layout, it) })
            }
        }
    }
}

/** Logo (36dp blocks) and the tagline; the gear on the right, read first. */
@Composable
private fun Header(onIntent: (HomeIntent) -> Unit, tier: HomeTierSize) {
    Row(
        modifier = Modifier.fillMaxWidth().semantics { isTraversalGroup = true },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f).padding(end = LexiTheme.dimens.gap8),
            verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap4),
        ) {
            LexiLogo(size = tier.logo)
            WithCappedFontScale(HomeDefaults.CONTROL_FONT_SCALE_CAP) {
                Text(stringResource(Res.string.home_tagline), style = LexiTheme.typography.tagline11)
            }
        }
        NavSquareButton(
            icon = LexiIcons.Settings,
            contentDescription = stringResource(Res.string.home_settings),
            onClick = { onIntent(HomeIntent.OpenSettings) },
            iconSize = IconButtonDefaults.IconSizeMedium,
            modifier = Modifier.semantics { traversalIndex = -1f },
        )
    }
}

/** The daily card's three states (`02c-home-v2`); streak and daily goal are kept in the state but not shown here. */
@Composable
private fun DailyWheelCard(
    state: HomeUiState,
    onIntent: (HomeIntent) -> Unit,
    tier: HomeTierSize,
    modifier: Modifier = Modifier,
) {
    val title = stringResource(Res.string.home_daily_card_title)
    when (val daily = state.dailyState) {
        DailyCardState.Available -> DailyCard(
            title = title,
            description = stringResource(Res.string.home_daily_title),
            status = DailyCardStatus.Solve,
            badgeLabel = stringResource(Res.string.home_daily_badge_solve),
            contentDescription = stringResource(Res.string.home_daily_desc_solve),
            onClick = { onIntent(HomeIntent.OpenDaily) },
            modifier = modifier,
            size = tier.daily,
        )

        is DailyCardState.AlreadyPlayed -> DailyCard(
            title = title,
            description = if (daily.hoursUntilNext > 0) {
                stringResource(
                    Res.string.home_daily_done_hours_minutes,
                    daily.score,
                    daily.hoursUntilNext,
                    daily.minutesUntilNext,
                )
            } else {
                stringResource(Res.string.home_daily_done_minutes, daily.score, daily.minutesUntilNext)
            },
            status = DailyCardStatus.Completed,
            badgeLabel = stringResource(Res.string.home_daily_badge_done),
            contentDescription = stringResource(Res.string.home_daily_desc_done, daily.score),
            onClick = {},
            modifier = modifier,
            size = tier.daily,
        )

        DailyCardState.Unavailable -> DailyCard(
            title = title,
            description = stringResource(Res.string.home_daily_unavailable),
            status = DailyCardStatus.Unavailable,
            badgeLabel = null,
            contentDescription = stringResource(Res.string.home_daily_desc_unavailable),
            onClick = {},
            modifier = modifier,
            size = tier.daily,
        )
    }
}

/** Hero card, level chips, mode control and the category row (hidden for a level without categories). */
@Composable
private fun BuildBlock(state: HomeUiState, onIntent: (HomeIntent) -> Unit, tier: HomeTierSize) {
    Column(verticalArrangement = Arrangement.spacedBy(tier.buildGap)) {
        val selected = state.selectedProgress
        val name = stringResource(state.selectedLevel.nameRes())
        val count = if (selected.total > 0) {
            stringResource(Res.string.home_level_progress, selected.learned, selected.total)
        } else {
            ""
        }
        LevelHeroCard(
            code = state.selectedLevel.name,
            name = name,
            count = count,
            progress = selected.fraction,
            description = stringResource(
                Res.string.home_level_hero_desc,
                state.selectedLevel.name,
                name,
                selected.learned,
                selected.total,
            ),
            minHeight = tier.heroMinHeight,
            paddingVertical = tier.heroPaddingVertical,
        )
        WithCappedFontScale(HomeDefaults.CONTROL_FONT_SCALE_CAP) {
            Column(verticalArrangement = Arrangement.spacedBy(tier.buildGap)) {
                val levels = Level.entries
                LevelChipRow(
                    labels = levels.map { it.name },
                    descriptions = levels.map {
                        stringResource(Res.string.home_level_chip_desc, it.name, stringResource(it.nameRes()))
                    },
                    selectedIndex = levels.indexOf(state.selectedLevel),
                    onSelect = { onIntent(HomeIntent.SelectLevel(levels[it])) },
                    chipHeight = tier.chipHeight,
                )
                SegmentedControl(
                    segments = Settings.HOME_MODES.map { mode ->
                        Segment(stringResource(mode.titleRes()), mode.hint(state.classicTimeLimitSeconds))
                    },
                    selectedIndex = Settings.HOME_MODES.indexOf(state.selectedMode),
                    onSelect = { onIntent(HomeIntent.SelectMode(Settings.HOME_MODES[it])) },
                    height = tier.modeHeight,
                    dividerWidth = LexiTheme.borders.regular,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (state.showCategoryRow) {
                    val value = categoryName(state)
                    PickerRow(
                        label = stringResource(Res.string.home_category_label),
                        value = value,
                        description = stringResource(Res.string.home_category_row_desc, value),
                        onClick = { onIntent(HomeIntent.OpenCategories) },
                        height = tier.pickerHeight,
                    )
                }
            }
        }
    }
}

/**
 * The category row's value: the selected category's name, Karışık for none (also when the list is unreadable); blank
 * for the moment a stored category's name is still being read, so the row never shows a wrong value.
 */
@Composable
private fun categoryName(state: HomeUiState): String {
    val selected = state.selectedCategoryUi
    return when {
        selected != null -> selected.localizedName()
        state.categories is CategoriesState.Loading && state.selectedCategory != null -> ""
        else -> stringResource(Res.string.home_category_mixed)
    }
}

/** Category names are content in both languages; the UI language picks one (as `07-categories` did). */
@Composable
private fun CategoryUi.localizedName(): String = if (Locale.current.language == TURKISH) nameTr else nameEn

private const val TURKISH = "tr"

@Composable
private fun Footer(state: HomeUiState, onIntent: (HomeIntent) -> Unit, tier: HomeTierSize, modifier: Modifier) {
    val due = state.dueReviews
    val badge = when {
        due > MAX_BADGE_COUNT -> stringResource(Res.string.home_key_badge_overflow)
        due > 0 -> due.toString()
        else -> null
    }
    val notebookDescription = if (due > 0) {
        pluralStringResource(Res.plurals.home_key_notebook_desc, due, due)
    } else {
        stringResource(Res.string.home_key_notebook_desc_none)
    }
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(HomeDefaults.FooterGap)) {
        val key = Modifier.weight(1f)
        KeyTile(
            icon = LexiIcons.Notebook,
            label = stringResource(Res.string.home_key_notebook),
            description = notebookDescription,
            onClick = { onIntent(HomeIntent.OpenNotebook) },
            modifier = key,
            keySize = tier.keySize,
            labelGap = tier.keyLabelGap,
            badge = badge,
        )
        KeyTile(
            icon = LexiIcons.Stats,
            label = stringResource(Res.string.home_key_stats),
            description = stringResource(Res.string.home_key_stats_desc),
            onClick = { onIntent(HomeIntent.OpenStats) },
            modifier = key,
            keySize = tier.keySize,
            labelGap = tier.keyLabelGap,
        )
        KeyTile(
            icon = LexiIcons.Exams,
            label = stringResource(Res.string.home_key_exams),
            description = stringResource(Res.string.home_key_exams_desc),
            onClick = { onIntent(HomeIntent.OpenExamPacks) },
            modifier = key,
            keySize = tier.keySize,
            labelGap = tier.keyLabelGap,
        )
        KeyTile(
            icon = LexiIcons.Duel,
            label = stringResource(Res.string.home_key_duel),
            description = stringResource(Res.string.home_key_duel_desc),
            onClick = { onIntent(HomeIntent.OpenDuel) },
            modifier = key,
            keySize = tier.keySize,
            labelGap = tier.keyLabelGap,
        )
    }
}

/** The badge shows at most 99, then "99+" (`home-v2-notes.md` §1). */
private const val MAX_BADGE_COUNT = 99

/**
 * The category sheet (`02c-home-v2`, the `07-categories` cards): a grab handle, at most 85 % of the screen high, the
 * cards scroll under the title with a fade at the bottom. The "Karışık çark" card first, then the level's categories in
 * two columns (one from font scale 1.3 or when a card would get narrower than 140dp). A card only selects (and closes
 * the sheet); OYNA starts the game. A read failure shows the empty state with "Tekrar dene".
 */
@Composable
private fun CategorySheet(state: HomeUiState, onIntent: (HomeIntent) -> Unit) {
    BottomSheet(
        visible = state.categorySheetOpen,
        onDismissRequest = { onIntent(HomeIntent.CloseCategories) },
        title = stringResource(Res.string.home_category_sheet_title),
        closeContentDescription = stringResource(Res.string.home_category_sheet_close),
        maxHeightFraction = HomeDefaults.SHEET_MAX_HEIGHT_FRACTION,
        showHandle = true,
        scrollContent = true,
    ) {
        Text(
            stringResource(Res.string.home_category_sheet_hint, state.selectedLevel.name),
            style = LexiTheme.typography.caption13,
            color = LexiTheme.colors.inkMuted,
        )
        when (val categories = state.categories) {
            CategoriesState.Loading -> Unit

            CategoriesState.Error -> EmptyState(
                message = stringResource(Res.string.home_category_error),
                actionText = stringResource(Res.string.home_retry),
                onAction = { onIntent(HomeIntent.RetryCategories) },
                modifier = Modifier.fillMaxWidth(),
            )

            is CategoriesState.Loaded -> CategoryChoices(state, categories.categories, onIntent)
        }
    }
}

@Composable
private fun CategoryChoices(state: HomeUiState, categories: List<CategoryUi>, onIntent: (HomeIntent) -> Unit) {
    val level = state.selectedProgress
    val gap = CategoryCardDefaults.GridGap
    Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(gap)) {
        val mixedTitle = stringResource(Res.string.home_category_mixed_title)
        MixedWheelCard(
            title = mixedTitle,
            subtitle = stringResource(
                Res.string.home_category_mixed_subtitle,
                state.selectedLevel.name,
                state.letterCount,
            ),
            trailing = level.total.toString(),
            description = stringResource(Res.string.home_category_card_desc, mixedTitle, level.learned, level.total),
            onClick = { onIntent(HomeIntent.SelectCategory(null)) },
            selected = state.selectedCategory == null,
        )
        BoxWithConstraints {
            val oneColumn = LocalDensity.current.fontScale >= CategoryCardDefaults.ONE_COLUMN_FONT_SCALE ||
                (maxWidth - gap) / CATEGORY_COLUMNS < CategoryCardDefaults.MinColumnWidth
            val columns = if (oneColumn) 1 else CATEGORY_COLUMNS
            Column(verticalArrangement = Arrangement.spacedBy(gap)) {
                categories.withIndex().chunked(columns).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(gap),
                    ) {
                        row.forEach { (index, category) ->
                            CategoryChoice(index, category, state, onIntent, Modifier.weight(1f).fillMaxHeight())
                        }
                        // An odd last row keeps its card at half width.
                        repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChoice(
    index: Int,
    category: CategoryUi,
    state: HomeUiState,
    onIntent: (HomeIntent) -> Unit,
    modifier: Modifier,
) {
    val colors = LexiTheme.colors
    // The `07-categories` cycle down the list: red square, yellow circle, blue leaf.
    val (marker, markerColor) = when (index % 3) {
        0 -> DecorShapeKind.Square to colors.red
        1 -> DecorShapeKind.Circle to colors.yellow
        else -> DecorShapeKind.Leaf to colors.blue
    }
    val name = category.localizedName()
    CategoryCard(
        title = name,
        progress = if (category.total > 0) category.learned.toFloat() / category.total else 0f,
        caption = stringResource(
            if (category.isComplete) Res.string.home_category_progress_done else Res.string.home_category_progress,
            category.learned,
            category.total,
        ),
        marker = marker,
        markerColor = markerColor,
        description = stringResource(Res.string.home_category_card_desc, name, category.learned, category.total),
        onClick = { onIntent(HomeIntent.SelectCategory(category.id)) },
        modifier = modifier,
        complete = category.isComplete,
        selected = state.selectedCategory == category.id,
    )
}

private const val CATEGORY_COLUMNS = 2

/**
 * How long the daily-gift notice stays. Longer than the design's 2.5 s toast: a screen reader needs time to speak it
 * (the notice is a live region) and the user can still dismiss it with a tap.
 */
private const val NOTICE_VISIBLE_MILLIS = 6_000L

private fun Level.nameRes(): StringResource = when (this) {
    Level.A1 -> Res.string.home_level_a1
    Level.A2 -> Res.string.home_level_a2
    Level.B1 -> Res.string.home_level_b1
    Level.B2 -> Res.string.home_level_b2
    Level.C1 -> Res.string.home_level_c1
    Level.NAT -> Res.string.home_level_nat
}

private fun GameMode.titleRes(): StringResource = when (this) {
    GameMode.PRACTICE -> Res.string.home_mode_practice
    GameMode.HARDCORE -> Res.string.home_mode_hardcore
    else -> Res.string.home_mode_classic
}

private fun GameMode.captionRes(): StringResource = when (this) {
    GameMode.PRACTICE -> Res.string.home_caption_practice
    GameMode.HARDCORE -> Res.string.home_caption_hardcore
    else -> Res.string.home_caption_classic
}

/** The small second line of a mode cell: Classic shows its time ("3:00"), the others a short rule. */
@Composable
private fun GameMode.hint(classicSeconds: Int): String = when (this) {
    GameMode.PRACTICE -> stringResource(Res.string.home_mode_practice_hint)
    GameMode.HARDCORE -> stringResource(Res.string.home_mode_hardcore_hint)
    else -> formatClock(classicSeconds)
}

// design sample, not user-facing
internal val HomeSampleState = HomeUiState(
    streakDays = 7,
    dueReviews = 24,
    dailyDay = 16,
    dailyMonth = 11,
    dailyGoalProgress = DailyGoalProgress(done = 0, goal = 1, reached = false),
    levels = persistentListOf(
        LevelProgressUi(Level.A1, 412, 850),
        LevelProgressUi(Level.A2, 96, 900),
        LevelProgressUi(Level.B1, 0, 1200),
        LevelProgressUi(Level.B2, 38, 1400),
        LevelProgressUi(Level.C1, 0, 1500),
        LevelProgressUi(Level.NAT, 0, 800),
    ),
    // The A1 categories of the category sheet (`02c-home-v2` frame 04).
    categories = CategoriesState.Loaded(
        persistentListOf(
            CategoryUi(CategoryId("daily-life"), "Günlük hayat", "Daily life", 64, 120),
            CategoryUi(CategoryId("food-drink"), "Yiyecek ve içecek", "Food and drink", 80, 110),
            CategoryUi(CategoryId("home"), "Ev ve eşyalar", "Home and things", 41, 90),
            CategoryUi(CategoryId("family-people"), "Aile ve insanlar", "Family and people", 55, 100),
            CategoryUi(CategoryId("colors-numbers"), "Renkler ve sayılar", "Colours and numbers", 26, 26),
        ),
    ),
)

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun HomeScreenPreview() {
    LexiTheme {
        HomeScreen(state = HomeSampleState, onIntent = {})
    }
}
