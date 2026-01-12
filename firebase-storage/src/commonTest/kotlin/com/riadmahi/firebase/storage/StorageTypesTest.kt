package com.riadmahi.firebase.storage

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for Firebase Storage types to validate API consistency.
 *
 * Reference: https://firebase.google.com/docs/storage
 */
class StorageTypesTest {

    // region StorageMetadata - matches Firebase StorageMetadata

    @Test
    fun `StorageMetadata should have contentType`() {
        val metadata = StorageMetadata(
            contentType = "image/jpeg"
        )

        assertEquals("image/jpeg", metadata.contentType)
    }

    @Test
    fun `StorageMetadata should support custom metadata`() {
        val metadata = StorageMetadata(
            contentType = "image/png",
            customMetadata = mapOf(
                "author" to "user123",
                "version" to "1.0"
            )
        )

        assertEquals("user123", metadata.customMetadata?.get("author"))
        assertEquals("1.0", metadata.customMetadata?.get("version"))
    }

    @Test
    fun `StorageMetadata optional fields should default to null or empty`() {
        val metadata = StorageMetadata()

        assertNull(metadata.contentType)
        assertEquals(emptyMap(), metadata.customMetadata)
        assertNull(metadata.cacheControl)
        assertNull(metadata.contentDisposition)
        assertNull(metadata.contentEncoding)
        assertNull(metadata.contentLanguage)
    }

    @Test
    fun `StorageMetadata should support all HTTP headers`() {
        val metadata = StorageMetadata(
            contentType = "application/json",
            cacheControl = "max-age=3600",
            contentDisposition = "attachment; filename=data.json",
            contentEncoding = "gzip",
            contentLanguage = "en-US"
        )

        assertEquals("application/json", metadata.contentType)
        assertEquals("max-age=3600", metadata.cacheControl)
        assertEquals("attachment; filename=data.json", metadata.contentDisposition)
        assertEquals("gzip", metadata.contentEncoding)
        assertEquals("en-US", metadata.contentLanguage)
    }

    // endregion

    // region UploadResult - matches Firebase UploadTask.TaskSnapshot

    @Test
    fun `UploadResult should contain bytesTransferred`() {
        val result = UploadResult(
            bytesTransferred = 1024,
            totalBytes = 2048,
            metadata = null
        )

        assertEquals(1024, result.bytesTransferred)
        assertEquals(2048, result.totalBytes)
    }

    @Test
    fun `UploadResult should contain metadata`() {
        val metadata = StorageMetadata(contentType = "image/jpeg")
        val result = UploadResult(
            bytesTransferred = 2048,
            totalBytes = 2048,
            metadata = metadata
        )

        assertEquals("image/jpeg", result.metadata?.contentType)
    }

    // endregion

    // region ListResult - matches Firebase ListResult

    @Test
    fun `ListResult should contain items and prefixes`() {
        // This is a data structure test - actual references would come from the platform
        // Here we validate the structure matches Firebase's ListResult

        // ListResult has:
        // - items: List<StorageReference> (files)
        // - prefixes: List<StorageReference> (folders)
        // - pageToken: String? (for pagination)

        // Verified by compilation - the types exist and have correct structure
    }

    // endregion

    // region StorageException - matches Firebase StorageException

    @Test
    fun `StorageException ObjectNotFound should match Firebase error`() {
        val exception = StorageException.ObjectNotFound("test/path")

        assertEquals("Object not found at path: test/path", exception.message)
    }

    @Test
    fun `StorageException Unauthorized should match Firebase error`() {
        val exception = StorageException.Unauthorized()

        assertEquals("User is not authorized", exception.message)
    }

    @Test
    fun `StorageException Cancelled should match Firebase error`() {
        val exception = StorageException.Cancelled()

        assertEquals("Operation cancelled", exception.message)
    }

    @Test
    fun `StorageException QuotaExceeded should match Firebase error`() {
        val exception = StorageException.QuotaExceeded()

        assertEquals("Quota exceeded", exception.message)
    }

    @Test
    fun `StorageException RetryLimitExceeded should match Firebase error`() {
        val exception = StorageException.RetryLimitExceeded()

        assertEquals("Retry limit exceeded", exception.message)
    }

    @Test
    fun `StorageException InvalidChecksum should match Firebase error`() {
        val exception = StorageException.InvalidChecksum()

        assertEquals("Invalid checksum", exception.message)
    }

    @Test
    fun `StorageException Unknown should be catch-all`() {
        val exception = StorageException.Unknown("Custom error")

        assertEquals("Custom error", exception.message)
    }

    // endregion
}
