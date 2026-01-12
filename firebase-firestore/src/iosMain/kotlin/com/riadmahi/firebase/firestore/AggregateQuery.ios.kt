@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.riadmahi.firebase.firestore

import cocoapods.FirebaseFirestoreInternal.FIRAggregateField
import cocoapods.FirebaseFirestoreInternal.FIRAggregateQuery
import cocoapods.FirebaseFirestoreInternal.FIRAggregateQuerySnapshot
import cocoapods.FirebaseFirestoreInternal.FIRAggregateSource
import com.riadmahi.firebase.core.FirebaseResult
import com.riadmahi.firebase.core.util.awaitResult

/**
 * iOS implementation of AggregateQuery using Firebase iOS SDK.
 */
actual class AggregateQuery internal constructor(
    internal val ios: FIRAggregateQuery
) {
    actual val query: Query
        get() = Query(ios.query)

    actual suspend fun get(source: AggregateSource): FirebaseResult<AggregateQuerySnapshot> = safeFirestoreCall {
        val snapshot = awaitResult<FIRAggregateQuerySnapshot> { callback ->
            ios.aggregationWithSource(source.toIos()) { snap, error ->
                callback(snap, error)
            }
        }
        AggregateQuerySnapshot(snapshot)
    }
}

/**
 * iOS implementation of AggregateQuerySnapshot.
 */
actual class AggregateQuerySnapshot internal constructor(
    internal val ios: FIRAggregateQuerySnapshot
) {
    actual val count: Long?
        get() = try {
            ios.count?.longValue
        } catch (e: Exception) {
            null
        }

    actual fun get(aggregateField: AggregateField): Any? = try {
        ios.valueForAggregateField(aggregateField.ios)
    } catch (e: Exception) {
        null
    }

    actual fun getLong(aggregateField: AggregateField): Long? = try {
        (ios.valueForAggregateField(aggregateField.ios) as? Number)?.toLong()
    } catch (e: Exception) {
        null
    }

    actual fun getDouble(aggregateField: AggregateField): Double? = try {
        (ios.valueForAggregateField(aggregateField.ios) as? Number)?.toDouble()
    } catch (e: Exception) {
        null
    }
}

/**
 * iOS implementation of AggregateField.
 */
actual class AggregateField internal constructor(
    internal val ios: FIRAggregateField
) {
    actual companion object {
        actual fun count(): AggregateField =
            AggregateField(FIRAggregateField.aggregateFieldForCount())

        actual fun sum(field: String): AggregateField =
            AggregateField(FIRAggregateField.aggregateFieldForSumOfField(field))

        actual fun sum(fieldPath: FieldPath): AggregateField =
            AggregateField(FIRAggregateField.aggregateFieldForSumOfFieldPath(fieldPath.ios))

        actual fun average(field: String): AggregateField =
            AggregateField(FIRAggregateField.aggregateFieldForAverageOfField(field))

        actual fun average(fieldPath: FieldPath): AggregateField =
            AggregateField(FIRAggregateField.aggregateFieldForAverageOfFieldPath(fieldPath.ios))
    }
}

private fun AggregateSource.toIos(): FIRAggregateSource = when (this) {
    AggregateSource.SERVER -> FIRAggregateSource.FIRAggregateSourceServer
}
