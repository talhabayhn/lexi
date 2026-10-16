package dev.talhabayhan.lexi.feature.notebook

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.tooling.preview.Preview
import dev.talhabayhan.lexi.core.designsystem.component.BottomSheet
import dev.talhabayhan.lexi.core.designsystem.component.CountChip
import dev.talhabayhan.lexi.core.designsystem.component.EmptyState
import dev.talhabayhan.lexi.core.designsystem.component.LexiButton
import dev.talhabayhan.lexi.core.designsystem.component.LexiButtonDefaults
import dev.talhabayhan.lexi.core.designsystem.component.LexiButtonVariant
import dev.talhabayhan.lexi.core.designsystem.component.NavSquareButton
import dev.talhabayhan.lexi.core.designsystem.component.SearchField
import dev.talhabayhan.lexi.core.designsystem.component.Segment
import dev.talhabayhan.lexi.core.designsystem.component.SegmentedControl
import dev.talhabayhan.lexi.core.designsystem.component.WordRow
import dev.talhabayhan.lexi.core.designsystem.icon.LexiIcons
import dev.talhabayhan.lexi.core.designsystem.modifier.lexiSurface
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.lexiContentWidth
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.feature.notebook.generated.resources.Res
import dev.talhabayhan.lexi.feature.notebook.generated.resources.notebook_back
import dev.talhabayhan.lexi.feature.notebook.generated.resources.notebook_empty_all
import dev.talhabayhan.lexi.feature.notebook.generated.resources.notebook_empty_due
import dev.talhabayhan.lexi.feature.notebook.generated.resources.notebook_empty_hard
import dev.talhabayhan.lexi.feature.notebook.generated.resources.notebook_empty_search
import dev.talhabayhan.lexi.feature.notebook.generated.resources.notebook_error
import dev.talhabayhan.lexi.feature.notebook.generated.resources.notebook_meta_days
import dev.talhabayhan.lexi.feature.notebook.generated.resources.notebook_meta_mastered
import dev.talhabayhan.lexi.feature.notebook.generated.resources.notebook_meta_today
import dev.talhabayhan.lexi.feature.notebook.generated.resources.notebook_meta_tomorrow
import dev.talhabayhan.lexi.feature.notebook.generated.resources.notebook_retry
import dev.talhabayhan.lexi.feature.notebook.generated.resources.notebook_review_count
import dev.talhabayhan.lexi.feature.notebook.generated.resources.notebook_review_start
import dev.talhabayhan.lexi.feature.notebook.generated.resources.notebook_search_placeholder
import dev.talhabayhan.lexi.feature.notebook.generated.resources.notebook_sheet_box
import dev.talhabayhan.lexi.feature.notebook.generated.resources.notebook_sheet_box_mastered
import dev.talhabayhan.lexi.feature.notebook.generated.resources.notebook_sheet_close
import dev.talhabayhan.lexi.feature.notebook.generated.resources.notebook_sheet_level_category
import dev.talhabayhan.lexi.feature.notebook.generated.resources.notebook_sheet_next
import dev.talhabayhan.lexi.feature.notebook.generated.resources.notebook_sheet_next_days
import dev.talhabayhan.lexi.feature.notebook.generated.resources.notebook_sheet_next_mastered
import dev.talhabayhan.lexi.feature.notebook.generated.resources.notebook_sheet_next_today
import dev.talhabayhan.lexi.feature.notebook.generated.resources.notebook_sheet_next_tomorrow
import dev.talhabayhan.lexi.feature.notebook.generated.resources.notebook_sheet_remove
import dev.talhabayhan.lexi.feature.notebook.generated.resources.notebook_sheet_speak
import dev.talhabayhan.lexi.feature.notebook.generated.resources.notebook_sheet_stats
import dev.talhabayhan.lexi.feature.notebook.generated.resources.notebook_speak
import dev.talhabayhan.lexi.feature.notebook.generated.resources.notebook_tab_all
import dev.talhabayhan.lexi.feature.notebook.generated.resources.notebook_tab_due
import dev.talhabayhan.lexi.feature.notebook.generated.resources.notebook_tab_hard
import dev.talhabayhan.lexi.feature.notebook.generated.resources.notebook_title
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.distinctUntilChanged
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * `08-notebook`: back button and title, the Tekrar / Tümü / Zor tabs, the search field, the blue review-wheel button
 * (only while something is due) and the word list. The list scrolls on its own below the fixed controls; its box
 * only grows as tall as its rows. Tapping a row opens the word detail sheet (DESIGN-DEBT: no design).
 */
@Composable
internal fun NotebookScreen(
    state: NotebookUiState,
    onIntent: (NotebookIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = LexiTheme.dimens
    Box(modifier.fillMaxSize().background(LexiTheme.colors.cream)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .lexiContentWidth()
                .padding(
                    start = dimens.screenPaddingHorizontal,
                    top = dimens.screenPaddingTop,
                    end = dimens.screenPaddingHorizontal,
                    bottom = dimens.screenPaddingBottomList,
                ),
            verticalArrangement = Arrangement.spacedBy(dimens.gap14),
        ) {
            Header(onBack = { onIntent(NotebookIntent.Back) })
            Tabs(state, onIntent)
            val search = rememberTextFieldState(state.query)
            LaunchedEffect(search) {
                snapshotFlow { search.text.toString() }
                    .distinctUntilChanged()
                    .collect { onIntent(NotebookIntent.QueryChanged(it)) }
            }
            SearchField(search, stringResource(Res.string.notebook_search_placeholder))
            if (state.dueCount > 0) ReviewButton(state.dueCount) { onIntent(NotebookIntent.StartReview) }
            if (state.isLoaded) {
                if (state.hasError) {
                    // DESIGN-DEBT: no design for a failed read; the component library's empty state with a retry.
                    EmptyState(
                        message = stringResource(Res.string.notebook_error),
                        actionText = stringResource(Res.string.notebook_retry),
                        onAction = { onIntent(NotebookIntent.Retry) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else if (state.words.isEmpty()) {
                    EmptyState(stringResource(emptyMessage(state)), Modifier.fillMaxWidth())
                } else {
                    WordList(state, onIntent, Modifier.weight(1f, fill = false))
                }
            }
        }
        WordSheet(state.detail, onIntent)
    }
}

@Composable
private fun Header(onBack: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NavSquareButton(
            icon = LexiIcons.Back,
            contentDescription = stringResource(Res.string.notebook_back),
            onClick = onBack,
        )
        Text(stringResource(Res.string.notebook_title), style = LexiTheme.typography.title)
    }
}

@Composable
private fun Tabs(state: NotebookUiState, onIntent: (NotebookIntent) -> Unit) {
    val segments = listOf(
        Segment(stringResource(Res.string.notebook_tab_due, state.dueCount)),
        Segment(stringResource(Res.string.notebook_tab_all, state.allCount)),
        Segment(stringResource(Res.string.notebook_tab_hard, state.hardCount)),
    )
    SegmentedControl(
        segments = segments,
        selectedIndex = NotebookTab.entries.indexOf(state.tab),
        onSelect = { onIntent(NotebookIntent.SelectTab(NotebookTab.entries[it])) },
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ReviewButton(dueCount: Int, onClick: () -> Unit) {
    LexiButton(
        onClick = onClick,
        variant = LexiButtonVariant.Blue,
        height = LexiButtonDefaults.LargerHeight,
        contentPadding = PaddingValues(horizontal = LexiTheme.dimens.gap16),
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            stringResource(Res.string.notebook_review_start),
            style = LexiTheme.typography.heading18,
            color = LexiTheme.colors.white,
            modifier = Modifier.weight(1f, fill = false),
        )
        CountChip(
            pluralStringResource(Res.plurals.notebook_review_count, dueCount, dueCount),
            Modifier.padding(start = LexiTheme.dimens.gap8),
        )
    }
}

private fun emptyMessage(state: NotebookUiState) = when {
    state.query.isNotBlank() -> Res.string.notebook_empty_search
    state.tab == NotebookTab.Due -> Res.string.notebook_empty_due
    state.tab == NotebookTab.Hard -> Res.string.notebook_empty_hard
    else -> Res.string.notebook_empty_all
}

@Composable
private fun WordList(state: NotebookUiState, onIntent: (NotebookIntent) -> Unit, modifier: Modifier = Modifier) {
    val colors = LexiTheme.colors
    val divider = LexiTheme.borders.thin
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .lexiSurface(colors.white, BorderStroke(LexiTheme.borders.regular, colors.ink)),
    ) {
        itemsIndexed(state.words, key = { _, word -> word.id.value }) { index, word ->
            Column {
                if (index > 0) Box(Modifier.fillMaxWidth().height(divider).background(colors.ink))
                WordRow(
                    word = word.word,
                    translation = word.clue,
                    meta = metaOf(word),
                    strength = word.strength,
                    speakContentDescription = stringResource(Res.string.notebook_speak, word.word),
                    onSpeak = { onIntent(NotebookIntent.Speak(word.word)) },
                    modifier = Modifier.clickable(role = Role.Button) { onIntent(NotebookIntent.OpenWord(word.id)) },
                )
            }
        }
    }
}

@Composable
private fun metaOf(word: WordUi): String {
    val level = word.level.name
    return when (val next = word.next) {
        NextReview.Today -> stringResource(Res.string.notebook_meta_today, level)
        NextReview.Tomorrow -> stringResource(Res.string.notebook_meta_tomorrow, level)
        is NextReview.InDays -> stringResource(Res.string.notebook_meta_days, level, next.days)
        NextReview.Mastered -> stringResource(Res.string.notebook_meta_mastered, level)
    }
}

/**
 * DESIGN-DEBT: the word detail sheet has no design. A bottom sheet like the joker sheet (`BottomSheet`): word, Turkish
 * clue, level and category, example sentence, memory box and next review, the listen button and "Defterden çıkar".
 */
@Composable
private fun WordSheet(detail: WordDetailUi?, onIntent: (NotebookIntent) -> Unit) {
    // The content stays while the sheet slides out.
    var last by remember { mutableStateOf<WordDetailUi?>(null) }
    LaunchedEffect(detail) { if (detail != null) last = detail }
    val shown = detail ?: last
    BottomSheet(
        visible = detail != null,
        onDismissRequest = { onIntent(NotebookIntent.CloseWord) },
        title = shown?.word?.word.orEmpty(),
        closeContentDescription = stringResource(Res.string.notebook_sheet_close),
    ) {
        if (shown != null) WordSheetContent(shown, onIntent)
    }
}

@Composable
private fun ColumnScope.WordSheetContent(detail: WordDetailUi, onIntent: (NotebookIntent) -> Unit) {
    val typography = LexiTheme.typography
    val dimens = LexiTheme.dimens
    val word = detail.word
    // TODO(M7): follow Settings.uiLanguage once the in-app language setting is applied.
    val category = if (Locale.current.language == TURKISH) detail.categoryNameTr else detail.categoryNameEn
    Text(word.clue, style = typography.heading20)
    Text(
        stringResource(Res.string.notebook_sheet_level_category, word.level.name, category),
        style = typography.caption14SemiBold,
    )
    Column(verticalArrangement = Arrangement.spacedBy(dimens.gap2)) {
        Text(detail.example, style = typography.body15)
        Text(detail.exampleTranslation, style = typography.caption13)
    }
    Column(verticalArrangement = Arrangement.spacedBy(dimens.gap2)) {
        Text(
            if (word.next == NextReview.Mastered) {
                stringResource(Res.string.notebook_sheet_box_mastered)
            } else {
                stringResource(Res.string.notebook_sheet_box, detail.box + 1)
            },
            style = typography.bodyStrong14,
        )
        Text(stringResource(Res.string.notebook_sheet_next, nextText(word.next)), style = typography.caption14)
        Text(
            stringResource(Res.string.notebook_sheet_stats, detail.seen, detail.correct, detail.wrong),
            style = typography.caption13,
        )
    }
    LexiButton(
        text = stringResource(Res.string.notebook_sheet_speak),
        onClick = { onIntent(NotebookIntent.Speak(word.word)) },
        variant = LexiButtonVariant.Ink,
        shadow = null,
        leadingIcon = LexiIcons.Speaker,
        height = LexiButtonDefaults.SmallHeight,
        modifier = Modifier.fillMaxWidth(),
    )
    LexiButton(
        text = stringResource(Res.string.notebook_sheet_remove),
        onClick = { onIntent(NotebookIntent.RemoveWord(word.id)) },
        variant = LexiButtonVariant.Secondary,
        shadow = null,
        height = LexiButtonDefaults.SmallHeight,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun nextText(next: NextReview): String = when (next) {
    NextReview.Today -> stringResource(Res.string.notebook_sheet_next_today)
    NextReview.Tomorrow -> stringResource(Res.string.notebook_sheet_next_tomorrow)
    is NextReview.InDays -> stringResource(Res.string.notebook_sheet_next_days, next.days)
    NextReview.Mastered -> stringResource(Res.string.notebook_sheet_next_mastered)
}

private const val TURKISH = "tr"

// design sample (`08-notebook.png`), not user-facing
private fun sampleWord(id: String, word: String, clue: String, level: Level, strength: Int, next: NextReview) =
    WordUi(CardId(id), word, clue, level, strength, next)

internal val NotebookSampleWords = persistentListOf(
    sampleWord("a1-egg", "egg", "yumurta", Level.A1, 1, NextReview.Today),
    sampleWord("a1-mirror", "mirror", "ayna", Level.A1, 1, NextReview.Today),
    sampleWord("a1-chair", "chair", "sandalye", Level.A1, 2, NextReview.Today),
    sampleWord("a1-horse", "horse", "at", Level.A1, 2, NextReview.Today),
    sampleWord("a1-yesterday", "yesterday", "dün", Level.A1, 3, NextReview.Tomorrow),
    sampleWord("a2-neighbour", "neighbour", "komşu", Level.A2, 3, NextReview.InDays(3)),
)

internal val NotebookSampleState = NotebookUiState(
    tab = NotebookTab.Due,
    words = NotebookSampleWords,
    dueCount = 24,
    allCount = 412,
    hardCount = 31,
    isLoaded = true,
)

internal val NotebookSampleDetail = WordDetailUi(
    word = NotebookSampleWords[2],
    example = "Please sit on the chair.",
    exampleTranslation = "Lütfen sandalyeye otur.",
    categoryNameTr = "Ev ve eşyalar",
    categoryNameEn = "Home",
    box = 1,
    seen = 4,
    correct = 2,
    wrong = 2,
)

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun NotebookScreenPreview() {
    LexiTheme {
        NotebookScreen(state = NotebookSampleState, onIntent = {})
    }
}
