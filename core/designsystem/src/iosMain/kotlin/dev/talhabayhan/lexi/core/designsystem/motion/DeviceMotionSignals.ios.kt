package dev.talhabayhan.lexi.core.designsystem.motion

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSProcessInfo
import platform.Foundation.NSProcessInfoPowerStateDidChangeNotification
import platform.Foundation.isLowPowerModeEnabled

@Composable
internal actual fun deviceMotionSignals(): MotionSignals {
    var lowPower by remember { mutableStateOf(NSProcessInfo.processInfo.isLowPowerModeEnabled()) }
    DisposableEffect(Unit) {
        val center = NSNotificationCenter.defaultCenter
        val observer = center.addObserverForName(
            name = NSProcessInfoPowerStateDidChangeNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue,
        ) { _ ->
            lowPower = NSProcessInfo.processInfo.isLowPowerModeEnabled()
        }
        onDispose { center.removeObserver(observer) }
    }
    return MotionSignals(reducedMotion = false, lowRamDevice = false, lowPowerMode = lowPower)
}
