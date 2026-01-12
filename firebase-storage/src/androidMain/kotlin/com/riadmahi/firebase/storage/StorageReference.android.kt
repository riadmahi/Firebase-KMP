package com.riadmahi.firebase.storage

import com.riadmahi.firebase.core.FirebaseResult
import com.google.firebase.storage.StorageReference as AndroidStorageReference
import com.google.firebase.storage.StorageMetadata as AndroidStorageMetadata
import kotlinx.coroutines.tasks.await

actual class StorageReference internal constructor(
    internal val android: AndroidStorageReference
) {
    actual val storage: FirebaseStorage
        get() = FirebaseStorage.getInstance()

    actual val name: String
        get() = android.name

    actual val path: String
        get() = android.path

    actual val bucket: String
        get() = android.bucket

    actual val parent: StorageReference?
        get() = android.parent?.let { StorageReference(it) }

    actual val root: StorageReference
        get() = StorageReference(android.root)

    actual fun child(path: String): StorageReference =
        StorageReference(android.child(path))

    actual suspend fun putBytes(bytes: ByteArray, metadata: StorageMetadata?): FirebaseResult<UploadResult> =
        safeStorageCall {
            val androidMetadata = metadata?.toAndroid()
            val task = if (androidMetadata != null) {
                android.putBytes(bytes, androidMetadata)
            } else {
                android.putBytes(bytes)
            }
            val snapshot = task.await()
            UploadResult(
                bytesTransferred = snapshot.bytesTransferred,
                totalBytes = snapshot.totalByteCount,
                metadata = snapshot.metadata?.toCommon()
            )
        }

    actual suspend fun getBytes(maxSize: Long): FirebaseResult<ByteArray> =
        safeStorageCall {
            android.getBytes(maxSize).await()
        }

    actual suspend fun getDownloadUrl(): FirebaseResult<String> =
        safeStorageCall {
            android.downloadUrl.await().toString()
        }

    actual suspend fun delete(): FirebaseResult<Unit> =
        safeStorageCall {
            android.delete().await()
        }

    actual suspend fun getMetadata(): FirebaseResult<StorageMetadata> =
        safeStorageCall {
            android.metadata.await().toCommon()
        }

    actual suspend fun updateMetadata(metadata: StorageMetadata): FirebaseResult<StorageMetadata> =
        safeStorageCall {
            android.updateMetadata(metadata.toAndroid()).await().toCommon()
        }

    actual suspend fun list(maxResults: Int): FirebaseResult<ListResult> =
        safeStorageCall {
            val result = android.list(maxResults).await()
            ListResult(
                items = result.items.map { StorageReference(it) },
                prefixes = result.prefixes.map { StorageReference(it) },
                pageToken = result.pageToken
            )
        }

    actual suspend fun listAll(): FirebaseResult<ListResult> =
        safeStorageCall {
            val result = android.listAll().await()
            ListResult(
                items = result.items.map { StorageReference(it) },
                prefixes = result.prefixes.map { StorageReference(it) },
                pageToken = null
            )
        }
}

private fun StorageMetadata.toAndroid(): AndroidStorageMetadata {
    return AndroidStorageMetadata.Builder().apply {
        contentType?.let { setContentType(it) }
        cacheControl?.let { setCacheControl(it) }
        contentDisposition?.let { setContentDisposition(it) }
        contentEncoding?.let { setContentEncoding(it) }
        contentLanguage?.let { setContentLanguage(it) }
        customMetadata.forEach { (key, value) -> setCustomMetadata(key, value) }
    }.build()
}

private fun AndroidStorageMetadata.toCommon(): StorageMetadata {
    return StorageMetadata(
        contentType = contentType,
        cacheControl = cacheControl,
        contentDisposition = contentDisposition,
        contentEncoding = contentEncoding,
        contentLanguage = contentLanguage,
        customMetadata = customMetadataKeys?.associateWith { getCustomMetadata(it) ?: "" } ?: emptyMap(),
        name = name,
        path = path,
        bucket = bucket,
        generation = generation,
        metageneration = null, // Not directly available on Android
        sizeBytes = sizeBytes,
        creationTimeMillis = creationTimeMillis,
        updatedTimeMillis = updatedTimeMillis,
        md5Hash = md5Hash
    )
}

private inline fun <T> safeStorageCall(block: () -> T): FirebaseResult<T> {
    return try {
        FirebaseResult.Success(block())
    } catch (e: com.google.firebase.storage.StorageException) {
        FirebaseResult.Failure(e.toKmpException())
    } catch (e: Exception) {
        FirebaseResult.Failure(StorageException.Unknown(e.message ?: "Unknown error", e))
    }
}

private fun com.google.firebase.storage.StorageException.toKmpException(): StorageException {
    return when (errorCode) {
        com.google.firebase.storage.StorageException.ERROR_OBJECT_NOT_FOUND ->
            StorageException.ObjectNotFound(message ?: "Unknown path", this)
        com.google.firebase.storage.StorageException.ERROR_BUCKET_NOT_FOUND ->
            StorageException.BucketNotFound(message ?: "Unknown bucket", this)
        com.google.firebase.storage.StorageException.ERROR_PROJECT_NOT_FOUND ->
            StorageException.ProjectNotFound(this)
        com.google.firebase.storage.StorageException.ERROR_QUOTA_EXCEEDED ->
            StorageException.QuotaExceeded(this)
        com.google.firebase.storage.StorageException.ERROR_NOT_AUTHENTICATED ->
            StorageException.Unauthenticated(this)
        com.google.firebase.storage.StorageException.ERROR_NOT_AUTHORIZED ->
            StorageException.Unauthorized(this)
        com.google.firebase.storage.StorageException.ERROR_RETRY_LIMIT_EXCEEDED ->
            StorageException.RetryLimitExceeded(this)
        com.google.firebase.storage.StorageException.ERROR_INVALID_CHECKSUM ->
            StorageException.InvalidChecksum(this)
        com.google.firebase.storage.StorageException.ERROR_CANCELED ->
            StorageException.Cancelled(this)
        else ->
            StorageException.Unknown(message ?: "Unknown error", this)
    }
}
