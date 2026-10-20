package dev.talhabayhan.lexi.feature.game

import dev.talhabayhan.lexi.core.designsystem.component.WheelLetterUi
import dev.talhabayhan.lexi.core.domain.game.GameEngine
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.game.SlotState
import dev.talhabayhan.lexi.core.domain.leveltest.LevelTestRun
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/**
 * Pure mapping of a level test run (`GAME_RULES.md` §9) to the game screen's resting look: the current card's
 * one-slot game through [GameUiMapper], with the wheel of the asked letters in asked order plus the current one (it
 * grows with each card), the correct answers so far in the score badge (unscored), no jokers, no restart and the
 * "TEST" badge ([GameUiState.isLevelTest]). DESIGN-DEBT: there is no design for the test HUD (`screen-map.md`).
 */
internal object LevelTestUiMapper {
    /** [previous] keeps the last card visible once the run is over (no current game) until the screen leaves. */
    fun map(run: LevelTestRun, engine: GameEngine, previous: GameUiState? = null): GameUiState {
        val asked = run.asked.map { WheelLetterUi(it.letter, it.state.toWheelState()) }
        val correct = run.asked.count { it.state == SlotState.CORRECT }
        val game = run.game
        val base = if (game == null) {
            (previous ?: GameUiState()).copy(isPaused = false, inputEnabled = false)
        } else {
            GameUiMapper.map(game, engine, previous)
        }
        val current = game?.wheel?.slots?.single()?.let { WheelLetterUi(it.letter, it.state.toWheelState()) }
        return base.copy(
            isLoading = false,
            wheel = (asked + listOfNotNull(current)).toImmutableList(),
            score = 0,
            correct = correct,
            isScored = false,
            jokers = persistentListOf(),
            showJokerAd = false,
            isJokerSheetOpen = false,
            isRestartConfirmation = false,
            canRestart = false,
            isLevelTest = true,
        )
    }

    /** The shared clock (⚙ `levelTestTimeLimitSeconds`) for the HUD timer. */
    fun clock(run: LevelTestRun, engine: GameEngine, warningSeconds: Int): GameClockUi = GameUiMapper.clock(
        remainingMs = run.remainingMs,
        timeLimitMs = engine.config.timeLimitMs(GameMode.LEVEL_TEST),
        warningSeconds = warningSeconds,
    )
}
