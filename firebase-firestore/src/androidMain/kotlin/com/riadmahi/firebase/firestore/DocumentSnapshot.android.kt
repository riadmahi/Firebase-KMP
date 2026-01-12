package com.riadmahi.firebase.firestore

import com.google.firebase.firestore.DocumentSnapshot as AndroidDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot as AndroidQuerySnapshot
import com.google.firebase.firestore.DocumentChange as AndroidDocumentChange

actual class DocumentSnapshot internal constructor(
    internal val android: AndroidDocumentSnapshot
) {
    actual val id: String
        get() = android.id

    actual val reference: DocumentReference
        get() = DocumentReference(android.reference)

    actual val exists: Boolean
        get() = android.exists()

    actual val metadata: SnapshotMetadata
        get() = SnapshotMetadata(
            hasPendingWrites = android.metadata.hasPendingWrites(),
            isFromCache = android.metadata.isFromCache
        )

    actual fun data(): Map<String, Any?>? = android.data

    @Suppress("UNCHECKED_CAST")
    actual fun <T> get(field: String): T? = android.get(field) as? T

    actual fun contains(field: String): Boolean = android.contains(field)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DocumentSnapshot) return false
        return id == other.id && reference.path == other.reference.path
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + reference.path.hashCode()
        return result
    }

    override fun toString(): String = "DocumentSnapshot(id='$id', exists=$exists)"
}

actual class QuerySnapshot internal constructor(
    private val android: AndroidQuerySnapshot
) {
    actual val documents: List<DocumentSnapshot>
        get() = android.documents.map { DocumentSnapshot(it) }

    actual val isEmpty: Boolean
        get() = android.isEmpty

    actual val size: Int
        get() = android.size()

    actual val metadata: SnapshotMetadata
        get() = SnapshotMetadata(
            hasPendingWrites = android.metadata.hasPendingWrites(),
            isFromCache = android.metadata.isFromCache
        )

    actual val documentChanges: List<DocumentChange>
        get() = android.documentChanges.map { it.toCommon() }

    override fun toString(): String = "QuerySnapshot(size=$size, isEmpty=$isEmpty)"
}

private fun AndroidDocumentChange.toCommon(): DocumentChange {
    return DocumentChange(
        document = DocumentSnapshot(document),
        type = when (type) {
            AndroidDocumentChange.Type.ADDED -> DocumentChangeType.ADDED
            AndroidDocumentChange.Type.MODIFIED -> DocumentChangeType.MODIFIED
            AndroidDocumentChange.Type.REMOVED -> DocumentChangeType.REMOVED
        },
        oldIndex = oldIndex,
        newIndex = newIndex
    )
}
