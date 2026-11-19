package dev.talhabayhan.lexi.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
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

    /**
     * Only the first card flip of a fresh process: the categories are opened and settled in setup, so the measured
     * frames are the tap, the lift, the Y flip and the dialog's first composition: the stutter the splash's first-launch
     * warm-up (`SplashRoute`, once per app version) and the baseline profile (`videoFlow`) are meant to remove. Compare
     * `CompilationMode.None()` (no profile) with the default [CompilationMode.Partial] on the device matrix.
     */
    @Test
    fun firstDialogFlip() = journey(
        setup = {
            openCategories()
            // Let the grid entrance land and the picker settle before the tap.
            Thread.sleep(WARM_UP_MS)
        },
    ) {
        click(MIXED_CARD)
        waitFor(PLAY_BUTTON)
        Thread.sleep(OPEN_SETTLE_MS)
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

    private fun journey(setup: (MacrobenchmarkScope.() -> Unit)? = null, steps: MacrobenchmarkScope.() -> Unit) =
        rule.measureRepeated(
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
                // Journeys that measure one step start from where the previous steps end (outside the measurement).
                waitForHome()
                setup?.invoke(this)
            },
        ) {
            // Without a setup the journey starts on Home; with one it continues from where the setup ended.
            if (setup == null) waitForHome()
            steps()
        }

    private companion object {
        const val ITERATIONS = 5
        const val WARM_UP_MS = 2_500L
        const val OPEN_SETTLE_MS = 1_000L
    }
}
