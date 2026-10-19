package dev.talhabayhan.lexi.startup

import co.touchlab.kermit.Logger
import kotlin.time.TimeSource

/**
 * Debug-only startup timing (Kermit tag `Startup`): each [mark] logs the time since the process started, so app
 * start, the first frame, the content warm-up and the end of the splash can be compared between builds. The platform
 * entry point calls [start] once; release builds pass `enabled = false` and nothing is logged.
 */
internal object StartupTrace {
    private val log = Logger.withTag("Startup")
    private var enabled = false
    private var origin = TimeSource.Monotonic.markNow()
    private var processStartOffsetMs = 0L
    private val marked = mutableSetOf<String>()

    /** [sinceProcessStartMs]: how long the process had been running when the entry point ran (0 if unknown). */
    fun start(enabled: Boolean, sinceProcessStartMs: Long = 0L) {
        this.enabled = enabled
        origin = TimeSource.Monotonic.markNow()
        processStartOffsetMs = sinceProcessStartMs
        marked.clear()
        mark("entry point")
    }

    /** Logs [event] once per start with its time since process start. */
    fun mark(event: String) {
        if (!enabled || !marked.add(event)) return
        log.d { "$event at ${processStartOffsetMs + origin.elapsedNow().inWholeMilliseconds} ms" }
    }

    /** Logs how long [block] took, as "[event] took N ms". */
    suspend fun <T> measureSuspend(event: String, block: suspend () -> T): T {
        val began = TimeSource.Monotonic.markNow()
        return block().also { duration(event, began.elapsedNow().inWholeMilliseconds) }
    }

    fun duration(event: String, ms: Long) {
        if (enabled) log.d { "$event took $ms ms" }
    }
}
