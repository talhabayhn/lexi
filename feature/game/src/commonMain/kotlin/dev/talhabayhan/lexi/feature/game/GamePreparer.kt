package dev.talhabayhan.lexi.feature.game

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Prepares a new game before the navigation to its [GameRoute], so the game screen's first frame already shows the
 * board instead of a loading screen that the board replaces mid-transition. Holds at most one preparation, keyed by
 * the route; a new [prepare] cancels and replaces it, and the game screen of that route takes it once. Restored,
 * restarted and retried games load as before. A preparation that is never taken has done only what the screen's own
 * load does (reads and the once-per-install joker start pack). Main thread only; the work itself runs in [scope].
 */
class GamePreparer internal constructor(private val loader: GameLoader, private val scope: CoroutineScope) {
    private class Preparation(val route: GameRoute, val result: Deferred<Opened>)

    private var pending: Preparation? = null

    /**
     * Starts preparing a new game for [route] and suspends until it is ready or [cap] has passed, whichever comes
     * first; after the cap the game screen waits for the same preparation and shows loading until then.
     */
    suspend fun prepare(route: GameRoute, cap: Duration = NAVIGATION_CAP) {
        pending?.result?.cancel()
        val result = scope.async { loader.openNew(route) }
        pending = Preparation(route, result)
        withTimeoutOrNull(cap) { result.join() }
    }

    /** Drops the preparation for [route] when its navigation did not happen. */
    fun discard(route: GameRoute) {
        val current = pending ?: return
        if (current.route != route) return
        current.result.cancel()
        pending = null
    }

    /** The preparation for [route], once: later calls (and other routes) get null. */
    internal fun take(route: GameRoute): Deferred<Opened>? {
        val current = pending?.takeIf { it.route == route } ?: return null
        pending = null
        return current.result
    }

    companion object {
        /** The longest a tap waits for the preparation before navigating anyway. */
        val NAVIGATION_CAP: Duration = 250.milliseconds
    }
}
