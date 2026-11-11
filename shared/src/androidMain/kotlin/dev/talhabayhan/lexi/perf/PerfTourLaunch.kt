package dev.talhabayhan.lexi.perf

import android.app.Activity
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Bundle

/**
 * Launch extras of the perf tour (`docs/PERF.md`). The launcher activity is exported, so any app could send them: they
 * are honoured only in a debuggable build or when the activity was started from the shell (`adb shell am start`),
 * which keeps release perf runs possible.
 */
public object PerfTourLaunch {
    internal const val EXTRA_PERF_TOUR: String = "lexi.perfTour"
    internal const val EXTRA_PERF_TOUR_LOOPS: String = "lexi.perfTourLoops"
    internal const val MAX_LOOPS: Int = 20
    private const val SHELL_REFERRER = "android-app://com.android.shell"

    /** The tour to run for this launch, or null; never on a re-created activity ([savedInstanceState] non-null). */
    public fun config(activity: Activity, savedInstanceState: Bundle?): PerfTourConfig? {
        if (savedInstanceState != null) return null
        val intent = activity.intent ?: return null
        val debuggable = activity.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        return perfTourConfig(intent, debuggable, isShellLaunch(intent, activity.referrer))
    }

    /**
     * [Activity.getReferrer] prefers the caller-supplied referrer extras, which anyone can set; only without them is it
     * the package the system saw starting the activity.
     */
    internal fun isShellLaunch(intent: Intent, referrer: Uri?): Boolean = !intent.hasExtra(Intent.EXTRA_REFERRER) &&
        !intent.hasExtra(Intent.EXTRA_REFERRER_NAME) &&
        referrer?.toString() == SHELL_REFERRER

    internal fun perfTourConfig(intent: Intent, debuggable: Boolean, fromShell: Boolean): PerfTourConfig? {
        if (!intent.getBooleanExtra(EXTRA_PERF_TOUR, false)) return null
        if (!debuggable && !fromShell) return null
        val loops = intent.getIntExtra(EXTRA_PERF_TOUR_LOOPS, PerfTourConfig.DEFAULT_LOOPS)
        return PerfTourConfig(loops.coerceIn(1, MAX_LOOPS))
    }
}
