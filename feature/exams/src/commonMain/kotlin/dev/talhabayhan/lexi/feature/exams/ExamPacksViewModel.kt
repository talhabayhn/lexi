package dev.talhabayhan.lexi.feature.exams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.ExamPack
import dev.talhabayhan.lexi.core.domain.repository.EntitlementRepository
import dev.talhabayhan.lexi.core.domain.repository.ExamPackRepository
import dev.talhabayhan.lexi.core.domain.repository.ProgressRepository
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
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Exam packs (`10-exam-packs`): the packs with their card counts and the player's progress, locked while a Pro pack
 * meets a player without Pro ([EntitlementRepository]). A tap on an unlocked pack starts its wheel; on a locked one it
 * shows a polite "coming soon" toast (no paywall until RevenueCat).
 */
internal class ExamPacksViewModel(
    private val examPacks: ExamPackRepository,
    private val entitlements: EntitlementRepository,
    private val progress: ProgressRepository,
    private val crashReporter: CrashReporter,
) : ViewModel() {
    private val mutableState = MutableStateFlow(ExamPacksUiState())
    val state: StateFlow<ExamPacksUiState> = mutableState.asStateFlow()

    private val effectChannel = Channel<ExamPacksEffect>(Channel.BUFFERED)
    val effects: Flow<ExamPacksEffect> = effectChannel.receiveAsFlow()

    /** Bumped by "Tekrar dene": reads the packs again. */
    private val reloads = MutableStateFlow(0)

    init {
        viewModelScope.launch {
            reloads.collectLatest {
                val content = loadContent()
                if (content == null) {
                    mutableState.update { it.copy(packs = persistentListOf(), isLoaded = true, hasError = true) }
                    return@collectLatest
                }
                // A broken progress read shows 0 % (the packs themselves still work) instead of never loading.
                val answered = progress.observeAll().catch { e ->
                    crashReporter.recordException(e, "exam pack progress not read")
                    emit(emptyList())
                }
                combine(entitlements.isPro, answered) { hasPro, cards -> rows(content, hasPro, cards) }
                    .collect { packs ->
                        mutableState.update {
                            it.copy(packs = packs.toImmutableList(), isLoaded = true, hasError = false)
                        }
                    }
            }
        }
    }

    private class PackContent(val pack: ExamPack, val cardIds: Set<CardId>)

    /** Content never changes while the app runs; null when it cannot be read. */
    private suspend fun loadContent(): List<PackContent>? = try {
        examPacks.packs().map { PackContent(it, examPacks.cardIds(it.id).toSet()) }
    } catch (e: CancellationException) {
        throw e
    } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
        crashReporter.recordException(e, "exam packs not read")
        null
    }

    private fun rows(content: List<PackContent>, hasPro: Boolean, answered: List<CardProgress>): List<ExamPackUi> {
        val learned = answered.filter { it.correct > 0 }.map { it.cardId }.toSet()
        return content.mapIndexed { index, entry ->
            val pack = entry.pack
            ExamPackUi(
                id = pack.id,
                badge = badgeOf(pack),
                badgeIndex = badgeIndexOf(pack, index),
                titleTr = pack.titleTr,
                titleEn = pack.titleEn,
                descriptionTr = pack.descriptionTr,
                cardCount = entry.cardIds.size,
                learnedPercent = percent(entry.cardIds.count { it in learned }, entry.cardIds.size),
                isPro = pack.isPro,
                locked = pack.isLocked(hasPro),
            )
        }
    }

    fun onIntent(intent: ExamPacksIntent) {
        when (intent) {
            is ExamPacksIntent.OpenPack -> open(intent)

            ExamPacksIntent.DismissNotice -> mutableState.update { it.copy(lockedNotice = false) }

            ExamPacksIntent.Back -> effectChannel.trySend(ExamPacksEffect.NavigateBack)

            ExamPacksIntent.Retry -> if (mutableState.value.hasError) {
                mutableState.update { it.copy(isLoaded = false, hasError = false) }
                reloads.update { it + 1 }
            }
        }
    }

    private fun open(intent: ExamPacksIntent.OpenPack) {
        val pack = mutableState.value.packs.firstOrNull { it.id == intent.id } ?: return
        if (pack.locked) {
            mutableState.update { it.copy(lockedNotice = true) }
        } else if (pack.cardCount > 0) {
            // A pack without cards (never shipped: the compiler requires cards for every letter) is not started.
            effectChannel.trySend(ExamPacksEffect.StartPack(pack.id))
        }
    }

    internal companion object {
        /** Pack id prefixes in the design's row order; their badge keeps its design color and shape. */
        private val DESIGN_ORDER = listOf("lgs", "meb", "yks", "yds", "ielts", "toefl")

        private const val BADGE_LETTERS = 3

        /** "LGS" for `lgs`, "YDS" for `yds-yokdil`: the first three letters of the id's first part. */
        fun badgeOf(pack: ExamPack): String = pack.id.value.substringBefore('-').take(BADGE_LETTERS).uppercase()

        /** The design's position for a known pack, else the pack's own position in the list. */
        fun badgeIndexOf(pack: ExamPack, position: Int): Int =
            DESIGN_ORDER.indexOf(pack.id.value.substringBefore('-')).takeIf { it >= 0 } ?: position

        /** Rounded half up, like `Stats`; 0 for an empty pack. */
        fun percent(part: Int, whole: Int): Int = if (whole == 0) 0 else (part * 200 + whole) / (2 * whole)
    }
}
