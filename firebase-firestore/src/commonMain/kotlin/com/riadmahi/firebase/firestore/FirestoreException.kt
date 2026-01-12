package com.riadmahi.firebase.firestore

import com.riadmahi.firebase.core.FirebaseException

/**
 * Exception thrown by Firestore operations.
 */
sealed class FirestoreException(
    message: String,
    val code: FirestoreErrorCode,
    cause: Throwable? = null
) : FirebaseException(message, cause) {

    class Cancelled(message: String = "Operation cancelled", cause: Throwable? = null)
        : FirestoreException(message, FirestoreErrorCode.CANCELLED, cause)

    class Unknown(message: String = "Unknown error", cause: Throwable? = null)
        : FirestoreException(message, FirestoreErrorCode.UNKNOWN, cause)

    class InvalidArgument(message: String = "Invalid argument", cause: Throwable? = null)
        : FirestoreException(message, FirestoreErrorCode.INVALID_ARGUMENT, cause)

    class DeadlineExceeded(message: String = "Deadline exceeded", cause: Throwable? = null)
        : FirestoreException(message, FirestoreErrorCode.DEADLINE_EXCEEDED, cause)

    class NotFound(message: String = "Document not found", cause: Throwable? = null)
        : FirestoreException(message, FirestoreErrorCode.NOT_FOUND, cause)

    class AlreadyExists(message: String = "Document already exists", cause: Throwable? = null)
        : FirestoreException(message, FirestoreErrorCode.ALREADY_EXISTS, cause)

    class PermissionDenied(message: String = "Permission denied", cause: Throwable? = null)
        : FirestoreException(message, FirestoreErrorCode.PERMISSION_DENIED, cause)

    class ResourceExhausted(message: String = "Resource exhausted", cause: Throwable? = null)
        : FirestoreException(message, FirestoreErrorCode.RESOURCE_EXHAUSTED, cause)

    class FailedPrecondition(message: String = "Failed precondition", cause: Throwable? = null)
        : FirestoreException(message, FirestoreErrorCode.FAILED_PRECONDITION, cause)

    class Aborted(message: String = "Operation aborted", cause: Throwable? = null)
        : FirestoreException(message, FirestoreErrorCode.ABORTED, cause)

    class OutOfRange(message: String = "Out of range", cause: Throwable? = null)
        : FirestoreException(message, FirestoreErrorCode.OUT_OF_RANGE, cause)

    class Unimplemented(message: String = "Unimplemented", cause: Throwable? = null)
        : FirestoreException(message, FirestoreErrorCode.UNIMPLEMENTED, cause)

    class Internal(message: String = "Internal error", cause: Throwable? = null)
        : FirestoreException(message, FirestoreErrorCode.INTERNAL, cause)

    class Unavailable(message: String = "Service unavailable", cause: Throwable? = null)
        : FirestoreException(message, FirestoreErrorCode.UNAVAILABLE, cause)

    class DataLoss(message: String = "Data loss", cause: Throwable? = null)
        : FirestoreException(message, FirestoreErrorCode.DATA_LOSS, cause)

    class Unauthenticated(message: String = "Unauthenticated", cause: Throwable? = null)
        : FirestoreException(message, FirestoreErrorCode.UNAUTHENTICATED, cause)
}

/**
 * Error codes for Firestore operations.
 */
enum class FirestoreErrorCode {
    CANCELLED,
    UNKNOWN,
    INVALID_ARGUMENT,
    DEADLINE_EXCEEDED,
    NOT_FOUND,
    ALREADY_EXISTS,
    PERMISSION_DENIED,
    RESOURCE_EXHAUSTED,
    FAILED_PRECONDITION,
    ABORTED,
    OUT_OF_RANGE,
    UNIMPLEMENTED,
    INTERNAL,
    UNAVAILABLE,
    DATA_LOSS,
    UNAUTHENTICATED
}
