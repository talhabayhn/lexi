package dev.talhabayhan.lexi.core.platform.uri

import dev.talhabayhan.lexi.core.domain.platform.UrlOpener
import platform.Foundation.NSThread
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

/**
 * Opens web links in Safari through `UIApplication.open(_:options:completionHandler:)` on the main thread.
 *
 * The deprecated one-argument `openURL:` is a no-op that returns NO on iOS 18+, and `canOpenURL:` needs the scheme in
 * `LSApplicationQueriesSchemes`; together they made every link (onboarding privacy, settings, about) dead on iOS.
 * [launch] is replaceable for tests.
 */
public class IosUrlOpener internal constructor(private val launch: (NSURL) -> Unit) : UrlOpener {
    public constructor() : this(::openOnMainThread)

    override fun openUrl(url: String): Boolean {
        val nsUrl = webUrlOrNull(url) ?: return false
        launch(nsUrl)
        return true
    }
}

/** [url] as an `http`/`https` [NSURL] with a host, or null. */
internal fun webUrlOrNull(url: String): NSURL? {
    val nsUrl = NSURL.URLWithString(url) ?: return null
    val scheme = nsUrl.scheme?.lowercase()
    if (scheme != "https" && scheme != "http") return null
    return nsUrl.takeIf { !it.host.isNullOrEmpty() }
}

private fun openOnMainThread(url: NSURL) {
    val open = {
        UIApplication.sharedApplication.openURL(url, options = emptyMap<Any?, Any>(), completionHandler = null)
    }
    if (NSThread.isMainThread) open() else dispatch_async(dispatch_get_main_queue()) { open() }
}
