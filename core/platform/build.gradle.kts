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
        }
    }
}
