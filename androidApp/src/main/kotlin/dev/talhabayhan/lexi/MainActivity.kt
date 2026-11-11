package dev.talhabayhan.lexi

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.talhabayhan.lexi.perf.PerfTourLaunch
import dev.talhabayhan.lexi.reminder.NotificationPermissionHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        MainActivityLanguage.init(this)

        // Light only (ADR 0010): dark system-bar icons even when the system is in dark mode.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        // The POST_NOTIFICATIONS launcher must be registered before the activity is STARTED (ADR 0021).
        NotificationPermissionHost.register(this)

        // Perf tour (docs/PERF.md): `adb shell am start ... --ez lexi.perfTour true [--ei lexi.perfTourLoops N]`;
        // honoured only in debuggable builds or from the shell.
        val perfTour = PerfTourLaunch.config(this, savedInstanceState)

        setContent {
            MainActivityLanguage.LexiApp(
                activity = this,
                perfTour = perfTour,
            )
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
