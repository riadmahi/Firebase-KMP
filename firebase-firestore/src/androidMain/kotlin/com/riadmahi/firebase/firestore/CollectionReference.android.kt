package com.riadmahi.firebase.firestore

import com.google.firebase.firestore.CollectionReference as AndroidCollectionReference
import com.riadmahi.firebase.core.FirebaseResult
import kotlinx.coroutines.tasks.await

actual class CollectionReference internal constructor(
    override val android: AndroidCollectionReference
) : Query(android) {

    actual val id: String
        get() = android.id

    actual val path: String
        get() = android.path

    actual val parent: DocumentReference?
        get() = android.parent?.let { DocumentReference(it) }

    actual fun document(documentPath: String?): DocumentReference {
        val docRef = if (documentPath != null) {
            android.document(documentPath)
        } else {
            android.document()
        }
        return DocumentReference(docRef)
    }

    actual suspend fun add(data: Map<String, Any?>): FirebaseResult<DocumentReference> = safeCall {
        val docRef = android.add(data).await()
        DocumentReference(docRef)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CollectionReference) return false
        return path == other.path
    }

    override fun hashCode(): Int = path.hashCode()

    override fun toString(): String = "CollectionReference(path='$path')"
}
