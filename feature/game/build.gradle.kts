plugins {
    id("lexi.feature")
    alias(libs.plugins.roborazzi)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // SavedStateHandle: route args and the process-death snapshot of the running game (ADR 0019).
            implementation(libs.androidx.lifecycle.viewmodelSavedstate)
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

roborazzi {
    outputDir.set(file("src/androidHostTest/screenshots"))
}

// Robolectric (SDK 36) touches FileDescriptor internals that JDK 17+ hides; see ADR 0013.
tasks.withType<Test>().configureEach {
    jvmArgs("--add-exports=java.base/jdk.internal.access=ALL-UNNAMED")
}
