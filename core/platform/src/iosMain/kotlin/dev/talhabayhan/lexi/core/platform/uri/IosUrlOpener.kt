package dev.talhabayhan.lexi.core.platform.uri

import dev.talhabayhan.lexi.core.domain.platform.UrlOpener
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

public class IosUrlOpener : UrlOpener {
    override fun openUrl(url: String): Boolean {
        val nsUrl = NSURL.URLWithString(url) ?: return false
        val application = UIApplication.sharedApplication
        if (!application.canOpenURL(nsUrl)) return false
        application.openURL(nsUrl)
        return true
    }
}
