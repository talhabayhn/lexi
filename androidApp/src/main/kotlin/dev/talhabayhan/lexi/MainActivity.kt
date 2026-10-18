package dev.talhabayhan.lexi

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.talhabayhan.lexi.core.platform.language.AndroidPlatformLanguage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val currentLanguage = AndroidPlatformLanguage.getStoredLanguage(this)
        AndroidPlatformLanguage.applyLanguage(this, currentLanguage)

        // Light only (ADR 0010): dark system-bar icons even when the system is in dark mode.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)

        setContent {
            App(
                onLanguageChanged = { newLanguage ->
                    if (newLanguage != currentLanguage) {
                        AndroidPlatformLanguage.setStoredLanguage(this, newLanguage)
                        AndroidPlatformLanguage.applyLanguage(this, newLanguage)
                        recreate()
                    }
                },
            )
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
