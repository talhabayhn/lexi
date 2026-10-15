package dev.talhabayhan.lexi.di

import androidx.lifecycle.SavedStateHandle
import dev.talhabayhan.lexi.core.data.db.DatabaseFiles
import dev.talhabayhan.lexi.feature.game.GameRoute
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.verify
import kotlin.test.Test

class AppModulesTest {
    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun `koin graph resolves every definition`() {
        // DatabaseFiles comes from the platform module created at the composition root; SavedStateHandle and the
        // GameRoute read from it come from the navigation back-stack entry.
        module { includes(appModules) }.verify(
            extraTypes = listOf(DatabaseFiles::class, SavedStateHandle::class, GameRoute::class),
        )
    }
}
