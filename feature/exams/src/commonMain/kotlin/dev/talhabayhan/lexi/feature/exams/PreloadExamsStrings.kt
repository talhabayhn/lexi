package dev.talhabayhan.lexi.feature.exams

import dev.talhabayhan.lexi.feature.exams.generated.resources.Res
import dev.talhabayhan.lexi.feature.exams.generated.resources.allStringResources
import org.jetbrains.compose.resources.getString

/**
 * Reads every string of this module into the compose-resources cache so the first `stringResource()` on the main
 * thread finds them already loaded. Call off the main thread, after the splash.
 */
suspend fun preloadExamsStrings() {
    Res.allStringResources.values.forEach { getString(it) }
}
