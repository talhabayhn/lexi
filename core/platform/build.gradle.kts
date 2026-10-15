plugins {
    id("lexi.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:domain"))
            // Time zone for AppClock.epochDay (ADR 0019).
            implementation(libs.kotlinx.datetime)
        }
    }
}
