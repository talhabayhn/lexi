package dev.talhabayhan.lexi.feature.game.duel

import dev.talhabayhan.lexi.core.domain.game.CardHistory
import dev.talhabayhan.lexi.core.domain.game.GameConfig
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.game.MatchEngine
import dev.talhabayhan.lexi.core.domain.game.MatchState
import dev.talhabayhan.lexi.core.domain.game.WheelGenerator
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.random.SeededRandom
import dev.talhabayhan.lexi.core.domain.repository.CardRepository
import dev.talhabayhan.lexi.feature.game.SeedSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Builds a new hot-seat match (`GAME_RULES.md` §8, §13): one random match seed, each player's wheel from
 * `SeededRandom(MatchEngine.playerSeed(seed, i)).derive("wheel")` over the same level's mixed pool, without card history
 * (both wheels depend on the seed only). The clock per player is the setup's choice ([clockSeconds] as the match
 * engine's `duelClockSeconds`). CPU work runs on [cpu].
 */
internal class DuelLoader(
    private val cards: CardRepository,
    private val config: GameConfig,
    private val seeds: SeedSource,
    private val cpu: CoroutineDispatcher = Dispatchers.Default,
) {
    /** A new match, or null when a wheel has no playable letter (no cards for the level). Read failures throw. */
    suspend fun open(level: Level, clockSeconds: Int): Pair<MatchEngine, MatchState>? {
        val pool = cards.wheelPool(level, CategoryId.MIXED)
        return withContext(cpu) {
            val clock = clockSeconds.takeIf { it in config.duelClockOptionsSeconds } ?: config.duelClockSeconds
            val engine = MatchEngine(config.copy(duelClockSeconds = clock))
            val seed = seeds.nextSeed()
            val wheels = List(MatchEngine.PLAYERS) { index ->
                WheelGenerator.generate(
                    pool = pool,
                    level = level,
                    category = CategoryId.MIXED,
                    mode = GameMode.DUEL,
                    random = SeededRandom(MatchEngine.playerSeed(seed, index)).derive(WheelGenerator.SEED_PURPOSE),
                    history = CardHistory.NONE,
                    config = config,
                )
            }
            if (wheels.any { it.playableSlots.isEmpty() }) null else engine to engine.newMatch(seed, level, wheels)
        }
    }
}
