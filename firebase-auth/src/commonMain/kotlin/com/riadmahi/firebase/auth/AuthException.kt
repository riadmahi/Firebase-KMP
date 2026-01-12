package com.riadmahi.firebase.auth

import com.riadmahi.firebase.core.FirebaseException

/**
 * Exception thrown by Firebase Authentication operations.
 */
sealed class AuthException(
    message: String,
    val code: AuthErrorCode,
    cause: Throwable? = null
) : FirebaseException(message, cause) {

    class InvalidCredential(message: String = "Invalid credential", cause: Throwable? = null)
        : AuthException(message, AuthErrorCode.INVALID_CREDENTIAL, cause)

    class UserNotFound(message: String = "User not found", cause: Throwable? = null)
        : AuthException(message, AuthErrorCode.USER_NOT_FOUND, cause)

    class WrongPassword(message: String = "Wrong password", cause: Throwable? = null)
        : AuthException(message, AuthErrorCode.WRONG_PASSWORD, cause)

    class EmailAlreadyInUse(message: String = "Email already in use", cause: Throwable? = null)
        : AuthException(message, AuthErrorCode.EMAIL_ALREADY_IN_USE, cause)

    class WeakPassword(message: String = "Password is too weak", cause: Throwable? = null)
        : AuthException(message, AuthErrorCode.WEAK_PASSWORD, cause)

    class InvalidEmail(message: String = "Invalid email address", cause: Throwable? = null)
        : AuthException(message, AuthErrorCode.INVALID_EMAIL, cause)

    class TooManyRequests(message: String = "Too many requests", cause: Throwable? = null)
        : AuthException(message, AuthErrorCode.TOO_MANY_REQUESTS, cause)

    class UserDisabled(message: String = "User has been disabled", cause: Throwable? = null)
        : AuthException(message, AuthErrorCode.USER_DISABLED, cause)

    class RequiresRecentLogin(message: String = "This operation requires recent authentication", cause: Throwable? = null)
        : AuthException(message, AuthErrorCode.REQUIRES_RECENT_LOGIN, cause)

    class CredentialAlreadyInUse(message: String = "Credential already in use by another account", cause: Throwable? = null)
        : AuthException(message, AuthErrorCode.CREDENTIAL_ALREADY_IN_USE, cause)

    class OperationNotAllowed(message: String = "Operation not allowed", cause: Throwable? = null)
        : AuthException(message, AuthErrorCode.OPERATION_NOT_ALLOWED, cause)

    class ExpiredActionCode(message: String = "Action code has expired", cause: Throwable? = null)
        : AuthException(message, AuthErrorCode.EXPIRED_ACTION_CODE, cause)

    class InvalidActionCode(message: String = "Invalid action code", cause: Throwable? = null)
        : AuthException(message, AuthErrorCode.INVALID_ACTION_CODE, cause)

    class NetworkError(message: String = "Network error", cause: Throwable? = null)
        : AuthException(message, AuthErrorCode.NETWORK_ERROR, cause)

    class Unknown(message: String = "Unknown error", cause: Throwable? = null)
        : AuthException(message, AuthErrorCode.UNKNOWN, cause)
}

/**
 * Error codes for Firebase Authentication.
 */
enum class AuthErrorCode {
    INVALID_CREDENTIAL,
    USER_NOT_FOUND,
    WRONG_PASSWORD,
    EMAIL_ALREADY_IN_USE,
    WEAK_PASSWORD,
    INVALID_EMAIL,
    TOO_MANY_REQUESTS,
    USER_DISABLED,
    REQUIRES_RECENT_LOGIN,
    CREDENTIAL_ALREADY_IN_USE,
    OPERATION_NOT_ALLOWED,
    EXPIRED_ACTION_CODE,
    INVALID_ACTION_CODE,
    NETWORK_ERROR,
    UNKNOWN
}
