package dev.talhabayhan.lexi.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Video-flow journeys (ADR 0027, `docs/design/video-flow-notes.md` § Performance) through the real UI, in English (the
 * emulator's language): Home → "Pick your wheel" (#12, #18) → card lift + Y flip into the mode / time dialog (#13–#15)
 * → OYNA letter morph (#16) → game board entrance and a few moves (#17).
 *
 * Acceptance per journey (`FrameTimingMetric`): `frameDurationCpuMs` P95 < 16.6 ms and no frame > 100 ms, on the
 * device matrix of the notes (Android 8 low-RAM, a Samsung A-series, a Pixel). CI runs them on an emulator with a
 * software GPU (`perf` workflow): those numbers show trends only.
 */
@RunWith(AndroidJUnit4::class)
class VideoFlowBenchmark {
    @get:Rule
    val rule = MacrobenchmarkRule()

    /** Home → play card → "Pick your wheel", fling the category grid to the exam packs and back up, back to Home. */
    @Test
    fun homeToCategories() = journey {
        openCategories()
        val grid = checkNotNull(device.findObject(By.scrollable(true))) { "the category grid is not scrollable" }
        grid.fling(Direction.DOWN)
        device.waitForIdle()
        grid.fling(Direction.UP)
        device.waitForIdle()
        device.pressBack()
        waitForHome()
    }

    /** Category card lift + Y flip into the dialog (#13–#14), change the mode, close by the arrow and by back (#15). */
    @Test
    fun categoryDialogFlip() = journey {
        openCategories()
        openDialog()
        click(By.text("Practice"))
        click(By.text("Classic"))
        click(By.desc("Close"))
        waitGone(PLAY_BUTTON)
        openDialog()
        device.pressBack()
        waitGone(PLAY_BUTTON)
        device.pressBack()
        waitForHome()
    }

    /** Dialog OYNA → letter morph (#16) → board entrance, then a few letters and passes (#17). */
    @Test
    fun playLetterMorph() = journey {
        openCategories()
        openDialog()
        click(PLAY_BUTTON)
        playAFewMoves()
    }

    /** The whole flow in one run: Home → Categories → Dialog (open, close, reopen) → Game → a few moves. */
    @Test
    fun fullVideoFlow() = journey {
        openCategories()
        openDialog()
        click(By.desc("Close"))
        waitGone(PLAY_BUTTON)
        openDialog()
        click(PLAY_BUTTON)
        playAFewMoves()
    }

    private fun journey(steps: MacrobenchmarkScope.() -> Unit) = rule.measureRepeated(
        packageName = PACKAGE_NAME,
        metrics = listOf(FrameTimingMetric()),
        compilationMode = CompilationMode.Partial(),
        startupMode = null,
        iterations = ITERATIONS,
        setupBlock = {
            // A fresh process each time: no game left from the previous iteration.
            killProcess()
            pressHome()
            startActivityAndWait()
            completeOnboardingIfShown()
        },
    ) {
        waitForHome()
        steps()
    }

    private fun MacrobenchmarkScope.openCategories() {
        click(By.descStartsWith("Play."))
        waitFor(MIXED_CARD)
        // The grid entrance (#18) plays on the first open only; let it land before the next step.
        Thread.sleep(SETTLE_MS)
    }

    private fun MacrobenchmarkScope.openDialog() {
        click(MIXED_CARD)
        waitFor(PLAY_BUTTON)
        // The dialog takes input once the flip has landed (#14, ≈ 0.81 s).
        Thread.sleep(SETTLE_MS)
    }

    /** Waits for the board (pause button), lets the entrance land, then types three letters and passes twice. */
    private fun MacrobenchmarkScope.playAFewMoves() {
        waitFor(By.desc("Pause"))
        Thread.sleep(SETTLE_MS)
        repeat(LETTERS) {
            device.findObjects(By.descStartsWith("Letter ")).firstOrNull {
                it.contentDescription?.endsWith("used") ==
                    false
            }
                ?.click()
            device.waitForIdle()
        }
        repeat(PASSES) {
            click(By.text("PASS"))
            Thread.sleep(PASS_SETTLE_MS)
        }
    }

    private fun MacrobenchmarkScope.click(selector: BySelector) {
        checkNotNull(device.wait(Until.findObject(selector), UI_TIMEOUT_MS)) { "not found: $selector" }.click()
    }

    private fun MacrobenchmarkScope.waitFor(selector: BySelector) {
        checkNotNull(device.wait(Until.findObject(selector), UI_TIMEOUT_MS)) { "did not appear: $selector" }
    }

    private fun MacrobenchmarkScope.waitGone(selector: BySelector) {
        device.wait(Until.gone(selector), UI_TIMEOUT_MS)
    }

    private companion object {
        const val ITERATIONS = 5
        const val UI_TIMEOUT_MS = 10_000L
        const val SETTLE_MS = 1_000L
        const val PASS_SETTLE_MS = 800L
        const val LETTERS = 3
        const val PASSES = 2
        val MIXED_CARD: BySelector = By.descStartsWith("Mixed")
        val PLAY_BUTTON: BySelector = By.text("PLAY")
    }
}
