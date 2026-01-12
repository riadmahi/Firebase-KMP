package com.riadmahi.firebase.firestore

import com.google.firebase.firestore.DocumentReference as AndroidDocumentReference
import com.google.firebase.firestore.SetOptions as AndroidSetOptions
import com.google.firebase.firestore.Source as AndroidSource
import com.riadmahi.firebase.core.FirebaseResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

actual class DocumentReference internal constructor(
    internal val android: AndroidDocumentReference
) {
    actual val id: String
        get() = android.id

    actual val path: String
        get() = android.path

    actual val parent: CollectionReference
        get() = CollectionReference(android.parent)

    actual fun collection(path: String): CollectionReference {
        return CollectionReference(android.collection(path))
    }

    actual suspend fun get(source: Source): FirebaseResult<DocumentSnapshot> = safeCall {
        val snapshot = android.get(source.toAndroid()).await()
        DocumentSnapshot(snapshot)
    }

    actual fun snapshots(includeMetadataChanges: Boolean): Flow<DocumentSnapshot> = callbackFlow {
        val metadataChanges = if (includeMetadataChanges) {
            com.google.firebase.firestore.MetadataChanges.INCLUDE
        } else {
            com.google.firebase.firestore.MetadataChanges.EXCLUDE
        }

        val registration = android.addSnapshotListener(metadataChanges) { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            snapshot?.let { trySend(DocumentSnapshot(it)) }
        }

        awaitClose { registration.remove() }
    }

    actual suspend fun set(data: Map<String, Any?>, options: SetOptions): FirebaseResult<Unit> = safeCall {
        when (options) {
            is SetOptions.Overwrite -> android.set(data).await()
            is SetOptions.Merge -> android.set(data, AndroidSetOptions.merge()).await()
            is SetOptions.MergeFields -> android.set(data, AndroidSetOptions.mergeFields(options.fields)).await()
        }
        Unit
    }

    actual suspend fun update(data: Map<String, Any?>): FirebaseResult<Unit> = safeCall {
        android.update(data).await()
        Unit
    }

    actual suspend fun update(vararg fields: Pair<String, Any?>): FirebaseResult<Unit> = safeCall {
        if (fields.isEmpty()) return@safeCall
        val first = fields.first()
        val rest = fields.drop(1).flatMap { listOf(it.first, it.second) }.toTypedArray()
        android.update(first.first, first.second, *rest).await()
        Unit
    }

    actual suspend fun delete(): FirebaseResult<Unit> = safeCall {
        android.delete().await()
        Unit
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DocumentReference) return false
        return path == other.path
    }

    override fun hashCode(): Int = path.hashCode()

    override fun toString(): String = "DocumentReference(path='$path')"
}

private fun Source.toAndroid(): AndroidSource = when (this) {
    Source.DEFAULT -> AndroidSource.DEFAULT
    Source.SERVER -> AndroidSource.SERVER
    Source.CACHE -> AndroidSource.CACHE
}
