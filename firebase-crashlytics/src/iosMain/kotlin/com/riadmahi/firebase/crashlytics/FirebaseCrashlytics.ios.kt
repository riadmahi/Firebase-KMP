@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.riadmahi.firebase.crashlytics

import cocoapods.FirebaseCrashlytics.FIRCrashlytics
import cocoapods.FirebaseCrashlytics.FIRExceptionModel
import platform.Foundation.NSException
import platform.Foundation.NSNumber

/**
 * iOS implementation of FirebaseCrashlytics using Firebase iOS SDK.
 */
actual class FirebaseCrashlytics private constructor(
    private val ios: FIRCrashlytics
) {
    actual fun recordException(throwable: Throwable) {
        val exceptionModel = FIRExceptionModel(
            name = throwable::class.simpleName ?: "Exception",
            reason = throwable.message ?: "Unknown error"
        )
        // Add stack trace if available
        throwable.stackTraceToString().lines().take(20).forEachIndexed { index, line ->
            ios.log("[$index] $line")
        }
        ios.recordExceptionModel(exceptionModel)
    }

    actual fun log(message: String) {
        ios.log(message)
    }

    actual fun setCustomKey(key: String, value: String) {
        ios.setCustomValue(value, forKey = key)
    }

    actual fun setCustomKey(key: String, value: Boolean) {
        ios.setCustomValue(NSNumber(bool = value), forKey = key)
    }

    actual fun setCustomKey(key: String, value: Int) {
        ios.setCustomValue(NSNumber(int = value), forKey = key)
    }

    actual fun setCustomKey(key: String, value: Long) {
        ios.setCustomValue(NSNumber(longLong = value), forKey = key)
    }

    actual fun setCustomKey(key: String, value: Float) {
        ios.setCustomValue(NSNumber(float = value), forKey = key)
    }

    actual fun setCustomKey(key: String, value: Double) {
        ios.setCustomValue(NSNumber(double = value), forKey = key)
    }

    actual fun setCustomKeys(keysAndValues: Map<String, Any>) {
        val iosValues: Map<Any?, *> = keysAndValues.mapValues { (_, value) ->
            when (value) {
                is Int -> NSNumber(int = value)
                is Long -> NSNumber(longLong = value)
                is Float -> NSNumber(float = value)
                is Double -> NSNumber(double = value)
                is Boolean -> NSNumber(bool = value)
                else -> value
            }
        }
        ios.setCustomKeysAndValues(iosValues)
    }

    actual fun setUserId(userId: String?) {
        ios.setUserID(userId)
    }

    actual fun setCrashlyticsCollectionEnabled(enabled: Boolean) {
        ios.setCrashlyticsCollectionEnabled(enabled)
    }

    actual fun isCrashlyticsCollectionEnabled(): Boolean =
        ios.isCrashlyticsCollectionEnabled()

    actual fun didCrashOnPreviousExecution(): Boolean =
        ios.didCrashDuringPreviousExecution()

    actual fun sendUnsentReports() {
        ios.sendUnsentReports()
    }

    actual fun deleteUnsentReports() {
        ios.deleteUnsentReports()
    }

    actual companion object {
        actual fun getInstance(): FirebaseCrashlytics =
            FirebaseCrashlytics(FIRCrashlytics.crashlytics())
    }
}
