package dev.talhabayhan.lexi.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Feature module: `lexi.kmp.compose` + the core modules a feature may use (ARCHITECTURE.md §2) and the MVI/UDF stack
 * (ViewModel, Koin, type-safe navigation). Features never depend on `core:data` or on other features;
 * `lexi.module-rules` enforces that.
 */
class FeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("lexi.kmp.compose")
        pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")

        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.getByName("commonMain").dependencies {
                implementation(project(":core:domain"))
                implementation(project(":core:designsystem"))
                implementation(project(":core:platform"))

                implementation(libs.library("androidx-lifecycle-viewmodelCompose"))
                implementation(libs.library("androidx-lifecycle-runtimeCompose"))
                implementation(libs.library("androidx-navigation-compose"))
                implementation(libs.library("koin-core"))
                implementation(libs.library("koin-compose"))
                implementation(libs.library("koin-compose-viewmodel"))
                implementation(libs.library("kotlinx-serialization-core"))
                implementation(libs.library("kotlinx-collections-immutable"))
            }
            sourceSets.getByName("commonTest").dependencies {
                implementation(project(":core:testing"))
            }
        }
    }
}
