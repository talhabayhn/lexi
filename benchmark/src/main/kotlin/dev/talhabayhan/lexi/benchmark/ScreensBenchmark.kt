package dev.talhabayhan.lexi.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Frame times over one perf tour (`docs/PERF.md`): Home, Settings, LevelSelect, Game, Stats, Notebook and back, with
 * the baseline profile (what Play installs). Includes the launch's frames before the tour starts. M8 target: 60 fps,
 * i.e. `frameOverrunMs` P90 ≤ 0 and few frames over 16.7 ms; on the CI emulator only the trend counts.
 */
@RunWith(AndroidJUnit4::class)
class ScreensBenchmark {
    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun perfTour() = rule.measureRepeated(
        packageName = PACKAGE_NAME,
        metrics = listOf(FrameTimingMetric()),
        compilationMode = CompilationMode.Partial(),
        startupMode = null,
        iterations = ITERATIONS,
        setupBlock = {
            pressHome()
            startActivityAndWait()
            completeOnboardingIfShown()
        },
    ) {
        runPerfTour(loops = 1)
    }

    private companion object {
        const val ITERATIONS = 3
    }
}
