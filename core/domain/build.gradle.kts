plugins {
    id("lexi.kmp.pure")
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Domain models are @Serializable (golden tests, saved games, online events); allowed by ARCHITECTURE.md §2.
            api(libs.kotlinx.serialization.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
