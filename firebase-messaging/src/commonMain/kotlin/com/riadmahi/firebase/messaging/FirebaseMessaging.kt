package com.riadmahi.firebase.messaging

import com.riadmahi.firebase.core.FirebaseApp
import com.riadmahi.firebase.core.FirebaseResult
import kotlinx.coroutines.flow.Flow

/**
 * Firebase Cloud Messaging (FCM) for Kotlin Multiplatform.
 *
 * Firebase Cloud Messaging enables sending notifications and data messages
 * to Android and iOS devices.
 *
 * Example usage:
 * ```kotlin
 * val messaging = FirebaseMessaging.getInstance()
 *
 * // Get the FCM token
 * when (val result = messaging.getToken()) {
 *     is FirebaseResult.Success -> println("Token: ${result.data}")
 *     is FirebaseResult.Failure -> println("Error: ${result.exception}")
 * }
 *
 * // Subscribe to a topic
 * messaging.subscribeToTopic("news")
 *
 * // Listen to incoming messages
 * messaging.onMessage.collect { message ->
 *     println("Received: ${message.data}")
 * }
 * ```
 */
expect class FirebaseMessaging {
    /**
     * The FCM registration token for this device.
     * This token can change during the app's lifecycle.
     */
    suspend fun getToken(): FirebaseResult<String>

    /**
     * Deletes the FCM registration token for this device.
     * A new token will be generated on the next call to [getToken].
     */
    suspend fun deleteToken(): FirebaseResult<Unit>

    /**
     * Subscribes to a topic.
     * Messages sent to this topic will be received by this device.
     *
     * @param topic The topic name to subscribe to.
     */
    suspend fun subscribeToTopic(topic: String): FirebaseResult<Unit>

    /**
     * Unsubscribes from a topic.
     *
     * @param topic The topic name to unsubscribe from.
     */
    suspend fun unsubscribeFromTopic(topic: String): FirebaseResult<Unit>

    /**
     * Whether auto-initialization of FCM is enabled.
     * When disabled, FCM will not automatically generate tokens.
     */
    var isAutoInitEnabled: Boolean

    companion object {
        /**
         * Returns the [FirebaseMessaging] instance for the default [FirebaseApp].
         */
        fun getInstance(): FirebaseMessaging
    }
}

/**
 * Represents a remote message received from FCM.
 */
data class RemoteMessage(
    /**
     * The message ID, unique for each message.
     */
    val messageId: String?,

    /**
     * The message type (e.g., "gcm", "deleted_messages").
     */
    val messageType: String?,

    /**
     * The sender ID or the topic name.
     */
    val from: String?,

    /**
     * The destination of the message (the app's sender ID).
     */
    val to: String?,

    /**
     * The collapse key of the message.
     */
    val collapseKey: String?,

    /**
     * The data payload of the message.
     */
    val data: Map<String, String>,

    /**
     * The notification payload, if present.
     */
    val notification: Notification?,

    /**
     * The time in milliseconds when the message was sent.
     */
    val sentTime: Long,

    /**
     * The TTL (time to live) of the message in seconds.
     */
    val ttl: Int
) {
    /**
     * Represents the notification payload of a remote message.
     */
    data class Notification(
        /**
         * The notification title.
         */
        val title: String?,

        /**
         * The notification body.
         */
        val body: String?,

        /**
         * The notification icon (Android only).
         */
        val icon: String?,

        /**
         * The notification image URL.
         */
        val imageUrl: String?,

        /**
         * The notification sound.
         */
        val sound: String?,

        /**
         * The notification tag (Android only).
         */
        val tag: String?,

        /**
         * The notification click action.
         */
        val clickAction: String?,

        /**
         * The notification channel ID (Android only).
         */
        val channelId: String?,

        /**
         * The notification badge count (iOS only).
         */
        val badge: String?
    )
}

/**
 * Exception thrown by Firebase Messaging operations.
 */
sealed class MessagingException(
    message: String,
    cause: Throwable? = null
) : com.riadmahi.firebase.core.FirebaseException(message, cause) {

    /**
     * The token could not be retrieved.
     */
    class TokenRetrievalFailed(message: String, cause: Throwable? = null) :
        MessagingException(message, cause)

    /**
     * The token could not be deleted.
     */
    class TokenDeletionFailed(message: String, cause: Throwable? = null) :
        MessagingException(message, cause)

    /**
     * Topic subscription/unsubscription failed.
     */
    class TopicOperationFailed(message: String, cause: Throwable? = null) :
        MessagingException(message, cause)

    /**
     * The device is not registered for push notifications.
     */
    class NotRegistered(message: String, cause: Throwable? = null) :
        MessagingException(message, cause)

    /**
     * An unknown error occurred.
     */
    class Unknown(message: String, cause: Throwable? = null) :
        MessagingException(message, cause)
}
