package dev.talhabayhan.lexi.core.domain.daily

import dev.talhabayhan.lexi.core.domain.game.Wheel
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.session.DailyResult

public sealed interface DailyPlayState {
    /** The daily wheel is ready to be played. */
    public data class Ready(val epochDay: Long, val level: Level, val seed: Long, val wheel: Wheel) : DailyPlayState

    /** The daily wheel has already been played today for this level. */
    public data class AlreadyPlayed(val epochDay: Long, val level: Level, val result: DailyResult) : DailyPlayState

    /** No playable cards exist for this level in the pool. */
    public data object NoCards : DailyPlayState
}
