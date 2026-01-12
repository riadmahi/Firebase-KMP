package com.riadmahi.firebase.core

/**
 * Base exception class for all Firebase-related errors.
 */
open class FirebaseException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {

    /**
     * Firebase has not been initialized.
     */
    class NotInitializedException(
        message: String = "Firebase has not been initialized",
        cause: Throwable? = null
    ) : FirebaseException(message, cause)

    /**
     * Firebase initialization failed.
     */
    class InitializationException(
        message: String,
        cause: Throwable? = null
    ) : FirebaseException(message, cause)

    /**
     * Network-related error.
     */
    class NetworkException(
        message: String,
        cause: Throwable? = null
    ) : FirebaseException(message, cause)

    /**
     * Permission denied error.
     */
    class PermissionDeniedException(
        message: String,
        cause: Throwable? = null
    ) : FirebaseException(message, cause)

    /**
     * Resource not found error.
     */
    class NotFoundException(
        message: String,
        cause: Throwable? = null
    ) : FirebaseException(message, cause)

    /**
     * Operation was cancelled.
     */
    class CancelledException(
        message: String,
        cause: Throwable? = null
    ) : FirebaseException(message, cause)

    /**
     * Unknown or unhandled error.
     */
    class UnknownException(
        message: String,
        cause: Throwable? = null
    ) : FirebaseException(message, cause)
}
