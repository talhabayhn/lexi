package dev.talhabayhan.lexi.feature.exams

import androidx.compose.runtime.Immutable
import dev.talhabayhan.lexi.core.domain.model.ExamPackId
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * One pack row of `10-exam-packs`. Titles are content in both languages (picked by the UI language); [cardCount] is
 * the real number of cards (the design's "600 kelime" is a placeholder, owner decision) and [learnedPercent] the share
 * of them the player has answered correctly at least once.
 */
@Immutable
internal data class ExamPackUi(
    val id: ExamPackId,
    val badge: String,
    /** Position in the design's badge cycle (`ExamPackBadgeStyle`). */
    val badgeIndex: Int,
    val titleTr: String,
    val titleEn: String,
    val descriptionTr: String,
    val cardCount: Int,
    val learnedPercent: Int,
)

/**
 * What `10-exam-packs` shows. [isLoaded] is false until the packs are read; [hasError] after a failed read (empty state
 * with "Tekrar dene"); [comingSoon] the static, non-clickable "Yakında" exams listed under the packs.
 */
@Immutable
internal data class ExamPacksUiState(
    val packs: ImmutableList<ExamPackUi> = persistentListOf(),
    val isLoaded: Boolean = false,
    val hasError: Boolean = false,
    val comingSoon: ImmutableList<ComingSoonExamUi> = ComingSoonExams,
)

internal sealed interface ExamPacksIntent {
    data class OpenPack(val id: ExamPackId) : ExamPacksIntent

    data object Back : ExamPacksIntent

    /** "Tekrar dene" after the packs could not be read. */
    data object Retry : ExamPacksIntent
}

internal sealed interface ExamPacksEffect {
    /** Start a wheel of the [pack]; the graph in `shared` owns the game route. */
    data class StartPack(val pack: ExamPackId) : ExamPacksEffect

    data object NavigateBack : ExamPacksEffect
}
