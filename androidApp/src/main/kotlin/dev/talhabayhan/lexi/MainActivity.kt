package dev.talhabayhan.lexi

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
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

        // Perf tour (docs/PERF.md): `adb shell am start ... --ez lexi.perfTour true [--ei lexi.perfTourLoops N]`.
        val perfTour = savedInstanceState == null && intent.getBooleanExtra(EXTRA_PERF_TOUR, false)
        val perfTourLoops = intent.getIntExtra(EXTRA_PERF_TOUR_LOOPS, DEFAULT_PERF_TOUR_LOOPS)

        setContent {
            MainActivityLanguage.LexiApp(
                activity = this,
                perfTour = perfTour,
                perfTourLoops = perfTourLoops,
            )
        }
    }
}

private const val EXTRA_PERF_TOUR = "lexi.perfTour"
private const val EXTRA_PERF_TOUR_LOOPS = "lexi.perfTourLoops"
private const val DEFAULT_PERF_TOUR_LOOPS = 5

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
