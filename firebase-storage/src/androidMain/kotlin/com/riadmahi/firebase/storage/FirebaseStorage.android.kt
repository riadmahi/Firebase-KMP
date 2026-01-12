package com.riadmahi.firebase.storage

import com.riadmahi.firebase.core.FirebaseApp
import com.google.firebase.storage.FirebaseStorage as AndroidFirebaseStorage

actual class FirebaseStorage private constructor(
    internal val android: AndroidFirebaseStorage
) {
    actual val app: FirebaseApp
        get() = FirebaseApp.getInstance(android.app.name)

    actual var maxUploadRetryTimeMillis: Long
        get() = android.maxUploadRetryTimeMillis
        set(value) { android.maxUploadRetryTimeMillis = value }

    actual var maxDownloadRetryTimeMillis: Long
        get() = android.maxDownloadRetryTimeMillis
        set(value) { android.maxDownloadRetryTimeMillis = value }

    actual var maxOperationRetryTimeMillis: Long
        get() = android.maxOperationRetryTimeMillis
        set(value) { android.maxOperationRetryTimeMillis = value }

    actual val reference: StorageReference
        get() = StorageReference(android.reference)

    actual fun getReference(location: String): StorageReference =
        StorageReference(android.getReference(location))

    actual fun getReferenceFromUrl(url: String): StorageReference =
        StorageReference(android.getReferenceFromUrl(url))

    actual companion object {
        actual fun getInstance(): FirebaseStorage =
            FirebaseStorage(AndroidFirebaseStorage.getInstance())

        actual fun getInstance(app: FirebaseApp): FirebaseStorage =
            FirebaseStorage(AndroidFirebaseStorage.getInstance(app.android))

        actual fun getInstance(url: String): FirebaseStorage =
            FirebaseStorage(AndroidFirebaseStorage.getInstance(url))

        actual fun getInstance(app: FirebaseApp, url: String): FirebaseStorage =
            FirebaseStorage(AndroidFirebaseStorage.getInstance(app.android, url))
    }
}
