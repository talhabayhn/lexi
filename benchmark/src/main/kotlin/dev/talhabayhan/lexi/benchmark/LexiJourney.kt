package dev.talhabayhan.lexi.benchmark

import android.content.Intent
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until

/** App under test (`:androidApp`, `benchmarkRelease` / `nonMinifiedRelease` variants share the release id). */
internal const val PACKAGE_NAME = "dev.talhabayhan.lexi"

private const val UI_TIMEOUT_MS = 10_000L
private const val UI_POLL_MS = 100L
private const val TOUR_TIMEOUT_MS = 180_000L
private const val TOUR_POLL_MS = 2_000L

/** Onboarding's Skip (`onboarding_skip`) and Home's settings button (`home_settings`); the splash shows neither. */
private val ONBOARDING_SKIP = By.text("Skip")
private val HOME_SETTINGS = By.desc("Settings")

/**
 * A fresh install opens the onboarding: Skip finishes it at A1 and opens Home.
 * The emulator runs in English; on later launches Home is shown and this returns as soon as it appears.
 */
internal fun MacrobenchmarkScope.completeOnboardingIfShown() {
    val deadline = System.currentTimeMillis() + UI_TIMEOUT_MS
    while (true) {
        if (device.hasObject(HOME_SETTINGS)) return
        val skip = device.findObject(ONBOARDING_SKIP)
        if (skip != null) {
            skip.click()
            break
        }
        check(System.currentTimeMillis() < deadline) { "Neither the onboarding nor Home appeared" }
        Thread.sleep(UI_POLL_MS)
    }
    waitForHome()
}

/** Waits until Home is on screen (after the splash), so the app's fully-drawn report is inside the measurement. */
internal fun MacrobenchmarkScope.waitForHome() {
    checkNotNull(device.wait(Until.findObject(HOME_SETTINGS), UI_TIMEOUT_MS)) { "Home did not appear" }
}

/**
 * Runs the perf tour (`docs/PERF.md`) in the running app: Home, Settings, LevelSelect, Game, Stats and Notebook through
 * the real navigation. Started from the shell, so a release build honours the extras. Waits for `LexiPerf DONE`.
 */
internal fun MacrobenchmarkScope.runPerfTour(loops: Int) {
    device.executeShellCommand("logcat -c")
    val intent = Intent(Intent.ACTION_MAIN).apply {
        setClassName(PACKAGE_NAME, "$PACKAGE_NAME.MainActivity")
        putExtra("lexi.perfTour", true)
        putExtra("lexi.perfTourLoops", loops)
    }
    killProcess()
    startActivityAndWait(intent)
    val deadline = System.currentTimeMillis() + TOUR_TIMEOUT_MS
    while (System.currentTimeMillis() < deadline) {
        val log = device.executeShellCommand("logcat -d -s LexiPerf:I System.out:I")
        if ("LexiPerf DONE" in log) return
        check("LexiPerf ABORT" !in log) { "Perf tour aborted:\n${log.lines().takeLast(5).joinToString("\n")}" }
        Thread.sleep(TOUR_POLL_MS)
    }
    error("Perf tour did not finish in ${TOUR_TIMEOUT_MS / 1000} s")
}
