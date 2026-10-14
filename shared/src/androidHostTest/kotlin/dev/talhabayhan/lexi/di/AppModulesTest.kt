package dev.talhabayhan.lexi.di

import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.verify
import kotlin.test.Test

class AppModulesTest {
    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun `koin graph resolves every definition`() {
        module { includes(appModules) }.verify()
    }
}
