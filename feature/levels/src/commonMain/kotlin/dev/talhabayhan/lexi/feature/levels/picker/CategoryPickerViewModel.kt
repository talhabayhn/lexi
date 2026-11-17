package dev.talhabayhan.lexi.feature.levels.picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.talhabayhan.lexi.core.domain.game.GameConfig
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.Category
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.ExamPack
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.repository.CardRepository
import dev.talhabayhan.lexi.core.domain.repository.CategoryRepository
import dev.talhabayhan.lexi.core.domain.repository.ExamPackRepository
import dev.talhabayhan.lexi.core.domain.repository.ProgressRepository
import dev.talhabayhan.lexi.core.domain.repository.SettingsRepository
import dev.talhabayhan.lexi.core.domain.settings.Settings
import dev.talhabayhan.lexi.core.domain.srs.CardProgress
import dev.talhabayhan.lexi.core.domain.telemetry.CrashReporter
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * "Çarkını seç" (`video-flow-notes.md` §2–§3): the level's cards with the player's progress, the exam packs, and the
 * mode / time dialog. The selection (level, category, mode, time) is remembered in [settings] as it changes; a stored
 * category the level does not have, or a stored time that is not offered, reads as the default (Karışık, 3:00).
 */
internal class CategoryPickerViewModel(
    route: CategoryPickerRoute,
    private val cards: CardRepository,
    private val categories: CategoryRepository,
    private val examPacks: ExamPackRepository,
    private val progress: ProgressRepository,
    private val settings: SettingsRepository,
    private val config: GameConfig,
    private val crashReporter: CrashReporter,
) : ViewModel() {
    private val mutableState = MutableStateFlow(
        CategoryPickerUiState(
            level = route.startLevel,
            timeOptions = config.classicTimeLimitOptionsSeconds.toImmutableList(),
            timeSeconds = config.classicTimeLimitSeconds,
        ),
    )
    val state: StateFlow<CategoryPickerUiState> = mutableState.asStateFlow()

    private val effectChannel = Channel<CategoryPickerEffect>(Channel.BUFFERED)
    val effects: Flow<CategoryPickerEffect> = effectChannel.receiveAsFlow()

    private val selectedLevel = MutableStateFlow(route.startLevel)

    /** Bumped by "Tekrar dene": reads the selected level's content again. */
    private val reloads = MutableStateFlow(0)

    /** The stored category, applied once the level's cards are known (a foreign one falls back to Karışık). */
    private var storedCategory: CategoryId? = null

    init {
        viewModelScope.launch {
            val stored = readSettings()
            // A level chip tapped while the settings were read wins: its own category starts at Karışık.
            if (selectedLevel.value == route.startLevel) {
                storedCategory = stored.playCategory?.takeIf { stored.selectedLevel == route.startLevel }
            }
            mutableState.update {
                it.copy(
                    mode = stored.playMode,
                    timeSeconds = stored.classicTimeLimitSeconds.takeIf { s -> s in it.timeOptions }
                        ?: config.classicTimeLimitSeconds,
                )
            }
            combine(selectedLevel, reloads) { level, _ -> level }.collectLatest { level -> observe(level) }
        }
    }

    private suspend fun readSettings(): Settings = try {
        settings.loaded.value ?: settings.settings.first()
    } catch (e: CancellationException) {
        throw e
    } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
        crashReporter.recordException(e, "picker settings not read")
        Settings()
    }

    private suspend fun observe(level: Level) {
        val content = loadContent(level)
        if (content == null) {
            mutableState.update {
                it.copy(categories = persistentListOf(), packs = persistentListOf(), isLoaded = true, hasError = true)
            }
            return
        }
        progress.observeAll()
            // A broken read shows 0 learned (the cards still work) instead of never loading.
            .catch { e ->
                crashReporter.recordException(e, "picker progress not read")
                emit(emptyList())
            }
            .collect { answered ->
                val learned = answered.learnedIds()
                val ui = content.cards(learned)
                val stored = storedCategory?.takeIf { id -> ui.any { it.id == id } }
                storedCategory = null
                mutableState.update { current ->
                    current.copy(
                        categories = ui.toImmutableList(),
                        packs = content.packs(learned).toImmutableList(),
                        selectedCategory = stored ?: current.selectedCategory.takeIf { id -> ui.any { it.id == id } }
                            ?: CategoryId.MIXED,
                        isLoaded = true,
                        hasError = false,
                    )
                }
            }
    }

    private fun List<CardProgress>.learnedIds(): Set<CardId> = filter { it.correct > 0 }.mapTo(HashSet()) { it.cardId }

    /** A level's content; it never changes while the app runs. */
    private class LevelContent(
        val mixed: Category?,
        val categories: List<Category>,
        val categoryOf: Map<CardId, CategoryId>,
        val packs: List<Pair<ExamPack, List<CardId>>>,
    ) {
        fun cards(learned: Set<CardId>): List<PickerCategoryUi> {
            val byCategory = categoryOf.entries.groupBy({ it.value }, { it.key })
            val mixedCard = PickerCategoryUi(
                id = CategoryId.MIXED,
                nameTr = mixed?.nameTr.orEmpty(),
                nameEn = mixed?.nameEn.orEmpty(),
                learned = categoryOf.keys.count { it in learned },
                total = categoryOf.size,
            )
            // Every category of the level, never hidden: one without cards shows 0 / 0.
            return listOf(mixedCard) + categories.map { category ->
                val ids = byCategory[category.id].orEmpty()
                PickerCategoryUi(category.id, category.nameTr, category.nameEn, ids.count { it in learned }, ids.size)
            }
        }

        fun packs(learned: Set<CardId>): List<PickerPackUi> = packs.map { (pack, ids) ->
            PickerPackUi(pack.id, pack.titleTr, pack.titleEn, ids.count { it in learned }, ids.size)
        }
    }

    /** Null when the content cannot be read. */
    private suspend fun loadContent(level: Level): LevelContent? = try {
        val all = categories.categories(level).sortedBy { it.sort }
        LevelContent(
            mixed = all.firstOrNull { it.id == CategoryId.MIXED },
            // NAT has only the neutral GENERAL bucket: Karışık plays it, no card of its own (CONTENT.md §4).
            categories = all.filter { it.id != CategoryId.MIXED && it.id != CategoryId.GENERAL },
            categoryOf = cards.categoryOfCards(level),
            // Pro packs are not listed (as on the exam packs page); the upcoming exams are the static locked cards.
            packs = examPacks.packs().filter { !it.isPro }.sortedBy { it.sort }.map { it to examPacks.cardIds(it.id) },
        )
    } catch (e: CancellationException) {
        throw e
    } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
        crashReporter.recordException(e, "picker content not read")
        null
    }

    fun onIntent(intent: CategoryPickerIntent) {
        when (intent) {
            is CategoryPickerIntent.SelectLevel -> selectLevel(intent.level)

            is CategoryPickerIntent.OpenCategory -> openCategory(intent.id)

            is CategoryPickerIntent.OpenPack -> {
                val pack = mutableState.value.packs.firstOrNull { it.id == intent.id }
                // A pack without cards (never shipped: the compiler requires cards for every letter) is not started.
                if (pack != null && pack.total > 0) effectChannel.trySend(CategoryPickerEffect.StartPack(pack.id))
            }

            is CategoryPickerIntent.SelectMode -> if (intent.mode in PickerModes) {
                mutableState.update { it.copy(mode = intent.mode) }
                persist { it.copy(playMode = intent.mode) }
            }

            is CategoryPickerIntent.SelectTime -> {
                val current = mutableState.value
                if (current.timeEnabled && intent.seconds in current.timeOptions) {
                    mutableState.update { it.copy(timeSeconds = intent.seconds) }
                    persist { it.copy(classicTimeLimitSeconds = intent.seconds) }
                }
            }

            CategoryPickerIntent.Play -> play()

            CategoryPickerIntent.CloseDialog -> mutableState.update { it.copy(dialog = null) }

            CategoryPickerIntent.Back -> if (mutableState.value.dialog != null) {
                mutableState.update { it.copy(dialog = null) }
            } else {
                effectChannel.trySend(CategoryPickerEffect.NavigateBack)
            }

            CategoryPickerIntent.Retry -> if (mutableState.value.hasError) {
                mutableState.update { it.copy(isLoaded = false, hasError = false) }
                reloads.update { it + 1 }
            }
        }
    }

    private fun selectLevel(level: Level) {
        if (level == mutableState.value.level) return
        // The old level's cards are dropped at once, so a tap cannot open the wrong level's category.
        mutableState.update {
            it.copy(
                level = level,
                categories = persistentListOf(),
                selectedCategory = CategoryId.MIXED,
                dialog = null,
                isLoaded = false,
                hasError = false,
            )
        }
        storedCategory = null
        selectedLevel.value = level
        persist { it.copy(selectedLevel = level, playCategory = null) }
    }

    private fun openCategory(id: CategoryId) {
        val current = mutableState.value
        if (!current.isLoaded || current.hasError || current.dialog != null) return
        val card = current.categories.firstOrNull { it.id == id } ?: return
        mutableState.update { it.copy(selectedCategory = id, dialog = card) }
        persist { it.copy(playCategory = id.takeUnless { it == CategoryId.MIXED }) }
    }

    private fun play() {
        val current = mutableState.value
        val card = current.dialog ?: return
        mutableState.update { it.copy(dialog = null) }
        effectChannel.trySend(
            CategoryPickerEffect.StartGame(
                level = current.level,
                mode = current.mode,
                category = card.id.takeUnless { card.isMixed },
                timeSeconds = current.timeSeconds.takeIf { current.mode != GameMode.PRACTICE },
            ),
        )
    }

    /** Remembers a choice; a failed write keeps the screen working (the next start uses the on-screen choice). */
    private fun persist(transform: (Settings) -> Settings) {
        viewModelScope.launch {
            try {
                settings.update(transform)
            } catch (e: CancellationException) {
                throw e
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                crashReporter.recordException(e, "picker selection not stored")
            }
        }
    }
}
