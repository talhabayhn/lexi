package dev.talhabayhan.lexi.core.platform.language

import dev.talhabayhan.lexi.core.domain.settings.AppLanguage

/** Whether the host platform is iOS. Used to show restart hints where live switching is not supported. */
public expect val isIos: Boolean

/** BCP 47 tag of the language the device (or the per-app language setting) currently uses, e.g. "tr-TR". */
public expect fun deviceLanguageTag(): String

/** Whether the UI is in Turkish now: the app's language setting, with [AppLanguage.SYSTEM] following the device. */
public fun AppLanguage.isTurkishUi(): Boolean = isTurkish(deviceLanguageTag())

/** Synchronizes the app language preference to platform storage (e.g. AppleLanguages on iOS). */
public expect fun syncPlatformAppLanguage(language: AppLanguage)
