package dev.talhabayhan.lexi.feature.levels

import androidx.compose.runtime.Immutable
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.settings.Settings
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/** Words learned of a level's [total] cards (`412 / 850 kelime`). */
@Immutable
internal data class LevelProgressUi(val level: Level, val learned: Int, val total: Int) {
    val fraction: Float get() = if (total <= 0) 0f else (learned.toFloat() / total).coerceIn(0f, 1f)
}

/** What `03-level-select` shows: the six levels with their progress, the selected level and mode. */
@Immutable
internal data class LevelSelectUiState(
    val levels: ImmutableList<LevelProgressUi> = Level.entries.map { LevelProgressUi(it, 0, 0) }.toImmutableList(),
    val selectedLevel: Level = Level.A1,
    val selectedMode: GameMode = GameMode.CLASSIC,
    /** Classic total time, the mode picker's "3:00" (the player's setting). */
    val classicTimeLimitSeconds: Int = Settings.DEFAULT_CLASSIC_TIME_LIMIT_SECONDS,
    /** Slots of a regular wheel, for the start button's caption "26 HARF". */
    val letterCount: Int = ('A'..'Z').count(),
)

/** The modes the picker offers, in display order. */
internal val SelectableModes: List<GameMode> = listOf(GameMode.CLASSIC, GameMode.PRACTICE, GameMode.HARDCORE)

internal sealed interface LevelSelectIntent {
    data class SelectLevel(val level: Level) : LevelSelectIntent

    data class SelectMode(val mode: GameMode) : LevelSelectIntent

    /** BAŞLA. */
    data object Start : LevelSelectIntent

    data object Back : LevelSelectIntent
}

internal sealed interface LevelSelectEffect {
    data class StartGame(val level: Level, val mode: GameMode) : LevelSelectEffect

    data object NavigateBack : LevelSelectEffect
}
