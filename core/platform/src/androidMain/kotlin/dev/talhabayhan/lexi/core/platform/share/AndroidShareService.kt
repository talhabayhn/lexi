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
 * Shares the card through the system chooser. Each share writes its own PNG in `cache/share/`, the only path the app's
 * FileProvider exposes (ADR 0020): a target that reads the URI late (an upload after the chooser closed) still gets
 * the card it was given, not a later one. Only the newest [SHARE_FILES_KEPT] files are kept.
 */
public class AndroidShareService(private val context: Context) : ShareService {
    override suspend fun sharePng(png: ByteArray, text: String): Boolean = try {
        val uri = withContext(Dispatchers.IO) {
            val file = writeShareFile(File(context.cacheDir, "share"), png)
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

/** Share PNGs kept in `cache/share/` (the new one included); older ones are deleted on the next share. */
internal const val SHARE_FILES_KEPT: Int = 5

/** Writes [png] to a new uniquely named file in [dir] and deletes all but the newest [keep] share files there. */
internal fun writeShareFile(dir: File, png: ByteArray, keep: Int = SHARE_FILES_KEPT): File {
    dir.mkdirs()
    val file = File.createTempFile("lexi-share-", ".png", dir).apply { writeBytes(png) }
    dir.listFiles { f -> f.isFile && f.name.startsWith("lexi-share") && f.name.endsWith(".png") && f != file }
        .orEmpty()
        .sortedByDescending { it.lastModified() }
        .drop(keep - 1)
        .forEach { it.delete() }
    return file
}
