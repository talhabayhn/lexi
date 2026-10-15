package dev.talhabayhan.lexi.feature.levels

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.tooling.preview.Preview
import dev.talhabayhan.lexi.core.designsystem.component.CategoryCard
import dev.talhabayhan.lexi.core.designsystem.component.DecorShapeKind
import dev.talhabayhan.lexi.core.designsystem.component.EmptyState
import dev.talhabayhan.lexi.core.designsystem.component.MixedWheelCard
import dev.talhabayhan.lexi.core.designsystem.component.NavSquareButton
import dev.talhabayhan.lexi.core.designsystem.component.Segment
import dev.talhabayhan.lexi.core.designsystem.component.SegmentedControl
import dev.talhabayhan.lexi.core.designsystem.icon.LexiIcons
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.feature.levels.generated.resources.Res
import dev.talhabayhan.lexi.feature.levels.generated.resources.categories_empty
import dev.talhabayhan.lexi.feature.levels.generated.resources.categories_mixed_subtitle
import dev.talhabayhan.lexi.feature.levels.generated.resources.categories_mixed_title
import dev.talhabayhan.lexi.feature.levels.generated.resources.categories_progress
import dev.talhabayhan.lexi.feature.levels.generated.resources.categories_progress_done
import dev.talhabayhan.lexi.feature.levels.generated.resources.categories_title
import dev.talhabayhan.lexi.feature.levels.generated.resources.levels_back
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource

/**
 * `07-categories`: back button and title, the level tabs, the red mixed-wheel card and the 2-column category grid with
 * the learned words per category. Scrolls when the categories do not fit.
 */
@Composable
internal fun CategoriesScreen(
    state: CategoriesUiState,
    onIntent: (CategoriesIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = LexiTheme.dimens
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LexiTheme.colors.cream)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(
                start = dimens.screenPaddingHorizontal,
                top = dimens.screenPaddingTop,
                end = dimens.screenPaddingHorizontal,
                bottom = dimens.screenPaddingBottom,
            ),
        verticalArrangement = Arrangement.spacedBy(dimens.gap14),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimens.gap12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavSquareButton(
                icon = LexiIcons.Back,
                contentDescription = stringResource(Res.string.levels_back),
                onClick = { onIntent(CategoriesIntent.Back) },
            )
            Text(stringResource(Res.string.categories_title), style = LexiTheme.typography.title)
        }
        SegmentedControl(
            segments = Level.entries.map { Segment(it.name) },
            selectedIndex = Level.entries.indexOf(state.level),
            onSelect = { onIntent(CategoriesIntent.SelectLevel(Level.entries[it])) },
            modifier = Modifier.fillMaxWidth(),
        )
        if (state.isLoaded) {
            if (state.categories.isEmpty()) {
                EmptyState(stringResource(Res.string.categories_empty), Modifier.fillMaxWidth())
            } else {
                MixedWheel(state, onIntent)
                CategoryGrid(state, onIntent)
            }
        }
    }
}

@Composable
private fun MixedWheel(state: CategoriesUiState, onIntent: (CategoriesIntent) -> Unit) {
    MixedWheelCard(
        title = stringResource(Res.string.categories_mixed_title),
        subtitle = stringResource(Res.string.categories_mixed_subtitle, state.level.name, state.letterCount),
        trailing = state.levelTotal.toString(),
        onClick = { onIntent(CategoriesIntent.PlayMixed) },
        enabled = state.levelTotal > 0,
    )
}

@Composable
private fun CategoryGrid(state: CategoriesUiState, onIntent: (CategoriesIntent) -> Unit) {
    val gap = LexiTheme.dimens.gap10
    val turkish = Locale.current.language == TURKISH
    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        state.categories.withIndex().chunked(CATEGORY_COLUMNS).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                row.forEach { (index, category) ->
                    val look = lookOf(index)
                    CategoryCard(
                        title = if (turkish) category.nameTr else category.nameEn,
                        progress = category.fraction,
                        caption = stringResource(
                            if (category.isComplete) {
                                Res.string.categories_progress_done
                            } else {
                                Res.string.categories_progress
                            },
                            category.learned,
                            category.total,
                        ),
                        marker = look.marker,
                        markerColor = look.color,
                        onClick = { onIntent(CategoriesIntent.OpenCategory(category.id)) },
                        complete = category.isComplete,
                        modifier = Modifier.weight(1f),
                    )
                }
                // An odd last row keeps its card at half width.
                repeat(CATEGORY_COLUMNS - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

private const val CATEGORY_COLUMNS = 2
private const val TURKISH = "tr"

/** Marker shape and color of a category card; the design cycles square, circle, leaf down the list. */
private class CategoryLook(val marker: DecorShapeKind, val color: Color)

@Composable
private fun lookOf(index: Int): CategoryLook {
    val colors = LexiTheme.colors
    return when (index % 3) {
        0 -> CategoryLook(DecorShapeKind.Square, colors.red)
        1 -> CategoryLook(DecorShapeKind.Circle, colors.yellow)
        else -> CategoryLook(DecorShapeKind.Leaf, colors.blue)
    }
}

// design sample, not user-facing
internal val CategoriesSampleState = CategoriesUiState(
    level = Level.A1,
    levelTotal = 850,
    isLoaded = true,
    categories = persistentListOf(
        CategoryUi(CategoryId("daily-life"), "Günlük hayat", "Daily life", 88, 120),
        CategoryUi(CategoryId("food-drink"), "Yiyecek ve içecek", "Food and drink", 61, 95),
        CategoryUi(CategoryId("home"), "Ev ve eşyalar", "Home", 52, 80),
        CategoryUi(CategoryId("family-people"), "Aile ve insanlar", "Family and people", 70, 70),
        CategoryUi(CategoryId("colors-numbers"), "Renkler ve sayılar", "Colours and numbers", 60, 60),
        CategoryUi(CategoryId("body-health"), "Vücut ve sağlık", "Body and health", 23, 75),
        CategoryUi(CategoryId("school-work"), "Okul ve iş", "School and work", 18, 65),
        CategoryUi(CategoryId("animals"), "Hayvanlar", "Animals", 30, 70),
        CategoryUi(CategoryId("city-transport"), "Şehir ve ulaşım", "City and transport", 0, 85),
        CategoryUi(CategoryId("time-weather"), "Zaman ve hava", "Time and weather", 0, 60),
    ),
)

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun CategoriesScreenPreview() {
    LexiTheme {
        CategoriesScreen(state = CategoriesSampleState, onIntent = {})
    }
}
