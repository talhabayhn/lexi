plugins {
    id("lexi.feature")
    alias(libs.plugins.roborazzi)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // System back closes the mode / time dialog (as the game screen's back handler).
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
        // Pixel tests on the iOS simulator's Skia renderer (CategoryPickerEntranceIosTest), as in :core:designsystem.
        iosTest.dependencies {
            implementation(libs.compose.uiTest)
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
