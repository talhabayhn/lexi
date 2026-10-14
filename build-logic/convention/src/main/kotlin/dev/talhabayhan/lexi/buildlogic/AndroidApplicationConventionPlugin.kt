package dev.talhabayhan.lexi.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

/** Android application shell (AGP 9 built-in Kotlin) with Compose enabled. */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
        pluginManager.apply("lexi.module-rules")
        pluginManager.apply("lexi.ktlint")

        extensions.configure<ApplicationExtension> {
            compileSdk = libs.version("android-compileSdk").toInt()
            defaultConfig {
                minSdk = libs.version("android-minSdk").toInt()
                targetSdk = libs.version("android-targetSdk").toInt()
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
            buildFeatures {
                compose = true
            }
        }

        extensions.configure<KotlinAndroidProjectExtension> {
            compilerOptions {
                jvmTarget.set(LexiConfig.JVM_TARGET)
            }
        }
    }
}
