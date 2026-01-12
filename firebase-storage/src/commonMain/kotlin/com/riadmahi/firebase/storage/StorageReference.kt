package com.riadmahi.firebase.storage

import com.riadmahi.firebase.core.FirebaseResult

/**
 * Represents a reference to a Google Cloud Storage object.
 *
 * A reference can be used to upload, download, or delete objects,
 * as well as get/set object metadata.
 */
expect class StorageReference {

    /**
     * The [FirebaseStorage] instance associated with this reference.
     */
    val storage: FirebaseStorage

    /**
     * The name of the object (last segment of the path).
     */
    val name: String

    /**
     * The full path to this object, not including the bucket.
     */
    val path: String

    /**
     * The bucket this object is stored in.
     */
    val bucket: String

    /**
     * The parent [StorageReference], or null if this is the root.
     */
    val parent: StorageReference?

    /**
     * The root [StorageReference].
     */
    val root: StorageReference

    /**
     * Returns a [StorageReference] to a child location.
     *
     * @param path The relative path to the child.
     */
    fun child(path: String): StorageReference

    /**
     * Uploads data to this reference.
     *
     * @param bytes The data to upload.
     * @param metadata Optional metadata for the upload.
     */
    suspend fun putBytes(bytes: ByteArray, metadata: StorageMetadata? = null): FirebaseResult<UploadResult>

    /**
     * Downloads the object at this reference as a byte array.
     *
     * @param maxSize The maximum size in bytes to download.
     */
    suspend fun getBytes(maxSize: Long): FirebaseResult<ByteArray>

    /**
     * Gets the download URL for this reference.
     */
    suspend fun getDownloadUrl(): FirebaseResult<String>

    /**
     * Deletes the object at this reference.
     */
    suspend fun delete(): FirebaseResult<Unit>

    /**
     * Gets the metadata for the object at this reference.
     */
    suspend fun getMetadata(): FirebaseResult<StorageMetadata>

    /**
     * Updates the metadata for the object at this reference.
     *
     * @param metadata The new metadata.
     */
    suspend fun updateMetadata(metadata: StorageMetadata): FirebaseResult<StorageMetadata>

    /**
     * Lists all items (files) and prefixes (folders) under this reference.
     *
     * @param maxResults The maximum number of results to return.
     */
    suspend fun list(maxResults: Int): FirebaseResult<ListResult>

    /**
     * Lists all items (files) and prefixes (folders) under this reference.
     */
    suspend fun listAll(): FirebaseResult<ListResult>
}
