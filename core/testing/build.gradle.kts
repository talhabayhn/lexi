// Test-only module: fakes, fixtures and test helpers. Allowed only in test configurations (module rules).
plugins {
    id("lexi.kmp.pure")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:domain"))
            api(libs.kotlin.test)
            api(libs.kotlinx.coroutines.test)
            api(libs.turbine)
        }
    }
}
