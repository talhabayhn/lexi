import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Toolchain spike (M0): AGP 9 KMP library plugin + Compose Multiplatform + Roborazzi.
// Moves to convention plugins later.
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.roborazzi)
}

kotlin {
    explicitApi()

    android {
        namespace = "dev.talhabayhan.lexi.core.designsystem"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
            implementation(libs.compose.material3)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
        }
        getByName("androidHostTest").dependencies {
            implementation(libs.junit)
            implementation(libs.robolectric)
            implementation(libs.roborazzi)
            implementation(libs.roborazzi.compose)
        }
    }
}

compose.resources {
    packageOfResClass = "dev.talhabayhan.lexi.core.designsystem.generated.resources"
}

roborazzi {
    outputDir.set(file("src/androidHostTest/screenshots"))
}

// Robolectric (SDK 36) touches FileDescriptor internals that JDK 17+ hides; see the toolchain ADR.
tasks.withType<Test>().configureEach {
    jvmArgs("--add-exports=java.base/jdk.internal.access=ALL-UNNAMED")
}
