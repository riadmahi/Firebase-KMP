package com.riadmahi.firebase.storage

/**
 * Result of an upload operation.
 */
data class UploadResult(
    /**
     * The total number of bytes uploaded.
     */
    val bytesTransferred: Long,

    /**
     * The total number of bytes to upload.
     */
    val totalBytes: Long,

    /**
     * The metadata for the uploaded object.
     */
    val metadata: StorageMetadata?
)
