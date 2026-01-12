package com.riadmahi.firebase.firestore

/**
 * iOS implementation of FieldPath.
 *
 * Note: FieldPath is used for query operations and document field access.
 */
actual class FieldPath private constructor(
    internal val fieldNames: List<String>,
    internal val isDocumentId: Boolean = false
) {
    actual companion object {
        actual fun of(vararg fieldNames: String): FieldPath =
            FieldPath(fieldNames.toList())

        actual fun documentId(): FieldPath =
            FieldPath(emptyList(), isDocumentId = true)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FieldPath) return false
        return fieldNames == other.fieldNames && isDocumentId == other.isDocumentId
    }

    override fun hashCode(): Int {
        var result = fieldNames.hashCode()
        result = 31 * result + isDocumentId.hashCode()
        return result
    }

    override fun toString(): String = if (isDocumentId) "__name__" else fieldNames.joinToString(".")
}
