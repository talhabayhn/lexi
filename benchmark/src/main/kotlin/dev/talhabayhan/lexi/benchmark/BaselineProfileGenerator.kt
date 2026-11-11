package dev.talhabayhan.lexi.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Generates `androidApp/src/release/generated/baselineProfiles/` (ADR 0025):
 * `./gradlew :androidApp:generateBaselineProfile` (managed device, CI `perf.yml`). Only [startup] (cold launch to Home)
 * goes into the startup profile; [screens] (the perf tour) adds the first frames of every main screen and the game to
 * the baseline profile only.
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun startup() = rule.collect(packageName = PACKAGE_NAME, includeInStartupProfile = true) {
        pressHome()
        startActivityAndWait()
        completeOnboardingIfShown()
    }

    @Test
    fun screens() = rule.collect(packageName = PACKAGE_NAME, includeInStartupProfile = false, maxIterations = 3) {
        pressHome()
        startActivityAndWait()
        completeOnboardingIfShown()
        runPerfTour(loops = TOUR_LOOPS)
    }

    private companion object {
        const val TOUR_LOOPS = 2
    }
}
