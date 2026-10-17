package dev.talhabayhan.lexi.core.domain.daily

import dev.talhabayhan.lexi.core.domain.game.Wheel
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.session.DailyResult

public sealed interface DailyPlayState {
    /** The daily wheel is ready to be played. */
    public data class Ready(val epochDay: Long, val seed: Long, val wheel: Wheel, val level: Level? = null) :
        DailyPlayState {
        public constructor(epochDay: Long, level: Level, seed: Long, wheel: Wheel) :
            this(epochDay, seed, wheel, level)
    }

    /** The daily wheel has already been played today. */
    public data class AlreadyPlayed(val epochDay: Long, val result: DailyResult, val level: Level? = null) :
        DailyPlayState {
        public constructor(epochDay: Long, level: Level, result: DailyResult) :
            this(epochDay, result, level)
    }

    /** No playable cards exist in the pool. */
    public data object NoCards : DailyPlayState
}
