package dev.talhabayhan.lexi.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import dev.talhabayhan.lexi.core.designsystem.component.DecorShapeKind
import dev.talhabayhan.lexi.core.designsystem.component.LevelBadgeStyle
import dev.talhabayhan.lexi.core.designsystem.component.LevelCard
import dev.talhabayhan.lexi.core.designsystem.component.LevelCardStyle
import dev.talhabayhan.lexi.core.designsystem.component.LexiTextLink
import dev.talhabayhan.lexi.core.designsystem.component.NavSquareButton
import dev.talhabayhan.lexi.core.designsystem.icon.LexiIcons
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.lexiContentWidth
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.Res
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.leveltest_picker_back
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.leveltest_picker_title
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.onboarding_level_a1
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.onboarding_level_a2
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.onboarding_level_b1
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.onboarding_level_b2
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.onboarding_level_c1
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.onboarding_level_nat
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.onboarding_skip
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * The cream onboarding page: safe-drawing insets, the 430dp content column and the `24px 20px` padding of
 * `01-onboarding`; [bottomPadding] is the design's 28 on the level choice (the steps keep 24). Shared by the
 * onboarding steps, the level picker and the level test result.
 */
@Composable
internal fun OnboardingFrame(
    modifier: Modifier = Modifier,
    bottomPadding: Dp = LexiTheme.dimens.screenPaddingTop,
    content: @Composable BoxScope.() -> Unit,
) {
    val dimens = LexiTheme.dimens
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LexiTheme.colors.cream)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .lexiContentWidth()
                .padding(
                    start = dimens.screenPaddingHorizontal,
                    top = dimens.screenPaddingTop,
                    end = dimens.screenPaddingHorizontal,
                    bottom = bottomPadding,
                ),
            content = content,
        )
    }
}

/**
 * Manual level pick (DESIGN-DEBT, built from `03-level-select`'s 2×3 [LevelCard] grid): back button, optional
 * "Atla" ([onSkip] null hides it), title and the six levels, [selectedLevel] highlighted. Used by the level test
 * result's "Seviyeyi değiştir".
 */
@Composable
internal fun LevelPickerContent(
    selectedLevel: Level,
    onBack: () -> Unit,
    onPick: (Level) -> Unit,
    modifier: Modifier = Modifier,
    onSkip: (() -> Unit)? = null,
) {
    val gap = LexiTheme.dimens.gap12
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap20),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavSquareButton(
                icon = LexiIcons.Back,
                contentDescription = stringResource(Res.string.leveltest_picker_back),
                onClick = onBack,
            )
            if (onSkip != null) {
                LexiTextLink(text = stringResource(Res.string.onboarding_skip), onClick = onSkip)
            }
        }

        Text(
            text = stringResource(Res.string.leveltest_picker_title),
            style = LexiTheme.typography.display,
            color = LexiTheme.colors.ink,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(gap),
        ) {
            Level.entries.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(gap),
                ) {
                    row.forEach { level ->
                        val selected = level == selectedLevel
                        val look = level.look()
                        LevelCard(
                            title = level.name,
                            subtitle = stringResource(level.subtitleRes()),
                            progress = 0f,
                            progressLabel = "",
                            marker = look.marker,
                            markerColor = if (selected && look.color == LexiTheme.colors.red) {
                                LexiTheme.colors.yellow
                            } else {
                                look.color
                            },
                            onClick = { onPick(level) },
                            style = when {
                                selected -> LevelCardStyle.Selected
                                level == Level.NAT -> LevelCardStyle.Inverted
                                else -> LevelCardStyle.Normal
                            },
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                        )
                    }
                }
            }
        }
    }
}

internal data class LevelLook(val marker: DecorShapeKind, val color: Color)

@Composable
internal fun Level.look(): LevelLook {
    val colors = LexiTheme.colors
    return when (this) {
        Level.A1 -> LevelLook(DecorShapeKind.Square, colors.red)
        Level.A2 -> LevelLook(DecorShapeKind.Circle, colors.yellow)
        Level.B1 -> LevelLook(DecorShapeKind.Leaf, colors.blue)
        Level.B2 -> LevelLook(DecorShapeKind.Square, colors.yellow)
        Level.C1 -> LevelLook(DecorShapeKind.Circle, colors.blue)
        Level.NAT -> LevelLook(DecorShapeKind.Leaf, colors.red)
    }
}

/** "Başlangıç", "Temel", … "Deyim · phrasal verb". */
internal fun Level.subtitleRes(): StringResource = when (this) {
    Level.A1 -> Res.string.onboarding_level_a1
    Level.A2 -> Res.string.onboarding_level_a2
    Level.B1 -> Res.string.onboarding_level_b1
    Level.B2 -> Res.string.onboarding_level_b2
    Level.C1 -> Res.string.onboarding_level_c1
    Level.NAT -> Res.string.onboarding_level_nat
}

internal fun Level.badgeStyle(): LevelBadgeStyle = when (this) {
    Level.A1 -> LevelBadgeStyle.A1
    Level.A2 -> LevelBadgeStyle.A2
    Level.B1 -> LevelBadgeStyle.B1
    Level.B2 -> LevelBadgeStyle.B2
    Level.C1 -> LevelBadgeStyle.C1
    Level.NAT -> LevelBadgeStyle.Native
}
