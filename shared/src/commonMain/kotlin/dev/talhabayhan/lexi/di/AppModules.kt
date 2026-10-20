package dev.talhabayhan.lexi.di

import androidx.compose.runtime.Composable
import dev.talhabayhan.lexi.core.data.db.BundledContentDb
import dev.talhabayhan.lexi.core.data.di.dataModule
import dev.talhabayhan.lexi.core.domain.config.FeatureFlags
import dev.talhabayhan.lexi.core.domain.daily.GetDailyWheel
import dev.talhabayhan.lexi.core.domain.game.GameConfig
import dev.talhabayhan.lexi.core.domain.goal.GetDailyGoalProgress
import dev.talhabayhan.lexi.core.domain.joker.GrantDailyJokers
import dev.talhabayhan.lexi.core.domain.reminder.AnswerReminderPrePrompt
import dev.talhabayhan.lexi.core.domain.reminder.ReminderTextProvider
import dev.talhabayhan.lexi.core.domain.reminder.RescheduleReminders
import dev.talhabayhan.lexi.core.domain.reminder.ShouldShowReminderPrePrompt
import dev.talhabayhan.lexi.core.domain.session.FinishGame
import dev.talhabayhan.lexi.core.domain.streak.GetStreakStatus
import dev.talhabayhan.lexi.core.domain.time.AppClock
import dev.talhabayhan.lexi.core.platform.feedback.LiveSettings
import dev.talhabayhan.lexi.core.platform.time.SystemAppClock
import dev.talhabayhan.lexi.data.ResourcesBundledContentDb
import dev.talhabayhan.lexi.feature.game.di.gameModule
import dev.talhabayhan.lexi.feature.home.di.homeModule
import dev.talhabayhan.lexi.feature.levels.di.levelsModule
import dev.talhabayhan.lexi.feature.notebook.di.notebookModule
import dev.talhabayhan.lexi.feature.onboarding.di.onboardingModule
import dev.talhabayhan.lexi.feature.settings.di.settingsModule
import dev.talhabayhan.lexi.feature.stats.di.statsModule
import dev.talhabayhan.lexi.reminder.ComposeReminderTextProvider
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.dsl.onClose

/**
 * App-level bindings: game tunables (Remote Config overrides later), the bundled content.db, the wall clock, the
 * live settings snapshot and domain use cases. `FinishGame` is a factory: one instance per game (its run-once guard is per instance).
 */
internal val appCoreModule: Module = module {
    single { GameConfig() }
    single { FeatureFlags() }
    single<BundledContentDb> { ResourcesBundledContentDb() }
    single<AppClock> { SystemAppClock() }
    factory { FinishGame(sessions = get(), progress = get(), clock = get(), config = get(), daily = get()) }
    factory { GrantDailyJokers(jokers = get(), streaks = get(), clock = get(), config = get()) }
    factory { GetDailyWheel(cards = get(), contentVersion = get(), daily = get(), clock = get()) }
    factory { GetStreakStatus(streaks = get(), clock = get()) }
    factory { GetDailyGoalProgress(sessions = get(), settings = get(), clock = get()) }
    // Haptics, SoundPlayer and Speaker are bound per platform (rememberPlatformModule) on top of this snapshot.
    single { LiveSettings(get()) }
    // Local reminders (ADR 0021): ReminderScheduler and NotificationPermission are bound per platform. A single: it
    // remembers the last plan it scheduled.
    single { ComposeReminderTextProvider() }
    single<ReminderTextProvider> { get<ComposeReminderTextProvider>() }
    factory { ShouldShowReminderPrePrompt(settings = get(), sessions = get(), permission = get()) }
    factory { AnswerReminderPrePrompt(settings = get(), permission = get(), reschedule = get()) }
    single {
        RescheduleReminders(
            settings = get(),
            streaks = get(),
            progress = get(),
            permission = get(),
            scheduler = get(),
            texts = get(),
            clock = get(),
        )
    }
}

/**
 * Every platform-independent Koin module of the app; `shared` is the only module that wires features and data
 * together. Platform objects (`DatabaseFiles`) come from [rememberPlatformModule].
 */
val appModules: List<Module> = listOf(
    appCoreModule,
    dataModule,
    onboardingModule,
    homeModule,
    levelsModule,
    notebookModule,
    statsModule,
    settingsModule,
    gameModule,
)

/** Koin definitions that need platform objects (Android `Context`), created at the composition root. */
@Composable
internal expect fun rememberPlatformModule(): Module
