package com.riadmahi.firebase.storage

/**
 * Metadata for a storage object.
 */
data class StorageMetadata(
    /**
     * The content type (MIME type) of the object.
     */
    val contentType: String? = null,

    /**
     * Cache control header for the object.
     */
    val cacheControl: String? = null,

    /**
     * Content disposition header for the object.
     */
    val contentDisposition: String? = null,

    /**
     * Content encoding header for the object.
     */
    val contentEncoding: String? = null,

    /**
     * Content language header for the object.
     */
    val contentLanguage: String? = null,

    /**
     * Custom metadata as key-value pairs.
     */
    val customMetadata: Map<String, String> = emptyMap(),

    // Read-only properties (set by Firebase)
    /**
     * The name of the object.
     */
    val name: String? = null,

    /**
     * The full path to the object.
     */
    val path: String? = null,

    /**
     * The bucket containing the object.
     */
    val bucket: String? = null,

    /**
     * The generation of the object.
     */
    val generation: String? = null,

    /**
     * The metageneration of the object.
     */
    val metageneration: String? = null,

    /**
     * The size in bytes of the object.
     */
    val sizeBytes: Long = 0,

    /**
     * The time the object was created (milliseconds since epoch).
     */
    val creationTimeMillis: Long = 0,

    /**
     * The time the object was last updated (milliseconds since epoch).
     */
    val updatedTimeMillis: Long = 0,

    /**
     * The MD5 hash of the object.
     */
    val md5Hash: String? = null
) {
    /**
     * Builder for creating [StorageMetadata] instances.
     */
    class Builder {
        private var contentType: String? = null
        private var cacheControl: String? = null
        private var contentDisposition: String? = null
        private var contentEncoding: String? = null
        private var contentLanguage: String? = null
        private val customMetadata = mutableMapOf<String, String>()

        fun setContentType(contentType: String?) = apply { this.contentType = contentType }
        fun setCacheControl(cacheControl: String?) = apply { this.cacheControl = cacheControl }
        fun setContentDisposition(contentDisposition: String?) = apply { this.contentDisposition = contentDisposition }
        fun setContentEncoding(contentEncoding: String?) = apply { this.contentEncoding = contentEncoding }
        fun setContentLanguage(contentLanguage: String?) = apply { this.contentLanguage = contentLanguage }
        fun setCustomMetadata(key: String, value: String) = apply { customMetadata[key] = value }

        fun build() = StorageMetadata(
            contentType = contentType,
            cacheControl = cacheControl,
            contentDisposition = contentDisposition,
            contentEncoding = contentEncoding,
            contentLanguage = contentLanguage,
            customMetadata = customMetadata.toMap()
        )
    }
}

/**
 * Creates a [StorageMetadata] using a builder.
 */
inline fun storageMetadata(block: StorageMetadata.Builder.() -> Unit): StorageMetadata {
    return StorageMetadata.Builder().apply(block).build()
}
