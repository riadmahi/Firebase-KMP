package com.riadmahi.firebase.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertIs

/**
 * Tests for FirebaseResult to validate API consistency with Firebase patterns.
 *
 * Firebase SDK uses Task<T> on Android and completion handlers on iOS.
 * KFire wraps these in FirebaseResult<T> for a consistent cross-platform API.
 */
class FirebaseResultTest {

    // region Success/Failure creation

    @Test
    fun `Success should contain data`() {
        val result = FirebaseResult.Success("test data")

        assertEquals("test data", result.data)
        assertTrue(result.isSuccess)
        assertFalse(result.isFailure)
    }

    @Test
    fun `Failure should contain exception`() {
        val exception = FirebaseException.UnknownException("test error")
        val result = FirebaseResult.Failure(exception)

        assertEquals(exception, result.exception)
        assertFalse(result.isSuccess)
        assertTrue(result.isFailure)
    }

    @Test
    fun `success factory should create Success`() {
        val result = FirebaseResult.success("data")

        assertIs<FirebaseResult.Success<String>>(result)
        assertEquals("data", result.data)
    }

    @Test
    fun `failure factory should create Failure`() {
        val exception = FirebaseException.UnknownException("error")
        val result = FirebaseResult.failure<String>(exception)

        assertIs<FirebaseResult.Failure>(result)
        assertEquals(exception, result.exception)
    }

    // endregion

    // region getOrNull - matches Firebase's Task.getResult() nullable pattern

    @Test
    fun `getOrNull should return data on Success`() {
        val result: FirebaseResult<String> = FirebaseResult.Success("value")

        assertEquals("value", result.getOrNull())
    }

    @Test
    fun `getOrNull should return null on Failure`() {
        val result: FirebaseResult<String> = FirebaseResult.Failure(
            FirebaseException.UnknownException("error")
        )

        assertNull(result.getOrNull())
    }

    // endregion

    // region getOrDefault - safe fallback pattern

    @Test
    fun `getOrDefault should return data on Success`() {
        val result: FirebaseResult<String> = FirebaseResult.Success("value")

        assertEquals("value", result.getOrDefault("default"))
    }

    @Test
    fun `getOrDefault should return default on Failure`() {
        val result: FirebaseResult<String> = FirebaseResult.Failure(
            FirebaseException.UnknownException("error")
        )

        assertEquals("default", result.getOrDefault("default"))
    }

    // endregion

    // region getOrElse - computed fallback pattern

    @Test
    fun `getOrElse should return data on Success`() {
        val result: FirebaseResult<String> = FirebaseResult.Success("value")

        assertEquals("value", result.getOrElse { "computed" })
    }

    @Test
    fun `getOrElse should compute value on Failure`() {
        val exception = FirebaseException.UnknownException("specific error")
        val result: FirebaseResult<String> = FirebaseResult.Failure(exception)

        val computed = result.getOrElse { e -> "Error: ${e.message}" }

        assertEquals("Error: specific error", computed)
    }

    // endregion

    // region map - transform success values

    @Test
    fun `map should transform Success value`() {
        val result: FirebaseResult<Int> = FirebaseResult.Success(5)

        val mapped = result.map { it * 2 }

        assertIs<FirebaseResult.Success<Int>>(mapped)
        assertEquals(10, mapped.data)
    }

    @Test
    fun `map should propagate Failure`() {
        val exception = FirebaseException.UnknownException("error")
        val result: FirebaseResult<Int> = FirebaseResult.Failure(exception)

        val mapped = result.map { it * 2 }

        assertIs<FirebaseResult.Failure>(mapped)
        assertEquals(exception, mapped.exception)
    }

    // endregion

    // region flatMap - chain operations

    @Test
    fun `flatMap should chain Success operations`() {
        val result: FirebaseResult<Int> = FirebaseResult.Success(5)

        val chained = result.flatMap { FirebaseResult.Success(it.toString()) }

        assertIs<FirebaseResult.Success<String>>(chained)
        assertEquals("5", chained.data)
    }

    @Test
    fun `flatMap should propagate Failure`() {
        val exception = FirebaseException.UnknownException("error")
        val result: FirebaseResult<Int> = FirebaseResult.Failure(exception)

        val chained = result.flatMap { FirebaseResult.Success(it.toString()) }

        assertIs<FirebaseResult.Failure>(chained)
    }

    @Test
    fun `flatMap should propagate inner Failure`() {
        val result: FirebaseResult<Int> = FirebaseResult.Success(5)
        val innerException = FirebaseException.UnknownException("inner error")

        val chained = result.flatMap { FirebaseResult.failure<String>(innerException) }

        assertIs<FirebaseResult.Failure>(chained)
        assertEquals(innerException, chained.exception)
    }

    // endregion

    // region onSuccess/onFailure callbacks - matches Firebase addOnSuccessListener/addOnFailureListener

    @Test
    fun `onSuccess should execute on Success`() {
        var executed = false
        val result: FirebaseResult<String> = FirebaseResult.Success("value")

        result.onSuccess { executed = true }

        assertTrue(executed)
    }

    @Test
    fun `onSuccess should not execute on Failure`() {
        var executed = false
        val result: FirebaseResult<String> = FirebaseResult.Failure(
            FirebaseException.UnknownException("error")
        )

        result.onSuccess { executed = true }

        assertFalse(executed)
    }

    @Test
    fun `onFailure should execute on Failure`() {
        var executed = false
        val result: FirebaseResult<String> = FirebaseResult.Failure(
            FirebaseException.UnknownException("error")
        )

        result.onFailure { executed = true }

        assertTrue(executed)
    }

    @Test
    fun `onFailure should not execute on Success`() {
        var executed = false
        val result: FirebaseResult<String> = FirebaseResult.Success("value")

        result.onFailure { executed = true }

        assertFalse(executed)
    }

    @Test
    fun `onSuccess and onFailure should be chainable`() {
        var successExecuted = false
        var failureExecuted = false

        val result: FirebaseResult<String> = FirebaseResult.Success("value")

        result
            .onSuccess { successExecuted = true }
            .onFailure { failureExecuted = true }

        assertTrue(successExecuted)
        assertFalse(failureExecuted)
    }

    // endregion

    // region exceptionOrNull

    @Test
    fun `exceptionOrNull should return exception on Failure`() {
        val exception = FirebaseException.UnknownException("error")
        val result: FirebaseResult<String> = FirebaseResult.Failure(exception)

        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `exceptionOrNull should return null on Success`() {
        val result: FirebaseResult<String> = FirebaseResult.Success("value")

        assertNull(result.exceptionOrNull())
    }

    // endregion

    // region runCatching - wraps exceptions

    @Test
    fun `runCatching should wrap successful execution`() {
        val result = FirebaseResult.runCatching { "computed value" }

        assertIs<FirebaseResult.Success<String>>(result)
        assertEquals("computed value", result.data)
    }

    @Test
    fun `runCatching should wrap FirebaseException`() {
        val exception = FirebaseException.UnknownException("firebase error")

        val result = FirebaseResult.runCatching<String> { throw exception }

        assertIs<FirebaseResult.Failure>(result)
        assertEquals(exception, result.exception)
    }

    @Test
    fun `runCatching should wrap generic Exception as UnknownException`() {
        val result = FirebaseResult.runCatching<String> {
            throw IllegalStateException("generic error")
        }

        assertIs<FirebaseResult.Failure>(result)
        assertIs<FirebaseException.UnknownException>(result.exception)
        assertEquals("generic error", result.exception.message)
    }

    // endregion

    // region Result conversion

    @Test
    fun `toFirebaseResult should convert successful Result`() {
        val kotlinResult = Result.success("value")

        val firebaseResult = kotlinResult.toFirebaseResult()

        assertIs<FirebaseResult.Success<String>>(firebaseResult)
        assertEquals("value", firebaseResult.data)
    }

    @Test
    fun `toFirebaseResult should convert failed Result with FirebaseException`() {
        val exception = FirebaseException.UnknownException("error")
        val kotlinResult = Result.failure<String>(exception)

        val firebaseResult = kotlinResult.toFirebaseResult()

        assertIs<FirebaseResult.Failure>(firebaseResult)
        assertEquals(exception, firebaseResult.exception)
    }

    @Test
    fun `toFirebaseResult should wrap non-Firebase exceptions`() {
        val kotlinResult = Result.failure<String>(IllegalStateException("error"))

        val firebaseResult = kotlinResult.toFirebaseResult()

        assertIs<FirebaseResult.Failure>(firebaseResult)
        assertIs<FirebaseException.UnknownException>(firebaseResult.exception)
    }

    // endregion
}
