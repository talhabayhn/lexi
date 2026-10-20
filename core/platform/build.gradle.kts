plugins {
    id("lexi.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:domain"))
            // Time zone for AppClock.epochDay (ADR 0019).
            implementation(libs.kotlinx.datetime)
            // Settings snapshot (StateFlow) and the preloading scopes of the sound players.
            implementation(libs.kotlinx.coroutines.core)
        }
        androidMain.dependencies {
            // FileProvider + ShareCompat-free chooser intents for AndroidShareService (ADR 0020).
            implementation(libs.androidx.core.ktx)
        }
        getByName("androidHostTest").dependencies {
            implementation(libs.kotlin.testJunit)
            implementation(libs.robolectric)
        }
    }
}

// Robolectric (SDK 36) touches FileDescriptor internals that JDK 17+ hides; see ADR 0013.
tasks.withType<Test>().configureEach {
    jvmArgs("--add-exports=java.base/jdk.internal.access=ALL-UNNAMED")
}
