@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.riadmahi.firebase.firestore

import cocoapods.FirebaseFirestore.FIRFieldPath

/**
 * iOS implementation of FieldPath using Firebase iOS SDK.
 */
actual class FieldPath internal constructor(
    internal val ios: FIRFieldPath
) {
    actual companion object {
        actual fun of(vararg fieldNames: String): FieldPath =
            FieldPath(FIRFieldPath(fieldNames.toList()))

        actual fun documentId(): FieldPath =
            FieldPath(FIRFieldPath.documentID())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FieldPath) return false
        return ios == other.ios
    }

    override fun hashCode(): Int = ios.hashCode()

    override fun toString(): String = ios.toString()
}
