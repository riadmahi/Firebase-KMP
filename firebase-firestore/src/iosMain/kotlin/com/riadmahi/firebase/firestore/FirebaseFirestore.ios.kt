@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.riadmahi.firebase.firestore

import cocoapods.FirebaseFirestore.FIRFieldValue
import cocoapods.FirebaseFirestore.FIRFirestore
import cocoapods.FirebaseFirestore.FIRFirestoreSettings
import cocoapods.FirebaseFirestore.FIRMemoryCacheSettings
import cocoapods.FirebaseFirestore.FIRMemoryEagerGCSettings
import cocoapods.FirebaseFirestore.FIRMemoryLRUGCSettings
import cocoapods.FirebaseFirestore.FIRPersistentCacheSettings
import cocoapods.FirebaseFirestore.FIRTimestamp
import com.riadmahi.firebase.core.FirebaseApp
import com.riadmahi.firebase.core.FirebaseResult
import com.riadmahi.firebase.core.util.awaitVoid
import kotlinx.datetime.Instant

/**
 * iOS implementation of FirebaseFirestore using Firebase iOS SDK.
 */
actual class FirebaseFirestore private constructor(
    internal val ios: FIRFirestore
) {
    actual fun collection(path: String): CollectionReference {
        return CollectionReference(ios.collectionWithPath(path))
    }

    actual fun document(path: String): DocumentReference {
        return DocumentReference(ios.documentWithPath(path))
    }

    actual fun collectionGroup(collectionId: String): Query {
        return Query(ios.collectionGroupWithID(collectionId))
    }

    actual suspend fun <T> runTransaction(
        block: suspend Transaction.() -> T
    ): FirebaseResult<T> = safeFirestoreCall {
        var result: T? = null
        ios.runTransactionWithBlock(
            { firTransaction, errorPointer ->
                try {
                    val transaction = Transaction(firTransaction!!)
                    result = kotlinx.coroutines.runBlocking { block(transaction) }
                    null // Return null to indicate success
                } catch (e: Exception) {
                    errorPointer?.pointed?.value = platform.Foundation.NSError(
                        domain = "FirestoreKMP",
                        code = -1,
                        userInfo = mapOf("message" to e.message)
                    )
                    null
                }
            },
            completion = { _, error ->
                if (error != null) throw error.toException()
            }
        )
        result!!
    }

    actual fun batch(): WriteBatch {
        return WriteBatch(ios.batch())
    }

    actual suspend fun clearPersistence(): FirebaseResult<Unit> = safeFirestoreCall {
        awaitVoid { callback ->
            ios.clearPersistenceWithCompletion { error ->
                callback(error)
            }
        }
    }

    actual suspend fun terminate(): FirebaseResult<Unit> = safeFirestoreCall {
        awaitVoid { callback ->
            ios.terminateWithCompletion { error ->
                callback(error)
            }
        }
    }

    actual suspend fun waitForPendingWrites(): FirebaseResult<Unit> = safeFirestoreCall {
        awaitVoid { callback ->
            ios.waitForPendingWritesWithCompletion { error ->
                callback(error)
            }
        }
    }

    actual fun useEmulator(host: String, port: Int) {
        ios.useEmulatorWithHost(host, port.toLong())
    }

    actual var settings: FirestoreSettings
        get() = ios.settings.toCommon()
        set(value) {
            ios.settings = value.toIos()
        }

    actual companion object {
        actual fun getInstance(): FirebaseFirestore {
            val firestore = FIRFirestore.firestore()
            return FirebaseFirestore(firestore)
        }

        actual fun getInstance(app: FirebaseApp): FirebaseFirestore {
            val firestore = FIRFirestore.firestoreForApp(app.ios)
            return FirebaseFirestore(firestore)
        }
    }
}

/**
 * Safe wrapper for Firestore operations.
 */
internal suspend fun <T> safeFirestoreCall(block: suspend () -> T): FirebaseResult<T> {
    return try {
        FirebaseResult.Success(block())
    } catch (e: Exception) {
        FirebaseResult.Failure(e.toFirestoreException())
    }
}

/**
 * Convert Exception to FirestoreException.
 */
internal fun Exception.toFirestoreException(): FirestoreException {
    val message = this.message ?: "Unknown error"
    return when {
        message.contains("NOT_FOUND") || message.contains("5") -> FirestoreException.NotFound(message, this)
        message.contains("PERMISSION_DENIED") || message.contains("7") -> FirestoreException.PermissionDenied(message, this)
        message.contains("ALREADY_EXISTS") || message.contains("6") -> FirestoreException.AlreadyExists(message, this)
        message.contains("UNAVAILABLE") || message.contains("14") -> FirestoreException.Unavailable(message, this)
        message.contains("CANCELLED") || message.contains("1") -> FirestoreException.Cancelled(message, this)
        message.contains("INVALID_ARGUMENT") || message.contains("3") -> FirestoreException.InvalidArgument(message, this)
        message.contains("DEADLINE_EXCEEDED") || message.contains("4") -> FirestoreException.DeadlineExceeded(message, this)
        message.contains("RESOURCE_EXHAUSTED") || message.contains("8") -> FirestoreException.ResourceExhausted(message, this)
        message.contains("FAILED_PRECONDITION") || message.contains("9") -> FirestoreException.FailedPrecondition(message, this)
        message.contains("ABORTED") || message.contains("10") -> FirestoreException.Aborted(message, this)
        message.contains("OUT_OF_RANGE") || message.contains("11") -> FirestoreException.OutOfRange(message, this)
        message.contains("UNIMPLEMENTED") || message.contains("12") -> FirestoreException.Unimplemented(message, this)
        message.contains("INTERNAL") || message.contains("13") -> FirestoreException.Internal(message, this)
        message.contains("DATA_LOSS") || message.contains("15") -> FirestoreException.DataLoss(message, this)
        message.contains("UNAUTHENTICATED") || message.contains("16") -> FirestoreException.Unauthenticated(message, this)
        else -> FirestoreException.Unknown(message, this)
    }
}

internal fun platform.Foundation.NSError.toException(): Exception {
    return Exception("[$domain:$code] $localizedDescription")
}

/**
 * Convert FIRFirestoreSettings to FirestoreSettings.
 */
internal fun FIRFirestoreSettings.toCommon(): FirestoreSettings {
    val cache = this.cacheSettings
    val cacheSettings = when (cache) {
        is FIRPersistentCacheSettings -> LocalCacheSettings.Persistent(
            sizeBytes = cache.sizeBytes?.longValue ?: LocalCacheSettings.DEFAULT_CACHE_SIZE_BYTES
        )
        is FIRMemoryCacheSettings -> {
            val gcSettings = when (val gc = cache.garbageCollectorSettings) {
                is FIRMemoryLRUGCSettings -> MemoryGarbageCollectorSettings.Lru(
                    sizeBytes = gc.sizeBytes?.longValue ?: LocalCacheSettings.DEFAULT_CACHE_SIZE_BYTES
                )
                else -> MemoryGarbageCollectorSettings.Eager
            }
            LocalCacheSettings.Memory(gcSettings)
        }
        else -> LocalCacheSettings.Persistent()
    }
    return FirestoreSettings(
        host = this.host,
        sslEnabled = this.sslEnabled,
        cacheSettings = cacheSettings
    )
}

/**
 * Convert FirestoreSettings to FIRFirestoreSettings.
 */
internal fun FirestoreSettings.toIos(): FIRFirestoreSettings {
    return FIRFirestoreSettings().apply {
        this.host = this@toIos.host
        this.sslEnabled = this@toIos.sslEnabled
        this.cacheSettings = this@toIos.cacheSettings.toIos()
    }
}

/**
 * Convert LocalCacheSettings to iOS cache settings.
 */
private fun LocalCacheSettings.toIos(): cocoapods.FirebaseFirestore.FIRLocalCacheSettingsProtocol {
    return when (this) {
        is LocalCacheSettings.Persistent -> FIRPersistentCacheSettings(
            sizeBytes = platform.Foundation.NSNumber(long = sizeBytes)
        )
        is LocalCacheSettings.Memory -> FIRMemoryCacheSettings(
            garbageCollectorSettings = garbageCollectorSettings.toIos()
        )
    }
}

/**
 * Convert MemoryGarbageCollectorSettings to iOS garbage collector settings.
 */
private fun MemoryGarbageCollectorSettings.toIos(): cocoapods.FirebaseFirestore.FIRMemoryGarbageCollectorSettingsProtocol {
    return when (this) {
        is MemoryGarbageCollectorSettings.Eager -> FIRMemoryEagerGCSettings()
        is MemoryGarbageCollectorSettings.Lru -> FIRMemoryLRUGCSettings(
            sizeBytes = platform.Foundation.NSNumber(long = sizeBytes)
        )
    }
}

/**
 * iOS implementation of FieldValue using Firebase iOS SDK.
 */
actual class FieldValue private constructor(
    internal val ios: Any
) {
    actual companion object {
        actual fun delete(): FieldValue = FieldValue(FIRFieldValue.fieldValueForDelete())
        actual fun serverTimestamp(): FieldValue = FieldValue(FIRFieldValue.fieldValueForServerTimestamp())
        actual fun increment(value: Long): FieldValue = FieldValue(FIRFieldValue.fieldValueForIntegerIncrement(value))
        actual fun increment(value: Double): FieldValue = FieldValue(FIRFieldValue.fieldValueForDoubleIncrement(value))
        actual fun arrayUnion(vararg elements: Any): FieldValue = FieldValue(FIRFieldValue.fieldValueForArrayUnion(elements.toList()))
        actual fun arrayRemove(vararg elements: Any): FieldValue = FieldValue(FIRFieldValue.fieldValueForArrayRemove(elements.toList()))
    }
}

/**
 * iOS implementation of Timestamp using Firebase iOS SDK.
 */
actual class Timestamp(
    internal val ios: FIRTimestamp
) {
    actual val seconds: Long
        get() = ios.seconds

    actual val nanoseconds: Int
        get() = ios.nanoseconds

    constructor(seconds: Long, nanoseconds: Int) : this(
        FIRTimestamp(seconds = seconds, nanoseconds = nanoseconds)
    )

    actual companion object {
        actual fun now(): Timestamp = Timestamp(FIRTimestamp.timestamp())

        actual fun fromDate(date: Instant): Timestamp {
            return Timestamp(date.epochSeconds, date.nanosecondsOfSecond)
        }
    }

    actual fun toDate(): Instant {
        return Instant.fromEpochSeconds(seconds, nanoseconds.toLong())
    }
}
