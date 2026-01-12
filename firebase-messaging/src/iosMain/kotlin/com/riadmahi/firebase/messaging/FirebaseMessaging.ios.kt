@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.riadmahi.firebase.messaging

import cocoapods.FirebaseMessaging.FIRMessaging
import com.riadmahi.firebase.core.FirebaseResult
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import platform.Foundation.NSError

/**
 * iOS implementation of FirebaseMessaging using Firebase iOS SDK.
 */
actual class FirebaseMessaging private constructor(
    private val ios: FIRMessaging
) {
    actual suspend fun getToken(): FirebaseResult<String> = suspendCoroutine { continuation ->
        ios.tokenWithCompletion { token, error ->
            if (error != null) {
                continuation.resume(FirebaseResult.Failure(error.toMessagingException()))
            } else if (token != null) {
                continuation.resume(FirebaseResult.Success(token))
            } else {
                continuation.resume(FirebaseResult.Failure(
                    MessagingException.TokenRetrievalFailed("No token returned")
                ))
            }
        }
    }

    actual suspend fun deleteToken(): FirebaseResult<Unit> = suspendCoroutine { continuation ->
        ios.deleteTokenWithCompletion { error ->
            if (error != null) {
                continuation.resume(FirebaseResult.Failure(error.toMessagingException()))
            } else {
                continuation.resume(FirebaseResult.Success(Unit))
            }
        }
    }

    actual suspend fun subscribeToTopic(topic: String): FirebaseResult<Unit> = suspendCoroutine { continuation ->
        ios.subscribeToTopic(topic) { error ->
            if (error != null) {
                continuation.resume(FirebaseResult.Failure(error.toMessagingException()))
            } else {
                continuation.resume(FirebaseResult.Success(Unit))
            }
        }
    }

    actual suspend fun unsubscribeFromTopic(topic: String): FirebaseResult<Unit> = suspendCoroutine { continuation ->
        ios.unsubscribeFromTopic(topic) { error ->
            if (error != null) {
                continuation.resume(FirebaseResult.Failure(error.toMessagingException()))
            } else {
                continuation.resume(FirebaseResult.Success(Unit))
            }
        }
    }

    actual var isAutoInitEnabled: Boolean
        get() = ios.autoInitEnabled
        set(value) { ios.autoInitEnabled = value }

    actual companion object {
        actual fun getInstance(): FirebaseMessaging =
            FirebaseMessaging(FIRMessaging.messaging())
    }
}

/**
 * Convert NSError to MessagingException.
 */
private fun NSError.toMessagingException(): MessagingException {
    val message = localizedDescription
    val code = code.toInt()

    return when (code) {
        // FIRMessagingErrorCodeUnknown
        0 -> MessagingException.Unknown(message)
        // FIRMessagingErrorCodeAuthentication
        1 -> MessagingException.NotRegistered(message)
        // FIRMessagingErrorCodeNoAccess
        2 -> MessagingException.NotRegistered(message)
        // FIRMessagingErrorCodeTimeout
        3 -> MessagingException.TokenRetrievalFailed(message)
        // FIRMessagingErrorCodeNetwork
        4 -> MessagingException.TokenRetrievalFailed(message)
        // FIRMessagingErrorCodeOperationInProgress
        5 -> MessagingException.TokenRetrievalFailed(message)
        // FIRMessagingErrorCodeInvalidRequest
        6 -> MessagingException.TopicOperationFailed(message)
        else -> MessagingException.Unknown(message)
    }
}
