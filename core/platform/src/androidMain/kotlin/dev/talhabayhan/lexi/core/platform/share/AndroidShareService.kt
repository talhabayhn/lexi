package dev.talhabayhan.lexi.core.platform.share

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import dev.talhabayhan.lexi.core.domain.platform.ShareService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Shares the card through the system chooser. The PNG is one overwritten file in `cache/share/`, the only path the
 * app's FileProvider exposes (ADR 0020).
 */
public class AndroidShareService(private val context: Context) : ShareService {
    override suspend fun sharePng(png: ByteArray, text: String): Boolean = try {
        val uri = withContext(Dispatchers.IO) {
            val dir = File(context.cacheDir, "share").apply { mkdirs() }
            val file = File(dir, "lexi-share.png").apply { writeBytes(png) }
            FileProvider.getUriForFile(context, "${context.packageName}.share", file)
        }
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, text)
            clipData = ClipData.newRawUri(null, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(send, null).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        context.startActivity(chooser)
        true
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        false
    }
}
