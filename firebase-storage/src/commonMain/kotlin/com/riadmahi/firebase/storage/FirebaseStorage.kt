package com.riadmahi.firebase.storage

import com.riadmahi.firebase.core.FirebaseApp

/**
 * Firebase Cloud Storage entry point.
 *
 * Use [getInstance] to get an instance of FirebaseStorage.
 */
expect class FirebaseStorage {

    /**
     * The [FirebaseApp] associated with this instance.
     */
    val app: FirebaseApp

    /**
     * Maximum time in milliseconds to retry uploads.
     */
    var maxUploadRetryTimeMillis: Long

    /**
     * Maximum time in milliseconds to retry downloads.
     */
    var maxDownloadRetryTimeMillis: Long

    /**
     * Maximum time in milliseconds to retry operations.
     */
    var maxOperationRetryTimeMillis: Long

    /**
     * Returns a [StorageReference] to the root of the default bucket.
     */
    val reference: StorageReference

    /**
     * Returns a [StorageReference] for the given path.
     *
     * @param location The path to the file or directory.
     */
    fun getReference(location: String): StorageReference

    /**
     * Returns a [StorageReference] for the given URL.
     *
     * @param url A gs:// or https:// URL pointing to a Firebase Storage location.
     */
    fun getReferenceFromUrl(url: String): StorageReference

    companion object {
        /**
         * Gets the default [FirebaseStorage] instance.
         */
        fun getInstance(): FirebaseStorage

        /**
         * Gets a [FirebaseStorage] instance for the specified [FirebaseApp].
         */
        fun getInstance(app: FirebaseApp): FirebaseStorage

        /**
         * Gets a [FirebaseStorage] instance for the specified bucket URL.
         *
         * @param url The gs:// URL of the bucket.
         */
        fun getInstance(url: String): FirebaseStorage

        /**
         * Gets a [FirebaseStorage] instance for the specified [FirebaseApp] and bucket URL.
         */
        fun getInstance(app: FirebaseApp, url: String): FirebaseStorage
    }
}
