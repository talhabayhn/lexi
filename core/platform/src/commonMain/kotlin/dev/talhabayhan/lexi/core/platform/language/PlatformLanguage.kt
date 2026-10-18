package dev.talhabayhan.lexi.core.platform.language

import dev.talhabayhan.lexi.core.domain.settings.AppLanguage

/** Whether the host platform is iOS. Used to show restart hints where live switching is not supported. */
public expect val isIos: Boolean

/** Synchronizes the app language preference to platform storage (e.g. AppleLanguages on iOS). */
public expect fun syncPlatformAppLanguage(language: AppLanguage)
