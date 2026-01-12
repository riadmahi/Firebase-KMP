package com.riadmahi.firebase.remoteconfig

import com.riadmahi.firebase.core.FirebaseApp
import com.riadmahi.firebase.core.FirebaseResult

/**
 * Firebase Remote Config for Kotlin Multiplatform.
 *
 * Firebase Remote Config lets you change the behavior and appearance of your app
 * without publishing an app update.
 *
 * Example usage:
 * ```kotlin
 * val config = FirebaseRemoteConfig.getInstance()
 *
 * // Set default values
 * config.setDefaults(mapOf(
 *     "welcome_message" to "Hello!",
 *     "feature_enabled" to false,
 *     "max_items" to 10
 * ))
 *
 * // Fetch and activate
 * config.fetchAndActivate()
 *
 * // Get values
 * val message = config.getString("welcome_message")
 * val enabled = config.getBoolean("feature_enabled")
 * val maxItems = config.getLong("max_items")
 * ```
 */
expect class FirebaseRemoteConfig {
    /**
     * Fetches config from the server.
     *
     * @return Success if fetch completed, Failure otherwise.
     */
    suspend fun fetch(): FirebaseResult<Unit>

    /**
     * Fetches config with a minimum fetch interval.
     *
     * @param minimumFetchIntervalInSeconds The minimum interval between fetches.
     */
    suspend fun fetch(minimumFetchIntervalInSeconds: Long): FirebaseResult<Unit>

    /**
     * Activates the most recently fetched config.
     *
     * @return True if the config was activated, false otherwise.
     */
    suspend fun activate(): FirebaseResult<Boolean>

    /**
     * Fetches and activates config in one call.
     *
     * @return True if a new config was fetched and activated.
     */
    suspend fun fetchAndActivate(): FirebaseResult<Boolean>

    /**
     * Sets default config values.
     *
     * @param defaults A map of default values.
     */
    fun setDefaults(defaults: Map<String, Any>)

    /**
     * Gets a string value for the given key.
     *
     * @param key The config key.
     * @return The config value, or empty string if not found.
     */
    fun getString(key: String): String

    /**
     * Gets a boolean value for the given key.
     *
     * @param key The config key.
     * @return The config value, or false if not found.
     */
    fun getBoolean(key: String): Boolean

    /**
     * Gets a long value for the given key.
     *
     * @param key The config key.
     * @return The config value, or 0 if not found.
     */
    fun getLong(key: String): Long

    /**
     * Gets a double value for the given key.
     *
     * @param key The config key.
     * @return The config value, or 0.0 if not found.
     */
    fun getDouble(key: String): Double

    /**
     * Gets all config values as a map.
     *
     * @return A map of all config keys to their values.
     */
    fun getAll(): Map<String, RemoteConfigValue>

    /**
     * Gets the value source for a key.
     *
     * @param key The config key.
     * @return The source of the value.
     */
    fun getValueSource(key: String): ValueSource

    /**
     * Sets the minimum fetch interval for development.
     *
     * @param intervalInSeconds The minimum interval in seconds.
     */
    fun setMinimumFetchInterval(intervalInSeconds: Long)

    /**
     * Resets all fetched config values to defaults.
     */
    suspend fun reset(): FirebaseResult<Unit>

    companion object {
        /**
         * Returns the [FirebaseRemoteConfig] instance for the default [FirebaseApp].
         */
        fun getInstance(): FirebaseRemoteConfig
    }
}

/**
 * Represents a Remote Config value.
 */
data class RemoteConfigValue(
    /**
     * The value as a string.
     */
    val asString: String,

    /**
     * The value as a boolean.
     */
    val asBoolean: Boolean,

    /**
     * The value as a long.
     */
    val asLong: Long,

    /**
     * The value as a double.
     */
    val asDouble: Double,

    /**
     * The source of this value.
     */
    val source: ValueSource
)

/**
 * The source of a Remote Config value.
 */
enum class ValueSource {
    /**
     * The value was defined in the app's default config.
     */
    DEFAULT,

    /**
     * The value was fetched from the server.
     */
    REMOTE,

    /**
     * No value was found (using static default).
     */
    STATIC
}

/**
 * Exception thrown by Remote Config operations.
 */
sealed class RemoteConfigException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {

    /**
     * Fetch failed due to throttling.
     */
    class FetchThrottled(message: String, cause: Throwable? = null) :
        RemoteConfigException(message, cause)

    /**
     * Fetch failed due to network error.
     */
    class FetchFailed(message: String, cause: Throwable? = null) :
        RemoteConfigException(message, cause)

    /**
     * Config activation failed.
     */
    class ActivationFailed(message: String, cause: Throwable? = null) :
        RemoteConfigException(message, cause)

    /**
     * An unknown error occurred.
     */
    class Unknown(message: String, cause: Throwable? = null) :
        RemoteConfigException(message, cause)
}
