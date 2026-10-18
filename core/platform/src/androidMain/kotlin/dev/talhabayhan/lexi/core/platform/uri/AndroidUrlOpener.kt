package dev.talhabayhan.lexi.core.platform.uri

import android.content.Context
import android.content.Intent
import android.net.Uri
import dev.talhabayhan.lexi.core.domain.platform.UrlOpener

public class AndroidUrlOpener(private val context: Context) : UrlOpener {
    override fun openUrl(url: String): Boolean = runCatching {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        true
    }.getOrDefault(false)
}
