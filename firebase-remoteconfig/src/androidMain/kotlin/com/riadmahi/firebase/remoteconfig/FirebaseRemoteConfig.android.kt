package com.riadmahi.firebase.remoteconfig

import com.google.firebase.remoteconfig.FirebaseRemoteConfig as AndroidFirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.remoteconfig.FirebaseRemoteConfigValue as AndroidRemoteConfigValue
import com.riadmahi.firebase.core.FirebaseResult
import kotlinx.coroutines.tasks.await

/**
 * Android implementation of FirebaseRemoteConfig using Firebase Android SDK.
 */
actual class FirebaseRemoteConfig private constructor(
    private val android: AndroidFirebaseRemoteConfig
) {
    actual suspend fun fetch(): FirebaseResult<Unit> = safeConfigCall {
        android.fetch().await()
    }

    actual suspend fun fetch(minimumFetchIntervalInSeconds: Long): FirebaseResult<Unit> = safeConfigCall {
        android.fetch(minimumFetchIntervalInSeconds).await()
    }

    actual suspend fun activate(): FirebaseResult<Boolean> = safeConfigCall {
        android.activate().await()
    }

    actual suspend fun fetchAndActivate(): FirebaseResult<Boolean> = safeConfigCall {
        android.fetchAndActivate().await()
    }

    actual fun setDefaults(defaults: Map<String, Any>) {
        android.setDefaultsAsync(defaults)
    }

    actual fun getString(key: String): String =
        android.getString(key)

    actual fun getBoolean(key: String): Boolean =
        android.getBoolean(key)

    actual fun getLong(key: String): Long =
        android.getLong(key)

    actual fun getDouble(key: String): Double =
        android.getDouble(key)

    actual fun getAll(): Map<String, RemoteConfigValue> =
        android.all.mapValues { it.value.toCommon() }

    actual fun getValueSource(key: String): ValueSource =
        android.getValue(key).source.toCommon()

    actual fun setMinimumFetchInterval(intervalInSeconds: Long) {
        val settings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(intervalInSeconds)
            .build()
        android.setConfigSettingsAsync(settings)
    }

    actual suspend fun reset(): FirebaseResult<Unit> = safeConfigCall {
        android.reset().await()
    }

    actual companion object {
        actual fun getInstance(): FirebaseRemoteConfig =
            FirebaseRemoteConfig(AndroidFirebaseRemoteConfig.getInstance())
    }
}

/**
 * Safe wrapper for Remote Config operations.
 */
private suspend fun <T> safeConfigCall(block: suspend () -> T): FirebaseResult<T> {
    return try {
        FirebaseResult.Success(block())
    } catch (e: Exception) {
        FirebaseResult.Failure(e.toRemoteConfigException())
    }
}

/**
 * Convert Exception to RemoteConfigException.
 */
private fun Exception.toRemoteConfigException(): RemoteConfigException {
    val message = this.message ?: "Unknown error"
    return when {
        message.contains("FETCH_THROTTLED") || message.contains("throttle") ->
            RemoteConfigException.FetchThrottled(message, this)
        message.contains("FETCH_ERROR") || message.contains("network") ->
            RemoteConfigException.FetchFailed(message, this)
        message.contains("ACTIVATION") ->
            RemoteConfigException.ActivationFailed(message, this)
        else -> RemoteConfigException.Unknown(message, this)
    }
}

/**
 * Convert Android RemoteConfigValue to common RemoteConfigValue.
 */
private fun AndroidRemoteConfigValue.toCommon(): RemoteConfigValue {
    return RemoteConfigValue(
        asString = asString(),
        asBoolean = asBoolean(),
        asLong = asLong(),
        asDouble = asDouble(),
        source = source.toCommon()
    )
}

/**
 * Convert Android source to common ValueSource.
 */
private fun Int.toCommon(): ValueSource {
    return when (this) {
        AndroidFirebaseRemoteConfig.VALUE_SOURCE_DEFAULT -> ValueSource.DEFAULT
        AndroidFirebaseRemoteConfig.VALUE_SOURCE_REMOTE -> ValueSource.REMOTE
        else -> ValueSource.STATIC
    }
}
