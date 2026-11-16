package dev.talhabayhan.lexi.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Video-flow journeys (ADR 0027, `docs/design/video-flow-notes.md` § Performance). Placeholders until phase 3 builds
 * the screens: each test names its journey and the acceptance bar so phase 3 only fills in the UI steps.
 *
 * Acceptance per journey (`FrameTimingMetric`): `frameDurationCpuMs` P95 < 16.6 ms and no frame > 100 ms, on the
 * device matrix of the notes (Android 8 low-RAM, a Samsung A-series, a Pixel).
 */
@RunWith(AndroidJUnit4::class)
class VideoFlowBenchmark {
    @get:Rule
    val rule = MacrobenchmarkRule()

    /** Home → play card → "Çarkını seç" (#12 slide + fade), scroll the category grid to the exam packs and back up. */
    @Ignore("video flow phase 3: categories screen not built yet")
    @Test
    fun homeToCategories() = journey { /* phase 3: tap play card, wait for categories, fling grid down/up */ }

    /** Categories → category card lift + Y flip into the settings dialog (#13–#14), change mode/time, close (#15). */
    @Ignore("video flow phase 3: settings dialog not built yet")
    @Test
    fun categoryDialogFlip() = journey { /* phase 3: open categories, tap a card, toggle segments, close */ }

    /** Dialog OYNA → letter morph (#16) → game board entrance, play two answers (#17). */
    @Ignore("video flow phase 3: letter morph not wired to the game yet")
    @Test
    fun playLetterMorph() = journey { /* phase 3: open dialog, tap OYNA, wait for the board, type two answers */ }

    /** The whole flow in one run: Home → Categories → Dialog → Game → back to Home. */
    @Ignore("video flow phase 3: screens not built yet")
    @Test
    fun fullVideoFlow() = journey { /* phase 3: homeToCategories + categoryDialogFlip + playLetterMorph */ }

    private fun journey(steps: () -> Unit) = rule.measureRepeated(
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
        waitForHome()
        steps()
    }

    private companion object {
        const val ITERATIONS = 5
    }
}
