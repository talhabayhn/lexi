package dev.talhabayhan.lexi.core.designsystem.capture

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import java.io.ByteArrayOutputStream

internal actual fun ImageBitmap.encodePng(): ByteArray? {
    val source = asAndroidBitmap()
    // A layer snapshot may be a HARDWARE bitmap; encode a software copy of it.
    val readable = if (source.config == Bitmap.Config.HARDWARE) source.copy(Bitmap.Config.ARGB_8888, false) else source
    readable ?: return null
    val out = ByteArrayOutputStream()
    return if (readable.compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, out)) out.toByteArray() else null
}

/** Ignored by the lossless PNG encoder. */
private const val PNG_QUALITY = 100
