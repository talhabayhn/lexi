package dev.talhabayhan.lexi.feature.settings.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import dev.talhabayhan.lexi.core.designsystem.component.ListCard
import dev.talhabayhan.lexi.core.designsystem.component.ListDefaults
import dev.talhabayhan.lexi.core.designsystem.component.ListRow
import dev.talhabayhan.lexi.core.designsystem.component.NavSquareButton
import dev.talhabayhan.lexi.core.designsystem.icon.LexiIcons
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.lexiContentWidth
import dev.talhabayhan.lexi.core.domain.platform.AppLinks
import dev.talhabayhan.lexi.feature.settings.generated.resources.Res
import dev.talhabayhan.lexi.feature.settings.generated.resources.about_back
import dev.talhabayhan.lexi.feature.settings.generated.resources.about_description
import dev.talhabayhan.lexi.feature.settings.generated.resources.about_link_privacy
import dev.talhabayhan.lexi.feature.settings.generated.resources.about_link_support
import dev.talhabayhan.lexi.feature.settings.generated.resources.about_link_terms
import dev.talhabayhan.lexi.feature.settings.generated.resources.about_section_legal
import dev.talhabayhan.lexi.feature.settings.generated.resources.about_section_licenses
import dev.talhabayhan.lexi.feature.settings.generated.resources.about_section_sources
import dev.talhabayhan.lexi.feature.settings.generated.resources.about_sources_content
import dev.talhabayhan.lexi.feature.settings.generated.resources.about_title
import dev.talhabayhan.lexi.feature.settings.generated.resources.about_version_format
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * About and licenses screen (M8.5, DESIGN-DEBT): app identity, version, legal/support links,
 * word list provenance notes and open-source dependency licenses.
 */
@Composable
internal fun AboutScreen(state: AboutUiState, onIntent: (AboutIntent) -> Unit, modifier: Modifier = Modifier) {
    val dimens = LexiTheme.dimens
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LexiTheme.colors.cream)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .lexiContentWidth()
            .verticalScroll(rememberScrollState())
            .padding(
                start = dimens.screenPaddingHorizontal,
                top = dimens.screenPaddingTop,
                end = dimens.screenPaddingHorizontal,
                bottom = dimens.screenPaddingBottomList,
            ),
        verticalArrangement = Arrangement.spacedBy(dimens.gap14),
    ) {
        Header(onBack = { onIntent(AboutIntent.Back) })

        ListCard(Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = dimens.gap16,
                        vertical = dimens.gap16,
                    ),
                verticalArrangement = Arrangement.spacedBy(dimens.gap6),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(text = state.appName, style = LexiTheme.typography.heading24)
                Text(
                    text = stringResource(Res.string.about_version_format, state.versionName, state.buildNumber),
                    style = LexiTheme.typography.caption13,
                )
                Text(
                    text = stringResource(Res.string.about_description),
                    style = LexiTheme.typography.body14,
                    textAlign = TextAlign.Center,
                )
            }
        }

        AboutSection(Res.string.about_section_legal) {
            ListRow(
                label = stringResource(Res.string.about_link_privacy),
                onClick = { onIntent(AboutIntent.OpenUrl(AppLinks.PRIVACY_POLICY)) },
            ) {
                Icon(
                    imageVector = LexiIcons.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(ListDefaults.ChevronSize),
                )
            }
            ListRow(
                label = stringResource(Res.string.about_link_terms),
                onClick = { onIntent(AboutIntent.OpenUrl(AppLinks.TERMS_OF_SERVICE)) },
            ) {
                Icon(
                    imageVector = LexiIcons.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(ListDefaults.ChevronSize),
                )
            }
            ListRow(
                label = stringResource(Res.string.about_link_support),
                onClick = { onIntent(AboutIntent.OpenUrl(AppLinks.SUPPORT)) },
            ) {
                Icon(
                    imageVector = LexiIcons.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(ListDefaults.ChevronSize),
                )
            }
        }

        AboutSection(Res.string.about_section_sources) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = dimens.gap16,
                        vertical = dimens.gap14,
                    ),
                verticalArrangement = Arrangement.spacedBy(dimens.gap4),
            ) {
                Text(
                    text = stringResource(Res.string.about_sources_content),
                    style = LexiTheme.typography.body14,
                )
            }
        }

        AboutSection(Res.string.about_section_licenses) {
            state.licenses.forEach { item ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = dimens.gap12,
                            vertical = dimens.gap10,
                        ),
                    verticalArrangement = Arrangement.spacedBy(dimens.gap2),
                ) {
                    Text(text = item.name, style = LexiTheme.typography.bodyStrong16)
                    Text(text = item.license, style = LexiTheme.typography.caption13)
                    if (item.copyright != null) {
                        Text(text = item.copyright, style = LexiTheme.typography.caption12)
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(onBack: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NavSquareButton(
            icon = LexiIcons.Back,
            contentDescription = stringResource(Res.string.about_back),
            onClick = onBack,
        )
        Text(stringResource(Res.string.about_title), style = LexiTheme.typography.title)
    }
}

@Composable
private fun AboutSection(title: StringResource, rows: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap6)) {
        Text(stringResource(title), style = LexiTheme.typography.label12)
        ListCard(Modifier.fillMaxWidth(), content = rows)
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun AboutScreenPreview() {
    LexiTheme(reducedMotion = false) {
        AboutScreen(state = AboutUiState(), onIntent = {})
    }
}
