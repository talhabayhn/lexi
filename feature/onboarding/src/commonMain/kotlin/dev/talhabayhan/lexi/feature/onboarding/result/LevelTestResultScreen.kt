package dev.talhabayhan.lexi.feature.onboarding.result

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
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
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.tooling.preview.Preview
import dev.talhabayhan.lexi.core.designsystem.component.DecorShape
import dev.talhabayhan.lexi.core.designsystem.component.DecorShapeKind
import dev.talhabayhan.lexi.core.designsystem.component.LevelBadge
import dev.talhabayhan.lexi.core.designsystem.component.LexiButton
import dev.talhabayhan.lexi.core.designsystem.component.LexiButtonVariant
import dev.talhabayhan.lexi.core.designsystem.component.ResultDefaults
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.lexiContentWidth
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.feature.onboarding.HeroButton
import dev.talhabayhan.lexi.feature.onboarding.LevelPickerContent
import dev.talhabayhan.lexi.feature.onboarding.OnboardingFrame
import dev.talhabayhan.lexi.feature.onboarding.badgeStyle
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.Res
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.leveltest_result_a1
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.leveltest_result_body
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.leveltest_result_change
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.leveltest_result_cta
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.leveltest_result_high
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.leveltest_result_label
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.leveltest_result_level
import dev.talhabayhan.lexi.feature.onboarding.subtitleRes
import org.jetbrains.compose.resources.stringResource

/**
 * Level test result — DESIGN-DEBT (no design; `06-result`'s header style): the yellow corner circle, "SEVİYEN" over
 * the level in the 76sp score style and its level badge (the level's name), the suggestion line and, for A1 and
 * C1/NAT only, a flavour line (`GAME_RULES.md` §9, owner decision 3); then "{level} ile başla" (red, s6) and
 * "Seviyeyi değiştir" (white), which opens the onboarding's level picker.
 */
@Composable
internal fun LevelTestResultScreen(
    state: LevelTestResultUiState,
    onIntent: (LevelTestResultIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state.page) {
        LevelTestResultPage.Result -> ResultPage(state = state, onIntent = onIntent, modifier = modifier)

        LevelTestResultPage.Picker -> OnboardingFrame(modifier = modifier) {
            LevelPickerContent(
                selectedLevel = state.level,
                onBack = { onIntent(LevelTestResultIntent.Back) },
                onPick = { onIntent(LevelTestResultIntent.PickLevel(it)) },
            )
        }
    }
}

@Composable
private fun ResultPage(
    state: LevelTestResultUiState,
    onIntent: (LevelTestResultIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LexiTheme.colors
    val dimens = LexiTheme.dimens
    val typography = LexiTheme.typography
    val level = state.level.name
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
        Box(
            modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .lexiContentWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = dimens.screenPaddingHorizontal,
                        top = dimens.screenPaddingTop,
                        end = dimens.screenPaddingHorizontal,
                        bottom = dimens.screenPaddingBottom,
                    ),
                verticalArrangement = Arrangement.spacedBy(dimens.gap16),
            ) {
                val heading = stringResource(Res.string.leveltest_result_level, level)
                Column(
                    modifier = Modifier.clearAndSetSemantics {
                        contentDescription = heading
                        heading()
                    },
                    verticalArrangement = Arrangement.spacedBy(dimens.gap2),
                ) {
                    Text(stringResource(Res.string.leveltest_result_label), style = typography.label13)
                    Text(level, style = typography.scoreHero)
                    // Under the level, so long names ("Deyim · phrasal verb") fit narrow phones.
                    LevelBadge(
                        text = stringResource(state.level.subtitleRes()),
                        style = state.level.badgeStyle(),
                        modifier = Modifier.padding(top = dimens.gap6),
                    )
                }
                Text(stringResource(Res.string.leveltest_result_body), style = typography.body15SemiBold)
                state.flavour?.let { flavour ->
                    Text(
                        text = stringResource(
                            when (flavour) {
                                LevelTestFlavour.Basics -> Res.string.leveltest_result_a1
                                LevelTestFlavour.High -> Res.string.leveltest_result_high
                            },
                        ),
                        style = typography.bodyStrong16,
                    )
                }
                Spacer(Modifier.weight(1f))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(dimens.gap16),
                ) {
                    HeroButton(
                        text = stringResource(Res.string.leveltest_result_cta, level),
                        onClick = { onIntent(LevelTestResultIntent.Start) },
                    )
                    LexiButton(
                        text = stringResource(Res.string.leveltest_result_change),
                        onClick = { onIntent(LevelTestResultIntent.ChangeLevel) },
                        variant = LexiButtonVariant.Secondary,
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                    )
                }
            }
        }
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun LevelTestResultPreview() {
    LexiTheme(reducedMotion = false) {
        LevelTestResultScreen(state = LevelTestResultUiState(level = Level.B1), onIntent = {})
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun LevelTestResultPickerPreview() {
    LexiTheme(reducedMotion = false) {
        LevelTestResultScreen(
            state = LevelTestResultUiState(level = Level.B1, page = LevelTestResultPage.Picker),
            onIntent = {},
        )
    }
}
