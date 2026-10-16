package dev.talhabayhan.lexi.feature.levels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.Category
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.repository.CardRepository
import dev.talhabayhan.lexi.core.domain.repository.CategoryRepository
import dev.talhabayhan.lexi.core.domain.repository.ProgressRepository
import dev.talhabayhan.lexi.core.domain.stats.Stats
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Level categories (`07-categories`): per category the card total and the player's learned words of the selected
 * level, and the start of a category (or mixed) wheel as an effect. The game mode comes from the level select.
 */
internal class CategoriesViewModel(
    private val route: CategoriesRoute,
    private val cards: CardRepository,
    private val categories: CategoryRepository,
    progress: ProgressRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(CategoriesUiState(level = route.startLevel))
    val state: StateFlow<CategoriesUiState> = mutableState.asStateFlow()

    private val effectChannel = Channel<CategoriesEffect>(Channel.BUFFERED)
    val effects: Flow<CategoriesEffect> = effectChannel.receiveAsFlow()

    private val selectedLevel = MutableStateFlow(route.startLevel)

    /** Bumped by "Tekrar dene": reads the selected level's content again. */
    private val reloads = MutableStateFlow(0)

    init {
        viewModelScope.launch {
            // A new level (or a retry) cancels the running collection of the old one.
            combine(selectedLevel, reloads) { level, _ -> level }.collectLatest { level ->
                val content = loadContent(level)
                if (content == null) {
                    mutableState.update {
                        it.copy(
                            level = level,
                            levelTotal = 0,
                            categories = persistentListOf(),
                            isLoaded = true,
                            hasError = true,
                        )
                    }
                    return@collectLatest
                }
                progress.observeAll()
                    // A broken read keeps the last numbers; the screen itself still works.
                    .catch { } // TODO(M7): report to the crash reporter once it is behind its interface.
                    .collect { answered ->
                        val byCategory = Stats.categoryProgress(answered, content.categoryOf)
                        val ui = content.categories.mapNotNull { category ->
                            val entry = byCategory[category.id] ?: return@mapNotNull null
                            CategoryUi(category.id, category.nameTr, category.nameEn, entry.learned, entry.total)
                        }
                        mutableState.update {
                            it.copy(
                                level = level,
                                levelTotal = content.categoryOf.size,
                                categories = ui.toImmutableList(),
                                isLoaded = true,
                                hasError = false,
                            )
                        }
                    }
            }
        }
    }

    /** The level's categories (without the virtual mixed one and the neutral general bucket) and the category of each of its cards. */
    private class LevelContent(val categories: List<Category>, val categoryOf: Map<CardId, CategoryId>)

    /** Content never changes while the app runs; null when it cannot be read. */
    private suspend fun loadContent(level: Level): LevelContent? = try {
        LevelContent(
            categories = categories.categories(level).filter {
                it.id != CategoryId.MIXED && it.id != CategoryId.GENERAL
            },
            categoryOf = cards.categoryOfCards(level),
        )
    } catch (e: CancellationException) {
        throw e
    } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
        // TODO(M7): report to the crash reporter once it is behind its interface.
        null
    }

    fun onIntent(intent: CategoriesIntent) {
        when (intent) {
            is CategoriesIntent.SelectLevel -> if (intent.level != mutableState.value.level) {
                // The old level's cards are dropped at once, so a tap cannot start the wrong level's category.
                mutableState.update {
                    it.copy(level = intent.level, levelTotal = 0, categories = persistentListOf(), isLoaded = false)
                }
                selectedLevel.value = intent.level
            }

            is CategoriesIntent.OpenCategory -> start(intent.id)

            CategoriesIntent.PlayMixed -> start(null)

            CategoriesIntent.Back -> effectChannel.trySend(CategoriesEffect.NavigateBack)

            CategoriesIntent.Retry -> if (mutableState.value.hasError) {
                mutableState.update { it.copy(isLoaded = false, hasError = false) }
                reloads.update { it + 1 }
            }
        }
    }

    private fun start(category: CategoryId?) {
        val current = mutableState.value
        if (!current.isLoaded || current.hasError) return
        effectChannel.trySend(CategoriesEffect.StartGame(current.level, route.startMode, category))
    }
}
