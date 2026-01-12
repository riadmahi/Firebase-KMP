rootProject.name = "kfire"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

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

// Demo app
include(":demo:shared")
include(":demo:androidApp")

// Firebase SDK modules
include(":firebase-core")
include(":firebase-auth")
include(":firebase-firestore")
include(":firebase-storage")
include(":firebase-messaging")
include(":firebase-analytics")
include(":firebase-remoteconfig")
include(":firebase-crashlytics")

// CLI tool
include(":firebase-cli")
