package com.riadmahi.firebase.firestore

import com.riadmahi.firebase.core.FirebaseResult

/**
 * A query that calculates aggregations over its results.
 *
 * Example usage:
 * ```kotlin
 * // Count documents
 * val count = firestore.collection("users")
 *     .count()
 *     .get()
 *
 * // Multiple aggregations
 * val result = firestore.collection("orders")
 *     .whereEqualTo("status", "completed")
 *     .aggregate(
 *         AggregateField.count(),
 *         AggregateField.sum("total"),
 *         AggregateField.average("total")
 *     )
 *     .get()
 * ```
 */
expect class AggregateQuery {
    /**
     * The query whose aggregations are calculated by this object.
     */
    val query: Query

    /**
     * Executes this query and returns the aggregation results.
     *
     * @param source The source from which to acquire the results (default: SERVER).
     * @return The aggregation snapshot containing the results.
     */
    suspend fun get(source: AggregateSource = AggregateSource.SERVER): FirebaseResult<AggregateQuerySnapshot>
}

/**
 * The results of executing an [AggregateQuery].
 */
expect class AggregateQuerySnapshot {
    /**
     * Returns the count of documents, or null if count was not requested.
     */
    val count: Long?

    /**
     * Returns the result of the given aggregation field.
     *
     * @param aggregateField The aggregation field to get the result for.
     * @return The aggregation result, or null if not available.
     */
    fun get(aggregateField: AggregateField): Any?

    /**
     * Returns the result of the given aggregation field as a Long.
     */
    fun getLong(aggregateField: AggregateField): Long?

    /**
     * Returns the result of the given aggregation field as a Double.
     */
    fun getDouble(aggregateField: AggregateField): Double?
}

/**
 * Represents an aggregation that can be performed on a query.
 */
expect class AggregateField {
    companion object {
        /**
         * Creates a count aggregation.
         */
        fun count(): AggregateField

        /**
         * Creates a sum aggregation for the given field.
         *
         * @param field The field to sum.
         */
        fun sum(field: String): AggregateField

        /**
         * Creates a sum aggregation for the given field path.
         *
         * @param fieldPath The field path to sum.
         */
        fun sum(fieldPath: FieldPath): AggregateField

        /**
         * Creates an average aggregation for the given field.
         *
         * @param field The field to average.
         */
        fun average(field: String): AggregateField

        /**
         * Creates an average aggregation for the given field path.
         *
         * @param fieldPath The field path to average.
         */
        fun average(fieldPath: FieldPath): AggregateField
    }
}

/**
 * The source from which to acquire aggregate query results.
 */
enum class AggregateSource {
    /**
     * Causes the aggregate query to fetch results from the server only,
     * never using the local cache.
     */
    SERVER
}
