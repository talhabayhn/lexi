package dev.talhabayhan.lexi.buildlogic

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal object LexiConfig {
    const val BASE_PACKAGE = "dev.talhabayhan.lexi"
    val JVM_TARGET = JvmTarget.JVM_17
}

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.version(alias: String): String = findVersion(alias).get().requiredVersion

internal fun VersionCatalog.library(alias: String): Provider<MinimalExternalModuleDependency> =
    findLibrary(alias).get()

/** `:core:data` -> `dev.talhabayhan.lexi.core.data`, `:shared` -> `dev.talhabayhan.lexi.shared`. */
internal val Project.lexiNamespace: String
    get() = LexiConfig.BASE_PACKAGE + path.replace(':', '.').replace('-', '.')

/** Core modules expose a deliberate public API (CONVENTIONS.md: explicit API mode in `core:*`). */
internal val Project.isCoreModule: Boolean
    get() = path.startsWith(":core:")

/** The `kotlin { android { } }` block of AGP 9's `com.android.kotlin.multiplatform.library` plugin. */
internal fun KotlinMultiplatformExtension.androidLibrary(action: Action<KotlinMultiplatformAndroidLibraryTarget>) {
    (this as ExtensionAware).extensions.configure(KotlinMultiplatformAndroidLibraryTarget::class.java, action)
}
