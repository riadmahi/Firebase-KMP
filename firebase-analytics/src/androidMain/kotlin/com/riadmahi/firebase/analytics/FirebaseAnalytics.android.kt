package com.riadmahi.firebase.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics as AndroidFirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

/**
 * Android implementation of FirebaseAnalytics using Firebase Android SDK.
 */
actual class FirebaseAnalytics private constructor(
    private val android: AndroidFirebaseAnalytics
) {
    actual fun logEvent(name: String, params: Map<String, Any>?) {
        val bundle = params?.toBundle()
        android.logEvent(name, bundle)
    }

    actual fun setUserProperty(name: String, value: String?) {
        android.setUserProperty(name, value)
    }

    actual fun setUserId(userId: String?) {
        android.setUserId(userId)
    }

    actual fun setAnalyticsCollectionEnabled(enabled: Boolean) {
        android.setAnalyticsCollectionEnabled(enabled)
    }

    actual fun resetAnalyticsData() {
        android.resetAnalyticsData()
    }

    actual fun setCurrentScreen(screenName: String, screenClass: String?) {
        val params = Bundle().apply {
            putString(AndroidFirebaseAnalytics.Param.SCREEN_NAME, screenName)
            screenClass?.let { putString(AndroidFirebaseAnalytics.Param.SCREEN_CLASS, it) }
        }
        android.logEvent(AndroidFirebaseAnalytics.Event.SCREEN_VIEW, params)
    }

    actual fun setDefaultEventParameters(params: Map<String, Any>?) {
        android.setDefaultEventParameters(params?.toBundle())
    }

    actual fun getAppInstanceId(callback: (String?) -> Unit) {
        android.appInstanceId.addOnSuccessListener { callback(it) }
            .addOnFailureListener { callback(null) }
    }

    actual fun setSessionTimeoutDuration(milliseconds: Long) {
        android.setSessionTimeoutDuration(milliseconds)
    }

    actual companion object {
        actual fun getInstance(): FirebaseAnalytics =
            FirebaseAnalytics(Firebase.analytics)
    }
}

/**
 * Convert a Map to a Bundle for Analytics.
 */
private fun Map<String, Any>.toBundle(): Bundle {
    return Bundle().apply {
        for ((key, value) in this@toBundle) {
            when (value) {
                is String -> putString(key, value)
                is Int -> putLong(key, value.toLong())
                is Long -> putLong(key, value)
                is Float -> putDouble(key, value.toDouble())
                is Double -> putDouble(key, value)
                is Boolean -> putBoolean(key, value)
                is Bundle -> putBundle(key, value)
                is Array<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val bundles = (value as? Array<Bundle>)
                    bundles?.let { putParcelableArray(key, it) }
                }
                is ArrayList<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val bundles = (value as? ArrayList<Bundle>)
                    bundles?.let { putParcelableArrayList(key, it) }
                }
            }
        }
    }
}
