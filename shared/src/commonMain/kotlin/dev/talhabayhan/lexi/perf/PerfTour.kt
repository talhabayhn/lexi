package dev.talhabayhan.lexi.perf

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import co.touchlab.kermit.Logger
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.feature.game.GameRoute
import dev.talhabayhan.lexi.feature.home.HomeRoute
import dev.talhabayhan.lexi.feature.levels.LevelSelectRoute
import dev.talhabayhan.lexi.feature.notebook.NotebookRoute
import dev.talhabayhan.lexi.feature.settings.SettingsRoute
import dev.talhabayhan.lexi.feature.stats.StatsRoute
import dev.talhabayhan.lexi.navigation.LexiNavigator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.reflect.KClass

/** Launch-flag perf tour (`docs/PERF.md`). [loops] is how many times the script repeats. */
public data class PerfTourConfig(val loops: Int = DEFAULT_LOOPS) {
    public companion object {
        public const val DEFAULT_LOOPS: Int = 5
    }
}

private const val IDLE_MS = 1_000L
private const val SETTLE_AFTER_RESUMED_MS = 400L
private const val STAY_IN_GAME_MS = 1_500L
private const val SEGMENT_TIMEOUT_MS = 10_000L
private const val HOME_TIMEOUT_MS = 60_000L

private val log = Logger.withTag("LexiPerf")

private fun report(line: String) {
    println(line)
    log.i { line }
}

/**
 * Runs the scripted navigation tour and prints per-segment frame statistics. Emits nothing and costs nothing until
 * composed, and the caller composes it only when the app was launched with the perf flag. Triggers call the same
 * [LexiNavigator] functions as the real buttons.
 */
@Composable
internal fun PerfTour(navigator: LexiNavigator, config: PerfTourConfig) {
    val reducedMotion = LexiTheme.motion.reducedMotion
    LaunchedEffect(navigator, config) {
        val recorder = FrameRecorder()
        val recording = launch { recorder.run() }
        try {
            PerfTourRunner(navigator, recorder, config, reducedMotion).run()
        } finally {
            recording.cancel()
        }
    }
}

/** Keeps every frame timestamp (nanoseconds) while the tour runs; one `withFrameNanos` per frame. */
private class FrameRecorder {
    private var times = LongArray(INITIAL_CAPACITY)
    var size = 0
        private set

    suspend fun run() {
        while (true) {
            val time = withFrameNanos { it }
            if (size == times.size) times = times.copyOf(size * 2)
            times[size++] = time
        }
    }

    /** Frames [from] until [to] (exclusive). */
    fun slice(from: Int, to: Int): List<Long> = (from.coerceAtLeast(0) until to.coerceAtMost(size)).map { times[it] }

    private companion object {
        const val INITIAL_CAPACITY = 16_384
    }
}

private class PerfTourRunner(
    private val navigator: LexiNavigator,
    private val recorder: FrameRecorder,
    private val config: PerfTourConfig,
    private val reducedMotion: Boolean,
) {
    private var intervalNanos = FALLBACK_INTERVAL_NANOS
    private val results = mutableListOf<Triple<String, Int, SegmentStats>>()

    suspend fun run() {
        if (!awaitResumed(HomeRoute::class, HOME_TIMEOUT_MS)) {
            return report("LexiPerf ABORT home not reached (finish onboarding first)")
        }
        val idleStart = recorder.size
        delay(IDLE_MS)
        intervalNanos = refreshInterval(recorder.slice(idleStart, recorder.size))

        for (loop in 0 until config.loops.coerceAtLeast(1)) {
            val completed = script(loop)
            if (!completed) return report("LexiPerf ABORT segment failed in loop $loop")
        }
        finish()
    }

    private suspend fun script(loop: Int): Boolean =
        segment(loop, "Home→Settings", SettingsRoute::class) { navigator.openSettings() } &&
            segment(loop, "Settings→Home", HomeRoute::class) { navigator.back() } &&
            segment(loop, "Home→LevelSelect", LevelSelectRoute::class) { navigator.openLevelSelect() } &&
            // Practice has no clock; the game is left unfinished, so nothing is recorded. Daily is never used.
            segment(loop, "LevelSelect→Game", GameRoute::class) { navigator.startGame(Level.A1, GameMode.PRACTICE) } &&
            stay(STAY_IN_GAME_MS) &&
            segment(loop, "Game→Settings", SettingsRoute::class) { navigator.openSettings() } &&
            segment(loop, "Settings→Game", GameRoute::class) { navigator.back() } &&
            segment(loop, "Game→Home", HomeRoute::class) { navigator.home() } &&
            segment(loop, "Home→Stats", StatsRoute::class) { navigator.openStats() } &&
            segment(loop, "Stats→Home", HomeRoute::class) { navigator.back() } &&
            segment(loop, "Home→Notebook", NotebookRoute::class) { navigator.openNotebook() } &&
            segment(loop, "Notebook→Home", HomeRoute::class) { navigator.back() }

    private suspend fun stay(millis: Long): Boolean {
        delay(millis)
        return true
    }

    /**
     * Frames from the trigger until [destination] is RESUMED, plus [SETTLE_AFTER_RESUMED_MS]. False when the trigger
     * was dropped or the destination never resumed.
     */
    private suspend fun segment(loop: Int, label: String, destination: KClass<*>, trigger: () -> Boolean): Boolean {
        // The frame before the trigger is the baseline of the first gap.
        val start = recorder.size - 1
        if (!trigger()) return false.also { report("LexiPerf ABORT $label: trigger dropped") }
        if (!awaitResumed(destination, SEGMENT_TIMEOUT_MS)) {
            return false.also {
                report("LexiPerf ABORT $label: timeout")
            }
        }
        delay(SETTLE_AFTER_RESUMED_MS)
        val stats = segmentStats(recorder.slice(start, recorder.size), intervalNanos)
        results += Triple(label, loop, stats)
        report("LexiPerf ${segmentJson(label, loop, stats)}")
        return true
    }

    private suspend fun awaitResumed(destination: KClass<*>, timeoutMs: Long): Boolean = withTimeoutOrNull(timeoutMs) {
        while (!navigator.isResumedOn(destination)) withFrameNanos { }
        true
    } ?: false

    private fun finish() {
        val json =
            summaryJson(summarize(results.map { it.first to it.third }), intervalNanos, perfPlatformName, reducedMotion)
        report("LexiPerf SUMMARY $json")
        writePerfReport(
            "{\"summary\":$json,\"segments\":[" +
                results.joinToString(",") { (label, loop, stats) -> segmentJson(label, loop, stats) } + "]}",
        )
        report("LexiPerf DONE")
    }
}
