package dev.talhabayhan.lexi.core.platform.language

import android.app.Activity
import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
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
            AppLanguage.SYSTEM -> systemLanguageLocale(
                appLocales = applicationLocales(activity).toList(),
                systemLocales = Resources.getSystem().configuration.locales.toList(),
                fallback = Locale.getDefault(),
            )

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

    /** The per-app language chosen in the system settings (Android 13+, `android:localeConfig`); empty if none. */
    private fun applicationLocales(context: Context): LocaleList {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return LocaleList.getEmptyLocaleList()
        return context.getSystemService(LocaleManager::class.java)?.applicationLocales
            ?: LocaleList.getEmptyLocaleList()
    }

    private fun LocaleList.toList(): List<Locale> = List(size()) { get(it) }
}

/**
 * Locale of the in-app "System" language: the per-app language from the system settings when the player picked one
 * there (Android 13+), else the device language, else [fallback].
 */
internal fun systemLanguageLocale(appLocales: List<Locale>, systemLocales: List<Locale>, fallback: Locale): Locale =
    appLocales.firstOrNull() ?: systemLocales.firstOrNull() ?: fallback
