import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension

plugins {
    id("lexi.android.application")
    // Firebase (ADR 0023): google-services.json -> resources; Crashlytics build id + R8 mapping upload.
    alias(libs.plugins.googleServices)
    alias(libs.plugins.firebaseCrashlytics)
}

dependencies {
    implementation(project(":shared"))

    implementation(libs.androidx.activity.compose)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

// Release signing comes only from the environment (CI writes the keystore from a secret, see docs/RELEASE.md).
// Without LEXI_KEYSTORE_FILE the release build is left unsigned, so it still builds on any machine.
val releaseKeystore = providers.environmentVariable("LEXI_KEYSTORE_FILE").orNull

// Telemetry (ADR 0023, docs/ANALYTICS.md): release builds collect; debug builds only with -Plexi.telemetry=true.
val telemetryInDebug = providers.gradleProperty("lexi.telemetry").orNull == "true"

// Pro content (exam packs) is unlocked in debug builds for testing until RevenueCat; -Plexi.debugUnlockPro=false shows
// the locked state. Release builds are never unlocked (StaticEntitlementRepository also requires a debuggable app).
val debugUnlockPro = providers.gradleProperty("lexi.debugUnlockPro").orNull != "false"

// The R8 mapping goes to Crashlytics only for store builds (release-android passes -Plexi.crashlyticsUpload=true), so
// `./gradlew build` and the push CI never need the network.
val crashlyticsUpload = providers.gradleProperty("lexi.crashlyticsUpload").orNull == "true"

android {
    namespace = "dev.talhabayhan.lexi"

    defaultConfig {
        applicationId = "dev.talhabayhan.lexi"
        // Overridden by CI from the git tag (docs/CONVENTIONS.md: major*10000 + minor*100 + patch).
        versionCode = providers.gradleProperty("lexi.versionCode").orNull?.toInt() ?: 1
        versionName = providers.gradleProperty("lexi.versionName").orNull ?: "1.0"
    }
    signingConfigs {
        if (releaseKeystore != null) {
            create("release") {
                storeFile = file(releaseKeystore)
                storePassword = providers.environmentVariable("LEXI_KEYSTORE_PASSWORD").orNull
                keyAlias = providers.environmentVariable("LEXI_KEY_ALIAS").orNull
                keyPassword = providers.environmentVariable("LEXI_KEY_PASSWORD").orNull
            }
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        debug {
            manifestPlaceholders["telemetryEnabled"] = telemetryInDebug.toString()
            manifestPlaceholders["debugUnlockPro"] = debugUnlockPro.toString()
        }
        release {
            manifestPlaceholders["telemetryEnabled"] = "true"
            manifestPlaceholders["debugUnlockPro"] = "false"
            configure<CrashlyticsExtension> {
                mappingFileUploadEnabled = crashlyticsUpload
            }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (releaseKeystore != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
}
