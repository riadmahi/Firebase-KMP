@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.riadmahi.firebase.remoteconfig

import cocoapods.FirebaseRemoteConfig.FIRRemoteConfig
import cocoapods.FirebaseRemoteConfig.FIRRemoteConfigFetchAndActivateStatus
import cocoapods.FirebaseRemoteConfig.FIRRemoteConfigSettings
import cocoapods.FirebaseRemoteConfig.FIRRemoteConfigValue
import cocoapods.FirebaseRemoteConfig.FIRRemoteConfigSource
import com.riadmahi.firebase.core.FirebaseResult
import platform.Foundation.NSNumber
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * iOS implementation of FirebaseRemoteConfig using Firebase iOS SDK.
 */
actual class FirebaseRemoteConfig private constructor(
    private val ios: FIRRemoteConfig
) {
    actual suspend fun fetch(): FirebaseResult<Unit> = suspendCoroutine { continuation ->
        ios.fetchWithCompletionHandler { status, error ->
            if (error != null) {
                continuation.resume(FirebaseResult.Failure(error.toRemoteConfigException()))
            } else {
                continuation.resume(FirebaseResult.Success(Unit))
            }
        }
    }

    actual suspend fun fetch(minimumFetchIntervalInSeconds: Long): FirebaseResult<Unit> = suspendCoroutine { continuation ->
        ios.fetchWithExpirationDuration(minimumFetchIntervalInSeconds.toDouble()) { status, error ->
            if (error != null) {
                continuation.resume(FirebaseResult.Failure(error.toRemoteConfigException()))
            } else {
                continuation.resume(FirebaseResult.Success(Unit))
            }
        }
    }

    actual suspend fun activate(): FirebaseResult<Boolean> = suspendCoroutine { continuation ->
        ios.activateWithCompletion { changed, error ->
            if (error != null) {
                continuation.resume(FirebaseResult.Failure(error.toRemoteConfigException()))
            } else {
                continuation.resume(FirebaseResult.Success(changed))
            }
        }
    }

    actual suspend fun fetchAndActivate(): FirebaseResult<Boolean> = suspendCoroutine { continuation ->
        ios.fetchAndActivateWithCompletionHandler { status, error ->
            if (error != null) {
                continuation.resume(FirebaseResult.Failure(error.toRemoteConfigException()))
            } else {
                // Check if config was fetched from remote
                val fetchedFromRemote = status == FIRRemoteConfigFetchAndActivateStatus.FIRRemoteConfigFetchAndActivateStatusSuccessFetchedFromRemote
                continuation.resume(FirebaseResult.Success(fetchedFromRemote))
            }
        }
    }

    actual fun setDefaults(defaults: Map<String, Any>) {
        val iosDefaults: Map<Any?, *> = defaults.mapValues { (_, value) ->
            when (value) {
                is Int -> NSNumber(int = value)
                is Long -> NSNumber(longLong = value)
                is Float -> NSNumber(float = value)
                is Double -> NSNumber(double = value)
                is Boolean -> NSNumber(bool = value)
                else -> value
            }
        }
        ios.setDefaults(iosDefaults)
    }

    actual fun getString(key: String): String =
        ios.configValueForKey(key).stringValue ?: ""

    actual fun getBoolean(key: String): Boolean =
        ios.configValueForKey(key).boolValue

    actual fun getLong(key: String): Long =
        ios.configValueForKey(key).numberValue?.longLongValue ?: 0L

    actual fun getDouble(key: String): Double =
        ios.configValueForKey(key).numberValue?.doubleValue ?: 0.0

    @Suppress("UNCHECKED_CAST")
    actual fun getAll(): Map<String, RemoteConfigValue> {
        val keys = ios.allKeysFromSource(FIRRemoteConfigSource.FIRRemoteConfigSourceRemote) as? List<String> ?: emptyList()
        return keys.associateWith { key ->
            ios.configValueForKey(key).toCommon()
        }
    }

    actual fun getValueSource(key: String): ValueSource {
        val value = ios.configValueForKey(key)
        return value.source.toCommon()
    }

    actual fun setMinimumFetchInterval(intervalInSeconds: Long) {
        val settings = FIRRemoteConfigSettings()
        settings.minimumFetchInterval = intervalInSeconds.toDouble()
        ios.configSettings = settings
    }

    actual suspend fun reset(): FirebaseResult<Unit> {
        // iOS doesn't have a direct reset method, but we can clear defaults
        val emptyDefaults: Map<Any?, *> = emptyMap<Any?, Any?>()
        ios.setDefaults(emptyDefaults)
        return FirebaseResult.Success(Unit)
    }

    actual companion object {
        actual fun getInstance(): FirebaseRemoteConfig =
            FirebaseRemoteConfig(FIRRemoteConfig.remoteConfig())
    }
}

/**
 * Convert NSError to RemoteConfigException.
 */
private fun platform.Foundation.NSError.toRemoteConfigException(): RemoteConfigException {
    val message = localizedDescription
    val code = code.toInt()

    return when (code) {
        8003 -> RemoteConfigException.FetchThrottled(message)
        8002 -> RemoteConfigException.FetchFailed(message)
        else -> RemoteConfigException.Unknown(message)
    }
}

/**
 * Convert iOS source to common ValueSource.
 */
private fun FIRRemoteConfigSource.toCommon(): ValueSource {
    return when (this) {
        FIRRemoteConfigSource.FIRRemoteConfigSourceDefault -> ValueSource.DEFAULT
        FIRRemoteConfigSource.FIRRemoteConfigSourceRemote -> ValueSource.REMOTE
        FIRRemoteConfigSource.FIRRemoteConfigSourceStatic -> ValueSource.STATIC
        else -> ValueSource.STATIC
    }
}

/**
 * Convert FIRRemoteConfigValue to common RemoteConfigValue.
 */
private fun FIRRemoteConfigValue.toCommon(): RemoteConfigValue {
    return RemoteConfigValue(
        asString = stringValue ?: "",
        asBoolean = boolValue,
        asLong = numberValue?.longLongValue ?: 0L,
        asDouble = numberValue?.doubleValue ?: 0.0,
        source = source.toCommon()
    )
}
