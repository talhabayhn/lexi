package dev.talhabayhan.lexi.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

/** JVM command-line tool (e.g. `tools:content-compiler`). */
class JvmApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.jvm")
        pluginManager.apply("application")
        pluginManager.apply("lexi.module-rules")
        pluginManager.apply("lexi.ktlint")

        extensions.configure<JavaPluginExtension> {
            sourceCompatibility = org.gradle.api.JavaVersion.VERSION_17
            targetCompatibility = org.gradle.api.JavaVersion.VERSION_17
        }
        extensions.configure<KotlinJvmProjectExtension> {
            compilerOptions {
                jvmTarget.set(LexiConfig.JVM_TARGET)
            }
        }
        dependencies {
            "testImplementation"(libs.library("kotlin-test"))
        }
        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
        }
    }
}
