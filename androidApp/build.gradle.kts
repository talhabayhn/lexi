plugins {
    id("lexi.android.application")
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
        release {
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
