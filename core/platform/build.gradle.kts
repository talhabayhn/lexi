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
        getByName("androidHostTest").dependencies {
            implementation(libs.kotlin.testJunit)
        }
    }
}
