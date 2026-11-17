package dev.talhabayhan.lexi.benchmark

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.Until

/**
 * The video flow's UI steps (English, the emulator's language), shared by [VideoFlowBenchmark] (measures them) and
 * [BaselineProfileGenerator.videoFlow] (records the classes and methods they touch into the baseline profile, so the
 * first card flip, the dialog and the letter morph run precompiled on a fresh install).
 */
private const val UI_TIMEOUT_MS = 10_000L
private const val SETTLE_MS = 1_000L
private const val PASS_SETTLE_MS = 800L
private const val LETTERS = 3
private const val PASSES = 2
internal val MIXED_CARD: BySelector = By.descStartsWith("Mixed")
internal val PLAY_BUTTON: BySelector = By.text("PLAY")

/** Home → play card → "Pick your wheel"; waits for the grid entrance (#18) to land. */
internal fun MacrobenchmarkScope.openCategories() {
    click(By.descStartsWith("Play."))
    waitFor(MIXED_CARD)
    Thread.sleep(SETTLE_MS)
}

/** The Mixed card → lift + Y flip into the mode / time dialog (takes input once the flip has landed, ≈ 0.81 s). */
internal fun MacrobenchmarkScope.openDialog() {
    click(MIXED_CARD)
    waitFor(PLAY_BUTTON)
    Thread.sleep(SETTLE_MS)
}

/** Waits for the board (pause button), lets the entrance land, then types three letters and passes twice. */
internal fun MacrobenchmarkScope.playAFewMoves() {
    waitFor(By.desc("Pause"))
    Thread.sleep(SETTLE_MS)
    repeat(LETTERS) {
        device.findObjects(By.descStartsWith("Letter ")).firstOrNull {
            it.contentDescription?.endsWith("used") == false
        }
            ?.click()
        device.waitForIdle()
    }
    repeat(PASSES) {
        click(By.text("PASS"))
        Thread.sleep(PASS_SETTLE_MS)
    }
}

/**
 * Home → categories → dialog (switch the mode, close by the arrow and by back, reopen) → OYNA letter morph → board
 * entrance and a few moves: every transition of the flow once.
 */
internal fun MacrobenchmarkScope.runVideoFlowTour() {
    openCategories()
    openDialog()
    click(By.text("Practice"))
    click(By.text("Hardcore"))
    click(By.text("Classic"))
    click(By.desc("Close"))
    waitGone(PLAY_BUTTON)
    openDialog()
    device.pressBack()
    waitGone(PLAY_BUTTON)
    openDialog()
    click(PLAY_BUTTON)
    playAFewMoves()
}

internal fun MacrobenchmarkScope.click(selector: BySelector) {
    checkNotNull(device.wait(Until.findObject(selector), UI_TIMEOUT_MS)) { "not found: $selector" }.click()
}

internal fun MacrobenchmarkScope.waitFor(selector: BySelector) {
    checkNotNull(device.wait(Until.findObject(selector), UI_TIMEOUT_MS)) { "did not appear: $selector" }
}

internal fun MacrobenchmarkScope.waitGone(selector: BySelector) {
    device.wait(Until.gone(selector), UI_TIMEOUT_MS)
}
