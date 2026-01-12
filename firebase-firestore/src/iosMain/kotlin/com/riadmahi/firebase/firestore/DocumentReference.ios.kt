@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.riadmahi.firebase.firestore

import cocoapods.FirebaseFirestoreInternal.FIRDocumentReference
import cocoapods.FirebaseFirestoreInternal.FIRDocumentSnapshot
import com.riadmahi.firebase.core.FirebaseResult
import com.riadmahi.firebase.core.util.awaitResult
import com.riadmahi.firebase.core.util.awaitVoid
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * iOS implementation of DocumentReference using Firebase iOS SDK.
 */
actual class DocumentReference internal constructor(
    internal val ios: FIRDocumentReference
) {
    actual val id: String
        get() = ios.documentID

    actual val path: String
        get() = ios.path

    actual val parent: CollectionReference
        get() = CollectionReference(ios.parent)

    actual fun collection(path: String): CollectionReference {
        return CollectionReference(ios.collectionWithPath(path))
    }

    actual suspend fun get(source: Source): FirebaseResult<DocumentSnapshot> = safeFirestoreCall {
        val snapshot = awaitResult<FIRDocumentSnapshot> { callback ->
            when (source) {
                Source.DEFAULT -> ios.getDocumentWithCompletion { snap, error ->
                    callback(snap, error)
                }
                Source.SERVER -> ios.getDocumentWithSource(
                    cocoapods.FirebaseFirestoreInternal.FIRFirestoreSource.FIRFirestoreSourceServer
                ) { snap, error ->
                    callback(snap, error)
                }
                Source.CACHE -> ios.getDocumentWithSource(
                    cocoapods.FirebaseFirestoreInternal.FIRFirestoreSource.FIRFirestoreSourceCache
                ) { snap, error ->
                    callback(snap, error)
                }
            }
        }
        DocumentSnapshot(snapshot)
    }

    actual fun snapshots(includeMetadataChanges: Boolean): Flow<DocumentSnapshot> = callbackFlow {
        val registration = ios.addSnapshotListenerWithIncludeMetadataChanges(includeMetadataChanges) { snapshot, error ->
            if (error != null) {
                close(Exception(error.localizedDescription))
                return@addSnapshotListenerWithIncludeMetadataChanges
            }
            snapshot?.let { trySend(DocumentSnapshot(it)) }
        }
        awaitClose { registration?.remove() }
    }

    actual suspend fun set(data: Map<String, Any?>, options: SetOptions): FirebaseResult<Unit> = safeFirestoreCall {
        val convertedData = data.convertForFirestore()
        awaitVoid { callback ->
            when (options) {
                is SetOptions.Overwrite -> ios.setData(convertedData) { error ->
                    callback(error)
                }
                is SetOptions.Merge -> ios.setData(convertedData, merge = true) { error ->
                    callback(error)
                }
                is SetOptions.MergeFields -> ios.setData(convertedData, mergeFields = options.fields) { error ->
                    callback(error)
                }
            }
        }
    }

    actual suspend fun update(data: Map<String, Any?>): FirebaseResult<Unit> = safeFirestoreCall {
        val convertedData = data.convertForFirestore()
        awaitVoid { callback ->
            ios.updateData(convertedData) { error ->
                callback(error)
            }
        }
    }

    actual suspend fun update(vararg fields: Pair<String, Any?>): FirebaseResult<Unit> = safeFirestoreCall {
        if (fields.isEmpty()) return@safeFirestoreCall
        val data = fields.toMap()
        val convertedData = data.convertForFirestore()
        awaitVoid { callback ->
            ios.updateData(convertedData) { error ->
                callback(error)
            }
        }
    }

    actual suspend fun delete(): FirebaseResult<Unit> = safeFirestoreCall {
        awaitVoid { callback ->
            ios.deleteDocumentWithCompletion { error ->
                callback(error)
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DocumentReference) return false
        return path == other.path
    }

    override fun hashCode(): Int = path.hashCode()

    override fun toString(): String = "DocumentReference(path='$path')"
}

/**
 * Convert Map data for Firestore, handling FieldValue and Timestamp types.
 */
@Suppress("UNCHECKED_CAST")
internal fun Map<String, Any?>.convertForFirestore(): Map<Any?, *> {
    return this.mapValues { (_, value) ->
        when (value) {
            is FieldValue -> value.ios
            is Timestamp -> value.toNSDate()
            is Map<*, *> -> (value as Map<String, Any?>).convertForFirestore()
            is List<*> -> value.map { item ->
                when (item) {
                    is FieldValue -> item.ios
                    is Timestamp -> item.toNSDate()
                    is Map<*, *> -> (item as Map<String, Any?>).convertForFirestore()
                    else -> item
                }
            }
            else -> value
        }
    }
}
