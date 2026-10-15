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
            api(project(":core:domain"))
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines.extensions)
            implementation(libs.kotlinx.coroutines.core)
        }
        androidMain {
            // java.io helpers shared by the Android and JVM (test) targets.
            kotlin.srcDir("src/jvmShared/kotlin")
            dependencies {
                implementation(libs.sqldelight.android.driver)
            }
        }
        jvmMain {
            kotlin.srcDir("src/jvmShared/kotlin")
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.native.driver)
        }
        commonTest.dependencies {
            implementation(project(":core:testing"))
        }
        jvmTest.dependencies {
            implementation(libs.sqldelight.sqlite.driver)
        }
    }
}

sqldelight {
    databases {
        // user.db: progress, sessions, daily results, streak, jokers (docs/CONTENT.md §8). Schema in
        // src/commonMain/sqldelight; every change after v1 is a numbered migration (<from-version>.sqm) plus a
        // schema snapshot for UserDatabaseMigrationTest.
        create("UserDatabase") {
            packageName.set("dev.talhabayhan.lexi.core.data.user.db")
        }
        // Read-only bundled content DB (docs/CONTENT.md §7). The schema files are shared with
        // tools:content-compiler, which generates its writer from them; the app's read queries live in a separate
        // directory so the compiler doesn't get them.
        create("ContentDatabase") {
            packageName.set("dev.talhabayhan.lexi.core.data.content.db")
            srcDirs("src/commonMain/sqldelight-content", "src/commonMain/sqldelight-content-reads")
        }
    }
}
