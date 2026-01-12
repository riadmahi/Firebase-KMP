package com.riadmahi.firebase.storage

import cocoapods.FirebaseStorage.FIRStorageReference
import cocoapods.FirebaseStorage.FIRStorageMetadata
import cocoapods.FirebaseStorage.FIRStorageListResult
import com.riadmahi.firebase.core.FirebaseResult
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.create
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalForeignApi::class)
actual class StorageReference internal constructor(
    internal val ios: FIRStorageReference
) {
    actual val storage: FirebaseStorage
        get() = FirebaseStorage.getInstance()

    actual val name: String
        get() = ios.name

    actual val path: String
        get() = ios.fullPath

    actual val bucket: String
        get() = ios.bucket

    actual val parent: StorageReference?
        get() = ios.parent()?.let { StorageReference(it) }

    actual val root: StorageReference
        get() = StorageReference(ios.root())

    actual fun child(path: String): StorageReference =
        StorageReference(ios.child(path))

    actual suspend fun putBytes(bytes: ByteArray, metadata: StorageMetadata?): FirebaseResult<UploadResult> =
        suspendCoroutine { continuation ->
            val nsData = bytes.toNSData()
            val iosMetadata = metadata?.toIos()

            ios.putData(nsData, iosMetadata) { resultMetadata, error ->
                if (error != null) {
                    continuation.resume(FirebaseResult.Failure(error.toStorageException()))
                } else {
                    continuation.resume(FirebaseResult.Success(
                        UploadResult(
                            bytesTransferred = resultMetadata?.size ?: 0,
                            totalBytes = resultMetadata?.size ?: 0,
                            metadata = resultMetadata?.toCommon()
                        )
                    ))
                }
            }
        }

    actual suspend fun getBytes(maxSize: Long): FirebaseResult<ByteArray> =
        suspendCoroutine { continuation ->
            ios.dataWithMaxSize(maxSize) { data, error ->
                if (error != null) {
                    continuation.resume(FirebaseResult.Failure(error.toStorageException()))
                } else if (data != null) {
                    continuation.resume(FirebaseResult.Success(data.toByteArray()))
                } else {
                    continuation.resume(FirebaseResult.Failure(
                        StorageException.Unknown("No data returned")
                    ))
                }
            }
        }

    actual suspend fun getDownloadUrl(): FirebaseResult<String> =
        suspendCoroutine { continuation ->
            ios.downloadURLWithCompletion { url, error ->
                if (error != null) {
                    continuation.resume(FirebaseResult.Failure(error.toStorageException()))
                } else if (url != null) {
                    continuation.resume(FirebaseResult.Success(url.absoluteString ?: ""))
                } else {
                    continuation.resume(FirebaseResult.Failure(
                        StorageException.Unknown("No URL returned")
                    ))
                }
            }
        }

    actual suspend fun delete(): FirebaseResult<Unit> =
        suspendCoroutine { continuation ->
            ios.deleteWithCompletion { error ->
                if (error != null) {
                    continuation.resume(FirebaseResult.Failure(error.toStorageException()))
                } else {
                    continuation.resume(FirebaseResult.Success(Unit))
                }
            }
        }

    actual suspend fun getMetadata(): FirebaseResult<StorageMetadata> =
        suspendCoroutine { continuation ->
            ios.metadataWithCompletion { metadata, error ->
                if (error != null) {
                    continuation.resume(FirebaseResult.Failure(error.toStorageException()))
                } else if (metadata != null) {
                    continuation.resume(FirebaseResult.Success(metadata.toCommon()))
                } else {
                    continuation.resume(FirebaseResult.Failure(
                        StorageException.Unknown("No metadata returned")
                    ))
                }
            }
        }

    actual suspend fun updateMetadata(metadata: StorageMetadata): FirebaseResult<StorageMetadata> =
        suspendCoroutine { continuation ->
            ios.updateMetadata(metadata.toIos()) { resultMetadata, error ->
                if (error != null) {
                    continuation.resume(FirebaseResult.Failure(error.toStorageException()))
                } else if (resultMetadata != null) {
                    continuation.resume(FirebaseResult.Success(resultMetadata.toCommon()))
                } else {
                    continuation.resume(FirebaseResult.Failure(
                        StorageException.Unknown("No metadata returned")
                    ))
                }
            }
        }

    actual suspend fun list(maxResults: Int): FirebaseResult<ListResult> =
        suspendCoroutine { continuation ->
            ios.listWithMaxResults(maxResults.toLong()) { result, error ->
                if (error != null) {
                    continuation.resume(FirebaseResult.Failure(error.toStorageException()))
                } else if (result != null) {
                    continuation.resume(FirebaseResult.Success(result.toCommon()))
                } else {
                    continuation.resume(FirebaseResult.Failure(
                        StorageException.Unknown("No result returned")
                    ))
                }
            }
        }

    actual suspend fun listAll(): FirebaseResult<ListResult> =
        suspendCoroutine { continuation ->
            ios.listAllWithCompletion { result, error ->
                if (error != null) {
                    continuation.resume(FirebaseResult.Failure(error.toStorageException()))
                } else if (result != null) {
                    continuation.resume(FirebaseResult.Success(result.toCommon()))
                } else {
                    continuation.resume(FirebaseResult.Failure(
                        StorageException.Unknown("No result returned")
                    ))
                }
            }
        }
}

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData {
    return NSData.create(bytes = this.toCValues().ptr, length = this.size.toULong())
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val bytes = ByteArray(length.toInt())
    if (bytes.isNotEmpty()) {
        bytes.usePinned { pinned ->
            platform.posix.memcpy(pinned.addressOf(0), this.bytes, length)
        }
    }
    return bytes
}

@OptIn(ExperimentalForeignApi::class)
private fun StorageMetadata.toIos(): FIRStorageMetadata {
    return FIRStorageMetadata().apply {
        contentType?.let { setContentType(it) }
        cacheControl?.let { setCacheControl(it) }
        contentDisposition?.let { setContentDisposition(it) }
        contentEncoding?.let { setContentEncoding(it) }
        contentLanguage?.let { setContentLanguage(it) }
        if (customMetadata.isNotEmpty()) {
            setCustomMetadata(customMetadata)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun FIRStorageMetadata.toCommon(): StorageMetadata {
    @Suppress("UNCHECKED_CAST")
    val customMeta = (customMetadata as? Map<String, String>) ?: emptyMap()

    return StorageMetadata(
        contentType = contentType,
        cacheControl = cacheControl,
        contentDisposition = contentDisposition,
        contentEncoding = contentEncoding,
        contentLanguage = contentLanguage,
        customMetadata = customMeta,
        name = name,
        path = path,
        bucket = bucket,
        generation = generation?.stringValue,
        metageneration = metageneration?.stringValue,
        sizeBytes = size,
        creationTimeMillis = (timeCreated?.timeIntervalSince1970?.times(1000))?.toLong() ?: 0,
        updatedTimeMillis = (updated?.timeIntervalSince1970?.times(1000))?.toLong() ?: 0,
        md5Hash = md5Hash
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun FIRStorageListResult.toCommon(): ListResult {
    @Suppress("UNCHECKED_CAST")
    val itemsList = (items as? List<FIRStorageReference>) ?: emptyList()
    @Suppress("UNCHECKED_CAST")
    val prefixesList = (prefixes as? List<FIRStorageReference>) ?: emptyList()

    return ListResult(
        items = itemsList.map { StorageReference(it) },
        prefixes = prefixesList.map { StorageReference(it) },
        pageToken = pageToken
    )
}

private fun NSError.toStorageException(): StorageException {
    val code = code.toInt()
    return when (code) {
        -13010 -> StorageException.ObjectNotFound(localizedDescription, null)
        -13011 -> StorageException.BucketNotFound(localizedDescription, null)
        -13012 -> StorageException.ProjectNotFound(null)
        -13013 -> StorageException.QuotaExceeded(null)
        -13020 -> StorageException.Unauthenticated(null)
        -13021 -> StorageException.Unauthorized(null)
        -13030 -> StorageException.RetryLimitExceeded(null)
        -13031 -> StorageException.InvalidChecksum(null)
        -13040 -> StorageException.Cancelled(null)
        else -> StorageException.Unknown(localizedDescription, null)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toCValues() = kotlinx.cinterop.ByteVar.pinned { cValuesOf(*this) }

@OptIn(ExperimentalForeignApi::class)
private inline fun <reified T : kotlinx.cinterop.CVariable> kotlinx.cinterop.CValues<T>.pinned() = this

@OptIn(ExperimentalForeignApi::class)
private fun <T : kotlinx.cinterop.CVariable> cValuesOf(vararg elements: Byte): kotlinx.cinterop.CValues<kotlinx.cinterop.ByteVar> {
    return kotlinx.cinterop.cValuesOf(*elements)
}

@OptIn(ExperimentalForeignApi::class)
private inline fun ByteArray.usePinned(block: (kotlinx.cinterop.Pinned<ByteArray>) -> Unit) {
    kotlinx.cinterop.memScoped {
        block(kotlinx.cinterop.pin(this@usePinned))
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun kotlinx.cinterop.Pinned<ByteArray>.addressOf(index: Int): kotlinx.cinterop.CPointer<kotlinx.cinterop.ByteVar> {
    return this.get().refTo(index).getPointer(kotlinx.cinterop.MemScope())
}
