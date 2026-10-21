package dev.talhabayhan.lexi.feature.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.talhabayhan.lexi.core.designsystem.component.LexiButton
import dev.talhabayhan.lexi.core.designsystem.component.LexiButtonDefaults
import dev.talhabayhan.lexi.core.designsystem.component.LexiButtonVariant
import dev.talhabayhan.lexi.core.designsystem.component.LexiTextLink
import dev.talhabayhan.lexi.core.designsystem.component.NavSquareButton
import dev.talhabayhan.lexi.core.designsystem.icon.LexiIcons
import dev.talhabayhan.lexi.core.designsystem.modifier.lexiHeading
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.Res
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.leveltest_choice_body
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.leveltest_choice_chip
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.leveltest_choice_pick
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.leveltest_choice_test
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.leveltest_choice_title
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.leveltest_intro_body
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.leveltest_intro_cta
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.leveltest_intro_hint
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.leveltest_intro_title
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.leveltest_picker_back
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.onboarding_1_body
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.onboarding_1_title
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.onboarding_2_body
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.onboarding_2_title
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.onboarding_3_body
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.onboarding_3_title
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.onboarding_next
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.onboarding_skip
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.onboarding_start
import dev.talhabayhan.lexi.feature.onboarding.generated.resources.onboarding_step_counter
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun OnboardingScreen(
    state: OnboardingUiState,
    onIntent: (OnboardingIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = LexiTheme.dimens
    val step = state.currentStep
    val choicePage = step == OnboardingStep.LevelChoice || step == OnboardingStep.LevelTestIntro
    OnboardingFrame(
        modifier = modifier,
        bottomPadding = if (choicePage) dimens.screenPaddingBottom else dimens.screenPaddingTop,
    ) {
        when (step) {
            OnboardingStep.Step1, OnboardingStep.Step2, OnboardingStep.Step3 ->
                OnboardingStepContent(step = step, onIntent = onIntent)

            OnboardingStep.LevelChoice -> LevelChoiceContent(onIntent = onIntent)

            OnboardingStep.LevelTestIntro -> LevelTestIntroContent(onIntent = onIntent)
        }
    }
}

@Composable
private fun StepIndicators(step: Int, totalSteps: Int = 3, modifier: Modifier = Modifier) {
    val description = stringResource(Res.string.onboarding_step_counter, step)
    val colors = LexiTheme.colors
    Row(
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
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

            OnboardingStep.Step2, OnboardingStep.LevelChoice, OnboardingStep.LevelTestIntro -> {
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
        }
    }
}

@Composable
private fun OnboardingStepContent(
    step: OnboardingStep,
    onIntent: (OnboardingIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val (titleRes, bodyRes, ctaTextRes) = when (step) {
        OnboardingStep.Step1 -> Triple(
            Res.string.onboarding_1_title,
            Res.string.onboarding_1_body,
            Res.string.onboarding_next,
        )

        OnboardingStep.Step2 -> Triple(
            Res.string.onboarding_2_title,
            Res.string.onboarding_2_body,
            Res.string.onboarding_next,
        )

        else -> Triple(Res.string.onboarding_3_title, Res.string.onboarding_3_body, Res.string.onboarding_start)
    }
    OnboardingPage(
        illustration = step,
        title = stringResource(titleRes),
        body = stringResource(bodyRes),
        onSkip = { onIntent(OnboardingIntent.Skip) },
        modifier = modifier,
        topStart = { StepIndicators(step = step.ordinal + 1) },
    ) {
        // Plain one-line label (ellipsis), as before the level test landed.
        HeroButton(text = stringResource(ctaTextRes), onClick = { onIntent(OnboardingIntent.Next) }, fitted = false)
    }
}

/**
 * `01-onboarding`: the red "SEVİYE TESTİNE BAŞLA", the "YA DA SEVİYENİ SEÇ" label 10dp lower and the six level
 * chips; title and body from `COPY.md` §4 (`GAME_RULES.md` §9, owner decision 5). A chip saves the level.
 */
@Composable
private fun LevelChoiceContent(onIntent: (OnboardingIntent) -> Unit, modifier: Modifier = Modifier) {
    OnboardingPage(
        illustration = OnboardingStep.LevelChoice,
        title = stringResource(Res.string.leveltest_choice_title),
        body = stringResource(Res.string.leveltest_choice_body),
        onSkip = { onIntent(OnboardingIntent.Skip) },
        modifier = modifier,
        topStart = { Spacer(Modifier.size(28.dp)) },
    ) {
        HeroButton(
            text = stringResource(Res.string.leveltest_choice_test),
            onClick = { onIntent(OnboardingIntent.StartLevelTest) },
        )
        Text(
            text = stringResource(Res.string.leveltest_choice_pick),
            style = LexiTheme.typography.wheelRule,
            color = LexiTheme.colors.ink,
            modifier = Modifier.padding(top = LexiTheme.dimens.gap10),
        )
        LevelChips(onPick = { onIntent(OnboardingIntent.PickLevel(it)) })
    }
}

/**
 * DESIGN-DEBT (no design; built from `16-components` on the onboarding layout): what the level test is
 * (`COPY.md` §4 intro), back to the level choice, "Atla", and "Başla".
 */
@Composable
private fun LevelTestIntroContent(onIntent: (OnboardingIntent) -> Unit, modifier: Modifier = Modifier) {
    OnboardingPage(
        illustration = OnboardingStep.LevelTestIntro,
        title = stringResource(Res.string.leveltest_intro_title),
        body = stringResource(Res.string.leveltest_intro_body),
        onSkip = { onIntent(OnboardingIntent.Skip) },
        modifier = modifier,
        topStart = {
            NavSquareButton(
                icon = LexiIcons.Back,
                contentDescription = stringResource(Res.string.leveltest_picker_back),
                onClick = { onIntent(OnboardingIntent.Back) },
            )
        },
        extra = {
            Text(
                text = stringResource(Res.string.leveltest_intro_hint),
                style = LexiTheme.typography.bodyBold15,
                color = LexiTheme.colors.ink,
            )
        },
    ) {
        HeroButton(
            text = stringResource(Res.string.leveltest_intro_cta),
            onClick = { onIntent(OnboardingIntent.BeginLevelTest) },
        )
    }
}

/** Top row ([topStart] + "Atla"), illustration, title + body (+ [extra]), then [actions] pushed to the bottom. */
@Composable
private fun OnboardingPage(
    illustration: OnboardingStep,
    title: String,
    body: String,
    onSkip: () -> Unit,
    topStart: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    extra: (@Composable () -> Unit)? = null,
    actions: @Composable ColumnScope.() -> Unit,
) {
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
            topStart()
            LexiTextLink(text = stringResource(Res.string.onboarding_skip), onClick = onSkip)
        }

        OnboardingIllustration(step = illustration)

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap10),
        ) {
            Text(
                text = title,
                style = LexiTheme.typography.display,
                color = LexiTheme.colors.ink,
                modifier = Modifier.lexiHeading(),
            )
            Text(text = body, style = LexiTheme.typography.body16, color = LexiTheme.colors.ink)
            extra?.invoke()
        }

        Spacer(Modifier.weight(1f))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap10),
            content = actions,
        )
    }
}

/**
 * The six level chips of `01-onboarding`: equal columns, gap 6, 48dp, 3dp border, no shadow; white with 15/800 ink
 * text, NAT ink with 13/800 cream text. Each is read as the level and its name ("NAT, Deyim · phrasal verb").
 */
@Composable
private fun LevelChips(onPick: (Level) -> Unit, modifier: Modifier = Modifier) {
    val colors = LexiTheme.colors
    val typography = LexiTheme.typography
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap6),
    ) {
        Level.entries.forEach { level ->
            val native = level == Level.NAT
            val description = stringResource(
                Res.string.leveltest_choice_chip,
                level.name,
                stringResource(level.subtitleRes()),
            )
            LexiButton(
                onClick = { onPick(level) },
                variant = if (native) LexiButtonVariant.Ink else LexiButtonVariant.Secondary,
                height = LexiButtonDefaults.SmallHeight,
                shadow = null,
                contentColor = if (native) colors.cream else colors.ink,
                contentPadding = PaddingValues(),
                modifier = Modifier.weight(1f),
            ) {
                FittedLabel(
                    text = level.name,
                    style = if (native) typography.wheelChip else typography.bodyStrong15,
                    modifier = Modifier.semantics { contentDescription = description },
                )
            }
        }
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun LevelChoicePreview() {
    LexiTheme(reducedMotion = false) {
        OnboardingScreen(state = OnboardingUiState(currentStep = OnboardingStep.LevelChoice), onIntent = {})
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun LevelTestIntroPreview() {
    LexiTheme(reducedMotion = false) {
        OnboardingScreen(state = OnboardingUiState(currentStep = OnboardingStep.LevelTestIntro), onIntent = {})
    }
}
