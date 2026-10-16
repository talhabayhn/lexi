package dev.talhabayhan.lexi.feature.levels

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import dev.talhabayhan.lexi.core.designsystem.component.ControlDefaults
import dev.talhabayhan.lexi.core.designsystem.component.DecorShapeKind
import dev.talhabayhan.lexi.core.designsystem.component.EmptyState
import dev.talhabayhan.lexi.core.designsystem.component.LevelCard
import dev.talhabayhan.lexi.core.designsystem.component.LevelCardStyle
import dev.talhabayhan.lexi.core.designsystem.component.NavSquareButton
import dev.talhabayhan.lexi.core.designsystem.component.PressableSurface
import dev.talhabayhan.lexi.core.designsystem.component.Segment
import dev.talhabayhan.lexi.core.designsystem.component.SegmentedControl
import dev.talhabayhan.lexi.core.designsystem.component.StartButton
import dev.talhabayhan.lexi.core.designsystem.format.formatClock
import dev.talhabayhan.lexi.core.designsystem.icon.LexiIcons
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
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
 * `03-level-select`: back button and title, the 2 × 3 level cards (the selected one red), the mode picker and the
 * BAŞLA button, pinned to the bottom. Scrolls only when the screen is too short for the content.
 */
@Composable
internal fun LevelSelectScreen(
    state: LevelSelectUiState,
    onIntent: (LevelSelectIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = LexiTheme.dimens
    BoxWithConstraints(
        modifier
            .fillMaxSize()
            .background(LexiTheme.colors.cream)
            .windowInsetsPadding(WindowInsets.safeDrawing),
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
            verticalArrangement = Arrangement.spacedBy(dimens.gap18),
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
                Text(stringResource(Res.string.levels_title), style = LexiTheme.typography.title)
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
            LevelGrid(state, onIntent)
            ModePicker(state, onIntent)
            Spacer(Modifier.weight(1f))
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
}

@Composable
private fun LevelGrid(state: LevelSelectUiState, onIntent: (LevelSelectIntent) -> Unit) {
    val gap = LexiTheme.dimens.gap12
    Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(gap)) {
        state.levels.chunked(LEVEL_COLUMNS).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
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
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun ModePicker(state: LevelSelectUiState, onIntent: (LevelSelectIntent) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap8)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(Res.string.levels_mode_label), style = LexiTheme.typography.label12)
            // DESIGN-DEBT: the design has no entry to the categories; a small link on the mode label's line, which
            // keeps every designed element where it is. Compose widens its touch target to the platform minimum.
            CategoriesLink(onClick = { onIntent(LevelSelectIntent.OpenCategories) })
        }
        SegmentedControl(
            segments = SelectableModes.map { mode ->
                Segment(stringResource(mode.titleRes()), mode.hint(state.classicTimeLimitSeconds))
            },
            selectedIndex = SelectableModes.indexOf(state.selectedMode),
            onSelect = { onIntent(LevelSelectIntent.SelectMode(SelectableModes[it])) },
            height = ControlDefaults.SegmentHeightWithSubtitle,
            dividerWidth = LexiTheme.borders.regular,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** Underlined "Kategoriler" link: transparent, no frame or shadow, so it does not move when pressed. */
@Composable
private fun CategoriesLink(onClick: () -> Unit) {
    PressableSurface(onClick = onClick, color = Color.Transparent, border = null, shadow = null) {
        Text(stringResource(Res.string.levels_open_categories), style = LexiTheme.typography.link12)
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
