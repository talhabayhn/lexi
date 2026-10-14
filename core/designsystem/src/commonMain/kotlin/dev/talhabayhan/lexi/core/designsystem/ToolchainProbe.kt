package dev.talhabayhan.lexi.core.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.talhabayhan.lexi.core.designsystem.generated.resources.Res
import dev.talhabayhan.lexi.core.designsystem.generated.resources.app_name
import org.jetbrains.compose.resources.stringResource

/**
 * Toolchain spike (M0): exercises material3, compose resources and Roborazzi on the AGP 9 KMP library plugin.
 * Replaced by the component gallery screenshot in M1.
 */
@Composable
internal fun ToolchainProbe(modifier: Modifier = Modifier) {
    MaterialTheme {
        Surface(modifier = modifier.fillMaxSize()) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = stringResource(Res.string.app_name), style = MaterialTheme.typography.headlineMedium)
            }
        }
    }
}
