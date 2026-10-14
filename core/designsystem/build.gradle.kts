plugins {
    id("lexi.kmp.compose")
    alias(libs.plugins.roborazzi)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Exposed so features get Material3 as the underlay of LexiTheme (ARCHITECTURE.md §9).
            api(libs.compose.material3)
            // Haptics/SoundPlayer interfaces (pure Kotlin, ARCHITECTURE.md §2), exposed via LocalHaptics/LocalSoundPlayer.
            api(project(":core:domain"))
        }
        getByName("androidHostTest").dependencies {
            implementation(libs.junit)
            implementation(libs.robolectric)
            implementation(libs.roborazzi)
            implementation(libs.roborazzi.compose)
            implementation(libs.compose.uiTest)
            implementation(libs.androidx.compose.uiTestManifest)
        }
    }
}

compose.resources {
    // Shared strings such as `app_name` are used by features.
    publicResClass = true
}

roborazzi {
    outputDir.set(file("src/androidHostTest/screenshots"))
}

// Robolectric (SDK 36) touches FileDescriptor internals that JDK 17+ hides; see ADR 0013.
tasks.withType<Test>().configureEach {
    jvmArgs("--add-exports=java.base/jdk.internal.access=ALL-UNNAMED")
}

// iOS AppIcon rendered from LexiAppIcon by AppIconRenderTest (build/app-icon), copied into the asset catalog.
val appIconRender = layout.buildDirectory.file("app-icon/app-icon-1024.png")
tasks.withType<Test>().matching { it.name == "testAndroidHostTest" }.configureEach { outputs.file(appIconRender) }
tasks.register<Copy>("exportIosAppIcon") {
    description = "Renders the iOS AppIcon from LexiAppIcon and copies it into iosApp's asset catalog."
    dependsOn("testAndroidHostTest")
    from(appIconRender)
    into(rootProject.layout.projectDirectory.dir("iosApp/iosApp/Assets.xcassets/AppIcon.appiconset"))
}
