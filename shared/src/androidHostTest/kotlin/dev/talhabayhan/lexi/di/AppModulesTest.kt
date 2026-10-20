package dev.talhabayhan.lexi.di

import androidx.lifecycle.SavedStateHandle
import dev.talhabayhan.lexi.core.data.db.DatabaseFiles
import dev.talhabayhan.lexi.core.domain.feedback.Haptics
import dev.talhabayhan.lexi.core.domain.feedback.SoundPlayer
import dev.talhabayhan.lexi.core.domain.feedback.Speaker
import dev.talhabayhan.lexi.core.domain.platform.ShareService
import dev.talhabayhan.lexi.core.domain.platform.UrlOpener
import dev.talhabayhan.lexi.feature.game.GameRoute
import dev.talhabayhan.lexi.feature.game.result.ResultRoute
import dev.talhabayhan.lexi.feature.levels.CategoriesRoute
import dev.talhabayhan.lexi.feature.levels.LevelSelectRoute
import kotlinx.coroutines.CoroutineScope
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.verify
import kotlin.test.Test

class AppModulesTest {
    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun `koin graph resolves every definition`() {
        // DatabaseFiles and the feedback services come from the platform module created at the composition root;
        // SavedStateHandle and the routes read from it come from the navigation back-stack entry.
        module { includes(appModules) }.verify(
            extraTypes = listOf(
                DatabaseFiles::class,
                Speaker::class,
                UrlOpener::class,
                ShareService::class,
                Haptics::class,
                SoundPlayer::class,
                SavedStateHandle::class,
                GameRoute::class,
                ResultRoute::class,
                CategoriesRoute::class,
                LevelSelectRoute::class,
                CoroutineScope::class,
            ),
        )
    }
}
