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

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
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
        register("kmpPure") {
            id = "lexi.kmp.pure"
            implementationClass = "dev.talhabayhan.lexi.buildlogic.KmpPureConventionPlugin"
        }
        register("feature") {
            id = "lexi.feature"
            implementationClass = "dev.talhabayhan.lexi.buildlogic.FeatureConventionPlugin"
        }
        register("jvmApplication") {
            id = "lexi.jvm.application"
            implementationClass = "dev.talhabayhan.lexi.buildlogic.JvmApplicationConventionPlugin"
        }
        register("moduleRules") {
            id = "lexi.module-rules"
            implementationClass = "dev.talhabayhan.lexi.buildlogic.ModuleRulesConventionPlugin"
        }
        register("androidApplication") {
            id = "lexi.android.application"
            implementationClass = "dev.talhabayhan.lexi.buildlogic.AndroidApplicationConventionPlugin"
        }
    }
}
