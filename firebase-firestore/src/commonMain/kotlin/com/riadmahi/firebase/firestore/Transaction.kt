package com.riadmahi.firebase.firestore

/**
 * A transaction for atomic read-modify-write operations.
 */
expect class Transaction {
    /**
     * Reads a document within the transaction.
     */
    fun get(documentRef: DocumentReference): DocumentSnapshot

    /**
     * Sets data on a document within the transaction.
     */
    fun set(
        documentRef: DocumentReference,
        data: Map<String, Any?>,
        options: SetOptions = SetOptions.Overwrite
    ): Transaction

    /**
     * Updates a document within the transaction.
     */
    fun update(
        documentRef: DocumentReference,
        data: Map<String, Any?>
    ): Transaction

    /**
     * Deletes a document within the transaction.
     */
    fun delete(documentRef: DocumentReference): Transaction
}

/**
 * A batch of write operations.
 */
expect class WriteBatch {
    /**
     * Sets data on a document.
     */
    fun set(
        documentRef: DocumentReference,
        data: Map<String, Any?>,
        options: SetOptions = SetOptions.Overwrite
    ): WriteBatch

    /**
     * Updates a document.
     */
    fun update(
        documentRef: DocumentReference,
        data: Map<String, Any?>
    ): WriteBatch

    /**
     * Deletes a document.
     */
    fun delete(documentRef: DocumentReference): WriteBatch

    /**
     * Commits the batch.
     */
    suspend fun commit(): com.riadmahi.firebase.core.FirebaseResult<Unit>
}
