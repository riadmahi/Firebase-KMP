package com.riadmahi.firebase.firestore

import com.riadmahi.firebase.core.FirebaseResult

/**
 * A reference to a collection in Firestore.
 */
expect class CollectionReference : Query {
    /**
     * The ID of the collection.
     */
    val id: String

    /**
     * The path of the collection.
     */
    val path: String

    /**
     * The parent document, or null if this is a root collection.
     */
    val parent: DocumentReference?

    /**
     * Returns a DocumentReference for a document in this collection.
     * If documentPath is null, a new document ID is generated.
     */
    fun document(documentPath: String? = null): DocumentReference

    /**
     * Adds a new document with auto-generated ID.
     */
    suspend fun add(data: Map<String, Any?>): FirebaseResult<DocumentReference>
}
