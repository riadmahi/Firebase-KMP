package com.riadmahi.firebase.storage

import com.riadmahi.firebase.core.FirebaseException

/**
 * Exception thrown by Firebase Storage operations.
 */
sealed class StorageException(
    message: String,
    cause: Throwable? = null
) : FirebaseException(message, cause) {

    /**
     * Object not found at the specified reference.
     */
    class ObjectNotFound(path: String, cause: Throwable? = null) :
        StorageException("Object not found at path: $path", cause)

    /**
     * Bucket not found.
     */
    class BucketNotFound(bucket: String, cause: Throwable? = null) :
        StorageException("Bucket not found: $bucket", cause)

    /**
     * Project not found.
     */
    class ProjectNotFound(cause: Throwable? = null) :
        StorageException("Project not found", cause)

    /**
     * User quota exceeded.
     */
    class QuotaExceeded(cause: Throwable? = null) :
        StorageException("Quota exceeded", cause)

    /**
     * User is not authenticated.
     */
    class Unauthenticated(cause: Throwable? = null) :
        StorageException("User is not authenticated", cause)

    /**
     * User is not authorized to perform this operation.
     */
    class Unauthorized(cause: Throwable? = null) :
        StorageException("User is not authorized", cause)

    /**
     * Maximum retry time exceeded.
     */
    class RetryLimitExceeded(cause: Throwable? = null) :
        StorageException("Retry limit exceeded", cause)

    /**
     * Invalid checksum during download.
     */
    class InvalidChecksum(cause: Throwable? = null) :
        StorageException("Invalid checksum", cause)

    /**
     * Operation was cancelled.
     */
    class Cancelled(cause: Throwable? = null) :
        StorageException("Operation cancelled", cause)

    /**
     * Invalid argument provided.
     */
    class InvalidArgument(message: String, cause: Throwable? = null) :
        StorageException(message, cause)

    /**
     * Unknown error occurred.
     */
    class Unknown(message: String, cause: Throwable? = null) :
        StorageException(message, cause)
}
