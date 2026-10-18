package dev.talhabayhan.lexi.core.platform.language

import dev.talhabayhan.lexi.core.domain.settings.AppLanguage
import platform.Foundation.NSUserDefaults

public actual val isIos: Boolean = true

public actual fun syncPlatformAppLanguage(language: AppLanguage) {
    val defaults = NSUserDefaults.standardUserDefaults
    val tag = language.localeTag
    if (tag == null) {
        defaults.removeObjectForKey("AppleLanguages")
    } else {
        defaults.setObject(listOf(tag), forKey = "AppleLanguages")
    }
    defaults.synchronize()
}
