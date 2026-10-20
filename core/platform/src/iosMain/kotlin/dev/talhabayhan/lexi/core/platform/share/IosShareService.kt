package dev.talhabayhan.lexi.core.platform.share

import dev.talhabayhan.lexi.core.domain.platform.ShareService
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UIKit.popoverPresentationController

/** Shares the card with a `UIActivityViewController` presented from the top-most view controller of the key window. */
public class IosShareService : ShareService {
    @OptIn(ExperimentalForeignApi::class)
    override suspend fun sharePng(png: ByteArray, text: String): Boolean = withContext(Dispatchers.Main) {
        val image = UIImage(data = png.toNSData())
        // UIApplication.windows is deprecated since iOS 15: find the key window through the connected scenes.
        val window = UIApplication.sharedApplication.connectedScenes
            .filterIsInstance<UIWindowScene>()
            .flatMap { scene -> scene.windows.filterIsInstance<UIWindow>() }
            .firstOrNull { it.isKeyWindow() }
        var top: UIViewController? = window?.rootViewController
        while (top?.presentedViewController != null) top = top.presentedViewController
        val presenter = top ?: return@withContext false
        val sheet = UIActivityViewController(activityItems = listOf(image, text), applicationActivities = null)
        sheet.popoverPresentationController?.let { popover ->
            popover.sourceView = presenter.view
            val (width, height) = presenter.view.bounds.useContents { size.width to size.height }
            popover.sourceRect = CGRectMake(width / 2.0, height / 2.0, 0.0, 0.0)
        }
        presenter.presentViewController(sheet, animated = true, completion = null)
        true
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun ByteArray.toNSData(): NSData = usePinned { pinned ->
    NSData.create(bytes = if (isEmpty()) null else pinned.addressOf(0), length = size.convert())
}
