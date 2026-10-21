package dev.talhabayhan.lexi.feature.levels

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import dev.talhabayhan.lexi.core.designsystem.component.CardDefaults
import dev.talhabayhan.lexi.core.designsystem.component.ControlDefaults
import dev.talhabayhan.lexi.core.designsystem.component.DecorShapeKind
import dev.talhabayhan.lexi.core.designsystem.component.EmptyState
import dev.talhabayhan.lexi.core.designsystem.component.LevelCard
import dev.talhabayhan.lexi.core.designsystem.component.LevelCardStyle
import dev.talhabayhan.lexi.core.designsystem.component.LevelSelectDefaults
import dev.talhabayhan.lexi.core.designsystem.component.ListDefaults
import dev.talhabayhan.lexi.core.designsystem.component.NavSquareButton
import dev.talhabayhan.lexi.core.designsystem.component.PressableSurface
import dev.talhabayhan.lexi.core.designsystem.component.Segment
import dev.talhabayhan.lexi.core.designsystem.component.SegmentedControl
import dev.talhabayhan.lexi.core.designsystem.component.StartButton
import dev.talhabayhan.lexi.core.designsystem.format.formatClock
import dev.talhabayhan.lexi.core.designsystem.icon.LexiIcons
import dev.talhabayhan.lexi.core.designsystem.modifier.lexiHeading
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.lexiContentWidth
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.feature.levels.generated.resources.Res
import dev.talhabayhan.lexi.feature.levels.generated.resources.levels_a1
import dev.talhabayhan.lexi.feature.levels.generated.resources.levels_a2
import dev.talhabayhan.lexi.feature.levels.generated.resources.levels_b1
import dev.talhabayhan.lexi.feature.levels.generated.resources.levels_b2
import dev.talhabayhan.lexi.feature.levels.generated.resources.levels_back
import dev.talhabayhan.lexi.feature.levels.generated.resources.levels_c1
import dev.talhabayhan.lexi.feature.levels.generated.resources.levels_caption_classic
import dev.talhabayhan.lexi.feature.levels.generated.resources.levels_caption_hardcore
import dev.talhabayhan.lexi.feature.levels.generated.resources.levels_caption_practice
import dev.talhabayhan.lexi.feature.levels.generated.resources.levels_category_mixed
import dev.talhabayhan.lexi.feature.levels.generated.resources.levels_category_row
import dev.talhabayhan.lexi.feature.levels.generated.resources.levels_error
import dev.talhabayhan.lexi.feature.levels.generated.resources.levels_mode_classic
import dev.talhabayhan.lexi.feature.levels.generated.resources.levels_mode_hardcore
import dev.talhabayhan.lexi.feature.levels.generated.resources.levels_mode_hardcore_hint
import dev.talhabayhan.lexi.feature.levels.generated.resources.levels_mode_label
import dev.talhabayhan.lexi.feature.levels.generated.resources.levels_mode_practice
import dev.talhabayhan.lexi.feature.levels.generated.resources.levels_mode_practice_hint
import dev.talhabayhan.lexi.feature.levels.generated.resources.levels_nat
import dev.talhabayhan.lexi.feature.levels.generated.resources.levels_open_categories
import dev.talhabayhan.lexi.feature.levels.generated.resources.levels_progress
import dev.talhabayhan.lexi.feature.levels.generated.resources.levels_retry
import dev.talhabayhan.lexi.feature.levels.generated.resources.levels_start
import dev.talhabayhan.lexi.feature.levels.generated.resources.levels_start_caption
import dev.talhabayhan.lexi.feature.levels.generated.resources.levels_title
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * `03-level-select`: back button and title, the 2 × 3 level cards (the selected one red), the mode picker, the
 * category row and the BAŞLA button, pinned to the bottom. It steps down [LevelSelectTier]s on short phones; only when
 * even the tightest tier does not fit (huge font) the part above BAŞLA scrolls, BAŞLA stays visible.
 */
@Composable
internal fun LevelSelectScreen(
    state: LevelSelectUiState,
    onIntent: (LevelSelectIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .fillMaxSize()
            .background(LexiTheme.colors.cream)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .lexiContentWidth(),
    ) {
        FitToHeight(Modifier.fillMaxSize()) { tier, fit -> LevelSelectContent(tier, fit, state, onIntent) }
    }
}

/** Heights of one layout tier (`screen-map.md`): regular is the design, the others are DESIGN-DEBT scalings of it. */
internal enum class LevelSelectTier { Regular, Compact, Tight }

private class LevelSelectMetrics(
    val paddingTop: Dp,
    val paddingBottom: Dp,
    val gap: Dp,
    val gridGap: Dp,
    val cardMinHeight: Dp,
    val categoryHeight: Dp,
    val cardPadding: Dp,
    val segmentHeight: Dp,
)

@Composable
private fun LevelSelectTier.metrics(): LevelSelectMetrics {
    val dimens = LexiTheme.dimens
    return when (this) {
        LevelSelectTier.Regular -> LevelSelectMetrics(
            dimens.screenPaddingTop,
            dimens.screenPaddingBottom,
            dimens.gap18,
            dimens.gap12,
            CardDefaults.LevelCardHeight,
            ListDefaults.RowMinHeight,
            CardDefaults.Padding,
            ControlDefaults.SegmentHeightWithSubtitle,
        )

        LevelSelectTier.Compact -> LevelSelectMetrics(
            LevelSelectDefaults.CompactPaddingTop,
            LevelSelectDefaults.CompactPaddingBottom,
            dimens.gap12,
            dimens.gap10,
            LevelSelectDefaults.CompactCardHeight,
            LevelSelectDefaults.CategoryHeight,
            CardDefaults.CompactPadding,
            ControlDefaults.SegmentHeightWithSubtitle,
        )

        LevelSelectTier.Tight -> LevelSelectMetrics(
            dimens.gap8,
            dimens.gap12,
            dimens.gap8,
            dimens.gap8,
            Dp.Unspecified,
            LevelSelectDefaults.CategoryHeight,
            dimens.gap8,
            LevelSelectDefaults.TightSegmentHeight,
        )
    }
}

/** Natural height of one tier's content, written by [LevelSelectColumn] while it is measured. */
private class LevelSelectFit {
    var naturalHeight: Int = 0
}

/**
 * Measures each tier in turn (from regular) at exactly the available size and keeps the first whose natural height
 * fits; the last one otherwise. Each tier is composed once; those measured but not chosen stay unplaced.
 */
@Composable
private fun FitToHeight(modifier: Modifier, content: @Composable (LevelSelectTier, LevelSelectFit) -> Unit) {
    val fits = remember { LevelSelectTier.entries.associateWith { LevelSelectFit() } }
    // Stable slot lambdas: SubcomposeLayout recomposes a slot whenever it gets a new lambda instance.
    val slots = remember(content) {
        LevelSelectTier.entries.associateWith { tier -> @Composable { content(tier, fits.getValue(tier)) } }
    }
    SubcomposeLayout(modifier) { constraints ->
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        val fixed = Constraints.fixed(width, height)
        var placeables = emptyList<Placeable>()
        for (candidate in LevelSelectTier.entries) {
            placeables = subcompose(candidate, slots.getValue(candidate)).map { it.measure(fixed) }
            if (fits.getValue(candidate).naturalHeight <= height) break
        }
        layout(width, height) { placeables.forEach { it.place(0, 0) } }
    }
}

/**
 * The flex column: the first child (scrolls when it does not fit) from the top padding, the second (BAŞLA) pinned above
 * the bottom padding with at least one [gap] to the first. Writes the natural height to [fit] while it is measured.
 */
@Composable
private fun LevelSelectColumn(
    fit: LevelSelectFit,
    paddingTop: Dp,
    paddingBottom: Dp,
    gap: Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Layout(content, modifier) { measurables, constraints ->
        val width = constraints.maxWidth
        val bottom = measurables[1].measure(Constraints(maxWidth = width))
        val top = paddingTop.roundToPx()
        val gapPx = gap.roundToPx()
        val bottomPad = paddingBottom.roundToPx()
        fit.naturalHeight = top + measurables[0].maxIntrinsicHeight(width) + gapPx + bottom.height + bottomPad
        val height = if (constraints.hasBoundedHeight) constraints.maxHeight else fit.naturalHeight
        val room = (height - top - gapPx - bottom.height - bottomPad).coerceAtLeast(0)
        val middle = measurables[0].measure(Constraints(maxWidth = width, maxHeight = room))
        layout(width, height) {
            middle.place(0, top)
            bottom.place(0, height - bottomPad - bottom.height)
        }
    }
}

@Composable
private fun LevelSelectContent(
    tier: LevelSelectTier,
    fit: LevelSelectFit,
    state: LevelSelectUiState,
    onIntent: (LevelSelectIntent) -> Unit,
) {
    val dimens = LexiTheme.dimens
    val metrics = tier.metrics()
    LevelSelectColumn(
        fit = fit,
        paddingTop = metrics.paddingTop,
        paddingBottom = metrics.paddingBottom,
        gap = metrics.gap,
        modifier = Modifier.fillMaxSize().padding(horizontal = dimens.screenPaddingHorizontal),
    ) {
        Column(
            // The room under the last row keeps its hard shadow out of the scroll clip.
            modifier = Modifier.verticalScroll(rememberScrollState()).padding(bottom = LexiTheme.shadows.s6.offset),
            verticalArrangement = Arrangement.spacedBy(metrics.gap),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(dimens.gap12),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NavSquareButton(
                    icon = LexiIcons.Back,
                    contentDescription = stringResource(Res.string.levels_back),
                    onClick = { onIntent(LevelSelectIntent.Back) },
                )
                Text(
                    stringResource(Res.string.levels_title),
                    style = LexiTheme.typography.title,
                    modifier = Modifier.lexiHeading(),
                )
            }
            if (state.hasError) {
                // DESIGN-DEBT: no design for a failed read; the component library's empty state with a retry above
                // the level cards (they show 0 / 0), the game itself reports a content it can't read.
                EmptyState(
                    message = stringResource(Res.string.levels_error),
                    actionText = stringResource(Res.string.levels_retry),
                    onAction = { onIntent(LevelSelectIntent.Retry) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            LevelGrid(state, onIntent, metrics)
            ModePicker(state, onIntent, metrics.segmentHeight)
            // NAT has no category selection (CONTENT.md §5): no row.
            if (state.selectedLevel != Level.NAT) {
                CategoryRow(
                    onClick = { onIntent(LevelSelectIntent.OpenCategories) },
                    height = metrics.categoryHeight,
                )
            }
        }
        StartButton(
            label = stringResource(Res.string.levels_start),
            caption = stringResource(
                Res.string.levels_start_caption,
                state.selectedLevel.name,
                stringResource(state.selectedMode.captionRes()),
                state.letterCount,
            ),
            onClick = { onIntent(LevelSelectIntent.Start) },
        )
    }
}

@Composable
private fun LevelGrid(state: LevelSelectUiState, onIntent: (LevelSelectIntent) -> Unit, metrics: LevelSelectMetrics) {
    val gap = metrics.gridGap
    Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(gap)) {
        state.levels.chunked(LEVEL_COLUMNS).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(gap),
            ) {
                row.forEach { progress ->
                    val selected = progress.level == state.selectedLevel
                    val look = progress.level.look()
                    LevelCard(
                        title = progress.level.name,
                        subtitle = stringResource(progress.level.subtitleRes()),
                        progress = progress.fraction,
                        progressLabel = stringResource(Res.string.levels_progress, progress.learned, progress.total),
                        marker = look.marker,
                        // A red marker would vanish on the selected (red) card: the design's A1 uses yellow there.
                        markerColor = if (selected && look.color == LexiTheme.colors.red) {
                            LexiTheme.colors.yellow
                        } else {
                            look.color
                        },
                        onClick = { onIntent(LevelSelectIntent.SelectLevel(progress.level)) },
                        style = when {
                            selected -> LevelCardStyle.Selected
                            progress.level == Level.NAT -> LevelCardStyle.Inverted
                            else -> LevelCardStyle.Normal
                        },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        minHeight = metrics.cardMinHeight,
                        padding = metrics.cardPadding,
                    )
                }
            }
        }
    }
}

@Composable
private fun ModePicker(state: LevelSelectUiState, onIntent: (LevelSelectIntent) -> Unit, segmentHeight: Dp) {
    Column(verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap8)) {
        Text(
            stringResource(Res.string.levels_mode_label),
            style = LexiTheme.typography.label12,
            modifier = Modifier.lexiHeading(),
        )
        SegmentedControl(
            segments = SelectableModes.map { mode ->
                Segment(stringResource(mode.titleRes()), mode.hint(state.classicTimeLimitSeconds))
            },
            selectedIndex = SelectableModes.indexOf(state.selectedMode),
            onSelect = { onIntent(LevelSelectIntent.SelectMode(SelectableModes[it])) },
            height = segmentHeight,
            dividerWidth = LexiTheme.borders.regular,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * DESIGN-DEBT (needs design, `screen-map.md`): the full-width "Kategori: Karışık" row under the mode picker, built from
 * the component library's pressable white surface with a list row's padding, icon and chevron. BAŞLA always plays the
 * mixed wheel; the row opens the category list.
 */
@Composable
private fun CategoryRow(onClick: () -> Unit, height: Dp) {
    val current = stringResource(Res.string.levels_category_mixed)
    val description = stringResource(Res.string.levels_open_categories, current)
    PressableSurface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().heightIn(min = height).semantics { contentDescription = description },
        shadow = LexiTheme.shadows.s3,
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(ListDefaults.RowPadding),
            horizontalArrangement = Arrangement.spacedBy(ListDefaults.RowGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                LexiIcons.Categories,
                contentDescription = null,
                modifier = Modifier.size(LevelSelectDefaults.IconSize),
            )
            Text(
                stringResource(Res.string.levels_category_row, current),
                style = LexiTheme.typography.caption14SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                // The row's description already says it; the label would be read twice.
                modifier = Modifier.weight(1f).clearAndSetSemantics {},
            )
            Icon(LexiIcons.ChevronRight, contentDescription = null, modifier = Modifier.size(ListDefaults.ChevronSize))
        }
    }
}

private const val LEVEL_COLUMNS = 2

/** The marker shape and color of a level's card, as in the design. */
private class LevelLook(val marker: DecorShapeKind, val color: Color)

@Composable
private fun Level.look(): LevelLook {
    val colors = LexiTheme.colors
    return when (this) {
        Level.A1, Level.B2 -> LevelLook(DecorShapeKind.Square, colors.yellow)
        Level.A2, Level.C1 -> LevelLook(DecorShapeKind.Circle, colors.blue)
        Level.B1, Level.NAT -> LevelLook(DecorShapeKind.Leaf, colors.red)
    }
}

private fun Level.subtitleRes(): StringResource = when (this) {
    Level.A1 -> Res.string.levels_a1
    Level.A2 -> Res.string.levels_a2
    Level.B1 -> Res.string.levels_b1
    Level.B2 -> Res.string.levels_b2
    Level.C1 -> Res.string.levels_c1
    Level.NAT -> Res.string.levels_nat
}

private fun GameMode.titleRes(): StringResource = when (this) {
    GameMode.PRACTICE -> Res.string.levels_mode_practice
    GameMode.HARDCORE -> Res.string.levels_mode_hardcore
    else -> Res.string.levels_mode_classic
}

private fun GameMode.captionRes(): StringResource = when (this) {
    GameMode.PRACTICE -> Res.string.levels_caption_practice
    GameMode.HARDCORE -> Res.string.levels_caption_hardcore
    else -> Res.string.levels_caption_classic
}

/** The small second line of a mode segment: Classic shows its time ("3:00"), the others a short rule. */
@Composable
private fun GameMode.hint(classicSeconds: Int): String = when (this) {
    GameMode.PRACTICE -> stringResource(Res.string.levels_mode_practice_hint)
    GameMode.HARDCORE -> stringResource(Res.string.levels_mode_hardcore_hint)
    else -> formatClock(classicSeconds)
}

// design sample, not user-facing
internal val LevelSelectSampleState = LevelSelectUiState(
    levels = persistentListOf(
        LevelProgressUi(Level.A1, 412, 850),
        LevelProgressUi(Level.A2, 96, 900),
        LevelProgressUi(Level.B1, 0, 1200),
        LevelProgressUi(Level.B2, 0, 1400),
        LevelProgressUi(Level.C1, 0, 1500),
        LevelProgressUi(Level.NAT, 0, 800),
    ),
)

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun LevelSelectScreenPreview() {
    LexiTheme {
        LevelSelectScreen(state = LevelSelectSampleState, onIntent = {})
    }
}
