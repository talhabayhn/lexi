package dev.talhabayhan.lexi.core.domain.platform

/** Opens the system share sheet (Android chooser / iOS `UIActivityViewController`) for the share card (M6). */
public interface ShareService {
    /** Shares [png] (the rendered card) with [text]. Returns true if the share sheet was shown. */
    public suspend fun sharePng(png: ByteArray, text: String): Boolean
}
