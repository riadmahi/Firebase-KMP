plugins {
    id("convention.kmp-library")
    id("convention.maven-publish")
}

kotlin {
    androidLibrary {
        namespace = "com.riadmahi.firebase.crashlytics"
        compileSdk = 36
        minSdk = 24
    }

    cocoapods {
        summary = "Firebase Crashlytics KMP wrapper"
        homepage = "https://github.com/riadmahi/kmpfire"
        ios.deploymentTarget = "15.0"
        version = "1.0.0"

        pod("FirebaseCrashlytics") {
            version = libs.versions.firebaseIos.get()
        }

        framework {
            baseName = "FirebaseCrashlytics"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":firebase-core"))
            implementation(libs.kotlinx.coroutines.core)
        }

        androidMain.dependencies {
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.firebase.crashlytics.android)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
