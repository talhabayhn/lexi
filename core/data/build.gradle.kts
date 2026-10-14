import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("lexi.kmp.library")
    alias(libs.plugins.sqldelight)
}

kotlin {
    // JVM target for fast repository tests with the in-memory SQLite driver (ARCHITECTURE.md §11).
    jvm {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
    }

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
