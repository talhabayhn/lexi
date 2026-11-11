import com.android.build.api.dsl.ManagedVirtualDevice

// Macrobenchmark + baseline profile generator for :androidApp (ADR 0025, docs/PERF.md). Test APK only; runs on the
// Gradle managed device below (CI: .github/workflows/perf.yml) or on a connected phone.
plugins {
    alias(libs.plugins.androidTest)
    alias(libs.plugins.baselineprofile)
    id("lexi.module-rules")
    id("lexi.ktlint")
}

// Gradle managed device for CI; Pixel 6 screen, API 34 AOSP image (no Play services, so no Firebase traffic).
val perfDevice = "pixel6Api34"

android {
    namespace = "dev.talhabayhan.lexi.benchmark"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        // Macrobenchmark and baseline profile collection need API 28+ (the app's minSdk stays 26).
        minSdk = 28
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    targetProjectPath = ":androidApp"

    testOptions.managedDevices.allDevices {
        create<ManagedVirtualDevice>(perfDevice) {
            device = "Pixel 6"
            apiLevel = 34
            systemImageSource = "aosp"
        }
    }
}

baselineProfile {
    managedDevices += perfDevice
    useConnectedDevices = false
}

dependencies {
    implementation(libs.androidx.testExt.junit)
    implementation(libs.androidx.uiautomator)
    implementation(libs.androidx.benchmark.macroJunit4)
}
