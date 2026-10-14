import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Toolchain spike (M0): AGP 9 KMP library plugin + SQLDelight. Moves to convention plugins later.
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.sqldelight)
}

kotlin {
    explicitApi()

    // JVM target for fast repository tests with the in-memory SQLite driver (ARCHITECTURE.md §11).
    jvm {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
    }

    android {
        namespace = "dev.talhabayhan.lexi.core.data"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
    }

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.sqldelight.runtime)
        }
        androidMain.dependencies {
            implementation(libs.sqldelight.android.driver)
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.native.driver)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmTest.dependencies {
            implementation(libs.sqldelight.sqlite.driver)
        }
    }
}

sqldelight {
    databases {
        // Temporary probe schema; replaced by content.db / user.db in M3.
        create("ProbeDatabase") {
            packageName.set("dev.talhabayhan.lexi.core.data.db")
        }
    }
}
