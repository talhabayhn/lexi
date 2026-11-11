package dev.talhabayhan.lexi.benchmark

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Cold start (ADR 0025, M8 target < 1.5 s on a mid-range phone; emulator numbers show the trend only):
 * `timeToInitialDisplay` = the splash's first frame, `timeToFullDisplay` = Home drawn after the splash (≈ 1.2 s of
 * designed animation, motion.md #9). `None` = no ahead-of-time code, like a first launch without the profile;
 * `BaselineProfile` = what Play installs. Run with `./gradlew :benchmark:pixel6Api34BenchmarkReleaseAndroidTest`.
 */
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {
    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun coldStartNoCompilation() = coldStart(CompilationMode.None())

    @Test
    fun coldStartBaselineProfile() = coldStart(CompilationMode.Partial(BaselineProfileMode.Require))

    private fun coldStart(compilationMode: CompilationMode) = rule.measureRepeated(
        packageName = PACKAGE_NAME,
        metrics = listOf(StartupTimingMetric(), FrameTimingMetric()),
        compilationMode = compilationMode,
        startupMode = StartupMode.COLD,
        iterations = ITERATIONS,
        setupBlock = {
            // A fresh install shows the onboarding once; measure the Home launch every returning player sees. COLD mode
            // kills the process before this block, so kill it again and drop the page cache for a real cold start.
            pressHome()
            startActivityAndWait()
            completeOnboardingIfShown()
            killProcess()
            dropKernelPageCache()
        },
    ) {
        startActivityAndWait()
        waitForHome()
    }

    private companion object {
        const val ITERATIONS = 10
    }
}
