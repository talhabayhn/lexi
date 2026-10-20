package dev.talhabayhan.lexi.feature.settings

import dev.talhabayhan.lexi.feature.settings.generated.resources.Res
import dev.talhabayhan.lexi.feature.settings.generated.resources.allPluralStringResources
import dev.talhabayhan.lexi.feature.settings.generated.resources.allStringResources
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString

/**
 * Reads every string and plural of this module into the compose-resources cache so the first `stringResource()` on
 * the main thread finds them already loaded. Call off the main thread, after the splash.
 */
suspend fun preloadSettingsStrings() {
    Res.allStringResources.values.forEach { getString(it) }
    Res.allPluralStringResources.values.forEach { getPluralString(it, 1) }
}
