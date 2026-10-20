package dev.talhabayhan.lexi.startup

import dev.talhabayhan.lexi.core.designsystem.preloadDesignStrings
import dev.talhabayhan.lexi.feature.game.preloadGameStrings
import dev.talhabayhan.lexi.feature.home.preloadHomeStrings
import dev.talhabayhan.lexi.feature.levels.preloadLevelsStrings
import dev.talhabayhan.lexi.feature.notebook.preloadNotebookStrings
import dev.talhabayhan.lexi.feature.onboarding.preloadOnboardingStrings
import dev.talhabayhan.lexi.feature.settings.preloadSettingsStrings
import dev.talhabayhan.lexi.feature.stats.preloadStatsStrings
import dev.talhabayhan.lexi.shared.generated.resources.Res
import dev.talhabayhan.lexi.shared.generated.resources.allPluralStringResources
import dev.talhabayhan.lexi.shared.generated.resources.allStringResources
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString

/**
 * Loads every string and plural of every module into the compose-resources cache on a background dispatcher, so
 * `stringResource()` does not read them from disk on the main thread during the first transitions. A failing module
 * is skipped (its strings are then read lazily as before) and noted in the debug startup trace only.
 */
internal suspend fun preloadAllStrings() = withContext(Dispatchers.Default) {
    val modules = listOf<Pair<String, suspend () -> Unit>>(
        "shared" to ::preloadSharedStrings,
        "designsystem" to ::preloadDesignStrings,
        "home" to ::preloadHomeStrings,
        "levels" to ::preloadLevelsStrings,
        "game" to ::preloadGameStrings,
        "settings" to ::preloadSettingsStrings,
        "stats" to ::preloadStatsStrings,
        "notebook" to ::preloadNotebookStrings,
        "onboarding" to ::preloadOnboardingStrings,
    )
    StartupTrace.measureSuspend("string preload") {
        for ((name, preload) in modules) {
            try {
                preload()
            } catch (e: CancellationException) {
                throw e
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                StartupTrace.mark("string preload failed in $name: ${e.message}")
            }
        }
    }
}

private suspend fun preloadSharedStrings() {
    Res.allStringResources.values.forEach { getString(it) }
    Res.allPluralStringResources.values.forEach { getPluralString(it, 1) }
}
