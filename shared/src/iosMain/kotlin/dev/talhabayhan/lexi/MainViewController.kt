package dev.talhabayhan.lexi

import androidx.compose.ui.window.ComposeUIViewController
import dev.talhabayhan.lexi.perf.PerfTourConfig
import dev.talhabayhan.lexi.startup.StartupTrace
import dev.talhabayhan.lexi.telemetry.installUncaughtExceptionReporting
import platform.Foundation.NSProcessInfo
import platform.UIKit.UIViewController
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform

/**
 * Entry point called from Swift (`MainViewControllerKt.MainViewController(services:)`); the name is part of that API.
 * [services] are the Swift-implemented platform services (Firebase telemetry, ADR 0023).
 */
@Suppress("ktlint:standard:function-naming", "FunctionName")
@OptIn(ExperimentalNativeApi::class)
fun MainViewController(services: PlatformServices): UIViewController {
    StartupTrace.start(enabled = Platform.isDebugBinary)
    IosPlatformServices.install(services)
    installUncaughtExceptionReporting(services.telemetry)
    val perfTour = perfTourConfig()
    return ComposeUIViewController { App(perfTour = perfTour) }
}

/** Perf tour (`docs/PERF.md`): process argument `-LexiPerfTour`, optionally `-LexiPerfTourLoops N`. */
private fun perfTourConfig(): PerfTourConfig? {
    val arguments = NSProcessInfo.processInfo.arguments.map { it.toString() }
    if ("-LexiPerfTour" !in arguments) return null
    val loops = arguments.indexOf("-LexiPerfTourLoops").takeIf {
        it >= 0
    }?.let { arguments.getOrNull(it + 1)?.toIntOrNull() }
    return PerfTourConfig(loops ?: PerfTourConfig.DEFAULT_LOOPS)
}
