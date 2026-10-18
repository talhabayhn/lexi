package dev.talhabayhan.lexi.feature.settings.about

import androidx.compose.runtime.Immutable

/**
 * Open-source dependency license entry shown on the About screen.
 */
@Immutable
internal data class OpenSourceLicense(
    val name: String,
    val license: String,
    val copyright: String? = null,
    val url: String? = null,
)

/**
 * Static list of third-party open-source licenses shown on the About screen.
 */
internal val OPEN_SOURCE_LICENSES: List<OpenSourceLicense> = listOf(
    OpenSourceLicense(
        name = "Jost",
        license = "SIL Open Font License 1.1",
        copyright = "Copyright 2020 The Jost Project Authors",
        url = "https://github.com/indestructible-type/Jost",
    ),
)
