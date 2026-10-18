package dev.talhabayhan.lexi.feature.settings.about

import androidx.compose.runtime.Immutable
import dev.talhabayhan.lexi.core.domain.AppInfo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Immutable
internal data class AboutUiState(
    val appName: String = AppInfo.NAME,
    val versionName: String = AppInfo.VERSION_NAME,
    val buildNumber: Int = AppInfo.BUILD_NUMBER,
    val licenses: ImmutableList<OpenSourceLicense> = OPEN_SOURCE_LICENSES.toImmutableList(),
)

internal sealed interface AboutIntent {
    data object Back : AboutIntent
    data class OpenUrl(val url: String) : AboutIntent
}

internal sealed interface AboutEffect {
    data object NavigateBack : AboutEffect
    data class OpenUrl(val url: String) : AboutEffect
}
