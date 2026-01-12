package com.riadmahi.firebase.messaging

import com.google.firebase.messaging.FirebaseMessaging as AndroidFirebaseMessaging
import com.riadmahi.firebase.core.FirebaseResult
import kotlinx.coroutines.tasks.await

/**
 * Android implementation of FirebaseMessaging using Firebase Android SDK.
 */
actual class FirebaseMessaging private constructor(
    private val android: AndroidFirebaseMessaging
) {
    actual suspend fun getToken(): FirebaseResult<String> = safeMessagingCall {
        android.token.await()
    }

    actual suspend fun deleteToken(): FirebaseResult<Unit> = safeMessagingCall {
        android.deleteToken().await()
    }

    actual suspend fun subscribeToTopic(topic: String): FirebaseResult<Unit> = safeMessagingCall {
        android.subscribeToTopic(topic).await()
    }

    actual suspend fun unsubscribeFromTopic(topic: String): FirebaseResult<Unit> = safeMessagingCall {
        android.unsubscribeFromTopic(topic).await()
    }

    actual var isAutoInitEnabled: Boolean
        get() = android.isAutoInitEnabled
        set(value) { android.isAutoInitEnabled = value }

    actual companion object {
        actual fun getInstance(): FirebaseMessaging =
            FirebaseMessaging(AndroidFirebaseMessaging.getInstance())
    }
}

/**
 * Safe wrapper for Messaging operations.
 */
private suspend fun <T> safeMessagingCall(block: suspend () -> T): FirebaseResult<T> {
    return try {
        FirebaseResult.Success(block())
    } catch (e: Exception) {
        FirebaseResult.Failure(e.toMessagingException())
    }
}

/**
 * Convert Exception to MessagingException.
 */
private fun Exception.toMessagingException(): MessagingException {
    val message = this.message ?: "Unknown error"
    return when {
        message.contains("TOKEN") || message.contains("token") ->
            MessagingException.TokenRetrievalFailed(message, this)
        message.contains("NOT_REGISTERED") || message.contains("unregistered") ->
            MessagingException.NotRegistered(message, this)
        message.contains("TOPIC") || message.contains("topic") ->
            MessagingException.TopicOperationFailed(message, this)
        else -> MessagingException.Unknown(message, this)
    }
}

/**
 * Extension to convert Android RemoteMessage to common RemoteMessage.
 */
fun com.google.firebase.messaging.RemoteMessage.toCommon(): RemoteMessage {
    return RemoteMessage(
        messageId = messageId,
        messageType = messageType,
        from = from,
        to = to,
        collapseKey = collapseKey,
        data = data,
        notification = notification?.toCommon(),
        sentTime = sentTime,
        ttl = ttl
    )
}

/**
 * Extension to convert Android Notification to common Notification.
 */
private fun com.google.firebase.messaging.RemoteMessage.Notification.toCommon(): RemoteMessage.Notification {
    return RemoteMessage.Notification(
        title = title,
        body = body,
        icon = icon,
        imageUrl = imageUrl?.toString(),
        sound = sound,
        tag = tag,
        clickAction = clickAction,
        channelId = channelId,
        badge = null // Android doesn't have badge
    )
}
