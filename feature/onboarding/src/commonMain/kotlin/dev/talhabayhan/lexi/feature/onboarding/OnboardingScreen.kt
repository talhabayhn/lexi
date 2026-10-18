package dev.talhabayhan.lexi.feature.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.talhabayhan.lexi.core.designsystem.component.DecorShapeKind
import dev.talhabayhan.lexi.core.designsystem.component.LevelCard
import dev.talhabayhan.lexi.core.designsystem.component.LevelCardStyle
import dev.talhabayhan.lexi.core.designsystem.component.LexiButton
import dev.talhabayhan.lexi.core.designsystem.component.LexiButtonDefaults
import dev.talhabayhan.lexi.core.designsystem.component.LexiTextLink
import dev.talhabayhan.lexi.core.designsystem.component.NavSquareButton
import dev.talhabayhan.lexi.core.designsystem.icon.LexiIcons
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.lexiContentWidth
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.Res
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.leveltest_choice_body
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.leveltest_choice_pick
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.leveltest_choice_test
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.leveltest_choice_title
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.leveltest_picker_back
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.leveltest_picker_title
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.onboarding_1_body
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.onboarding_1_title
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.onboarding_2_body
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.onboarding_2_title
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.onboarding_3_body
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.onboarding_3_title
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.onboarding_level_a1
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.onboarding_level_a2
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.onboarding_level_b1
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.onboarding_level_b2
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.onboarding_level_c1
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.onboarding_level_nat
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.onboarding_level_pick_body
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.onboarding_next
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.onboarding_skip
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.onboarding_start
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.onboarding_step_counter
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun OnboardingScreen(
    state: OnboardingUiState,
    onIntent: (OnboardingIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
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
                .padding(horizontal = 20.dp, vertical = 24.dp),
        ) {
            if (state.currentStep == OnboardingStep.LevelPicker) {
                LevelPickerContent(
                    selectedLevel = state.selectedLevel,
                    onIntent = onIntent,
                )
            } else {
                OnboardingStepContent(
                    step = state.currentStep,
                    onIntent = onIntent,
                )
            }
        }
    }
}

@Composable
private fun StepIndicators(step: Int, totalSteps: Int = 3, modifier: Modifier = Modifier) {
    val description = stringResource(Res.string.onboarding_step_counter, step)
    val colors = LexiTheme.colors
    Row(
        modifier = modifier.semantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (i in 1..totalSteps) {
            when {
                i < step -> {
                    Box(
                        modifier = Modifier
                            .size(width = 28.dp, height = 8.dp)
                            .background(colors.ink),
                    )
                }

                i == step -> {
                    val fill = when (step) {
                        1 -> colors.ink
                        2 -> colors.red
                        else -> colors.yellow
                    }
                    Box(
                        modifier = Modifier
                            .size(width = 28.dp, height = 8.dp)
                            .background(fill),
                    )
                }

                else -> {
                    Box(
                        modifier = Modifier
                            .size(width = 28.dp, height = 8.dp)
                            .border(BorderStroke(LexiTheme.borders.thin, colors.ink)),
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingIllustration(step: OnboardingStep, modifier: Modifier = Modifier) {
    val colors = LexiTheme.colors
    val thickBorder = BorderStroke(LexiTheme.borders.thick, colors.ink)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp)
            .clearAndSetSemantics {},
    ) {
        when (step) {
            OnboardingStep.Step1 -> {
                Box(
                    modifier = Modifier
                        .offset(x = 30.dp, y = 20.dp)
                        .size(200.dp)
                        .background(colors.blue, LexiTheme.shapes.circle)
                        .border(thickBorder, LexiTheme.shapes.circle),
                )
                Box(
                    modifier = Modifier
                        .offset(x = 170.dp, y = 70.dp)
                        .size(150.dp)
                        .background(colors.red, LexiTheme.shapes.square)
                        .border(thickBorder, LexiTheme.shapes.square),
                )
                Box(
                    modifier = Modifier
                        .offset(x = 120.dp, y = 120.dp)
                        .size(110.dp)
                        .background(colors.yellow, LexiTheme.shapes.leaf)
                        .border(thickBorder, LexiTheme.shapes.leaf),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "A",
                        color = colors.ink,
                        style = LexiTheme.typography.display.copy(fontSize = 52.sp, lineHeight = 52.sp),
                    )
                }
            }

            OnboardingStep.Step2, OnboardingStep.LevelChoice -> {
                Box(
                    modifier = Modifier
                        .offset(x = 30.dp, y = 20.dp)
                        .size(200.dp)
                        .background(colors.yellow, LexiTheme.shapes.circle)
                        .border(thickBorder, LexiTheme.shapes.circle),
                )
                Box(
                    modifier = Modifier
                        .offset(x = 170.dp, y = 70.dp)
                        .size(150.dp)
                        .background(colors.blue, LexiTheme.shapes.square)
                        .border(thickBorder, LexiTheme.shapes.square),
                )
                Box(
                    modifier = Modifier
                        .offset(x = 120.dp, y = 120.dp)
                        .size(110.dp)
                        .background(colors.red, LexiTheme.shapes.leaf)
                        .border(thickBorder, LexiTheme.shapes.leaf),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "?",
                        color = colors.white,
                        style = LexiTheme.typography.display.copy(fontSize = 52.sp, lineHeight = 52.sp),
                    )
                }
            }

            OnboardingStep.Step3 -> {
                Box(
                    modifier = Modifier
                        .offset(x = 30.dp, y = 20.dp)
                        .size(200.dp)
                        .background(colors.red, LexiTheme.shapes.circle)
                        .border(thickBorder, LexiTheme.shapes.circle),
                )
                Box(
                    modifier = Modifier
                        .offset(x = 170.dp, y = 70.dp)
                        .size(150.dp)
                        .background(colors.yellow, LexiTheme.shapes.square)
                        .border(thickBorder, LexiTheme.shapes.square),
                )
                Box(
                    modifier = Modifier
                        .offset(x = 120.dp, y = 120.dp)
                        .size(110.dp)
                        .background(colors.blue, LexiTheme.shapes.leaf)
                        .border(thickBorder, LexiTheme.shapes.leaf),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Z",
                        color = colors.white,
                        style = LexiTheme.typography.display.copy(fontSize = 52.sp, lineHeight = 52.sp),
                    )
                }
            }

            OnboardingStep.LevelPicker -> Unit
        }
    }
}

@Composable
private fun OnboardingStepContent(
    step: OnboardingStep,
    onIntent: (OnboardingIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val titleRes: StringResource
    val bodyRes: StringResource
    val ctaTextRes: StringResource
    val stepIndex: Int?

    when (step) {
        OnboardingStep.Step1 -> {
            titleRes = Res.string.onboarding_1_title
            bodyRes = Res.string.onboarding_1_body
            ctaTextRes = Res.string.onboarding_next
            stepIndex = 1
        }

        OnboardingStep.Step2 -> {
            titleRes = Res.string.onboarding_2_title
            bodyRes = Res.string.onboarding_2_body
            ctaTextRes = Res.string.onboarding_next
            stepIndex = 2
        }

        OnboardingStep.Step3 -> {
            titleRes = Res.string.onboarding_3_title
            bodyRes = Res.string.onboarding_3_body
            ctaTextRes = Res.string.onboarding_start
            stepIndex = 3
        }

        OnboardingStep.LevelChoice -> {
            titleRes = Res.string.leveltest_choice_title
            bodyRes = if (LEVEL_TEST_AVAILABLE) {
                // TODO(B5): Wire level test once implemented
                Res.string.leveltest_choice_body
            } else {
                Res.string.onboarding_level_pick_body
            }
            ctaTextRes = Res.string.leveltest_choice_pick
            stepIndex = null
        }

        OnboardingStep.LevelPicker -> return
    }

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
            if (stepIndex != null) {
                StepIndicators(step = stepIndex)
            } else {
                Spacer(Modifier.size(28.dp))
            }
            LexiTextLink(
                text = stringResource(Res.string.onboarding_skip),
                onClick = { onIntent(OnboardingIntent.Skip) },
            )
        }

        OnboardingIllustration(step = step)

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap10),
        ) {
            Text(
                text = stringResource(titleRes),
                style = LexiTheme.typography.display,
                color = LexiTheme.colors.ink,
            )
            Text(
                text = stringResource(bodyRes),
                style = LexiTheme.typography.body16,
                color = LexiTheme.colors.ink,
            )
        }

        Spacer(Modifier.weight(1f))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap10),
        ) {
            if (step == OnboardingStep.LevelChoice && LEVEL_TEST_AVAILABLE) {
                LexiButton(
                    onClick = { onIntent(OnboardingIntent.StartLevelTest) },
                    height = LexiButtonDefaults.HeroHeight,
                    shadow = LexiTheme.shadows.s6,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(Res.string.leveltest_choice_test),
                        style = LexiTheme.typography.button20,
                    )
                }
            }

            LexiButton(
                onClick = {
                    when (step) {
                        OnboardingStep.LevelChoice -> onIntent(OnboardingIntent.OpenLevelPicker)
                        else -> onIntent(OnboardingIntent.Next)
                    }
                },
                height = LexiButtonDefaults.HeroHeight,
                shadow = LexiTheme.shadows.s6,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(ctaTextRes),
                    style = LexiTheme.typography.button20,
                )
            }
        }
    }
}

@Composable
private fun LevelPickerContent(
    selectedLevel: Level,
    onIntent: (OnboardingIntent) -> Unit,
    modifier: Modifier = Modifier,
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
                onClick = { onIntent(OnboardingIntent.Back) },
            )
            LexiTextLink(
                text = stringResource(Res.string.onboarding_skip),
                onClick = { onIntent(OnboardingIntent.Skip) },
            )
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
                    modifier = Modifier.fillMaxWidth(),
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
                            onClick = { onIntent(OnboardingIntent.PickLevel(level)) },
                            style = when {
                                selected -> LevelCardStyle.Selected
                                level == Level.NAT -> LevelCardStyle.Inverted
                                else -> LevelCardStyle.Normal
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

private data class LevelLook(val marker: DecorShapeKind, val color: Color)

@Composable
private fun Level.look(): LevelLook {
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

private fun Level.subtitleRes(): StringResource = when (this) {
    Level.A1 -> Res.string.onboarding_level_a1
    Level.A2 -> Res.string.onboarding_level_a2
    Level.B1 -> Res.string.onboarding_level_b1
    Level.B2 -> Res.string.onboarding_level_b2
    Level.C1 -> Res.string.onboarding_level_c1
    Level.NAT -> Res.string.onboarding_level_nat
}
