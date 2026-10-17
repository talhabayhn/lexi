package dev.talhabayhan.lexi.core.domain.session

import dev.talhabayhan.lexi.core.domain.game.GameConfig
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.game.GameState
import dev.talhabayhan.lexi.core.domain.game.GameStatus
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.repository.DailyRepository
import dev.talhabayhan.lexi.core.domain.repository.ProgressRepository
import dev.talhabayhan.lexi.core.domain.repository.SessionRepository
import dev.talhabayhan.lexi.core.domain.srs.Srs
import dev.talhabayhan.lexi.core.domain.time.AppClock
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Stores a finished game (ADR 0019): builds the [SessionResult] ([AppClock] gives `endedAtMs` and the end day in the
 * player's time zone), records it ([SessionRepository.record]) and applies it to the notebook ([Srs.update] over
 * [ProgressRepository]). Modes in `GameConfig.srsExcludedModes` change neither (the repository skips the session,
 * `Srs.update` returns no changes).
 *
 * One instance per game (Koin `factory`): the first call does the work, later calls for the same game return the
 * same result without writing again (safe to call from every `GameOver` path). A step that threw is retried by the
 * next call; steps that succeeded are not repeated. Callers run it under `NonCancellable`.
 */
public class FinishGame(
    private val sessions: SessionRepository,
    private val progress: ProgressRepository,
    private val clock: AppClock,
    private val config: GameConfig,
    private val daily: DailyRepository? = null,
) {
    private val mutex = Mutex()
    private var game: GameState? = null
    private var session: SessionResult? = null
    private var recorded = false
    private var srsApplied = false
    private var dailyRecorded = false

    /**
     * Records [finished] (status FINISHED) once. [category] is the wheel's category (null for review, daily and
     * level-test wheels); the engine state doesn't carry it.
     */
    public suspend operator fun invoke(finished: GameState, category: CategoryId?): SessionResult = mutex.withLock {
        require(finished.status == GameStatus.FINISHED) { "only finished games are recorded (${finished.status})" }
        val first = game
        check(first == null || first == finished) { "FinishGame records one game; use a new instance per game" }
        game = finished

        val result = session ?: sessionOf(finished, category).also { session = it }
        if (!recorded) {
            sessions.record(result)
            recorded = true
        }
        if (!srsApplied) {
            applySrs(finished, result)
            srsApplied = true
        }
        if (!dailyRecorded && result.mode == GameMode.DAILY && daily != null) {
            val dailyResult = DailyResult(
                epochDay = result.epochDay,
                level = null,
                score = result.score,
                timeMs = result.endedAtMs - result.startedAtMs,
                shareCode = null,
            )
            daily.record(dailyResult)
            dailyRecorded = true
        }
        result
    }

    private fun sessionOf(game: GameState, category: CategoryId?): SessionResult {
        val summary = checkNotNull(game.summary) { "finished game without summary" }
        val endedAtMs = clock.nowMs()
        return SessionResult(
            mode = game.mode,
            level = game.level,
            category = category,
            seed = game.seed,
            // A forfeit before Start (duel handover) has no start time.
            startedAtMs = game.startedAtMs ?: endedAtMs,
            endedAtMs = endedAtMs,
            epochDay = clock.epochDay(endedAtMs),
            score = summary.score,
            correct = summary.correct,
            wrong = summary.wrong,
            passed = summary.passed,
            completed = summary.completed,
        )
    }

    private suspend fun applySrs(game: GameState, session: SessionResult) {
        if (game.mode in config.srsExcludedModes) return
        val ids = Srs.results(game).map { it.cardId }
        if (ids.isEmpty()) return
        val changes = Srs.update(progress.get(ids), game, session.epochDay, session.endedAtMs, config)
        if (changes.isNotEmpty()) progress.save(changes.values)
    }
}
