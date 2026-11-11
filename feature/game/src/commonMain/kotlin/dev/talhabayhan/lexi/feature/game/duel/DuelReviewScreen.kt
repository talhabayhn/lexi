package dev.talhabayhan.lexi.feature.game.duel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import dev.talhabayhan.lexi.core.designsystem.component.EmptyState
import dev.talhabayhan.lexi.core.designsystem.component.NavSquareButton
import dev.talhabayhan.lexi.core.designsystem.component.ReviewOutcome
import dev.talhabayhan.lexi.core.designsystem.icon.LexiIcons
import dev.talhabayhan.lexi.core.designsystem.modifier.lexiHeading
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.lexiContentWidth
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.feature.game.generated.resources.Res
import dev.talhabayhan.lexi.feature.game.generated.resources.duel_review_back
import dev.talhabayhan.lexi.feature.game.generated.resources.duel_review_empty
import dev.talhabayhan.lexi.feature.game.generated.resources.result_header
import dev.talhabayhan.lexi.feature.game.generated.resources.result_mode_duel
import dev.talhabayhan.lexi.feature.game.generated.resources.result_retry
import dev.talhabayhan.lexi.feature.game.generated.resources.result_review_error
import dev.talhabayhan.lexi.feature.game.result.ReviewGroupList
import dev.talhabayhan.lexi.feature.game.result.ReviewGroupUi
import dev.talhabayhan.lexi.feature.game.result.ReviewRowUi
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource

/**
 * One player's answers after a duel. DESIGN-DEBT (needs design): built from `16-components` and the review list of
 * `06-result`: back button and the header (level · DÜELLO · reason, the player's name), then the Doğru / Yanlış / Pas
 * groups with answer, Turkish clue and example sentence. Scrolls when it does not fit.
 */
@Composable
internal fun DuelReviewScreen(
    state: DuelReviewUiState,
    onIntent: (DuelReviewIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LexiTheme.colors
    val dimens = LexiTheme.dimens
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.cream)
            .windowInsetsPadding(WindowInsets.safeDrawing)
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
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimens.gap12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavSquareButton(
                icon = LexiIcons.Back,
                contentDescription = stringResource(Res.string.duel_review_back),
                onClick = { onIntent(DuelReviewIntent.Back) },
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(dimens.gap2)) {
                Text(
                    text = stringResource(
                        Res.string.result_header,
                        state.level.name,
                        stringResource(Res.string.result_mode_duel),
                        stringResource(state.end.reasonRes()),
                    ),
                    style = LexiTheme.typography.label13,
                )
                Text(
                    text = duelPlayerName(state.name, state.player),
                    style = LexiTheme.typography.title,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.lexiHeading(),
                )
            }
        }
        when {
            state.isLoading -> Unit

            // DESIGN-DEBT: no design for a failed read; the component library's empty state with a retry.
            state.reviewError -> EmptyState(
                message = stringResource(Res.string.result_review_error),
                actionText = stringResource(Res.string.result_retry),
                onAction = { onIntent(DuelReviewIntent.Retry) },
                modifier = Modifier.fillMaxWidth(),
            )

            // DESIGN-DEBT: no design for a player who answered nothing; the component library's empty state.
            state.groups.isEmpty() -> EmptyState(stringResource(Res.string.duel_review_empty), Modifier.fillMaxWidth())

            else -> ReviewGroupList(
                groups = state.groups,
                expandedGroups = state.expandedGroups,
                onToggleGroup = { onIntent(DuelReviewIntent.ToggleGroup(it)) },
                onSpeak = { onIntent(DuelReviewIntent.Speak(it)) },
            )
        }
    }
}

// design sample, not user-facing: the duel answer review (needs design)
internal object DuelReviewSamples {
    private fun row(letter: Char, word: String, clue: String, example: String, outcome: ReviewOutcome) =
        ReviewRowUi(letter, word, clue, example, outcome, cardId = "a1-$word")

    val design = DuelReviewUiState(
        level = Level.A1,
        end = DuelEnd.ALL_ANSWERED,
        player = 0,
        name = "selinay",
        isLoading = false,
        groups = persistentListOf(
            ReviewGroupUi(
                ReviewOutcome.Correct,
                persistentListOf(
                    row('A', "apple", "Elma", "I eat an apple every day.", ReviewOutcome.Correct),
                    row('B', "bread", "Ekmek", "We buy fresh bread.", ReviewOutcome.Correct),
                    row('C', "chair", "Sandalye", "Sit on the chair.", ReviewOutcome.Correct),
                ),
            ),
            ReviewGroupUi(
                ReviewOutcome.Wrong,
                persistentListOf(row('D', "dog", "Köpek", "The dog is sleeping.", ReviewOutcome.Wrong)),
            ),
            ReviewGroupUi(
                ReviewOutcome.Passed,
                persistentListOf(row('E', "egg", "Yumurta", "She cooks an egg.", ReviewOutcome.Passed)),
            ),
        ),
    )
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun DuelReviewScreenPreview() {
    LexiTheme { DuelReviewScreen(DuelReviewSamples.design, onIntent = {}) }
}
