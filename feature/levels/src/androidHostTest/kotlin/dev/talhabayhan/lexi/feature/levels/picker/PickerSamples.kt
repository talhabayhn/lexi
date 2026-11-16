package dev.talhabayhan.lexi.feature.levels.picker

import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.ExamPackId
import dev.talhabayhan.lexi.core.domain.model.Level
import kotlinx.collections.immutable.persistentListOf

/** "Çarkını seç" at B2 as in `drafts/video-flow/cats_v3.png`. */
internal val PickerSampleState = CategoryPickerUiState(
    level = Level.B2,
    categories = persistentListOf(
        PickerCategoryUi(CategoryId.MIXED, "Karışık", "Mixed", 412, 850),
        PickerCategoryUi(CategoryId("daily-life"), "Günlük hayat", "Daily life", 120, 240),
        PickerCategoryUi(CategoryId("business"), "İş İngilizcesi", "Business English", 40, 125),
        PickerCategoryUi(CategoryId("phrasal-verbs"), "Phrasal verbs", "Phrasal verbs", 30, 121),
        PickerCategoryUi(CategoryId("school-work"), "Okul ve iş", "School and work", 52, 117),
        PickerCategoryUi(CategoryId("travel"), "Seyahat", "Travel", 20, 53),
        PickerCategoryUi(CategoryId("body-health"), "Vücut ve sağlık", "Body and health", 18, 47),
        PickerCategoryUi(CategoryId("idioms"), "Deyimler", "Idioms", 12, 45),
        PickerCategoryUi(CategoryId("film-tv"), "Film ve dizi", "Film and TV", 4, 22),
        PickerCategoryUi(CategoryId("food-drink"), "Yiyecek ve içecek", "Food and drink", 6, 20),
    ),
    packs = persistentListOf(PickerPackUi(ExamPackId("lgs"), "LGS İngilizce", "LGS English", 120, 600)),
    isLoaded = true,
)

/** The dialog open on "Yiyecek ve içecek". */
internal fun CategoryPickerUiState.withDialog(mode: GameMode = GameMode.CLASSIC): CategoryPickerUiState =
    copy(dialog = categories.last(), selectedCategory = categories.last().id, mode = mode)
