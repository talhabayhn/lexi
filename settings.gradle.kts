rootProject.name = "Lexi"

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":androidApp")
include(":shared")
include(":core:domain")
include(":core:data")
include(":core:designsystem")
include(":core:platform")
include(":core:testing")
include(":feature:onboarding")
include(":feature:home")
include(":feature:levels")
include(":feature:game")
include(":feature:notebook")
include(":feature:stats")
include(":feature:exams")
include(":feature:settings")
include(":tools:content-compiler")
