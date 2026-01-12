package com.riadmahi.firebase.firestore

/**
 * A snapshot of a document in Firestore.
 */
expect class DocumentSnapshot {
    /**
     * The ID of the document.
     */
    val id: String

    /**
     * The reference to the document.
     */
    val reference: DocumentReference

    /**
     * Whether the document exists.
     */
    val exists: Boolean

    /**
     * Metadata about the snapshot.
     */
    val metadata: SnapshotMetadata

    /**
     * Returns all fields as a Map.
     */
    fun data(): Map<String, Any?>?

    /**
     * Returns a specific field value.
     */
    fun <T> get(field: String): T?

    /**
     * Returns whether the document contains a field.
     */
    fun contains(field: String): Boolean
}

/**
 * A snapshot of a query result.
 */
expect class QuerySnapshot {
    /**
     * The documents in this snapshot.
     */
    val documents: List<DocumentSnapshot>

    /**
     * Whether the snapshot is empty.
     */
    val isEmpty: Boolean

    /**
     * The number of documents.
     */
    val size: Int

    /**
     * Metadata about the snapshot.
     */
    val metadata: SnapshotMetadata

    /**
     * The changes since the last snapshot.
     */
    val documentChanges: List<DocumentChange>
}

/**
 * Metadata about a snapshot.
 */
data class SnapshotMetadata(
    /**
     * Whether the snapshot has pending writes.
     */
    val hasPendingWrites: Boolean,

    /**
     * Whether the snapshot is from cache.
     */
    val isFromCache: Boolean
)

/**
 * Represents a change to a document.
 */
data class DocumentChange(
    /**
     * The affected document.
     */
    val document: DocumentSnapshot,

    /**
     * The type of change.
     */
    val type: DocumentChangeType,

    /**
     * The old index (before the change).
     */
    val oldIndex: Int,

    /**
     * The new index (after the change).
     */
    val newIndex: Int
)

/**
 * The type of document change.
 */
enum class DocumentChangeType {
    ADDED,
    MODIFIED,
    REMOVED
}
