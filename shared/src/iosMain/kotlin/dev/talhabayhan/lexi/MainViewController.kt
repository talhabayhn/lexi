package dev.talhabayhan.lexi

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/** Entry point called from Swift (`MainViewControllerKt.MainViewController()`); the name is part of that API. */
@Suppress("ktlint:standard:function-naming", "FunctionName")
fun MainViewController(): UIViewController = ComposeUIViewController { App() }
