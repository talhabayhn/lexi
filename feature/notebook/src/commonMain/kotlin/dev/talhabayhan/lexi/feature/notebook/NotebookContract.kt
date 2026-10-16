package dev.talhabayhan.lexi.feature.notebook

import androidx.compose.runtime.Immutable
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.Level
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/** The three tabs of `08-notebook`: due today, everything in the notebook, hard (>= 2 wrong answers). */
internal enum class NotebookTab { Due, All, Hard }

/** When a word comes up again ("tekrar: bugün" / "yarın" / "3 gün sonra"). */
internal sealed interface NextReview {
    /** Due today or overdue. */
    data object Today : NextReview

    data object Tomorrow : NextReview

    /** [days] >= 2. */
    data class InDays(val days: Int) : NextReview

    /** Learned: left the due queue. */
    data object Mastered : NextReview
}

/** One notebook row. [word] is lower case like in the design; [clue] is the Turkish translation. */
@Immutable
internal data class WordUi(
    val id: CardId,
    val word: String,
    val clue: String,
    val level: Level,
    /** Memory-strength bars 1-4 (Leitner box + 1; box 4 and mastered both show 4). */
    val strength: Int,
    val next: NextReview,
)

/**
 * Word detail sheet (DESIGN-DEBT: no design). Category names are content in both languages, the UI picks one;
 * [box] is the Leitner box 0-4.
 */
@Immutable
internal data class WordDetailUi(
    val word: WordUi,
    val example: String,
    val exampleTranslation: String,
    val categoryNameTr: String,
    val categoryNameEn: String,
    val box: Int,
    val seen: Int,
    val correct: Int,
    val wrong: Int,
)

@Immutable
internal data class NotebookUiState(
    val tab: NotebookTab = NotebookTab.Due,
    /** The search text; written by the intent at once, the rows follow. */
    val query: String = "",
    /** Rows of the selected tab that match [query]. */
    val words: ImmutableList<WordUi> = persistentListOf(),
    val dueCount: Int = 0,
    val allCount: Int = 0,
    val hardCount: Int = 0,
    val detail: WordDetailUi? = null,
    /** False until the notebook is read, so "empty" is only reported afterwards. */
    val isLoaded: Boolean = false,
)

internal sealed interface NotebookIntent {
    data class SelectTab(val tab: NotebookTab) : NotebookIntent

    data class QueryChanged(val query: String) : NotebookIntent

    data class OpenWord(val id: CardId) : NotebookIntent

    data object CloseWord : NotebookIntent

    /** Speaker button of a row or of the sheet. */
    data class Speak(val word: String) : NotebookIntent

    /** "Defterden çıkar" in the sheet. */
    data class RemoveWord(val id: CardId) : NotebookIntent

    /** "Tekrar çarkını başlat": the review wheel over the due words. */
    data object StartReview : NotebookIntent

    data object Back : NotebookIntent
}

internal sealed interface NotebookEffect {
    data object NavigateBack : NotebookEffect

    data object StartReview : NotebookEffect
}
