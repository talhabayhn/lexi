package dev.talhabayhan.lexi.core.domain.platform

/**
 * Abstraction for opening external web links in the system browser.
 */
public fun interface UrlOpener {
    /** Opens [url] in the system browser. Returns true if the launch succeeded. */
    public fun openUrl(url: String): Boolean
}
