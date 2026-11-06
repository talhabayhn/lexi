package dev.talhabayhan.lexi.core.platform.rating

import dev.talhabayhan.lexi.core.domain.platform.StoreLinks
import dev.talhabayhan.lexi.core.domain.rating.ReviewRequester
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.StoreKit.SKStoreReviewController
import platform.UIKit.UIApplication
import platform.UIKit.UISceneActivationStateForegroundActive
import platform.UIKit.UIWindowScene

/**
 * `SKStoreReviewController` in the foreground-active window scene (ADR 0024). The OS rate-limits it and shows it only
 * in release/TestFlight builds; the call gives no result, so "invoked" is the answer.
 */
public class IosReviewRequester : ReviewRequester {
    override val storePageUrl: String = StoreLinks.IOS_STORE_URL

    override suspend fun requestReview(): Boolean = withContext(Dispatchers.Main) {
        val scene = UIApplication.sharedApplication.connectedScenes
            .filterIsInstance<UIWindowScene>()
            .firstOrNull { it.activationState == UISceneActivationStateForegroundActive }
            ?: return@withContext false
        SKStoreReviewController.requestReviewInScene(scene)
        true
    }
}
