@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.riadmahi.firebase.firestore

import com.riadmahi.firebase.core.FirebaseResult

/**
 * iOS implementation of AggregateQuery.
 *
 * Note: Aggregate queries are not yet fully supported on iOS through CocoaPods cinterop.
 * This is a stub implementation that will be completed when the Firebase iOS SDK
 * properly exposes aggregate query APIs.
 */
actual class AggregateQuery internal constructor(
    actual val query: Query
) {
    actual suspend fun get(source: AggregateSource): FirebaseResult<AggregateQuerySnapshot> {
        return FirebaseResult.Failure(
            FirestoreException.Unimplemented("Aggregate queries are not yet supported on iOS")
        )
    }
}

/**
 * iOS implementation of AggregateQuerySnapshot.
 */
actual class AggregateQuerySnapshot internal constructor(
    private val countValue: Long? = null,
    private val results: Map<AggregateField, Any?> = emptyMap()
) {
    actual val count: Long? = countValue

    actual fun get(aggregateField: AggregateField): Any? = results[aggregateField]

    actual fun getLong(aggregateField: AggregateField): Long? =
        (results[aggregateField] as? Number)?.toLong()

    actual fun getDouble(aggregateField: AggregateField): Double? =
        (results[aggregateField] as? Number)?.toDouble()
}

/**
 * iOS implementation of AggregateField.
 */
actual class AggregateField private constructor(
    internal val type: Type,
    internal val field: String? = null,
    internal val fieldPath: FieldPath? = null
) {
    internal enum class Type { COUNT, SUM, AVERAGE }

    actual companion object {
        actual fun count(): AggregateField = AggregateField(Type.COUNT)

        actual fun sum(field: String): AggregateField = AggregateField(Type.SUM, field = field)

        actual fun sum(fieldPath: FieldPath): AggregateField = AggregateField(Type.SUM, fieldPath = fieldPath)

        actual fun average(field: String): AggregateField = AggregateField(Type.AVERAGE, field = field)

        actual fun average(fieldPath: FieldPath): AggregateField = AggregateField(Type.AVERAGE, fieldPath = fieldPath)
    }
}
