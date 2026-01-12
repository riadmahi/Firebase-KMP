package com.riadmahi.firebase.core

import android.content.Context
import com.google.firebase.FirebaseApp as AndroidFirebaseApp
import com.google.firebase.FirebaseOptions as AndroidFirebaseOptions

/**
 * Application context holder for Firebase initialization.
 */
object FirebaseContext {
    private var _applicationContext: Context? = null

    /**
     * Returns the application context, or null if not set.
     */
    val applicationContext: Context?
        get() = _applicationContext

    /**
     * Sets the application context for Firebase.
     * Must be called before any Firebase initialization.
     */
    fun setContext(context: Context) {
        _applicationContext = context.applicationContext
    }
}

/**
 * Extension function to initialize Firebase with a context.
 */
fun FirebaseApp.Companion.initialize(context: Context): FirebaseApp {
    FirebaseContext.setContext(context)
    return initialize()
}

/**
 * Extension function to initialize Firebase with a context and options.
 */
fun FirebaseApp.Companion.initialize(context: Context, options: FirebaseOptions): FirebaseApp {
    FirebaseContext.setContext(context)
    return initialize(options)
}

/**
 * Extension function to initialize Firebase with a context, options, and name.
 */
fun FirebaseApp.Companion.initialize(context: Context, options: FirebaseOptions, name: String): FirebaseApp {
    FirebaseContext.setContext(context)
    return initialize(options, name)
}

actual class FirebaseApp private constructor(
    val android: AndroidFirebaseApp
) {
    actual val name: String
        get() = android.name

    actual val options: FirebaseOptions
        get() = android.options.toCommon()

    actual fun delete() {
        android.delete()
    }

    actual companion object {
        actual val DEFAULT_APP_NAME: String = AndroidFirebaseApp.DEFAULT_APP_NAME

        actual fun initialize(): FirebaseApp {
            val context = FirebaseContext.applicationContext
                ?: throw FirebaseException.InitializationException(
                    "Context not set. Call FirebaseContext.setContext(context) or " +
                            "FirebaseApp.initialize(context) first."
                )

            val app = AndroidFirebaseApp.initializeApp(context)
                ?: throw FirebaseException.InitializationException(
                    "Failed to initialize Firebase. Make sure google-services.json is configured."
                )

            return FirebaseApp(app)
        }

        actual fun initialize(options: FirebaseOptions): FirebaseApp {
            val context = FirebaseContext.applicationContext
                ?: throw FirebaseException.InitializationException(
                    "Context not set. Call FirebaseContext.setContext(context) first."
                )

            val app = AndroidFirebaseApp.initializeApp(context, options.toAndroid())
            return FirebaseApp(app)
        }

        actual fun initialize(options: FirebaseOptions, name: String): FirebaseApp {
            val context = FirebaseContext.applicationContext
                ?: throw FirebaseException.InitializationException(
                    "Context not set. Call FirebaseContext.setContext(context) first."
                )

            val app = AndroidFirebaseApp.initializeApp(context, options.toAndroid(), name)
            return FirebaseApp(app)
        }

        actual fun getInstance(): FirebaseApp {
            return try {
                FirebaseApp(AndroidFirebaseApp.getInstance())
            } catch (e: IllegalStateException) {
                throw FirebaseException.NotInitializedException(
                    "Default FirebaseApp is not initialized",
                    e
                )
            }
        }

        actual fun getInstance(name: String): FirebaseApp {
            return try {
                FirebaseApp(AndroidFirebaseApp.getInstance(name))
            } catch (e: IllegalStateException) {
                throw FirebaseException.NotInitializedException(
                    "FirebaseApp with name '$name' is not initialized",
                    e
                )
            }
        }

        actual val apps: List<FirebaseApp>
            get() {
                val context = FirebaseContext.applicationContext ?: return emptyList()
                return AndroidFirebaseApp.getApps(context).map { FirebaseApp(it) }
            }
    }
}

private fun FirebaseOptions.toAndroid(): AndroidFirebaseOptions {
    return AndroidFirebaseOptions.Builder()
        .setApiKey(apiKey)
        .setApplicationId(applicationId)
        .setProjectId(projectId)
        .apply {
            storageBucket?.let { setStorageBucket(it) }
            gcmSenderId?.let { setGcmSenderId(it) }
            databaseUrl?.let { setDatabaseUrl(it) }
        }
        .build()
}

private fun AndroidFirebaseOptions.toCommon(): FirebaseOptions {
    return FirebaseOptions(
        apiKey = apiKey,
        applicationId = applicationId,
        projectId = projectId ?: "",
        storageBucket = storageBucket,
        gcmSenderId = gcmSenderId,
        databaseUrl = databaseUrl
    )
}
