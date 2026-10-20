package dev.talhabayhan.lexi.feature.onboarding.result

import androidx.compose.runtime.Immutable
import dev.talhabayhan.lexi.core.domain.model.Level

/** The extra line under the result (`GAME_RULES.md` §9, owner decision 3): only for A1 and for C1/NAT. */
internal enum class LevelTestFlavour {
    /** A1: `leveltest_result_a1`. */
    Basics,

    /** C1 and NAT: `leveltest_result_high`. */
    High,
    ;

    companion object {
        fun of(level: Level): LevelTestFlavour? = when (level) {
            Level.A1 -> Basics
            Level.C1, Level.NAT -> High
            Level.A2, Level.B1, Level.B2 -> null
        }
    }
}

internal enum class LevelTestResultPage {
    /** "Seviyen: B1", "{level} ile başla" and "Seviyeyi değiştir". */
    Result,

    /** The manual level pick ([dev.talhabayhan.lexi.feature.onboarding.LevelPickerContent]). */
    Picker,
}

@Immutable
internal data class LevelTestResultUiState(
    /** The placement of the test. */
    val level: Level,
    val page: LevelTestResultPage = LevelTestResultPage.Result,
) {
    val flavour: LevelTestFlavour? get() = LevelTestFlavour.of(level)
}

internal sealed interface LevelTestResultIntent {
    /** "{level} ile başla": saves the placement and finishes the onboarding (no game starts). */
    data object Start : LevelTestResultIntent

    /** "Seviyeyi değiştir": opens the picker. */
    data object ChangeLevel : LevelTestResultIntent

    /** A level of the picker: saves it and finishes the onboarding. */
    data class PickLevel(val level: Level) : LevelTestResultIntent

    /** Back button / system back: the picker returns to the result, the result to the level choice. */
    data object Back : LevelTestResultIntent
}

internal sealed interface LevelTestResultEffect {
    data object NavigateToHome : LevelTestResultEffect

    /** Back to the onboarding's level choice; nothing is saved. */
    data object NavigateBack : LevelTestResultEffect
}
