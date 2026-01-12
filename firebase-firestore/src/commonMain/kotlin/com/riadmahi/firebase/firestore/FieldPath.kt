package com.riadmahi.firebase.firestore

/**
 * A [FieldPath] refers to a field in a document.
 *
 * The path may consist of a single field name (referring to a top level field in the document),
 * or a list of field names (referring to a nested field in the document).
 *
 * Example usage:
 * ```kotlin
 * // Reference a top-level field
 * val path = FieldPath.of("name")
 *
 * // Reference a nested field
 * val nestedPath = FieldPath.of("address", "city")
 *
 * // Use the document ID field
 * val idPath = FieldPath.documentId()
 *
 * // Use in queries
 * firestore.collection("users")
 *     .whereEqualTo(FieldPath.of("profile", "age"), 25)
 *     .orderBy(FieldPath.documentId())
 * ```
 */
expect class FieldPath {
    companion object {
        /**
         * Creates a [FieldPath] from a list of field names.
         *
         * @param fieldNames The field names that make up the path.
         */
        fun of(vararg fieldNames: String): FieldPath

        /**
         * Returns a special sentinel [FieldPath] to refer to the ID of a document.
         * It can be used in queries to sort or filter by the document ID.
         */
        fun documentId(): FieldPath
    }
}
