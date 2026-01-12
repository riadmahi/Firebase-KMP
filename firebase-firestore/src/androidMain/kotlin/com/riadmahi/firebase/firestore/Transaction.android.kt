package com.riadmahi.firebase.firestore

import com.google.firebase.firestore.Transaction as AndroidTransaction
import com.google.firebase.firestore.WriteBatch as AndroidWriteBatch
import com.google.firebase.firestore.SetOptions as AndroidSetOptions
import com.riadmahi.firebase.core.FirebaseResult
import kotlinx.coroutines.tasks.await

actual class Transaction internal constructor(
    private val android: AndroidTransaction
) {
    actual fun get(documentRef: DocumentReference): DocumentSnapshot {
        return DocumentSnapshot(android.get(documentRef.android))
    }

    actual fun set(
        documentRef: DocumentReference,
        data: Map<String, Any?>,
        options: SetOptions
    ): Transaction {
        when (options) {
            is SetOptions.Overwrite -> android.set(documentRef.android, data)
            is SetOptions.Merge -> android.set(documentRef.android, data, AndroidSetOptions.merge())
            is SetOptions.MergeFields -> android.set(documentRef.android, data, AndroidSetOptions.mergeFields(options.fields))
        }
        return this
    }

    actual fun update(
        documentRef: DocumentReference,
        data: Map<String, Any?>
    ): Transaction {
        android.update(documentRef.android, data)
        return this
    }

    actual fun delete(documentRef: DocumentReference): Transaction {
        android.delete(documentRef.android)
        return this
    }
}

actual class WriteBatch internal constructor(
    private val android: AndroidWriteBatch
) {
    actual fun set(
        documentRef: DocumentReference,
        data: Map<String, Any?>,
        options: SetOptions
    ): WriteBatch {
        when (options) {
            is SetOptions.Overwrite -> android.set(documentRef.android, data)
            is SetOptions.Merge -> android.set(documentRef.android, data, AndroidSetOptions.merge())
            is SetOptions.MergeFields -> android.set(documentRef.android, data, AndroidSetOptions.mergeFields(options.fields))
        }
        return this
    }

    actual fun update(
        documentRef: DocumentReference,
        data: Map<String, Any?>
    ): WriteBatch {
        android.update(documentRef.android, data)
        return this
    }

    actual fun delete(documentRef: DocumentReference): WriteBatch {
        android.delete(documentRef.android)
        return this
    }

    actual suspend fun commit(): FirebaseResult<Unit> = safeCall {
        android.commit().await()
        Unit
    }
}
