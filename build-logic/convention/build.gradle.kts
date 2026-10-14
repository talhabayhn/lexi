import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "dev.talhabayhan.lexi.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    // compileOnly: the plugin versions on the runtime classpath come from the root build's `apply false` block.
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.composeCompiler.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("kmpLibrary") {
            id = "lexi.kmp.library"
            implementationClass = "dev.talhabayhan.lexi.buildlogic.KmpLibraryConventionPlugin"
        }
        register("kmpCompose") {
            id = "lexi.kmp.compose"
            implementationClass = "dev.talhabayhan.lexi.buildlogic.KmpComposeConventionPlugin"
        }
        register("androidApplication") {
            id = "lexi.android.application"
            implementationClass = "dev.talhabayhan.lexi.buildlogic.AndroidApplicationConventionPlugin"
        }
    }
}
