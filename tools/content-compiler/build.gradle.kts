plugins {
    id("lexi.jvm.application")
}

dependencies {
    implementation(project(":core:domain"))
}

application {
    mainClass.set("dev.talhabayhan.lexi.tools.contentcompiler.MainKt")
}
