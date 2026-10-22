package dev.talhabayhan.lexi.feature.exams

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.tooling.preview.Preview
import dev.talhabayhan.lexi.core.designsystem.component.EmptyState
import dev.talhabayhan.lexi.core.designsystem.component.ExamIntroCard
import dev.talhabayhan.lexi.core.designsystem.component.ExamPackBadgeStyle
import dev.talhabayhan.lexi.core.designsystem.component.ExamPackProChip
import dev.talhabayhan.lexi.core.designsystem.component.ExamPackProgressChip
import dev.talhabayhan.lexi.core.designsystem.component.ExamPackRow
import dev.talhabayhan.lexi.core.designsystem.component.IconButtonDefaults
import dev.talhabayhan.lexi.core.designsystem.component.NavSquareButton
import dev.talhabayhan.lexi.core.designsystem.component.SlidingToast
import dev.talhabayhan.lexi.core.designsystem.icon.LexiIcons
import dev.talhabayhan.lexi.core.designsystem.modifier.lexiHeading
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.lexiContentWidth
import dev.talhabayhan.lexi.core.domain.model.ExamPackId
import dev.talhabayhan.lexi.feature.exams.generated.resources.Res
import dev.talhabayhan.lexi.feature.exams.generated.resources.exams_back
import dev.talhabayhan.lexi.feature.exams.generated.resources.exams_empty
import dev.talhabayhan.lexi.feature.exams.generated.resources.exams_error
import dev.talhabayhan.lexi.feature.exams.generated.resources.exams_intro_text
import dev.talhabayhan.lexi.feature.exams.generated.resources.exams_intro_title
import dev.talhabayhan.lexi.feature.exams.generated.resources.exams_locked_description
import dev.talhabayhan.lexi.feature.exams.generated.resources.exams_locked_toast
import dev.talhabayhan.lexi.feature.exams.generated.resources.exams_play_action
import dev.talhabayhan.lexi.feature.exams.generated.resources.exams_pro
import dev.talhabayhan.lexi.feature.exams.generated.resources.exams_progress
import dev.talhabayhan.lexi.feature.exams.generated.resources.exams_progress_description
import dev.talhabayhan.lexi.feature.exams.generated.resources.exams_retry
import dev.talhabayhan.lexi.feature.exams.generated.resources.exams_soon
import dev.talhabayhan.lexi.feature.exams.generated.resources.exams_subtitle
import dev.talhabayhan.lexi.feature.exams.generated.resources.exams_subtitle_count
import dev.talhabayhan.lexi.feature.exams.generated.resources.exams_title
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource

/**
 * `10-exam-packs`: back button and title, the yellow intro banner and one row per pack with its real card count. A
 * free (or unlocked) pack shows the learned share ("%34") and starts its wheel; a locked Pro pack shows "PRO" over
 * "Yakında" (DESIGN-DEBT: the design has the PRO chip only) and a polite toast on tap (no paywall until RevenueCat).
 * DESIGN-DEBT: loading, empty and error states and the toast are built from `16-components`.
 */
@Composable
internal fun ExamPacksScreen(
    state: ExamPacksUiState,
    onIntent: (ExamPacksIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = LexiTheme.dimens
    Box(modifier.fillMaxSize().background(LexiTheme.colors.cream)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .lexiContentWidth()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = dimens.screenPaddingHorizontal,
                    top = dimens.screenPaddingTop,
                    end = dimens.screenPaddingHorizontal,
                    bottom = dimens.screenPaddingBottomList,
                ),
            verticalArrangement = Arrangement.spacedBy(dimens.gap14),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(dimens.gap12),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NavSquareButton(
                    icon = LexiIcons.Back,
                    contentDescription = stringResource(Res.string.exams_back),
                    onClick = { onIntent(ExamPacksIntent.Back) },
                )
                Text(
                    stringResource(Res.string.exams_title),
                    style = LexiTheme.typography.title,
                    modifier = Modifier.lexiHeading(),
                )
            }
            ExamIntroCard(
                title = stringResource(Res.string.exams_intro_title),
                text = stringResource(Res.string.exams_intro_text),
            )
            if (state.isLoaded) {
                when {
                    state.hasError -> EmptyState(
                        message = stringResource(Res.string.exams_error),
                        actionText = stringResource(Res.string.exams_retry),
                        onAction = { onIntent(ExamPacksIntent.Retry) },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    state.packs.isEmpty() -> EmptyState(stringResource(Res.string.exams_empty), Modifier.fillMaxWidth())

                    else -> PackList(state, onIntent)
                }
            }
        }
        if (state.lockedNotice) {
            SlidingToast(
                message = stringResource(Res.string.exams_locked_toast),
                onDismissed = { onIntent(ExamPacksIntent.DismissNotice) },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .lexiContentWidth()
                    // Below the header (never over the back button), like the home toast under its top bar.
                    .padding(
                        start = dimens.screenPaddingHorizontal,
                        top = dimens.screenPaddingTop + IconButtonDefaults.NavSize + dimens.gap14,
                        end = dimens.screenPaddingHorizontal,
                    )
                    .fillMaxWidth()
                    .clickable(role = Role.Button, onClick = { onIntent(ExamPacksIntent.DismissNotice) }),
            )
        }
    }
}

@Composable
private fun PackList(state: ExamPacksUiState, onIntent: (ExamPacksIntent) -> Unit) {
    // TODO(M7): follow Settings.uiLanguage once the in-app language setting is applied (as on the categories).
    val turkish = Locale.current.language == TURKISH
    Column(verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap10)) {
        state.packs.forEach { pack ->
            val styles = ExamPackBadgeStyle.entries
            ExamPackRow(
                badge = pack.badge,
                badgeStyle = styles[pack.badgeIndex % styles.size],
                title = if (turkish) pack.titleTr else pack.titleEn,
                subtitle = when {
                    turkish && pack.descriptionTr.isNotEmpty() ->
                        stringResource(Res.string.exams_subtitle, pack.descriptionTr, pack.cardCount)

                    else -> stringResource(Res.string.exams_subtitle_count, pack.cardCount)
                },
                onClick = { onIntent(ExamPacksIntent.OpenPack(pack.id)) },
                onClickLabel = stringResource(
                    if (pack.locked) Res.string.exams_locked_description else Res.string.exams_play_action,
                ),
            ) {
                if (pack.locked) LockedBadge() else ProgressBadge(pack.learnedPercent)
            }
        }
    }
}

@Composable
private fun ProgressBadge(percent: Int) {
    val description = stringResource(Res.string.exams_progress_description, percent)
    ExamPackProgressChip(
        stringResource(Res.string.exams_progress, percent),
        Modifier.clearAndSetSemantics { contentDescription = description },
    )
}

/** "PRO" chip (design) over "Yakında" (DESIGN-DEBT) until purchases exist. */
@Composable
private fun LockedBadge() {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap4),
    ) {
        ExamPackProChip(stringResource(Res.string.exams_pro))
        Text(stringResource(Res.string.exams_soon), style = LexiTheme.typography.caption12Bold)
    }
}

private const val TURKISH = "tr"

// design sample, not user-facing: the shipped packs with sample progress
internal val ExamPacksSampleState = ExamPacksUiState(
    isLoaded = true,
    packs = persistentListOf(
        ExamPackUi(
            id = ExamPackId("lgs"),
            badge = "LGS",
            badgeIndex = 0,
            titleTr = "LGS İngilizce",
            titleEn = "LGS English",
            descriptionTr = "8. sınıf · ünite bazlı",
            cardCount = 573,
            learnedPercent = 34,
            isPro = false,
            locked = false,
        ),
        ExamPackUi(
            id = ExamPackId("yds-yokdil"),
            badge = "YDS",
            badgeIndex = 3,
            titleTr = "YDS / YÖKDİL",
            titleEn = "YDS / YÖKDİL",
            descriptionTr = "Akademik metin kelimeleri",
            cardCount = 990,
            learnedPercent = 0,
            isPro = true,
            locked = true,
        ),
    ),
)

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun ExamPacksScreenPreview() {
    LexiTheme(reducedMotion = false) { ExamPacksScreen(state = ExamPacksSampleState, onIntent = {}) }
}
