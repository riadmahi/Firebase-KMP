package com.riadmahi.firebase.firestore

import com.riadmahi.firebase.core.FirebaseException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Tests for FirestoreException to validate API consistency with Firebase Firestore errors.
 *
 * Reference: https://firebase.google.com/docs/reference/kotlin/com/google/firebase/firestore/FirebaseFirestoreException
 *
 * Firestore uses gRPC status codes which are mapped to typed exceptions.
 */
class FirestoreExceptionTest {

    // region Exception hierarchy

    @Test
    fun `FirestoreException should extend FirebaseException`() {
        val exception: FirebaseException = FirestoreException.NotFound()

        assertIs<FirestoreException>(exception)
    }

    // endregion

    // region Error codes - matching gRPC/Firestore error codes

    @Test
    fun `Cancelled should have CANCELLED code`() {
        val exception = FirestoreException.Cancelled()

        assertEquals(FirestoreErrorCode.CANCELLED, exception.code)
        assertEquals("Operation cancelled", exception.message)
    }

    @Test
    fun `Unknown should have UNKNOWN code`() {
        val exception = FirestoreException.Unknown()

        assertEquals(FirestoreErrorCode.UNKNOWN, exception.code)
        assertEquals("Unknown error", exception.message)
    }

    @Test
    fun `InvalidArgument should have INVALID_ARGUMENT code`() {
        val exception = FirestoreException.InvalidArgument()

        assertEquals(FirestoreErrorCode.INVALID_ARGUMENT, exception.code)
        assertEquals("Invalid argument", exception.message)
    }

    @Test
    fun `DeadlineExceeded should have DEADLINE_EXCEEDED code`() {
        val exception = FirestoreException.DeadlineExceeded()

        assertEquals(FirestoreErrorCode.DEADLINE_EXCEEDED, exception.code)
        assertEquals("Deadline exceeded", exception.message)
    }

    @Test
    fun `NotFound should have NOT_FOUND code`() {
        val exception = FirestoreException.NotFound()

        assertEquals(FirestoreErrorCode.NOT_FOUND, exception.code)
        assertEquals("Document not found", exception.message)
    }

    @Test
    fun `AlreadyExists should have ALREADY_EXISTS code`() {
        val exception = FirestoreException.AlreadyExists()

        assertEquals(FirestoreErrorCode.ALREADY_EXISTS, exception.code)
        assertEquals("Document already exists", exception.message)
    }

    @Test
    fun `PermissionDenied should have PERMISSION_DENIED code`() {
        val exception = FirestoreException.PermissionDenied()

        assertEquals(FirestoreErrorCode.PERMISSION_DENIED, exception.code)
        assertEquals("Permission denied", exception.message)
    }

    @Test
    fun `ResourceExhausted should have RESOURCE_EXHAUSTED code`() {
        val exception = FirestoreException.ResourceExhausted()

        assertEquals(FirestoreErrorCode.RESOURCE_EXHAUSTED, exception.code)
        assertEquals("Resource exhausted", exception.message)
    }

    @Test
    fun `FailedPrecondition should have FAILED_PRECONDITION code`() {
        val exception = FirestoreException.FailedPrecondition()

        assertEquals(FirestoreErrorCode.FAILED_PRECONDITION, exception.code)
        assertEquals("Failed precondition", exception.message)
    }

    @Test
    fun `Aborted should have ABORTED code`() {
        val exception = FirestoreException.Aborted()

        assertEquals(FirestoreErrorCode.ABORTED, exception.code)
        assertEquals("Operation aborted", exception.message)
    }

    @Test
    fun `OutOfRange should have OUT_OF_RANGE code`() {
        val exception = FirestoreException.OutOfRange()

        assertEquals(FirestoreErrorCode.OUT_OF_RANGE, exception.code)
        assertEquals("Out of range", exception.message)
    }

    @Test
    fun `Unimplemented should have UNIMPLEMENTED code`() {
        val exception = FirestoreException.Unimplemented()

        assertEquals(FirestoreErrorCode.UNIMPLEMENTED, exception.code)
        assertEquals("Unimplemented", exception.message)
    }

    @Test
    fun `Internal should have INTERNAL code`() {
        val exception = FirestoreException.Internal()

        assertEquals(FirestoreErrorCode.INTERNAL, exception.code)
        assertEquals("Internal error", exception.message)
    }

    @Test
    fun `Unavailable should have UNAVAILABLE code`() {
        val exception = FirestoreException.Unavailable()

        assertEquals(FirestoreErrorCode.UNAVAILABLE, exception.code)
        assertEquals("Service unavailable", exception.message)
    }

    @Test
    fun `DataLoss should have DATA_LOSS code`() {
        val exception = FirestoreException.DataLoss()

        assertEquals(FirestoreErrorCode.DATA_LOSS, exception.code)
        assertEquals("Data loss", exception.message)
    }

    @Test
    fun `Unauthenticated should have UNAUTHENTICATED code`() {
        val exception = FirestoreException.Unauthenticated()

        assertEquals(FirestoreErrorCode.UNAUTHENTICATED, exception.code)
        assertEquals("Unauthenticated", exception.message)
    }

    // endregion

    // region Custom messages

    @Test
    fun `FirestoreException should accept custom message`() {
        val exception = FirestoreException.PermissionDenied("Access to collection 'users' denied")

        assertEquals("Access to collection 'users' denied", exception.message)
        assertEquals(FirestoreErrorCode.PERMISSION_DENIED, exception.code)
    }

    @Test
    fun `FirestoreException should accept cause`() {
        val cause = RuntimeException("Network failure")
        val exception = FirestoreException.Unavailable("Cannot connect to Firestore", cause)

        assertEquals("Cannot connect to Firestore", exception.message)
        assertEquals(cause, exception.cause)
    }

    // endregion

    // region Exhaustive when

    @Test
    fun `when expression should be exhaustive for FirestoreException`() {
        val exceptions: List<FirestoreException> = listOf(
            FirestoreException.Cancelled(),
            FirestoreException.Unknown(),
            FirestoreException.InvalidArgument(),
            FirestoreException.DeadlineExceeded(),
            FirestoreException.NotFound(),
            FirestoreException.AlreadyExists(),
            FirestoreException.PermissionDenied(),
            FirestoreException.ResourceExhausted(),
            FirestoreException.FailedPrecondition(),
            FirestoreException.Aborted(),
            FirestoreException.OutOfRange(),
            FirestoreException.Unimplemented(),
            FirestoreException.Internal(),
            FirestoreException.Unavailable(),
            FirestoreException.DataLoss(),
            FirestoreException.Unauthenticated()
        )

        exceptions.forEach { exception ->
            val handled = when (exception) {
                is FirestoreException.Cancelled -> true
                is FirestoreException.Unknown -> true
                is FirestoreException.InvalidArgument -> true
                is FirestoreException.DeadlineExceeded -> true
                is FirestoreException.NotFound -> true
                is FirestoreException.AlreadyExists -> true
                is FirestoreException.PermissionDenied -> true
                is FirestoreException.ResourceExhausted -> true
                is FirestoreException.FailedPrecondition -> true
                is FirestoreException.Aborted -> true
                is FirestoreException.OutOfRange -> true
                is FirestoreException.Unimplemented -> true
                is FirestoreException.Internal -> true
                is FirestoreException.Unavailable -> true
                is FirestoreException.DataLoss -> true
                is FirestoreException.Unauthenticated -> true
            }
            assertEquals(true, handled)
        }

        assertEquals(16, exceptions.size, "All 16 FirestoreException types should be tested")
    }

    // endregion

    // region FirestoreErrorCode enum

    @Test
    fun `FirestoreErrorCode should have all gRPC status codes`() {
        val codes = FirestoreErrorCode.entries

        assertEquals(16, codes.size)
        assertEquals(FirestoreErrorCode.CANCELLED, codes[0])
        assertEquals(FirestoreErrorCode.UNAUTHENTICATED, codes.last())
    }

    // endregion
}
