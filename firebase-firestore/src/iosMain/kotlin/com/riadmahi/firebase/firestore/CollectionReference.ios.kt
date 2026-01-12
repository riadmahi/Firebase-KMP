@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.riadmahi.firebase.firestore

import cocoapods.FirebaseFirestore.FIRCollectionReference
import cocoapods.FirebaseFirestore.FIRDocumentReference
import com.riadmahi.firebase.core.FirebaseResult
import com.riadmahi.firebase.core.util.awaitVoid

/**
 * iOS implementation of CollectionReference using Firebase iOS SDK.
 */
actual class CollectionReference internal constructor(
    override val ios: FIRCollectionReference
) : Query(ios) {

    actual val id: String
        get() = ios.collectionID

    actual val path: String
        get() = ios.path

    actual val parent: DocumentReference?
        get() = ios.parent?.let { DocumentReference(it) }

    actual fun document(documentPath: String?): DocumentReference {
        val docRef = if (documentPath != null) {
            ios.documentWithPath(documentPath)
        } else {
            ios.documentWithAutoID()
        }
        return DocumentReference(docRef)
    }

    actual suspend fun add(data: Map<String, Any?>): FirebaseResult<DocumentReference> = safeFirestoreCall {
        val convertedData = data.convertForFirestore()
        // addDocumentWithData returns the DocumentReference synchronously
        // and schedules the write. The callback signals completion/error.
        var docRef: FIRDocumentReference? = null
        awaitVoid { callback ->
            docRef = ios.addDocumentWithData(convertedData) { error ->
                callback(error)
            }
        }
        DocumentReference(docRef!!)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CollectionReference) return false
        return path == other.path
    }

    override fun hashCode(): Int = path.hashCode()

    override fun toString(): String = "CollectionReference(path='$path')"
}
