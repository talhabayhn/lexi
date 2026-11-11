package dev.talhabayhan.lexi

import android.app.Activity
import android.content.pm.ApplicationInfo
import android.os.Process
import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import dev.talhabayhan.lexi.core.platform.language.AndroidPlatformLanguage
import dev.talhabayhan.lexi.perf.PerfTourConfig
import dev.talhabayhan.lexi.splash.SplashState
import dev.talhabayhan.lexi.startup.StartupTrace
import kotlinx.coroutines.flow.first

/**
 * Android activity helper for app-level language initialization and dynamic locale updates.
 * Exposes language lifecycle methods to `androidApp` without requiring it to depend on core modules.
 */
public object MainActivityLanguage {
    public fun init(activity: Activity) {
        val debuggable = activity.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        StartupTrace.start(debuggable, SystemClock.elapsedRealtime() - Process.getStartElapsedRealtime())
        val currentLanguage = AndroidPlatformLanguage.getStoredLanguage(activity)
        AndroidPlatformLanguage.applyLanguage(activity, currentLanguage)
    }

    @Composable
    public fun LexiApp(activity: Activity, perfTour: PerfTourConfig? = null) {
        val currentLanguage = AndroidPlatformLanguage.getStoredLanguage(activity)
        LaunchedEffect(activity) {
            // Time to full display (Macrobenchmark StartupTimingMetric, ADR 0025): the first screen after the splash
            // (Home or the onboarding) has drawn. Two frames: the one composing it and the one after it was drawn.
            SplashState.finished.first { it }
            repeat(FRAMES_UNTIL_DRAWN) { withFrameNanos { } }
            activity.reportFullyDrawn()
        }
        App(
            perfTour = perfTour,
            onLanguageChanged = { newLanguage ->
                if (newLanguage != currentLanguage) {
                    AndroidPlatformLanguage.setStoredLanguage(activity, newLanguage)
                    AndroidPlatformLanguage.applyLanguage(activity, newLanguage)
                    activity.recreate()
                }
            },
        )
    }

    private const val FRAMES_UNTIL_DRAWN = 2
}
