package com.riadmahi.firebase.demo

/**
 * Utility functions for time operations in the demo app.
 */
expect object TimeUtils {
    /**
     * Get current time in milliseconds since epoch.
     */
    fun currentTimeMillis(): Long

    /**
     * Get current time as ISO-8601 string.
     */
    fun currentTimeString(): String
}
