package com.riadmahi.firebase.firestore

import com.google.firebase.firestore.FirebaseFirestore as AndroidFirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings as AndroidFirestoreSettings
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.MemoryCacheSettings as AndroidMemoryCacheSettings
import com.google.firebase.firestore.MemoryEagerGcSettings
import com.google.firebase.firestore.MemoryLruGcSettings
import com.google.firebase.firestore.PersistentCacheSettings as AndroidPersistentCacheSettings
import com.riadmahi.firebase.core.FirebaseApp
import com.riadmahi.firebase.core.FirebaseResult
import kotlinx.coroutines.tasks.await

actual class FirebaseFirestore private constructor(
    internal val android: AndroidFirebaseFirestore
) {
    actual fun collection(path: String): CollectionReference {
        return CollectionReference(android.collection(path))
    }

    actual fun document(path: String): DocumentReference {
        return DocumentReference(android.document(path))
    }

    actual fun collectionGroup(collectionId: String): Query {
        return Query(android.collectionGroup(collectionId))
    }

    actual suspend fun <T> runTransaction(
        block: suspend Transaction.() -> T
    ): FirebaseResult<T> = safeCall {
        android.runTransaction { androidTransaction ->
            val transaction = Transaction(androidTransaction)
            kotlinx.coroutines.runBlocking { transaction.block() }
        }.await()
    }

    actual fun batch(): WriteBatch {
        return WriteBatch(android.batch())
    }

    actual suspend fun clearPersistence(): FirebaseResult<Unit> = safeCall {
        android.clearPersistence().await()
    }

    actual suspend fun terminate(): FirebaseResult<Unit> = safeCall {
        android.terminate().await()
    }

    actual suspend fun waitForPendingWrites(): FirebaseResult<Unit> = safeCall {
        android.waitForPendingWrites().await()
    }

    actual fun useEmulator(host: String, port: Int) {
        android.useEmulator(host, port)
    }

    actual var settings: FirestoreSettings
        get() = android.firestoreSettings.toCommon()
        set(value) {
            android.firestoreSettings = value.toAndroid()
        }

    actual companion object {
        actual fun getInstance(): FirebaseFirestore {
            return FirebaseFirestore(AndroidFirebaseFirestore.getInstance())
        }

        actual fun getInstance(app: FirebaseApp): FirebaseFirestore {
            return FirebaseFirestore(AndroidFirebaseFirestore.getInstance(app.android))
        }
    }
}

private fun FirestoreSettings.toAndroid(): AndroidFirestoreSettings {
    return AndroidFirestoreSettings.Builder()
        .setHost(host)
        .setSslEnabled(sslEnabled)
        .setLocalCacheSettings(cacheSettings.toAndroid())
        .build()
}

private fun LocalCacheSettings.toAndroid(): com.google.firebase.firestore.LocalCacheSettings {
    return when (this) {
        is LocalCacheSettings.Persistent -> AndroidPersistentCacheSettings.newBuilder()
            .setSizeBytes(sizeBytes)
            .build()
        is LocalCacheSettings.Memory -> AndroidMemoryCacheSettings.newBuilder()
            .setGcSettings(garbageCollectorSettings.toAndroid())
            .build()
    }
}

private fun MemoryGarbageCollectorSettings.toAndroid(): com.google.firebase.firestore.MemoryGarbageCollectorSettings {
    return when (this) {
        is MemoryGarbageCollectorSettings.Eager -> MemoryEagerGcSettings.newBuilder().build()
        is MemoryGarbageCollectorSettings.Lru -> MemoryLruGcSettings.newBuilder()
            .setSizeBytes(sizeBytes)
            .build()
    }
}

private fun AndroidFirestoreSettings.toCommon(): FirestoreSettings {
    val cacheSettings = when (val cache = this.cacheSettings) {
        is AndroidPersistentCacheSettings -> LocalCacheSettings.Persistent(cache.sizeBytes)
        is AndroidMemoryCacheSettings -> {
            // Note: Android SDK doesn't expose gcSettings getter, so we default to LRU (the SDK default)
            LocalCacheSettings.Memory(MemoryGarbageCollectorSettings.Lru())
        }
        else -> LocalCacheSettings.Persistent()
    }
    return FirestoreSettings(
        host = host,
        sslEnabled = isSslEnabled,
        cacheSettings = cacheSettings
    )
}

internal suspend fun <T> safeCall(block: suspend () -> T): FirebaseResult<T> {
    return try {
        FirebaseResult.Success(block())
    } catch (e: FirebaseFirestoreException) {
        FirebaseResult.Failure(e.toFirestoreException())
    } catch (e: Exception) {
        FirebaseResult.Failure(FirestoreException.Unknown(e.message ?: "Unknown error", e))
    }
}

internal fun FirebaseFirestoreException.toFirestoreException(): FirestoreException {
    return when (code) {
        FirebaseFirestoreException.Code.CANCELLED -> FirestoreException.Cancelled(message ?: "Cancelled", this)
        FirebaseFirestoreException.Code.UNKNOWN -> FirestoreException.Unknown(message ?: "Unknown", this)
        FirebaseFirestoreException.Code.INVALID_ARGUMENT -> FirestoreException.InvalidArgument(message ?: "Invalid argument", this)
        FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> FirestoreException.DeadlineExceeded(message ?: "Deadline exceeded", this)
        FirebaseFirestoreException.Code.NOT_FOUND -> FirestoreException.NotFound(message ?: "Not found", this)
        FirebaseFirestoreException.Code.ALREADY_EXISTS -> FirestoreException.AlreadyExists(message ?: "Already exists", this)
        FirebaseFirestoreException.Code.PERMISSION_DENIED -> FirestoreException.PermissionDenied(message ?: "Permission denied", this)
        FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED -> FirestoreException.ResourceExhausted(message ?: "Resource exhausted", this)
        FirebaseFirestoreException.Code.FAILED_PRECONDITION -> FirestoreException.FailedPrecondition(message ?: "Failed precondition", this)
        FirebaseFirestoreException.Code.ABORTED -> FirestoreException.Aborted(message ?: "Aborted", this)
        FirebaseFirestoreException.Code.OUT_OF_RANGE -> FirestoreException.OutOfRange(message ?: "Out of range", this)
        FirebaseFirestoreException.Code.UNIMPLEMENTED -> FirestoreException.Unimplemented(message ?: "Unimplemented", this)
        FirebaseFirestoreException.Code.INTERNAL -> FirestoreException.Internal(message ?: "Internal", this)
        FirebaseFirestoreException.Code.UNAVAILABLE -> FirestoreException.Unavailable(message ?: "Unavailable", this)
        FirebaseFirestoreException.Code.DATA_LOSS -> FirestoreException.DataLoss(message ?: "Data loss", this)
        FirebaseFirestoreException.Code.UNAUTHENTICATED -> FirestoreException.Unauthenticated(message ?: "Unauthenticated", this)
        else -> FirestoreException.Unknown(message ?: "Unknown error", this)
    }
}
