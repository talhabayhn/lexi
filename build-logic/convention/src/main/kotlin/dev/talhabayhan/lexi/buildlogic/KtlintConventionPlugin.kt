package dev.talhabayhan.lexi.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType
import java.io.File

/**
 * ktlint (rules configured in the root `.editorconfig`) wired into `check`, so `./gradlew build` fails on style
 * violations. Fix with `./gradlew ktlintFormat`. detekt follows once a stable release supports our Kotlin (ADR 0012).
 */
class KtlintConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply("org.jlleitschuh.gradle.ktlint")

        extensions.configure<KtlintExtension> {
            version.set(libs.version("ktlint"))
            reporters {
                reporter(ReporterType.PLAIN)
                reporter(ReporterType.CHECKSTYLE)
            }
            filter {
                // Generated code: compose resources, SQLDelight, BuildKonfig.
                exclude { it.file.path.contains("${File.separator}build${File.separator}") }
            }
        }
    }
}
