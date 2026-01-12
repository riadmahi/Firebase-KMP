package com.riadmahi.firebase.crashlytics

import com.google.firebase.crashlytics.FirebaseCrashlytics as AndroidFirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.crashlytics.setCustomKeys
import com.google.firebase.ktx.Firebase

/**
 * Android implementation of FirebaseCrashlytics using Firebase Android SDK.
 */
actual class FirebaseCrashlytics private constructor(
    private val android: AndroidFirebaseCrashlytics
) {
    actual fun recordException(throwable: Throwable) {
        android.recordException(throwable)
    }

    actual fun log(message: String) {
        android.log(message)
    }

    actual fun setCustomKey(key: String, value: String) {
        android.setCustomKey(key, value)
    }

    actual fun setCustomKey(key: String, value: Boolean) {
        android.setCustomKey(key, value)
    }

    actual fun setCustomKey(key: String, value: Int) {
        android.setCustomKey(key, value)
    }

    actual fun setCustomKey(key: String, value: Long) {
        android.setCustomKey(key, value)
    }

    actual fun setCustomKey(key: String, value: Float) {
        android.setCustomKey(key, value)
    }

    actual fun setCustomKey(key: String, value: Double) {
        android.setCustomKey(key, value)
    }

    actual fun setCustomKeys(keysAndValues: Map<String, Any>) {
        android.setCustomKeys {
            for ((key, value) in keysAndValues) {
                when (value) {
                    is String -> key(key, value)
                    is Boolean -> key(key, value)
                    is Int -> key(key, value)
                    is Long -> key(key, value)
                    is Float -> key(key, value)
                    is Double -> key(key, value)
                }
            }
        }
    }

    actual fun setUserId(userId: String?) {
        android.setUserId(userId ?: "")
    }

    actual fun setCrashlyticsCollectionEnabled(enabled: Boolean) {
        android.setCrashlyticsCollectionEnabled(enabled)
    }

    actual fun isCrashlyticsCollectionEnabled(): Boolean =
        android.isCrashlyticsCollectionEnabled

    actual fun didCrashOnPreviousExecution(): Boolean =
        android.didCrashOnPreviousExecution()

    actual fun sendUnsentReports() {
        android.sendUnsentReports()
    }

    actual fun deleteUnsentReports() {
        android.deleteUnsentReports()
    }

    actual companion object {
        actual fun getInstance(): FirebaseCrashlytics =
            FirebaseCrashlytics(Firebase.crashlytics)
    }
}
