package dev.talhabayhan.lexi.feature.levels

import androidx.compose.runtime.Immutable
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.Level
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * One category card: both names (content, picked by the UI language) and the words learned of its [total] cards.
 * TODO(M7): Pro categories (`Category.isPro`) lock behind the paywall; none is Pro in the shipped content.
 */
@Immutable
internal data class CategoryUi(
    val id: CategoryId,
    val nameTr: String,
    val nameEn: String,
    val learned: Int,
    val total: Int,
) {
    val fraction: Float get() = if (total <= 0) 0f else (learned.toFloat() / total).coerceIn(0f, 1f)

    /** All words learned ("70 / 70 · tamam"). */
    val isComplete: Boolean get() = total > 0 && learned >= total
}

/**
 * What `07-categories` shows for [level]: the mixed wheel's word count ([levelTotal]) and the level's categories in
 * display order (categories without cards in the level are left out). [isLoaded] is false until the level's content is
 * read, so an empty list is only reported as such afterwards.
 */
@Immutable
internal data class CategoriesUiState(
    val level: Level = Level.A1,
    val levelTotal: Int = 0,
    val categories: ImmutableList<CategoryUi> = persistentListOf(),
    /** Slots of a regular wheel, for "26 kelime" on the mixed card. */
    val letterCount: Int = ('A'..'Z').count(),
    val isLoaded: Boolean = false,
)

internal sealed interface CategoriesIntent {
    /** A tab of the level bar. */
    data class SelectLevel(val level: Level) : CategoriesIntent

    data class OpenCategory(val id: CategoryId) : CategoriesIntent

    /** The red "Karışık çark" card. */
    data object PlayMixed : CategoriesIntent

    data object Back : CategoriesIntent
}

internal sealed interface CategoriesEffect {
    /** [category] null = the mixed wheel of the level. */
    data class StartGame(val level: Level, val mode: GameMode, val category: CategoryId?) : CategoriesEffect

    data object NavigateBack : CategoriesEffect
}
