package com.riadmahi.firebase.storage

/**
 * Result of a list operation.
 */
data class ListResult(
    /**
     * The list of items (files) under the reference.
     */
    val items: List<StorageReference>,

    /**
     * The list of prefixes (folders) under the reference.
     */
    val prefixes: List<StorageReference>,

    /**
     * The page token for the next page of results, or null if there are no more results.
     */
    val pageToken: String?
)
