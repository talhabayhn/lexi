package dev.talhabayhan.lexi.buildlogic

/**
 * Module dependency rules from ARCHITECTURE.md §2, checked by `verifyModuleDependencies` (part of `check`).
 * Keep this table and the doc in sync.
 */
internal object ModuleRules {

    private val FEATURE_ALLOWED = setOf(":core:domain", ":core:designsystem", ":core:platform")

    /** Project dependencies each module may declare in main (non-test) configurations. `null` = anything. */
    private fun allowedMainProjects(module: String): Set<String>? = when {
        module == ":shared" -> null
        module == ":androidApp" -> setOf(":shared")
        module == ":core:domain" -> emptySet()
        module == ":core:data" -> setOf(":core:domain")
        module == ":core:designsystem" -> setOf(":core:domain")
        module == ":core:platform" -> setOf(":core:domain")
        module == ":core:testing" -> setOf(":core:domain")
        module.startsWith(":feature:") -> FEATURE_ALLOWED
        module == ":tools:content-compiler" -> setOf(":core:domain")
        else -> emptySet()
    }

    /** `core:testing` is a test-only dependency, available to every module's tests (except its own dependencies). */
    private fun allowedTestProjects(module: String): Set<String>? =
        allowedMainProjects(module)?.let { main ->
            if (module == ":core:domain" || module == ":core:testing") main else main + ":core:testing"
        }

    /** External libraries `core:domain` may use (main code). Kotlin stdlib is implicit. */
    private val DOMAIN_MAIN_EXTERNALS = listOf(
        "org.jetbrains.kotlinx:kotlinx-coroutines-core",
        "org.jetbrains.kotlinx:kotlinx-datetime",
        "org.jetbrains.kotlinx:kotlinx-serialization-",
        "org.jetbrains.kotlin:kotlin-stdlib",
    )
    private val DOMAIN_TEST_EXTERNALS = DOMAIN_MAIN_EXTERNALS + listOf(
        "org.jetbrains.kotlin:kotlin-test",
        "org.jetbrains.kotlinx:kotlinx-coroutines-test",
        "app.cash.turbine:turbine",
    )

    fun isTestConfiguration(configuration: String): Boolean =
        configuration.startsWith("test") || configuration.contains("Test")

    /** Returns human-readable violations for one declared dependency, or an empty list. */
    fun violations(module: String, configuration: String, dependency: Dependency): List<String> {
        val isTest = isTestConfiguration(configuration)
        return when (dependency) {
            is Dependency.ProjectDep -> {
                val allowed = if (isTest) allowedTestProjects(module) else allowedMainProjects(module)
                if (allowed == null || dependency.path in allowed || dependency.path == module) {
                    emptyList()
                } else {
                    listOf("$module ($configuration) must not depend on ${dependency.path}")
                }
            }
            is Dependency.ExternalDep -> {
                if (module != ":core:domain") return emptyList()
                val allowed = if (isTest) DOMAIN_TEST_EXTERNALS else DOMAIN_MAIN_EXTERNALS
                if (allowed.any { dependency.coordinates.startsWith(it) }) {
                    emptyList()
                } else {
                    listOf("$module ($configuration) must stay pure Kotlin; ${dependency.coordinates} is not allowed")
                }
            }
        }
    }

    sealed interface Dependency {
        data class ProjectDep(val path: String) : Dependency
        data class ExternalDep(val coordinates: String) : Dependency
    }
}
