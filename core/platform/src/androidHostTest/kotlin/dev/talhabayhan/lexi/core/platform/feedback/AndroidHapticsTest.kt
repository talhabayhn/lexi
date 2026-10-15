package dev.talhabayhan.lexi.core.platform.feedback

import android.os.VibrationEffect
import dev.talhabayhan.lexi.core.domain.feedback.HapticKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AndroidHapticsTest {
    @Test
    fun apiTwentyNineUsesPredefinedEffects() {
        assertEquals(VibrationEffect.EFFECT_TICK, vibrationSpecFor(HapticKind.Light, 29).predefinedEffect)
        assertEquals(VibrationEffect.EFFECT_HEAVY_CLICK, vibrationSpecFor(HapticKind.Heavy, 36).predefinedEffect)
        assertEquals(VibrationEffect.EFFECT_CLICK, vibrationSpecFor(HapticKind.Soft, 29).predefinedEffect)
        assertEquals(VibrationEffect.EFFECT_DOUBLE_CLICK, vibrationSpecFor(HapticKind.Success, 30).predefinedEffect)
        for (kind in listOf(HapticKind.Error, HapticKind.Tick)) {
            assertNull(vibrationSpecFor(kind, 34).predefinedEffect, "$kind")
        }
    }

    @Test
    fun minSdkFallsBackToWaveforms() {
        for (kind in HapticKind.entries) {
            val spec = vibrationSpecFor(kind, 26)
            assertNull(spec.predefinedEffect, "$kind")
            assertEquals(spec.timings.size, spec.amplitudes.size, "$kind")
        }
    }
}
