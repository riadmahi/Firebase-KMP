package com.riadmahi.firebase.firestore

import com.google.firebase.firestore.Query as AndroidQuery
import com.google.firebase.firestore.Source as AndroidSource
import com.riadmahi.firebase.core.FirebaseResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

actual open class Query internal constructor(
    internal open val android: AndroidQuery
) {
    actual suspend fun get(source: Source): FirebaseResult<QuerySnapshot> = safeCall {
        val snapshot = android.get(source.toAndroid()).await()
        QuerySnapshot(snapshot)
    }

    actual fun snapshots(includeMetadataChanges: Boolean): Flow<QuerySnapshot> = callbackFlow {
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
            snapshot?.let { trySend(QuerySnapshot(it)) }
        }

        awaitClose { registration.remove() }
    }

    actual fun whereEqualTo(field: String, value: Any?): Query =
        Query(android.whereEqualTo(field, value))

    actual fun whereNotEqualTo(field: String, value: Any?): Query =
        Query(android.whereNotEqualTo(field, value))

    actual fun whereLessThan(field: String, value: Any): Query =
        Query(android.whereLessThan(field, value))

    actual fun whereLessThanOrEqualTo(field: String, value: Any): Query =
        Query(android.whereLessThanOrEqualTo(field, value))

    actual fun whereGreaterThan(field: String, value: Any): Query =
        Query(android.whereGreaterThan(field, value))

    actual fun whereGreaterThanOrEqualTo(field: String, value: Any): Query =
        Query(android.whereGreaterThanOrEqualTo(field, value))

    actual fun whereArrayContains(field: String, value: Any): Query =
        Query(android.whereArrayContains(field, value))

    actual fun whereArrayContainsAny(field: String, values: List<Any>): Query =
        Query(android.whereArrayContainsAny(field, values))

    actual fun whereIn(field: String, values: List<Any>): Query =
        Query(android.whereIn(field, values))

    actual fun whereNotIn(field: String, values: List<Any>): Query =
        Query(android.whereNotIn(field, values))

    // FieldPath overloads
    actual fun whereEqualTo(fieldPath: FieldPath, value: Any?): Query =
        Query(android.whereEqualTo(fieldPath.android, value))

    actual fun whereNotEqualTo(fieldPath: FieldPath, value: Any?): Query =
        Query(android.whereNotEqualTo(fieldPath.android, value))

    actual fun whereLessThan(fieldPath: FieldPath, value: Any): Query =
        Query(android.whereLessThan(fieldPath.android, value))

    actual fun whereLessThanOrEqualTo(fieldPath: FieldPath, value: Any): Query =
        Query(android.whereLessThanOrEqualTo(fieldPath.android, value))

    actual fun whereGreaterThan(fieldPath: FieldPath, value: Any): Query =
        Query(android.whereGreaterThan(fieldPath.android, value))

    actual fun whereGreaterThanOrEqualTo(fieldPath: FieldPath, value: Any): Query =
        Query(android.whereGreaterThanOrEqualTo(fieldPath.android, value))

    actual fun whereArrayContains(fieldPath: FieldPath, value: Any): Query =
        Query(android.whereArrayContains(fieldPath.android, value))

    actual fun whereArrayContainsAny(fieldPath: FieldPath, values: List<Any>): Query =
        Query(android.whereArrayContainsAny(fieldPath.android, values))

    actual fun whereIn(fieldPath: FieldPath, values: List<Any>): Query =
        Query(android.whereIn(fieldPath.android, values))

    actual fun whereNotIn(fieldPath: FieldPath, values: List<Any>): Query =
        Query(android.whereNotIn(fieldPath.android, values))

    actual fun orderBy(field: String, direction: Direction): Query =
        Query(android.orderBy(field, direction.toAndroid()))

    actual fun orderBy(fieldPath: FieldPath, direction: Direction): Query =
        Query(android.orderBy(fieldPath.android, direction.toAndroid()))

    // Aggregation
    actual fun count(): AggregateQuery =
        AggregateQuery(android.count())

    actual fun aggregate(vararg aggregateFields: AggregateField): AggregateQuery {
        require(aggregateFields.isNotEmpty()) { "At least one AggregateField is required" }
        val androidFields = aggregateFields.map { it.android }
        return AggregateQuery(
            android.aggregate(androidFields.first(), *androidFields.drop(1).toTypedArray())
        )
    }

    actual fun limit(limit: Long): Query =
        Query(android.limit(limit))

    actual fun limitToLast(limit: Long): Query =
        Query(android.limitToLast(limit))

    actual fun startAt(vararg fieldValues: Any): Query =
        Query(android.startAt(*fieldValues))

    actual fun startAfter(vararg fieldValues: Any): Query =
        Query(android.startAfter(*fieldValues))

    actual fun endAt(vararg fieldValues: Any): Query =
        Query(android.endAt(*fieldValues))

    actual fun endBefore(vararg fieldValues: Any): Query =
        Query(android.endBefore(*fieldValues))

    actual fun startAt(snapshot: DocumentSnapshot): Query =
        Query(android.startAt(snapshot.android))

    actual fun startAfter(snapshot: DocumentSnapshot): Query =
        Query(android.startAfter(snapshot.android))

    actual fun endAt(snapshot: DocumentSnapshot): Query =
        Query(android.endAt(snapshot.android))

    actual fun endBefore(snapshot: DocumentSnapshot): Query =
        Query(android.endBefore(snapshot.android))
}

private fun Source.toAndroid(): AndroidSource = when (this) {
    Source.DEFAULT -> AndroidSource.DEFAULT
    Source.SERVER -> AndroidSource.SERVER
    Source.CACHE -> AndroidSource.CACHE
}

private fun Direction.toAndroid(): AndroidQuery.Direction = when (this) {
    Direction.ASCENDING -> AndroidQuery.Direction.ASCENDING
    Direction.DESCENDING -> AndroidQuery.Direction.DESCENDING
}
