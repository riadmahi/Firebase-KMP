package com.riadmahi.firebase.crashlytics

import com.riadmahi.firebase.core.FirebaseApp

/**
 * Firebase Crashlytics for Kotlin Multiplatform.
 *
 * Firebase Crashlytics is a lightweight, realtime crash reporter that helps you
 * track, prioritize, and fix stability issues.
 *
 * Example usage:
 * ```kotlin
 * val crashlytics = FirebaseCrashlytics.getInstance()
 *
 * // Log a non-fatal exception
 * try {
 *     riskyOperation()
 * } catch (e: Exception) {
 *     crashlytics.recordException(e)
 * }
 *
 * // Add context with custom keys
 * crashlytics.setCustomKey("user_level", 42)
 * crashlytics.setCustomKey("is_premium", true)
 *
 * // Add breadcrumbs
 * crashlytics.log("User clicked checkout button")
 *
 * // Set user identifier
 * crashlytics.setUserId("user_12345")
 *
 * // Force a crash (for testing)
 * crashlytics.crash()
 * ```
 */
expect class FirebaseCrashlytics {
    /**
     * Records a non-fatal exception.
     *
     * @param throwable The exception to record.
     */
    fun recordException(throwable: Throwable)

    /**
     * Logs a message that will be included in the next crash report.
     *
     * @param message The message to log.
     */
    fun log(message: String)

    /**
     * Sets a custom key-value pair that will be included in crash reports.
     *
     * @param key The key name.
     * @param value The string value.
     */
    fun setCustomKey(key: String, value: String)

    /**
     * Sets a custom key-value pair that will be included in crash reports.
     *
     * @param key The key name.
     * @param value The boolean value.
     */
    fun setCustomKey(key: String, value: Boolean)

    /**
     * Sets a custom key-value pair that will be included in crash reports.
     *
     * @param key The key name.
     * @param value The int value.
     */
    fun setCustomKey(key: String, value: Int)

    /**
     * Sets a custom key-value pair that will be included in crash reports.
     *
     * @param key The key name.
     * @param value The long value.
     */
    fun setCustomKey(key: String, value: Long)

    /**
     * Sets a custom key-value pair that will be included in crash reports.
     *
     * @param key The key name.
     * @param value The float value.
     */
    fun setCustomKey(key: String, value: Float)

    /**
     * Sets a custom key-value pair that will be included in crash reports.
     *
     * @param key The key name.
     * @param value The double value.
     */
    fun setCustomKey(key: String, value: Double)

    /**
     * Sets multiple custom keys at once.
     *
     * @param keysAndValues A map of keys to values.
     */
    fun setCustomKeys(keysAndValues: Map<String, Any>)

    /**
     * Sets a user identifier that will be included in crash reports.
     *
     * @param userId The user identifier, or null to clear it.
     */
    fun setUserId(userId: String?)

    /**
     * Enables or disables automatic data collection.
     *
     * @param enabled Whether to enable collection.
     */
    fun setCrashlyticsCollectionEnabled(enabled: Boolean)

    /**
     * Returns whether Crashlytics collection is enabled.
     */
    fun isCrashlyticsCollectionEnabled(): Boolean

    /**
     * Checks whether the app crashed during the previous run.
     *
     * @return True if the app crashed, false otherwise.
     */
    fun didCrashOnPreviousExecution(): Boolean

    /**
     * Sends all unsent reports immediately.
     */
    fun sendUnsentReports()

    /**
     * Deletes any unsent reports.
     */
    fun deleteUnsentReports()

    companion object {
        /**
         * Returns the [FirebaseCrashlytics] instance for the default [FirebaseApp].
         */
        fun getInstance(): FirebaseCrashlytics
    }
}
