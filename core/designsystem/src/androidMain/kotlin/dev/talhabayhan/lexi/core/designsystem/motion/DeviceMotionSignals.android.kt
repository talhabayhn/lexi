package dev.talhabayhan.lexi.core.designsystem.motion

import android.app.ActivityManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/** Low-RAM is a fixed device property, read once; Android has no low-power signal here (open item). */
@Composable
internal actual fun deviceMotionSignals(): MotionSignals {
    val context = LocalContext.current
    return remember(context) {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        MotionSignals(
            reducedMotion = false,
            lowRamDevice = activityManager?.isLowRamDevice == true,
            lowPowerMode = false,
        )
    }
}
