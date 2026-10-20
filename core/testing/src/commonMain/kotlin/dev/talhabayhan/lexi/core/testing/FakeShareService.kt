package dev.talhabayhan.lexi.core.testing

import dev.talhabayhan.lexi.core.domain.platform.ShareService

/** Records shared cards; also a silent [ShareService] for previews and tests. */
public class FakeShareService(private val result: Boolean = true) : ShareService {
    private val _shared = mutableListOf<Pair<ByteArray, String>>()

    /** Every (png, text) passed to [sharePng], in order. */
    public val shared: List<Pair<ByteArray, String>> get() = _shared.toList()

    override suspend fun sharePng(png: ByteArray, text: String): Boolean {
        _shared += png to text
        return result
    }
}
