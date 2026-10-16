package dev.talhabayhan.lexi.feature.notebook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.talhabayhan.lexi.core.domain.feedback.Speaker
import dev.talhabayhan.lexi.core.domain.model.Card
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.repository.CardRepository
import dev.talhabayhan.lexi.core.domain.repository.CategoryRepository
import dev.talhabayhan.lexi.core.domain.repository.ProgressRepository
import dev.talhabayhan.lexi.core.domain.srs.CardProgress
import dev.talhabayhan.lexi.core.domain.srs.Srs
import dev.talhabayhan.lexi.core.domain.time.AppClock
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Kelime Defterim (`08-notebook`): the notebook cards (progress rows with a Leitner box) joined with their content,
 * split into the Tekrar / Tümü / Zor tabs, filtered by the search text (English or Turkish). The review wheel itself
 * is built by the game from the due cards; this screen only starts it. Days are epoch days of the player's zone.
 */
internal class NotebookViewModel(
    private val progress: ProgressRepository,
    private val cards: CardRepository,
    private val categories: CategoryRepository,
    private val appClock: AppClock,
    private val speaker: Speaker,
) : ViewModel() {
    private val mutableState = MutableStateFlow(NotebookUiState())
    val state: StateFlow<NotebookUiState> = mutableState.asStateFlow()

    private val effectChannel = Channel<NotebookEffect>(Channel.BUFFERED)
    val effects: Flow<NotebookEffect> = effectChannel.receiveAsFlow()

    private val tab = MutableStateFlow(NotebookTab.Due)
    private val query = MutableStateFlow("")
    private val openId = MutableStateFlow<CardId?>(null)

    /** Content never changes while the app runs, so cards and category names are read once per id. */
    private val cardCache = HashMap<CardId, Card>()
    private var categoryNames: Map<CategoryId, Pair<String, String>>? = null

    private class Entry(val progress: CardProgress, val card: Card)

    /** The running read of the notebook; restarted by "Tekrar dene". */
    private var observing: Job? = null

    init {
        observe()
    }

    private fun observe() {
        observing = viewModelScope.launch {
            combine(
                progress.observeAll().map { all -> entriesOf(all) },
                tab,
                query,
                openId,
            ) { entries, tab, query, open ->
                present(entries, tab, query, open, appClock.epochDay(appClock.nowMs()))
            }
                // A broken read (notebook or word content) shows the error: hidden words would look like an empty
                // notebook. TODO(M7): report to the crash reporter once it is behind its interface.
                .catch {
                    mutableState.update {
                        it.copy(words = persistentListOf(), detail = null, isLoaded = true, hasError = true)
                    }
                }
                .collect { derived ->
                    // Query and tab are written by onIntent right away: a keystroke must not wait for this flow.
                    mutableState.update {
                        it.copy(
                            words = derived.words,
                            dueCount = derived.dueCount,
                            allCount = derived.allCount,
                            hardCount = derived.hardCount,
                            detail = derived.detail,
                            isLoaded = true,
                            hasError = false,
                        )
                    }
                }
        }
    }

    private suspend fun entriesOf(all: List<CardProgress>): List<Entry> {
        val inNotebook = all.filter { it.inNotebook }
        val missing = inNotebook.map { it.cardId }.filter { it !in cardCache }
        // Unreadable content throws: the flow ends in the error state.
        if (missing.isNotEmpty()) cards.cards(missing).forEach { cardCache[it.id] = it }
        if (categoryNames == null) {
            categoryNames = try {
                categories.categories().associate { it.id to (it.nameTr to it.nameEn) }
            } catch (e: CancellationException) {
                throw e
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                emptyMap()
            }
        }
        // Progress of a card that left the content stays stored but is hidden (ProgressRepository).
        return inNotebook.mapNotNull { p -> cardCache[p.cardId]?.let { Entry(p, it) } }
    }

    private fun present(
        entries: List<Entry>,
        tab: NotebookTab,
        query: String,
        open: CardId?,
        today: Long,
    ): NotebookUiState {
        val due = entries.filter { it.progress.isDue(today) }
            .sortedWith(compareBy<Entry>({ it.progress.dueEpochDay }, { it.card.id.value }))
        val hard = entries.filter { it.progress.wrong >= Srs.HARD_WRONGS }
            .sortedWith(compareByDescending<Entry> { it.progress.wrong }.thenBy { it.card.id.value })
        // Soonest first; mastered cards (no due day) last.
        val all = entries.sortedWith(
            compareBy<Entry>({ it.progress.dueEpochDay ?: Long.MAX_VALUE }, { it.card.id.value }),
        )
        val shown = when (tab) {
            NotebookTab.Due -> due
            NotebookTab.All -> all
            NotebookTab.Hard -> hard
        }
        val needle = query.trim().lowercase()
        val matching = if (needle.isEmpty()) {
            shown
        } else {
            shown.filter { it.card.answer.lowercase().contains(needle) || it.card.clue.lowercase().contains(needle) }
        }
        val detail = open?.let { id -> entries.firstOrNull { it.card.id == id }?.let { detailOf(it, today) } }
        return NotebookUiState(
            tab = tab,
            query = query,
            words = matching.map { wordOf(it, today) }.toImmutableList(),
            dueCount = due.size,
            allCount = entries.size,
            hardCount = hard.size,
            detail = detail,
        )
    }

    private fun wordOf(entry: Entry, today: Long): WordUi {
        val p = entry.progress
        val box = checkNotNull(p.box)
        val due = p.dueEpochDay
        val next = when {
            p.mastered || due == null -> NextReview.Mastered
            due <= today -> NextReview.Today
            due == today + 1 -> NextReview.Tomorrow
            else -> NextReview.InDays((due - today).toInt())
        }
        return WordUi(
            id = entry.card.id,
            word = entry.card.answer.lowercase(),
            clue = entry.card.clue,
            level = entry.card.level,
            strength = if (p.mastered) STRENGTH_STEPS else (box + 1).coerceAtMost(STRENGTH_STEPS),
            next = next,
        )
    }

    private fun detailOf(entry: Entry, today: Long): WordDetailUi {
        val names = categoryNames?.get(entry.card.category)
        return WordDetailUi(
            word = wordOf(entry, today),
            example = entry.card.example,
            exampleTranslation = entry.card.exampleTranslation,
            categoryNameTr = names?.first ?: entry.card.category.value,
            categoryNameEn = names?.second ?: entry.card.category.value,
            box = checkNotNull(entry.progress.box),
            seen = entry.progress.seen,
            correct = entry.progress.correct,
            wrong = entry.progress.wrong,
        )
    }

    fun onIntent(intent: NotebookIntent) {
        when (intent) {
            is NotebookIntent.SelectTab -> {
                mutableState.update { it.copy(tab = intent.tab) }
                tab.value = intent.tab
            }

            is NotebookIntent.QueryChanged -> {
                mutableState.update { it.copy(query = intent.query) }
                query.value = intent.query
            }

            is NotebookIntent.OpenWord -> openId.value = intent.id

            NotebookIntent.CloseWord -> closeWord()

            is NotebookIntent.Speak -> speaker.speak(intent.word)

            is NotebookIntent.RemoveWord -> remove(intent.id)

            NotebookIntent.StartReview -> if (mutableState.value.dueCount > 0) {
                effectChannel.trySend(NotebookEffect.StartReview)
            }

            NotebookIntent.Back -> effectChannel.trySend(NotebookEffect.NavigateBack)

            NotebookIntent.Retry -> if (mutableState.value.hasError) {
                observing?.cancel()
                mutableState.update { it.copy(isLoaded = false, hasError = false) }
                observe()
            }
        }
    }

    private fun closeWord() {
        speaker.stop()
        openId.value = null
        mutableState.update { it.copy(detail = null) }
    }

    private fun remove(id: CardId) {
        closeWord()
        viewModelScope.launch {
            try {
                val stored = progress.get(listOf(id))[id] ?: return@launch
                progress.save(listOf(Srs.removeFromNotebook(stored)))
            } catch (e: CancellationException) {
                throw e
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                // TODO(M7): report to the crash reporter; the word simply stays in the notebook.
            }
        }
    }

    override fun onCleared() {
        speaker.stop()
    }

    private companion object {
        /** Memory-strength bars of a word row. */
        const val STRENGTH_STEPS = 4
    }
}
