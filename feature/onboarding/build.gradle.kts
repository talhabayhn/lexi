plugins {
    id("lexi.feature")
    alias(libs.plugins.roborazzi)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // SavedStateHandle: the level test result's route args (as in feature:game).
            implementation(libs.androidx.lifecycle.viewmodelSavedstate)
            // System back of the level test intro and result; already a transitive dependency of
            // navigation-compose, declared explicitly at the same version (as in feature:game).
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
