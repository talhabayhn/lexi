plugins {
    id("lexi.kmp.pure")
    alias(libs.plugins.kotlinSerialization)
}

abstract class GenerateAppVersionTask : DefaultTask() {
    @get:Input
    abstract val versionName: Property<String>

    @get:Input
    abstract val versionCode: Property<Int>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val file = outputDir.get().file("dev/talhabayhan/lexi/core/domain/AppVersion.kt").asFile
        file.parentFile.mkdirs()
        file.writeText(
            """
            package dev.talhabayhan.lexi.core.domain

            internal object AppVersion {
                const val VERSION_NAME: String = "${versionName.get()}"
                const val BUILD_NUMBER: Int = ${versionCode.get()}
            }
            """.trimIndent() + "\n",
        )
    }
}

val generateAppVersion by tasks.registering(GenerateAppVersionTask::class) {
    versionName.set(providers.gradleProperty("lexi.versionName").orElse("1.0.0"))
    versionCode.set(providers.gradleProperty("lexi.versionCode").map { it.toInt() }.orElse(1))
    outputDir.set(layout.buildDirectory.dir("generated/lexi/version"))
}

abstract class PrintAppVersionTask : DefaultTask() {
    @get:InputDirectory
    abstract val inputDir: DirectoryProperty

    @TaskAction
    fun printVersion() {
        val file = inputDir.get().file("dev/talhabayhan/lexi/core/domain/AppVersion.kt").asFile
        logger.quiet(file.readText())
    }
}

val printAppVersion by tasks.registering(PrintAppVersionTask::class) {
    inputDir.set(generateAppVersion.flatMap { it.outputDir })
}

kotlin {
    sourceSets {
        commonMain {
            kotlin.srcDir(generateAppVersion.map { it.outputDir })
            dependencies {
                // Domain models are @Serializable (golden tests, saved games, online events); allowed by ARCHITECTURE.md §2.
                api(libs.kotlinx.serialization.core)
                // SavedGameCodec (process-death snapshot, ADR 0019); allowed by ARCHITECTURE.md §2.
                implementation(libs.kotlinx.serialization.json)
                // Repository interfaces expose Flow (ARCHITECTURE.md §6); allowed by ARCHITECTURE.md §2.
                api(libs.kotlinx.coroutines.core)
            }
        }
    }
}
