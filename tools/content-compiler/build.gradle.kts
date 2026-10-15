plugins {
    id("lexi.jvm.application")
    alias(libs.plugins.sqldelight)
}

dependencies {
    implementation(project(":core:domain"))
    implementation(libs.sqldelight.sqlite.driver)
    implementation(libs.kotlinx.serialization.json)
}

sqldelight {
    databases {
        // Generated from core:data's content.db schema (single source of truth) plus the compiler-only
        // INSERT statements in src/main/sqldelight. No project dependency on core:data (ARCHITECTURE.md §2).
        create("ContentDatabase") {
            packageName.set("dev.talhabayhan.lexi.tools.contentcompiler.db")
            srcDirs(
                rootProject.file("core/data/src/commonMain/sqldelight-content"),
                file("src/main/sqldelight"),
            )
        }
    }
}

application {
    mainClass.set("dev.talhabayhan.lexi.tools.contentcompiler.MainKt")
}

// `./gradlew :tools:content-compiler:run` resolves the default paths (content/, shared/...) from the repo root.
tasks.named<JavaExec>("run") {
    workingDir = rootDir
}
