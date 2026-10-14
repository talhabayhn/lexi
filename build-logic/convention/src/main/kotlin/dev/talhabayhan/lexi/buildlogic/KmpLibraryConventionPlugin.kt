package dev.talhabayhan.lexi.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Kotlin Multiplatform library with Android (AGP 9 `com.android.kotlin.multiplatform.library`) and iOS
 * (`iosArm64`, `iosSimulatorArm64`) targets. Namespace comes from the project path; core modules use explicit API.
 */
class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        pluginManager.apply("com.android.kotlin.multiplatform.library")
        pluginManager.apply("lexi.module-rules")
        pluginManager.apply("lexi.ktlint")

        extensions.configure<KotlinMultiplatformExtension> {
            if (isCoreModule) explicitApi()

            androidLibrary {
                namespace = lexiNamespace
                compileSdk = libs.version("android-compileSdk").toInt()
                minSdk = libs.version("android-minSdk").toInt()
                compilerOptions {
                    jvmTarget.set(LexiConfig.JVM_TARGET)
                }
                // Host tests may only be enabled once per module; include resources for Robolectric/compose tests.
                withHostTest {
                    isIncludeAndroidResources = true
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
