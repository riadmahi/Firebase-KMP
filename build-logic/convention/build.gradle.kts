plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.plugins.kotlinMultiplatform.toDep())
    compileOnly(libs.plugins.androidLibrary.toDep())
    compileOnly(libs.plugins.androidKmpLibrary.toDep())
    compileOnly(libs.plugins.kotlinSerialization.toDep())
    compileOnly(libs.plugins.vanniktechPublish.toDep())
}

fun Provider<PluginDependency>.toDep() = map {
    "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}"
}

gradlePlugin {
    plugins {
        register("kmpLibrary") {
            id = "convention.kmp-library"
            implementationClass = "KmpLibraryConventionPlugin"
        }
        register("mavenPublish") {
            id = "convention.maven-publish"
            implementationClass = "MavenPublishConventionPlugin"
        }
    }
}
