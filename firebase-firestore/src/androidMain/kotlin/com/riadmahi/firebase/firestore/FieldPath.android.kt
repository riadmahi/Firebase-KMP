package com.riadmahi.firebase.firestore

import com.google.firebase.firestore.FieldPath as AndroidFieldPath

/**
 * Android implementation of FieldPath using Firebase Android SDK.
 */
actual class FieldPath internal constructor(
    internal val android: AndroidFieldPath
) {
    actual companion object {
        actual fun of(vararg fieldNames: String): FieldPath =
            FieldPath(AndroidFieldPath.of(*fieldNames))

        actual fun documentId(): FieldPath =
            FieldPath(AndroidFieldPath.documentId())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FieldPath) return false
        return android == other.android
    }

    override fun hashCode(): Int = android.hashCode()

    override fun toString(): String = android.toString()
}
