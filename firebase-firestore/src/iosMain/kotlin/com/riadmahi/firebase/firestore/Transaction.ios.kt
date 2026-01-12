@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package com.riadmahi.firebase.firestore

import cocoapods.FirebaseFirestoreInternal.FIRTransaction
import cocoapods.FirebaseFirestoreInternal.FIRWriteBatch
import com.riadmahi.firebase.core.FirebaseResult
import com.riadmahi.firebase.core.util.awaitVoid
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.Foundation.NSError

/**
 * iOS implementation of Transaction using Firebase iOS SDK.
 */
actual class Transaction internal constructor(
    internal val ios: FIRTransaction
) {
    actual fun get(documentRef: DocumentReference): DocumentSnapshot = memScoped {
        val errorPtr = alloc<ObjCObjectVar<NSError?>>()
        val snapshot = ios.getDocument(documentRef.ios, error = errorPtr.ptr)

        errorPtr.value?.let { error ->
            throw error.toException().toFirestoreException()
        }

        DocumentSnapshot(snapshot!!)
    }

    actual fun set(
        documentRef: DocumentReference,
        data: Map<String, Any?>,
        options: SetOptions
    ): Transaction {
        val convertedData = data.convertForFirestore()
        when (options) {
            is SetOptions.Overwrite -> ios.setData(convertedData, forDocument = documentRef.ios)
            is SetOptions.Merge -> ios.setData(convertedData, forDocument = documentRef.ios, merge = true)
            is SetOptions.MergeFields -> ios.setData(convertedData, forDocument = documentRef.ios, mergeFields = options.fields)
        }
        return this
    }

    actual fun update(
        documentRef: DocumentReference,
        data: Map<String, Any?>
    ): Transaction {
        val convertedData = data.convertForFirestore()
        ios.updateData(convertedData, forDocument = documentRef.ios)
        return this
    }

    actual fun delete(documentRef: DocumentReference): Transaction {
        ios.deleteDocument(documentRef.ios)
        return this
    }
}

/**
 * iOS implementation of WriteBatch using Firebase iOS SDK.
 */
actual class WriteBatch internal constructor(
    internal val ios: FIRWriteBatch
) {
    actual fun set(
        documentRef: DocumentReference,
        data: Map<String, Any?>,
        options: SetOptions
    ): WriteBatch {
        val convertedData = data.convertForFirestore()
        when (options) {
            is SetOptions.Overwrite -> ios.setData(convertedData, forDocument = documentRef.ios)
            is SetOptions.Merge -> ios.setData(convertedData, forDocument = documentRef.ios, merge = true)
            is SetOptions.MergeFields -> ios.setData(convertedData, forDocument = documentRef.ios, mergeFields = options.fields)
        }
        return this
    }

    actual fun update(
        documentRef: DocumentReference,
        data: Map<String, Any?>
    ): WriteBatch {
        val convertedData = data.convertForFirestore()
        ios.updateData(convertedData, forDocument = documentRef.ios)
        return this
    }

    actual fun delete(documentRef: DocumentReference): WriteBatch {
        ios.deleteDocument(documentRef.ios)
        return this
    }

    actual suspend fun commit(): FirebaseResult<Unit> = safeFirestoreCall {
        awaitVoid { callback ->
            ios.commitWithCompletion { error ->
                callback(error)
            }
        }
    }
}
