@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.riadmahi.firebase.firestore

import cocoapods.FirebaseFirestore.FIRDocumentChange
import cocoapods.FirebaseFirestore.FIRDocumentChangeType
import cocoapods.FirebaseFirestore.FIRDocumentSnapshot
import cocoapods.FirebaseFirestore.FIRQueryDocumentSnapshot
import cocoapods.FirebaseFirestore.FIRQuerySnapshot

/**
 * iOS implementation of DocumentSnapshot using Firebase iOS SDK.
 */
actual class DocumentSnapshot internal constructor(
    internal val ios: FIRDocumentSnapshot
) {
    actual val id: String
        get() = ios.documentID

    actual val reference: DocumentReference
        get() = DocumentReference(ios.reference)

    actual val exists: Boolean
        get() = ios.exists

    actual val metadata: SnapshotMetadata
        get() = SnapshotMetadata(
            hasPendingWrites = ios.metadata.hasPendingWrites,
            isFromCache = ios.metadata.isFromCache
        )

    actual fun data(): Map<String, Any?>? {
        @Suppress("UNCHECKED_CAST")
        return ios.data()?.let { convertFromFirestore(it as Map<String, Any?>) }
    }

    @Suppress("UNCHECKED_CAST")
    actual fun <T> get(field: String): T? {
        val value = ios.valueForField(field)
        return convertValueFromFirestore(value) as? T
    }

    actual fun contains(field: String): Boolean {
        return ios.data()?.containsKey(field) == true
    }

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

/**
 * iOS implementation of QuerySnapshot using Firebase iOS SDK.
 */
actual class QuerySnapshot internal constructor(
    internal val ios: FIRQuerySnapshot
) {
    actual val documents: List<DocumentSnapshot>
        get() = ios.documents.map { doc ->
            DocumentSnapshot(doc as FIRQueryDocumentSnapshot)
        }

    actual val isEmpty: Boolean
        get() = ios.isEmpty

    actual val size: Int
        get() = ios.count.toInt()

    actual val metadata: SnapshotMetadata
        get() = SnapshotMetadata(
            hasPendingWrites = ios.metadata.hasPendingWrites,
            isFromCache = ios.metadata.isFromCache
        )

    actual val documentChanges: List<DocumentChange>
        get() = ios.documentChanges.map { change ->
            val firChange = change as FIRDocumentChange
            DocumentChange(
                document = DocumentSnapshot(firChange.document),
                type = when (firChange.type) {
                    FIRDocumentChangeType.FIRDocumentChangeTypeAdded -> DocumentChangeType.ADDED
                    FIRDocumentChangeType.FIRDocumentChangeTypeModified -> DocumentChangeType.MODIFIED
                    FIRDocumentChangeType.FIRDocumentChangeTypeRemoved -> DocumentChangeType.REMOVED
                    else -> DocumentChangeType.MODIFIED
                },
                oldIndex = firChange.oldIndex.toInt(),
                newIndex = firChange.newIndex.toInt()
            )
        }

    override fun toString(): String = "QuerySnapshot(size=$size, isEmpty=$isEmpty)"
}

/**
 * Convert Firestore data back to Kotlin types.
 */
@Suppress("UNCHECKED_CAST")
private fun convertFromFirestore(data: Map<String, Any?>): Map<String, Any?> {
    return data.mapValues { (_, value) -> convertValueFromFirestore(value) }
}

/**
 * Convert a single value from Firestore format.
 */
@Suppress("UNCHECKED_CAST")
private fun convertValueFromFirestore(value: Any?): Any? {
    return when (value) {
        is cocoapods.FirebaseFirestore.FIRTimestamp -> Timestamp(value)
        is cocoapods.FirebaseFirestore.FIRDocumentReference -> DocumentReference(value)
        is cocoapods.FirebaseFirestore.FIRGeoPoint -> GeoPoint(value.latitude, value.longitude)
        is Map<*, *> -> convertFromFirestore(value as Map<String, Any?>)
        is List<*> -> value.map { convertValueFromFirestore(it) }
        else -> value
    }
}
