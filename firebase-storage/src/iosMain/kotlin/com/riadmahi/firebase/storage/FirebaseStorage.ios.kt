@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.riadmahi.firebase.storage

import cocoapods.FirebaseStorage.FIRStorage
import com.riadmahi.firebase.core.FirebaseApp

actual class FirebaseStorage private constructor(
    internal val ios: FIRStorage
) {
    actual val app: FirebaseApp
        get() = FirebaseApp.getInstance()

    actual var maxUploadRetryTimeMillis: Long
        get() = (ios.maxUploadRetryTime() * 1000).toLong()
        set(value) { ios.setMaxUploadRetryTime(value / 1000.0) }

    actual var maxDownloadRetryTimeMillis: Long
        get() = (ios.maxDownloadRetryTime() * 1000).toLong()
        set(value) { ios.setMaxDownloadRetryTime(value / 1000.0) }

    actual var maxOperationRetryTimeMillis: Long
        get() = (ios.maxOperationRetryTime() * 1000).toLong()
        set(value) { ios.setMaxOperationRetryTime(value / 1000.0) }

    actual val reference: StorageReference
        get() = StorageReference(ios.reference())

    actual fun getReference(location: String): StorageReference =
        StorageReference(ios.referenceWithPath(location))

    actual fun getReferenceFromUrl(url: String): StorageReference =
        StorageReference(ios.referenceForURL(url))

    actual companion object {
        actual fun getInstance(): FirebaseStorage =
            FirebaseStorage(FIRStorage.storage())

        @Suppress("UNCHECKED_CAST")
        actual fun getInstance(app: FirebaseApp): FirebaseStorage =
            FirebaseStorage(FIRStorage.storageForApp(app.ios as objcnames.classes.FIRApp))

        actual fun getInstance(url: String): FirebaseStorage =
            FirebaseStorage(FIRStorage.storageWithURL(url))

        @Suppress("UNCHECKED_CAST")
        actual fun getInstance(app: FirebaseApp, url: String): FirebaseStorage =
            FirebaseStorage(FIRStorage.storageForApp(app.ios as objcnames.classes.FIRApp, URL = url))
    }
}
