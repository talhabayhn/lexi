package dev.talhabayhan.lexi.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Pure Kotlin Multiplatform module without Android framework: `jvm` (fast tests, JVM tools, consumed by Android
 * targets) + `iosArm64` + `iosSimulatorArm64`. Used by `core:domain` and `core:testing`.
 */
class KmpPureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        pluginManager.apply("lexi.module-rules")

        extensions.configure<KotlinMultiplatformExtension> {
            if (isCoreModule) explicitApi()

            jvm {
                compilerOptions {
                    jvmTarget.set(LexiConfig.JVM_TARGET)
                }
            }
            iosArm64()
            iosSimulatorArm64()

            sourceSets.getByName("commonTest").dependencies {
                implementation(libs.library("kotlin-test"))
            }
        }
    }
}
