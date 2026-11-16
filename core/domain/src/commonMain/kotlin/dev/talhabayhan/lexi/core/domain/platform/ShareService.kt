package dev.talhabayhan.lexi.core.domain.platform

/**
 * Opens the system share sheet (Android chooser / iOS `UIActivityViewController`): the result's share card (M6) and
 * Home's invite ("Davet et", `video-flow-notes.md` §1).
 */
public interface ShareService {
    /** Shares [png] (the rendered card) with [text]. Returns true if the share sheet was shown. */
    public suspend fun sharePng(png: ByteArray, text: String): Boolean

    /** Shares plain [text] (the invite message with the landing link). Returns true if the share sheet was shown. */
    public suspend fun shareText(text: String): Boolean
}
