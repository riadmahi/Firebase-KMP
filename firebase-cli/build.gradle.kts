plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    application
}

application {
    mainClass.set("com.riadmahi.firebase.cli.MainKt")
    applicationName = "kfire"
}

dependencies {
    implementation(libs.clikt)
    implementation(libs.mordant)
    implementation(libs.mordant.coroutines)
    implementation(libs.okio)
    implementation(libs.jline)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)

    // Suppress SLF4J warnings from Ktor
    implementation(libs.slf4j.nop)
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.riadmahi.firebase.cli.MainKt"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
