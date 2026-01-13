plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.vanniktechPublish) apply false
    alias(libs.plugins.googleServices) apply false
    alias(libs.plugins.kotlinAndroid) apply false
}

allprojects {
    group = "com.riadmahi.firebase"
    version = findProperty("VERSION_NAME") ?: "1.0.0-SNAPSHOT"
}
