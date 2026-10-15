plugins {
    id("lexi.kmp.pure")
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Domain models are @Serializable (golden tests, saved games, online events); allowed by ARCHITECTURE.md §2.
            api(libs.kotlinx.serialization.core)
            // SavedGameCodec (process-death snapshot, ADR 0019); allowed by ARCHITECTURE.md §2.
            implementation(libs.kotlinx.serialization.json)
            // Repository interfaces expose Flow (ARCHITECTURE.md §6); allowed by ARCHITECTURE.md §2.
            api(libs.kotlinx.coroutines.core)
        }
    }
}
