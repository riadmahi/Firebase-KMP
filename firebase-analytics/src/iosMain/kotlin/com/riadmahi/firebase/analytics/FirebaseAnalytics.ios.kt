@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.riadmahi.firebase.analytics

import cocoapods.FirebaseAnalytics.FIRAnalytics
import platform.Foundation.NSNumber

/**
 * iOS implementation of FirebaseAnalytics using Firebase iOS SDK.
 */
actual class FirebaseAnalytics private constructor() {
    actual fun logEvent(name: String, params: Map<String, Any>?) {
        val iosParams = params?.mapValues { (_, value) ->
            when (value) {
                is Int -> NSNumber(int = value)
                is Long -> NSNumber(longLong = value)
                is Float -> NSNumber(float = value)
                is Double -> NSNumber(double = value)
                is Boolean -> NSNumber(bool = value)
                else -> value
            }
        }
        FIRAnalytics.logEventWithName(name, parameters = iosParams)
    }

    actual fun setUserProperty(name: String, value: String?) {
        FIRAnalytics.setUserPropertyString(value, forName = name)
    }

    actual fun setUserId(userId: String?) {
        FIRAnalytics.setUserID(userId)
    }

    actual fun setAnalyticsCollectionEnabled(enabled: Boolean) {
        FIRAnalytics.setAnalyticsCollectionEnabled(enabled)
    }

    actual fun resetAnalyticsData() {
        FIRAnalytics.resetAnalyticsData()
    }

    actual fun setCurrentScreen(screenName: String, screenClass: String?) {
        val params = mutableMapOf<String, Any>(
            "screen_name" to screenName
        )
        screenClass?.let { params["screen_class"] = it }
        logEvent("screen_view", params)
    }

    actual fun setDefaultEventParameters(params: Map<String, Any>?) {
        val iosParams = params?.mapValues { (_, value) ->
            when (value) {
                is Int -> NSNumber(int = value)
                is Long -> NSNumber(longLong = value)
                is Float -> NSNumber(float = value)
                is Double -> NSNumber(double = value)
                is Boolean -> NSNumber(bool = value)
                else -> value
            }
        }
        FIRAnalytics.setDefaultEventParameters(iosParams)
    }

    actual fun getAppInstanceId(callback: (String?) -> Unit) {
        FIRAnalytics.appInstanceID { instanceId ->
            callback(instanceId)
        }
    }

    actual fun setSessionTimeoutDuration(milliseconds: Long) {
        FIRAnalytics.setSessionTimeoutInterval(milliseconds / 1000.0)
    }

    actual companion object {
        private val instance = FirebaseAnalytics()

        actual fun getInstance(): FirebaseAnalytics = instance
    }
}
