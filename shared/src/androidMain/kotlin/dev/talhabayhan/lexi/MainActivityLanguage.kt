package dev.talhabayhan.lexi

import android.app.Activity
import androidx.compose.runtime.Composable
import dev.talhabayhan.lexi.core.platform.language.AndroidPlatformLanguage

/**
 * Android activity helper for app-level language initialization and dynamic locale updates.
 * Exposes language lifecycle methods to `androidApp` without requiring it to depend on core modules.
 */
public object MainActivityLanguage {
    public fun init(activity: Activity) {
        val currentLanguage = AndroidPlatformLanguage.getStoredLanguage(activity)
        AndroidPlatformLanguage.applyLanguage(activity, currentLanguage)
    }

    @Composable
    public fun LexiApp(activity: Activity) {
        val currentLanguage = AndroidPlatformLanguage.getStoredLanguage(activity)
        App(
            onLanguageChanged = { newLanguage ->
                if (newLanguage != currentLanguage) {
                    AndroidPlatformLanguage.setStoredLanguage(activity, newLanguage)
                    AndroidPlatformLanguage.applyLanguage(activity, newLanguage)
                    activity.recreate()
                }
            },
        )
    }
}
