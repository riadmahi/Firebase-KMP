package com.riadmahi.firebase.firestore

import com.riadmahi.firebase.core.FirebaseApp
import com.riadmahi.firebase.core.FirebaseResult

/**
 * Entry point for Firebase Firestore.
 */
expect class FirebaseFirestore {
    /**
     * Returns a CollectionReference for the specified path.
     */
    fun collection(path: String): CollectionReference

    /**
     * Returns a DocumentReference for the specified path.
     */
    fun document(path: String): DocumentReference

    /**
     * Returns a Query for a collection group.
     */
    fun collectionGroup(collectionId: String): Query

    /**
     * Executes a transaction.
     */
    suspend fun <T> runTransaction(
        block: suspend Transaction.() -> T
    ): FirebaseResult<T>

    /**
     * Creates a write batch.
     */
    fun batch(): WriteBatch

    /**
     * Clears the local persistence.
     */
    suspend fun clearPersistence(): FirebaseResult<Unit>

    /**
     * Terminates the Firestore instance.
     */
    suspend fun terminate(): FirebaseResult<Unit>

    /**
     * Waits for pending writes to complete.
     */
    suspend fun waitForPendingWrites(): FirebaseResult<Unit>

    /**
     * Configures Firestore to use the emulator.
     */
    fun useEmulator(host: String, port: Int)

    /**
     * The settings for this Firestore instance.
     */
    var settings: FirestoreSettings

    companion object {
        /**
         * Returns the FirebaseFirestore instance for the default FirebaseApp.
         */
        fun getInstance(): FirebaseFirestore

        /**
         * Returns the FirebaseFirestore instance for the given FirebaseApp.
         */
        fun getInstance(app: FirebaseApp): FirebaseFirestore
    }
}

/**
 * Settings for Firebase Firestore.
 */
data class FirestoreSettings(
    val host: String = "firestore.googleapis.com",
    val sslEnabled: Boolean = true,
    val cacheSettings: LocalCacheSettings = LocalCacheSettings.Persistent()
) {
    class Builder {
        var host: String = "firestore.googleapis.com"
        var sslEnabled: Boolean = true
        var cacheSettings: LocalCacheSettings = LocalCacheSettings.Persistent()

        fun setHost(host: String) = apply { this.host = host }
        fun setSslEnabled(enabled: Boolean) = apply { this.sslEnabled = enabled }
        fun setCacheSettings(settings: LocalCacheSettings) = apply { this.cacheSettings = settings }

        fun build() = FirestoreSettings(host, sslEnabled, cacheSettings)
    }
}

/**
 * Local cache settings for Firestore.
 * Use [Persistent] for offline persistence or [Memory] for in-memory only cache.
 */
sealed class LocalCacheSettings {
    /**
     * Persistent cache that survives app restarts.
     * @param sizeBytes Maximum size of the cache in bytes. Use [CACHE_SIZE_UNLIMITED] for unlimited.
     */
    data class Persistent(
        val sizeBytes: Long = DEFAULT_CACHE_SIZE_BYTES
    ) : LocalCacheSettings()

    /**
     * Memory-only cache that is cleared when the app terminates.
     * @param garbageCollectorSettings Configuration for garbage collection.
     */
    data class Memory(
        val garbageCollectorSettings: MemoryGarbageCollectorSettings = MemoryGarbageCollectorSettings.Eager
    ) : LocalCacheSettings()

    companion object {
        const val CACHE_SIZE_UNLIMITED = -1L
        const val DEFAULT_CACHE_SIZE_BYTES = 104857600L // 100 MB
    }
}

/**
 * Garbage collector settings for memory cache.
 */
sealed class MemoryGarbageCollectorSettings {
    /**
     * Eagerly removes documents from cache as soon as they are no longer needed.
     */
    data object Eager : MemoryGarbageCollectorSettings()

    /**
     * Uses LRU (Least Recently Used) algorithm to remove documents when cache exceeds size limit.
     * @param sizeBytes Maximum size of the cache in bytes.
     */
    data class Lru(
        val sizeBytes: Long = LocalCacheSettings.DEFAULT_CACHE_SIZE_BYTES
    ) : MemoryGarbageCollectorSettings()
}
