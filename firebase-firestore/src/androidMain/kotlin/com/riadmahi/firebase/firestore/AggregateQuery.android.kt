package com.riadmahi.firebase.firestore

import com.google.firebase.firestore.AggregateField as AndroidAggregateField
import com.google.firebase.firestore.AggregateQuery as AndroidAggregateQuery
import com.google.firebase.firestore.AggregateQuerySnapshot as AndroidAggregateQuerySnapshot
import com.google.firebase.firestore.AggregateSource as AndroidAggregateSource
import com.riadmahi.firebase.core.FirebaseResult
import kotlinx.coroutines.tasks.await

/**
 * Android implementation of AggregateQuery using Firebase Android SDK.
 */
actual class AggregateQuery internal constructor(
    internal val android: AndroidAggregateQuery
) {
    actual val query: Query
        get() = Query(android.query)

    actual suspend fun get(source: AggregateSource): FirebaseResult<AggregateQuerySnapshot> = safeCall {
        val snapshot = android.get(source.toAndroid()).await()
        AggregateQuerySnapshot(snapshot)
    }
}

/**
 * Android implementation of AggregateQuerySnapshot.
 */
actual class AggregateQuerySnapshot internal constructor(
    internal val android: AndroidAggregateQuerySnapshot
) {
    actual val count: Long?
        get() = try {
            android.count
        } catch (e: Exception) {
            null
        }

    actual fun get(aggregateField: AggregateField): Any? = try {
        android.get(aggregateField.android)
    } catch (e: Exception) {
        null
    }

    actual fun getLong(aggregateField: AggregateField): Long? = try {
        android.getLong(aggregateField.android)
    } catch (e: Exception) {
        null
    }

    actual fun getDouble(aggregateField: AggregateField): Double? = try {
        android.getDouble(aggregateField.android)
    } catch (e: Exception) {
        null
    }
}

/**
 * Android implementation of AggregateField.
 */
actual class AggregateField internal constructor(
    internal val android: AndroidAggregateField
) {
    actual companion object {
        actual fun count(): AggregateField =
            AggregateField(AndroidAggregateField.count())

        actual fun sum(field: String): AggregateField =
            AggregateField(AndroidAggregateField.sum(field))

        actual fun sum(fieldPath: FieldPath): AggregateField =
            AggregateField(AndroidAggregateField.sum(fieldPath.android))

        actual fun average(field: String): AggregateField =
            AggregateField(AndroidAggregateField.average(field))

        actual fun average(fieldPath: FieldPath): AggregateField =
            AggregateField(AndroidAggregateField.average(fieldPath.android))
    }
}

private fun AggregateSource.toAndroid(): AndroidAggregateSource = when (this) {
    AggregateSource.SERVER -> AndroidAggregateSource.SERVER
}
