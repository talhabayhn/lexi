package dev.talhabayhan.lexi.buildlogic

import dev.talhabayhan.lexi.buildlogic.ModuleRules.Dependency.ExternalDep
import dev.talhabayhan.lexi.buildlogic.ModuleRules.Dependency.ProjectDep
import kotlin.test.Test
import kotlin.test.assertTrue

class ModuleRulesTest {
    private fun ok(module: String, configuration: String, dependency: ModuleRules.Dependency) =
        assertTrue(ModuleRules.violations(module, configuration, dependency).isEmpty(), "$module -> $dependency")

    private fun forbidden(module: String, configuration: String, dependency: ModuleRules.Dependency) =
        assertTrue(ModuleRules.violations(module, configuration, dependency).isNotEmpty(), "$module -> $dependency")

    @Test
    fun `feature may use domain, designsystem and platform`() {
        ok(":feature:home", "commonMainImplementation", ProjectDep(":core:domain"))
        ok(":feature:home", "commonMainImplementation", ProjectDep(":core:designsystem"))
        ok(":feature:home", "commonMainImplementation", ProjectDep(":core:platform"))
    }

    @Test
    fun `designsystem may use domain but not platform or data`() {
        ok(":core:designsystem", "commonMainApi", ProjectDep(":core:domain"))
        forbidden(":core:designsystem", "commonMainApi", ProjectDep(":core:platform"))
        forbidden(":core:designsystem", "commonMainImplementation", ProjectDep(":core:data"))
    }

    @Test
    fun `feature must not depend on data or another feature`() {
        forbidden(":feature:home", "commonMainImplementation", ProjectDep(":core:data"))
        forbidden(":feature:home", "commonMainImplementation", ProjectDep(":feature:game"))
    }

    @Test
    fun `core testing is allowed only in test configurations`() {
        ok(":feature:game", "commonTestImplementation", ProjectDep(":core:testing"))
        forbidden(":feature:game", "commonMainImplementation", ProjectDep(":core:testing"))
    }

    @Test
    fun `domain depends on no project and only on allowed kotlinx libraries`() {
        forbidden(":core:domain", "commonMainImplementation", ProjectDep(":core:data"))
        ok(":core:domain", "commonMainImplementation", ExternalDep("org.jetbrains.kotlinx:kotlinx-coroutines-core"))
        ok(":core:domain", "commonMainApi", ExternalDep("org.jetbrains.kotlinx:kotlinx-serialization-json"))
        forbidden(":core:domain", "commonMainImplementation", ExternalDep("app.cash.sqldelight:runtime"))
        forbidden(":core:domain", "commonMainImplementation", ExternalDep("org.jetbrains.compose.runtime:runtime"))
        ok(":core:domain", "commonTestImplementation", ExternalDep("app.cash.turbine:turbine"))
    }

    @Test
    fun `shared may depend on anything and androidApp only on shared`() {
        ok(":shared", "commonMainImplementation", ProjectDep(":core:data"))
        ok(":shared", "commonMainImplementation", ProjectDep(":feature:home"))
        ok(":androidApp", "implementation", ProjectDep(":shared"))
        forbidden(":androidApp", "implementation", ProjectDep(":core:data"))
    }

    @Test
    fun `content compiler may depend only on domain`() {
        ok(":tools:content-compiler", "implementation", ProjectDep(":core:domain"))
        forbidden(":tools:content-compiler", "implementation", ProjectDep(":core:data"))
    }
}
