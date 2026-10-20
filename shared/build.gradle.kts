plugins {
    id("lexi.kmp.compose")
    // @Serializable navigation route of the splash (ADR 0016); the features apply it through lexi.feature.
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:domain"))
            implementation(project(":core:data"))
            implementation(project(":core:designsystem"))
            implementation(project(":core:platform"))
            implementation(project(":feature:onboarding"))
            implementation(project(":feature:home"))
            implementation(project(":feature:levels"))
            implementation(project(":feature:game"))
            implementation(project(":feature:notebook"))
            implementation(project(":feature:stats"))
            implementation(project(":feature:settings"))

            implementation(libs.androidx.navigation.compose)
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.koin.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            // Re-plan the reminders on app start/resume (ON_START).
            implementation(libs.androidx.lifecycle.runtimeCompose)
            // Debug-only speech timing logs (ADR 0015).
            implementation(libs.kermit)
        }
        androidMain.dependencies {
            // POST_NOTIFICATIONS ActivityResult bridge and NotificationManagerCompat (ADR 0021); catalog entries only.
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.core.ktx)
        }
        getByName("androidHostTest").dependencies {
            implementation(libs.koin.test)
            implementation(libs.robolectric)
        }
    }
}

// content.db is a build output of tools:content-compiler (docs/CONTENT.md §1), consumed as an artifact (no code
// dependency) and merged with shared's own Compose resources, so it is bundled on Android and iOS
// (embedAndSignAppleFrameworkForXcode) without a manual step.
val contentDb = configurations.dependencyScope("contentDb")
val contentDbFiles = configurations.resolvable("contentDbFiles") {
    extendsFrom(contentDb.get())
    attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, "lexi-content-db"))
}
dependencies {
    "contentDb"(project(":tools:content-compiler"))
}

val composeResourcesWithContentDir: Provider<Directory> =
    layout.buildDirectory.dir("generated/composeResourcesWithContent")
val composeResourcesWithContent by tasks.registering(Sync::class) {
    from("src/commonMain/composeResources") {
        // Old compiler output location; the generated copy is the only source now.
        exclude("files/content.db")
    }
    from(contentDbFiles)
    into(composeResourcesWithContentDir)
    duplicatesStrategy = DuplicatesStrategy.FAIL
}

run {
    // Local copy: the provider lambda must not capture the script object (configuration cache).
    val dir = composeResourcesWithContentDir
    compose.resources {
        customDirectory(sourceSetName = "commonMain", directoryProvider = composeResourcesWithContent.map { dir.get() })
    }
}

// Robolectric (SDK 36) touches FileDescriptor internals that JDK 17+ hides; see ADR 0013.
tasks.withType<Test>().configureEach {
    jvmArgs("--add-exports=java.base/jdk.internal.access=ALL-UNNAMED")
}
