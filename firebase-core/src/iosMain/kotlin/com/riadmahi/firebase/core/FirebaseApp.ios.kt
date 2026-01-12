@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.riadmahi.firebase.core

import cocoapods.FirebaseCore.FIRApp
import cocoapods.FirebaseCore.FIROptions

/**
 * iOS implementation of FirebaseApp using Firebase iOS SDK.
 */
actual class FirebaseApp internal constructor(
    val ios: FIRApp
) {
    actual val name: String
        get() = ios.name

    actual val options: FirebaseOptions
        get() = ios.options.toCommon()

    actual fun delete() {
        ios.deleteApp { }
    }

    actual companion object {
        actual val DEFAULT_APP_NAME: String = "__FIRAPP_DEFAULT"

        actual fun initialize(): FirebaseApp {
            FIRApp.configure()
            return getInstance()
        }

        actual fun initialize(options: FirebaseOptions): FirebaseApp {
            return initialize(options, DEFAULT_APP_NAME)
        }

        actual fun initialize(options: FirebaseOptions, name: String): FirebaseApp {
            val firOptions = options.toIos()
            if (name == DEFAULT_APP_NAME) {
                FIRApp.configureWithOptions(firOptions)
            } else {
                FIRApp.configureWithName(name, options = firOptions)
            }
            return getInstance(name)
        }

        actual fun getInstance(): FirebaseApp {
            val app = FIRApp.defaultApp()
                ?: throw FirebaseException.NotInitializedException(
                    "Default FirebaseApp is not initialized. Call FirebaseApp.initialize() first."
                )
            return FirebaseApp(app)
        }

        actual fun getInstance(name: String): FirebaseApp {
            if (name == DEFAULT_APP_NAME) {
                return getInstance()
            }
            val app = FIRApp.appNamed(name)
                ?: throw FirebaseException.NotInitializedException(
                    "FirebaseApp with name '$name' is not initialized"
                )
            return FirebaseApp(app)
        }

        actual val apps: List<FirebaseApp>
            get() = FIRApp.allApps()?.values?.mapNotNull {
                (it as? FIRApp)?.let { app -> FirebaseApp(app) }
            } ?: emptyList()
    }
}

/**
 * Convert FIROptions to FirebaseOptions.
 */
internal fun FIROptions.toCommon(): FirebaseOptions {
    return FirebaseOptions(
        apiKey = this.APIKey ?: "",
        applicationId = this.googleAppID,
        projectId = this.projectID ?: "",
        storageBucket = this.storageBucket,
        gcmSenderId = this.GCMSenderID,
        databaseUrl = this.databaseURL
    )
}

/**
 * Convert FirebaseOptions to FIROptions.
 */
internal fun FirebaseOptions.toIos(): FIROptions {
    return FIROptions(
        googleAppID = applicationId,
        GCMSenderID = gcmSenderId ?: ""
    ).apply {
        this.APIKey = this@toIos.apiKey
        this.projectID = this@toIos.projectId
        this.storageBucket = this@toIos.storageBucket
        this.databaseURL = this@toIos.databaseUrl
    }
}
