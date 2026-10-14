package dev.talhabayhan.lexi.buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask
import org.gradle.language.base.plugins.LifecycleBasePlugin

/**
 * Registers `verifyModuleDependencies` and hooks it into `check`, so `./gradlew build` fails when a module declares
 * a dependency that ARCHITECTURE.md §2 forbids. Only the module's own declared dependencies are read
 * (configuration-cache and isolated-projects safe).
 */
class ModuleRulesConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply(LifecycleBasePlugin::class.java)

        val modulePath = path
        val declared = provider {
            configurations
                .filter { c -> DECLARING_SUFFIXES.any { c.name.endsWith(it) } }
                .flatMap { configuration ->
                    configuration.dependencies.mapNotNull { dependency ->
                        when (dependency) {
                            is ProjectDependency -> "${configuration.name}|project|${dependency.path}"
                            is ExternalModuleDependency ->
                                "${configuration.name}|external|${dependency.group}:${dependency.name}"
                            else -> null
                        }
                    }
                }
                .distinct()
                .sorted()
        }

        val verify = tasks.register("verifyModuleDependencies", VerifyModuleDependenciesTask::class.java) {
            group = LifecycleBasePlugin.VERIFICATION_GROUP
            description = "Checks declared dependencies against the module rules in ARCHITECTURE.md §2."
            module.set(modulePath)
            dependencies.set(declared)
        }
        tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME) { dependsOn(verify) }
        Unit
    }

    private companion object {
        val DECLARING_SUFFIXES = listOf("Implementation", "Api", "CompileOnly", "implementation", "api", "compileOnly")
    }
}

@UntrackedTask(because = "Cheap check over declared dependencies; always re-run")
abstract class VerifyModuleDependenciesTask : DefaultTask() {
    @get:Input
    abstract val module: org.gradle.api.provider.Property<String>

    @get:Input
    abstract val dependencies: ListProperty<String>

    @TaskAction
    fun verify() {
        val violations = dependencies.get().flatMap { entry ->
            val (configuration, kind, value) = entry.split('|', limit = 3)
            val dependency = if (kind == "project") {
                ModuleRules.Dependency.ProjectDep(value)
            } else {
                ModuleRules.Dependency.ExternalDep(value)
            }
            ModuleRules.violations(module.get(), configuration, dependency)
        }
        if (violations.isNotEmpty()) {
            throw GradleException(
                "Module dependency rules violated (ARCHITECTURE.md §2):\n" +
                    violations.distinct().joinToString("\n") { "  - $it" },
            )
        }
    }
}
