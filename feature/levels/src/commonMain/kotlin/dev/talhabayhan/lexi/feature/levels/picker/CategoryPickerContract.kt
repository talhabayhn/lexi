package dev.talhabayhan.lexi.feature.levels.picker

import androidx.compose.runtime.Immutable
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.ExamPackId
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.settings.Settings
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * One card of "Çarkını seç": Karışık ([id] = [CategoryId.MIXED], the level's mixed wheel) or a category of the level.
 * Names are content in both languages (picked by the UI language); [learned] of its [total] cards.
 */
@Immutable
internal data class PickerCategoryUi(
    val id: CategoryId,
    val nameTr: String,
    val nameEn: String,
    val learned: Int,
    val total: Int,
) {
    val fraction: Float get() = if (total <= 0) 0f else (learned.toFloat() / total).coerceIn(0f, 1f)

    val isMixed: Boolean get() = id == CategoryId.MIXED
}

/** An open exam pack under "Sınav paketleri" (LGS today): tapping it starts its wheel as before. */
@Immutable
internal data class PickerPackUi(
    val id: ExamPackId,
    val titleTr: String,
    val titleEn: String,
    val learned: Int,
    val total: Int,
) {
    val fraction: Float get() = if (total <= 0) 0f else (learned.toFloat() / total).coerceIn(0f, 1f)
}

/** The upcoming exams, locked: dimmed art, a lock badge and "YAKINDA"; never tappable. Ids match `ExamArtKind`. */
internal val LockedExamIds: ImmutableList<String> = persistentListOf("yds-yokdil", "yks-dil", "toefl", "ielts")

/**
 * What "Çarkını seç" shows (`video-flow-notes.md` §2–§3): the [level] and its cards (Karışık first, then **every**
 * category of the level in `sort` order, even one without cards; NAT only Karışık), the open packs and the locked
 * exams, and the mode / time dialog for [dialog]. [mode] and [timeSeconds] are the remembered choice (Settings).
 */
@Immutable
internal data class CategoryPickerUiState(
    val level: Level = Level.A1,
    val categories: ImmutableList<PickerCategoryUi> = persistentListOf(),
    val packs: ImmutableList<PickerPackUi> = persistentListOf(),
    val lockedExams: ImmutableList<String> = LockedExamIds,
    /** The last chosen category of [level]; [CategoryId.MIXED] = Karışık. */
    val selectedCategory: CategoryId = CategoryId.MIXED,
    val mode: GameMode = GameMode.CLASSIC,
    val timeSeconds: Int = Settings.DEFAULT_CLASSIC_TIME_LIMIT_SECONDS,
    val timeOptions: ImmutableList<Int> = persistentListOf(180, 300, 420),
    /** The card whose mode / time dialog is open; null = no dialog. */
    val dialog: PickerCategoryUi? = null,
    /** False until the level's content is read. */
    val isLoaded: Boolean = false,
    /** The level's content could not be read: the grid shows an empty state with "Tekrar dene". */
    val hasError: Boolean = false,
) {
    /** Practice has no clock: the time row is disabled ("Pratik modda süre yok."). */
    val timeEnabled: Boolean get() = mode != GameMode.PRACTICE
}

/** The modes the dialog offers, in display order. */
internal val PickerModes: List<GameMode> = Settings.PLAY_MODES

internal sealed interface CategoryPickerIntent {
    /** A level chip: the level's cards; the category goes back to Karışık. */
    data class SelectLevel(val level: Level) : CategoryPickerIntent

    /** A category card (or Karışık with [CategoryId.MIXED]): remembers it and opens the mode / time dialog. */
    data class OpenCategory(val id: CategoryId) : CategoryPickerIntent

    /** An open exam pack: starts its wheel. */
    data class OpenPack(val id: ExamPackId) : CategoryPickerIntent

    data class SelectMode(val mode: GameMode) : CategoryPickerIntent

    data class SelectTime(val seconds: Int) : CategoryPickerIntent

    /** OYNA in the dialog. */
    data object Play : CategoryPickerIntent

    /** The dialog's back arrow, the scrim or system back while it is open. */
    data object CloseDialog : CategoryPickerIntent

    data object Back : CategoryPickerIntent

    /** "Tekrar dene" after the content could not be read. */
    data object Retry : CategoryPickerIntent
}

internal sealed interface CategoryPickerEffect {
    /** [category] null = the level's mixed wheel; [timeSeconds] null = untimed (Practice). */
    data class StartGame(val level: Level, val mode: GameMode, val category: CategoryId?, val timeSeconds: Int?) :
        CategoryPickerEffect

    data class StartPack(val pack: ExamPackId) : CategoryPickerEffect

    data object NavigateBack : CategoryPickerEffect
}
