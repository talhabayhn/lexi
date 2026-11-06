package dev.talhabayhan.lexi.core.platform.rating

import android.app.Activity
import android.content.Context
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import dev.talhabayhan.lexi.core.domain.platform.StoreLinks
import dev.talhabayhan.lexi.core.domain.rating.ReviewRequester
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * Google Play In-App Review (ADR 0024). Works only for an app installed from Play; elsewhere the flow fails and
 * [requestReview] returns false. [activity] is the current activity (null while none is attached).
 */
public class PlayReviewRequester(
    context: Context,
    private val activity: () -> Activity?,
    private val manager: ReviewManager = ReviewManagerFactory.create(context.applicationContext),
) : ReviewRequester {
    override val storePageUrl: String = StoreLinks.ANDROID_STORE_URL

    override suspend fun requestReview(): Boolean = withContext(Dispatchers.Main) {
        try {
            fun current() = activity()?.takeUnless { it.isFinishing || it.isDestroyed }
            if (current() == null) return@withContext false
            val info = suspendCancellableCoroutine<ReviewInfo?> { cont ->
                manager.requestReviewFlow().addOnCompleteListener { task ->
                    cont.resume(if (task.isSuccessful) task.result else null)
                }
            } ?: return@withContext false
            // The activity may have been recreated while the flow was prepared: look it up again.
            val host = current() ?: return@withContext false
            // The flow completes whether or not the dialog was shown; the OS gives no more detail.
            suspendCancellableCoroutine<Unit> { cont ->
                manager.launchReviewFlow(host, info).addOnCompleteListener { cont.resume(Unit) }
            }
            true
        } catch (e: CancellationException) {
            throw e
        } catch (@Suppress("TooGenericExceptionCaught") _: Exception) {
            false
        }
    }
}
