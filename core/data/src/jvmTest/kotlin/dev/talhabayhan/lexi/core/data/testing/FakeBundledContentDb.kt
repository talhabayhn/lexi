package dev.talhabayhan.lexi.core.data.testing

import dev.talhabayhan.lexi.core.data.db.BundledContentDb

class FakeBundledContentDb(var version: Long, var bytes: ByteArray) : BundledContentDb {
    var reads: Int = 0
        private set

    override suspend fun contentVersion(): Long = version

    override suspend fun readBytes(): ByteArray {
        reads++
        return bytes
    }
}
