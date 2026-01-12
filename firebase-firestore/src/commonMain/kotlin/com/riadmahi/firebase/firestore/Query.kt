package com.riadmahi.firebase.firestore

import com.riadmahi.firebase.core.FirebaseResult
import kotlinx.coroutines.flow.Flow

/**
 * A Query for filtering and ordering documents.
 */
expect open class Query {
    /**
     * Executes the query and returns the results.
     */
    suspend fun get(source: Source = Source.DEFAULT): FirebaseResult<QuerySnapshot>

    /**
     * Returns a Flow of query snapshots.
     */
    fun snapshots(includeMetadataChanges: Boolean = false): Flow<QuerySnapshot>

    // Filtering by field name
    fun whereEqualTo(field: String, value: Any?): Query
    fun whereNotEqualTo(field: String, value: Any?): Query
    fun whereLessThan(field: String, value: Any): Query
    fun whereLessThanOrEqualTo(field: String, value: Any): Query
    fun whereGreaterThan(field: String, value: Any): Query
    fun whereGreaterThanOrEqualTo(field: String, value: Any): Query
    fun whereArrayContains(field: String, value: Any): Query
    fun whereArrayContainsAny(field: String, values: List<Any>): Query
    fun whereIn(field: String, values: List<Any>): Query
    fun whereNotIn(field: String, values: List<Any>): Query

    // Filtering by FieldPath
    fun whereEqualTo(fieldPath: FieldPath, value: Any?): Query
    fun whereNotEqualTo(fieldPath: FieldPath, value: Any?): Query
    fun whereLessThan(fieldPath: FieldPath, value: Any): Query
    fun whereLessThanOrEqualTo(fieldPath: FieldPath, value: Any): Query
    fun whereGreaterThan(fieldPath: FieldPath, value: Any): Query
    fun whereGreaterThanOrEqualTo(fieldPath: FieldPath, value: Any): Query
    fun whereArrayContains(fieldPath: FieldPath, value: Any): Query
    fun whereArrayContainsAny(fieldPath: FieldPath, values: List<Any>): Query
    fun whereIn(fieldPath: FieldPath, values: List<Any>): Query
    fun whereNotIn(fieldPath: FieldPath, values: List<Any>): Query

    // Ordering
    fun orderBy(field: String, direction: Direction = Direction.ASCENDING): Query
    fun orderBy(fieldPath: FieldPath, direction: Direction = Direction.ASCENDING): Query

    // Aggregation
    /**
     * Returns an [AggregateQuery] that counts the documents matching this query.
     */
    fun count(): AggregateQuery

    /**
     * Returns an [AggregateQuery] that performs the given aggregations.
     *
     * @param aggregateFields The aggregations to perform.
     */
    fun aggregate(vararg aggregateFields: AggregateField): AggregateQuery

    // Pagination
    fun limit(limit: Long): Query
    fun limitToLast(limit: Long): Query
    fun startAt(vararg fieldValues: Any): Query
    fun startAfter(vararg fieldValues: Any): Query
    fun endAt(vararg fieldValues: Any): Query
    fun endBefore(vararg fieldValues: Any): Query
    fun startAt(snapshot: DocumentSnapshot): Query
    fun startAfter(snapshot: DocumentSnapshot): Query
    fun endAt(snapshot: DocumentSnapshot): Query
    fun endBefore(snapshot: DocumentSnapshot): Query
}

/**
 * Direction for ordering.
 */
enum class Direction {
    ASCENDING,
    DESCENDING
}
