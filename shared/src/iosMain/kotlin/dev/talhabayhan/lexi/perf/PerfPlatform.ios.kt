package dev.talhabayhan.lexi.perf

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.writeToFile

internal actual val perfPlatformName: String = "ios"

@OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
internal actual fun writePerfReport(json: String) {
    val directory =
        NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true).firstOrNull() as? String
            ?: return
    NSString.create(string = json).writeToFile(
        "$directory/lexi-perf.json",
        atomically = true,
        encoding = NSUTF8StringEncoding,
        error = null,
    )
}
