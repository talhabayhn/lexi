package dev.talhabayhan.lexi.feature.home

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

/** Type-safe navigation destination of the exam packs page (a "coming soon" page until M7). */
@Serializable
data object ExamPacksRoute

/** Registers the exam packs page; [onBack] pops it. */
fun NavGraphBuilder.examPacksScreen(onBack: () -> Unit) {
    composable<ExamPacksRoute> {
        ExamPacksRoute(onBack = onBack)
    }
}

@Composable
internal fun ExamPacksRoute(onBack: () -> Unit) {
    ExamPacksScreen(onBack = onBack)
}
