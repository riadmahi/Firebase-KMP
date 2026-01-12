package com.riadmahi.firebase.firestore

import com.riadmahi.firebase.core.FirebaseResult
import kotlinx.coroutines.flow.Flow

/**
 * A reference to a document in Firestore.
 */
expect class DocumentReference {
    /**
     * The ID of the document.
     */
    val id: String

    /**
     * The path of the document.
     */
    val path: String

    /**
     * The parent collection.
     */
    val parent: CollectionReference

    /**
     * Returns a CollectionReference for a subcollection.
     */
    fun collection(path: String): CollectionReference

    /**
     * Gets the document snapshot.
     */
    suspend fun get(source: Source = Source.DEFAULT): FirebaseResult<DocumentSnapshot>

    /**
     * Returns a Flow of document snapshots.
     */
    fun snapshots(includeMetadataChanges: Boolean = false): Flow<DocumentSnapshot>

    /**
     * Sets the document data.
     */
    suspend fun set(data: Map<String, Any?>, options: SetOptions = SetOptions.Overwrite): FirebaseResult<Unit>

    /**
     * Updates the document.
     */
    suspend fun update(data: Map<String, Any?>): FirebaseResult<Unit>

    /**
     * Updates specific fields in the document.
     */
    suspend fun update(vararg fields: Pair<String, Any?>): FirebaseResult<Unit>

    /**
     * Deletes the document.
     */
    suspend fun delete(): FirebaseResult<Unit>
}

/**
 * Options for setting document data.
 */
sealed class SetOptions {
    /**
     * Overwrites the entire document.
     */
    object Overwrite : SetOptions()

    /**
     * Merges the data with existing document data.
     */
    object Merge : SetOptions()

    /**
     * Merges only the specified fields.
     */
    data class MergeFields(val fields: List<String>) : SetOptions() {
        constructor(vararg fields: String) : this(fields.toList())
    }
}

/**
 * Source of data for document reads.
 */
enum class Source {
    DEFAULT,
    SERVER,
    CACHE
}
