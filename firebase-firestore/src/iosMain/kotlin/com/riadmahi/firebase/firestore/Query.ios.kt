@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.riadmahi.firebase.firestore

import cocoapods.FirebaseFirestore.FIRQuery
import cocoapods.FirebaseFirestore.FIRQuerySnapshot
import cocoapods.FirebaseFirestore.FIRSnapshotListenOptions
import com.riadmahi.firebase.core.FirebaseResult
import com.riadmahi.firebase.core.util.awaitResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * iOS implementation of Query using Firebase iOS SDK.
 */
actual open class Query internal constructor(
    internal open val ios: FIRQuery
) {
    actual suspend fun get(source: Source): FirebaseResult<QuerySnapshot> = safeFirestoreCall {
        val snapshot = awaitResult<FIRQuerySnapshot> { callback ->
            when (source) {
                Source.DEFAULT -> ios.getDocumentsWithCompletion { snap, error ->
                    callback(snap, error)
                }
                Source.SERVER -> ios.getDocumentsWithSource(
                    cocoapods.FirebaseFirestore.FIRFirestoreSource.FIRFirestoreSourceServer
                ) { snap, error ->
                    callback(snap, error)
                }
                Source.CACHE -> ios.getDocumentsWithSource(
                    cocoapods.FirebaseFirestore.FIRFirestoreSource.FIRFirestoreSourceCache
                ) { snap, error ->
                    callback(snap, error)
                }
            }
        }
        QuerySnapshot(snapshot)
    }

    actual fun snapshots(includeMetadataChanges: Boolean): Flow<QuerySnapshot> = callbackFlow {
        val options = FIRSnapshotListenOptions().apply {
            this.includeMetadataChanges = includeMetadataChanges
        }
        val registration = ios.addSnapshotListenerWithOptions(options) { snapshot, error ->
            if (error != null) {
                close(Exception(error.localizedDescription))
                return@addSnapshotListenerWithOptions
            }
            snapshot?.let { trySend(QuerySnapshot(it)) }
        }
        awaitClose { registration.remove() }
    }

    actual fun whereEqualTo(field: String, value: Any?): Query =
        Query(ios.queryWhereField(field, isEqualTo = value))

    actual fun whereNotEqualTo(field: String, value: Any?): Query =
        Query(ios.queryWhereField(field, isNotEqualTo = value))

    actual fun whereLessThan(field: String, value: Any): Query =
        Query(ios.queryWhereField(field, isLessThan = value))

    actual fun whereLessThanOrEqualTo(field: String, value: Any): Query =
        Query(ios.queryWhereField(field, isLessThanOrEqualTo = value))

    actual fun whereGreaterThan(field: String, value: Any): Query =
        Query(ios.queryWhereField(field, isGreaterThan = value))

    actual fun whereGreaterThanOrEqualTo(field: String, value: Any): Query =
        Query(ios.queryWhereField(field, isGreaterThanOrEqualTo = value))

    actual fun whereArrayContains(field: String, value: Any): Query =
        Query(ios.queryWhereField(field, arrayContains = value))

    actual fun whereArrayContainsAny(field: String, values: List<Any>): Query =
        Query(ios.queryWhereField(field, arrayContainsAny = values))

    actual fun whereIn(field: String, values: List<Any>): Query =
        Query(ios.queryWhereField(field, `in` = values))

    actual fun whereNotIn(field: String, values: List<Any>): Query =
        Query(ios.queryWhereField(field, notIn = values))

    actual fun orderBy(field: String, direction: Direction): Query {
        val descending = direction == Direction.DESCENDING
        return Query(ios.queryOrderedByField(field, descending = descending))
    }

    actual fun limit(limit: Long): Query =
        Query(ios.queryLimitedTo(limit))

    actual fun limitToLast(limit: Long): Query =
        Query(ios.queryLimitedToLast(limit))

    actual fun startAt(vararg fieldValues: Any): Query =
        Query(ios.queryStartingAtValues(fieldValues.toList()))

    actual fun startAfter(vararg fieldValues: Any): Query =
        Query(ios.queryStartingAfterValues(fieldValues.toList()))

    actual fun endAt(vararg fieldValues: Any): Query =
        Query(ios.queryEndingAtValues(fieldValues.toList()))

    actual fun endBefore(vararg fieldValues: Any): Query =
        Query(ios.queryEndingBeforeValues(fieldValues.toList()))

    actual fun startAt(snapshot: DocumentSnapshot): Query =
        Query(ios.queryStartingAtDocument(snapshot.ios))

    actual fun startAfter(snapshot: DocumentSnapshot): Query =
        Query(ios.queryStartingAfterDocument(snapshot.ios))

    actual fun endAt(snapshot: DocumentSnapshot): Query =
        Query(ios.queryEndingAtDocument(snapshot.ios))

    actual fun endBefore(snapshot: DocumentSnapshot): Query =
        Query(ios.queryEndingBeforeDocument(snapshot.ios))
}
