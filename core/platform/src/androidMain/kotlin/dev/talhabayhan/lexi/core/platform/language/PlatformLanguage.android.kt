package dev.talhabayhan.lexi.core.platform.language

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import dev.talhabayhan.lexi.core.domain.settings.AppLanguage
import java.util.Locale

public actual val isIos: Boolean = false

public actual fun syncPlatformAppLanguage(language: AppLanguage) {
    // Android live switching is managed at Activity recreate time via AndroidPlatformLanguage.
}

public object AndroidPlatformLanguage {
    private const val PREFS_NAME = "lexi_language_prefs"
    private const val KEY_LANGUAGE = "app_language"

    public fun getStoredLanguage(context: Context): AppLanguage {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val name = prefs.getString(KEY_LANGUAGE, null) ?: return AppLanguage.SYSTEM
        return runCatching { AppLanguage.valueOf(name) }.getOrDefault(AppLanguage.SYSTEM)
    }

    public fun setStoredLanguage(context: Context, language: AppLanguage) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, language.name).apply()
    }

    public fun applyLanguage(activity: Activity, language: AppLanguage) {
        val locale = when (language) {
            AppLanguage.SYSTEM -> {
                val systemLocales = Resources.getSystem().configuration.locales
                if (!systemLocales.isEmpty) systemLocales[0] else Locale.getDefault()
            }
            AppLanguage.TR -> Locale.forLanguageTag("tr")
            AppLanguage.EN -> Locale.forLanguageTag("en")
        }
        Locale.setDefault(locale)
        val resources = activity.resources
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}
