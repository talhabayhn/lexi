package dev.talhabayhan.lexi

import androidx.compose.ui.window.ComposeUIViewController
import dev.talhabayhan.lexi.startup.StartupTrace
import platform.UIKit.UIViewController
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform

/** Entry point called from Swift (`MainViewControllerKt.MainViewController()`); the name is part of that API. */
@Suppress("ktlint:standard:function-naming", "FunctionName")
@OptIn(ExperimentalNativeApi::class)
fun MainViewController(): UIViewController {
    StartupTrace.start(enabled = Platform.isDebugBinary)
    return ComposeUIViewController { App() }
}
