plugins {
    id("lexi.feature")
    alias(libs.plugins.roborazzi)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Logging of storage failures until the crash reporter lands (M7); in the catalog by ADR 0015.
            implementation(libs.kermit)
            implementation(libs.androidx.navigationevent.compose)
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
