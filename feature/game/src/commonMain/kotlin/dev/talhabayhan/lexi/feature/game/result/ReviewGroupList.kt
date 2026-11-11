package dev.talhabayhan.lexi.feature.game.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.talhabayhan.lexi.core.designsystem.component.ListCard
import dev.talhabayhan.lexi.core.designsystem.component.ReviewMoreRow
import dev.talhabayhan.lexi.core.designsystem.component.ReviewOutcome
import dev.talhabayhan.lexi.core.designsystem.component.ReviewRow
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.feature.game.generated.resources.Res
import dev.talhabayhan.lexi.feature.game.generated.resources.result_add_to_notebook
import dev.talhabayhan.lexi.feature.game.generated.resources.result_added_to_notebook
import dev.talhabayhan.lexi.feature.game.generated.resources.result_group_correct
import dev.talhabayhan.lexi.feature.game.generated.resources.result_group_passed
import dev.talhabayhan.lexi.feature.game.generated.resources.result_group_time_up
import dev.talhabayhan.lexi.feature.game.generated.resources.result_group_wrong
import dev.talhabayhan.lexi.feature.game.generated.resources.result_more
import dev.talhabayhan.lexi.feature.game.generated.resources.result_outcome_correct
import dev.talhabayhan.lexi.feature.game.generated.resources.result_outcome_passed
import dev.talhabayhan.lexi.feature.game.generated.resources.result_outcome_time_up
import dev.talhabayhan.lexi.feature.game.generated.resources.result_outcome_wrong
import dev.talhabayhan.lexi.feature.game.generated.resources.result_show_less
import dev.talhabayhan.lexi.feature.game.generated.resources.result_speak
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * The review list of `06-result`, also the duel's per-player answer review: one titled group per outcome (Doğru,
 * Yanlış, Pas, Süre bitti), each a list card of its first [ReviewGroupUi.PREVIEW_ROWS] rows plus "Daha fazla göster"
 * unless it is in [expandedGroups]. [showLevel] adds the level chip (Daily). With [onAddToNotebook] the Süre bitti rows
 * offer "Deftere ekle" (by card id); without it they show no notebook action.
 */
@Composable
internal fun ReviewGroupList(
    groups: ImmutableList<ReviewGroupUi>,
    expandedGroups: ImmutableSet<ReviewOutcome>,
    onToggleGroup: (ReviewOutcome) -> Unit,
    onSpeak: (String) -> Unit,
    modifier: Modifier = Modifier,
    showLevel: Boolean = false,
    onAddToNotebook: ((String) -> Unit)? = null,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap16)) {
        groups.forEach { group ->
            Column(verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap6)) {
                Text(
                    text = stringResource(group.outcome.titleRes()),
                    style = LexiTheme.typography.heading18,
                )
                val expanded = group.outcome in expandedGroups
                val hidden = group.rows.size - ReviewGroupUi.PREVIEW_ROWS
                ListCard(Modifier.fillMaxWidth(), shadow = LexiTheme.shadows.s6) {
                    (if (expanded) group.rows else group.rows.take(ReviewGroupUi.PREVIEW_ROWS)).forEach { row ->
                        val notebookAction = onAddToNotebook != null && row.outcome == ReviewOutcome.TimeUp
                        ReviewRow(
                            letter = row.letter,
                            word = row.word,
                            clue = row.clue,
                            example = row.example,
                            outcome = row.outcome,
                            outcomeDescription = stringResource(row.outcome.descriptionRes()),
                            speakContentDescription = stringResource(Res.string.result_speak, row.word),
                            onSpeak = { onSpeak(row.word) },
                            level = if (showLevel) row.level?.name else null,
                            onAddToNotebook = if (notebookAction && !row.isAddedToNotebook) {
                                { onAddToNotebook(row.cardId) }
                            } else {
                                null
                            },
                            addToNotebookText = if (notebookAction) {
                                stringResource(
                                    if (row.isAddedToNotebook) {
                                        Res.string.result_added_to_notebook
                                    } else {
                                        Res.string.result_add_to_notebook
                                    },
                                )
                            } else {
                                null
                            },
                            isAddedToNotebook = row.isAddedToNotebook,
                        )
                    }
                    if (hidden > 0) {
                        ReviewMoreRow(
                            text = if (expanded) {
                                stringResource(Res.string.result_show_less)
                            } else {
                                pluralStringResource(Res.plurals.result_more, hidden, hidden)
                            },
                            onClick = { onToggleGroup(group.outcome) },
                        )
                    }
                }
            }
        }
    }
}

private fun ReviewOutcome.titleRes(): StringResource = when (this) {
    ReviewOutcome.Correct -> Res.string.result_group_correct
    ReviewOutcome.Wrong -> Res.string.result_group_wrong
    ReviewOutcome.Passed -> Res.string.result_group_passed
    ReviewOutcome.TimeUp -> Res.string.result_group_time_up
}

private fun ReviewOutcome.descriptionRes(): StringResource = when (this) {
    ReviewOutcome.Correct -> Res.string.result_outcome_correct
    ReviewOutcome.Wrong -> Res.string.result_outcome_wrong
    ReviewOutcome.Passed -> Res.string.result_outcome_passed
    ReviewOutcome.TimeUp -> Res.string.result_outcome_time_up
}
