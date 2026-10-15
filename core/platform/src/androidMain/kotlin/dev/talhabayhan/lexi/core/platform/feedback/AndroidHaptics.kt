package dev.talhabayhan.lexi.core.platform.feedback

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dev.talhabayhan.lexi.core.domain.feedback.HapticKind
import dev.talhabayhan.lexi.core.domain.feedback.Haptics

/** What to vibrate for a [HapticKind]: a predefined effect id (API 29+) or a waveform. */
internal class VibrationSpec(
    val predefinedEffect: Int?,
    val timings: LongArray = longArrayOf(),
    val amplitudes: IntArray = intArrayOf(),
)

/** Pure `docs/design/motion.md` haptic map for Android; [sdk] selects the predefined effects of API 29+. */
internal fun vibrationSpecFor(kind: HapticKind, sdk: Int): VibrationSpec {
    val predefined = sdk >= Build.VERSION_CODES.Q
    return when (kind) {
        HapticKind.Light ->
            if (predefined) VibrationSpec(VibrationEffect.EFFECT_TICK) else oneShot(10, 60)

        HapticKind.Soft ->
            if (predefined) VibrationSpec(VibrationEffect.EFFECT_CLICK) else oneShot(15, 40)

        HapticKind.Heavy ->
            if (predefined) VibrationSpec(VibrationEffect.EFFECT_HEAVY_CLICK) else oneShot(40, 255)

        // Double tick.
        HapticKind.Success ->
            if (predefined) {
                VibrationSpec(VibrationEffect.EFFECT_DOUBLE_CLICK)
            } else {
                VibrationSpec(null, longArrayOf(0, 15, 60, 25), intArrayOf(0, 120, 0, 200))
            }

        // Long tick.
        HapticKind.Error -> VibrationSpec(null, longArrayOf(0, 80), intArrayOf(0, 220))

        HapticKind.Tick -> oneShot(8, 40)
    }
}

private fun oneShot(millis: Long, amplitude: Int) =
    VibrationSpec(null, longArrayOf(0, millis), intArrayOf(0, amplitude))

/** [Haptics] on the device vibrator (needs the normal `VIBRATE` permission); silent without a vibrator. */
public class AndroidHaptics(context: Context) : Haptics {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    override fun perform(kind: HapticKind) {
        val vibrator = vibrator?.takeIf { it.hasVibrator() } ?: return
        val spec = vibrationSpecFor(kind, Build.VERSION.SDK_INT)
        val predefined = spec.predefinedEffect
        val effect = if (predefined != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            VibrationEffect.createPredefined(predefined)
        } else {
            VibrationEffect.createWaveform(spec.timings, spec.amplitudes, NO_REPEAT)
        }
        runCatching { vibrator.vibrate(effect) }
    }

    private companion object {
        const val NO_REPEAT = -1
    }
}
